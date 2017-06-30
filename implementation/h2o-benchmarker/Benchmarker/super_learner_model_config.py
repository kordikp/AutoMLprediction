import yaml
import sklearn.grid_search as grid
import datetime
import pandas as pd
import config
import numpy as np
import numbers
import h2o
import os
import re
import readline # workaround for anaconda readline install
from utils import toR, persist
from rpy2.robjects.packages import importr
import rpy2.rinterface as ri
import rpy2.robjects as ro


class SLModelConfig(yaml.YAMLObject):
    yaml_tag = u"SLModelConfig"

    def __init__(self, name, base_models, meta_model, params):
        self.name = name
        self.params = params

    def execute(self, name, x, y, training_frame, validation_frame, test_frame, subset_coef):
        params = grid.ParameterGrid(self.params_grid)
        if self.params_grid is None or len(self.params_grid) == 0:
            params = ["default"]
        results = []
        dt = datetime.datetime

        # R stuff
        ri.initr()
        h2or = importr("h2o")
        h2o_ensemble = importr("h2oEnsemble")
        base = importr("base")
        stats = importr("stats")
        cvauc = importr("cvAUC")

        h2or.h2o_init(ip = config.hostname, port = config.port, startH2O = False)

        # Add some base learners
        with open("{}/R/wrappers.r".format(os.path.dirname(__file__)),"r") as f:
            ro.r("\n".join(f.readlines()))

        keep_frames = re.compile("|".join([training_frame.frame_id,validation_frame.frame_id,test_frame.frame_id])+
                                 "|.*\\.hex|py_.*")

        for p in params:
            row = [config.cluster, config.nthreads, name, subset_coef, self.name, str(p)]

            # Initialize the model
            init_time = dt.now()
            # get frame names
            # load it in R
            train = h2or.h2o_getFrame(training_frame.frame_id)
            valid = h2or.h2o_getFrame(validation_frame.frame_id)
            test = h2or.h2o_getFrame(test_frame.frame_id)
            init_time = dt.now() - init_time

            # Train the model
            train_time = dt.now()
            if p == "default":
                model = h2o_ensemble.h2o_ensemble(x=toR(x), y=y, training_frame=train,
                                                  validation_frame=valid)
            else:
                p = {k: toR(v) for k,v in p.items()}
                model = h2o_ensemble.h2o_ensemble(x=toR(x), y=y, training_frame=train,
                                                  validation_frame=valid,**p)
            train_time = dt.now() - train_time


            # Model metrics
            metrics_time = dt.now()
            RpredTrain = stats.predict(model, train)
            RpredValid = stats.predict(model, valid)
            RpredTest = stats.predict(model, test)
            predTrain = h2o.get_frame(h2or.h2o_getId(RpredTrain.rx2("pred"))[0])
            predValid = h2o.get_frame(h2or.h2o_getId(RpredValid.rx2("pred"))[0])
            predTest = h2o.get_frame(h2or.h2o_getId(RpredTest.rx2("pred"))[0])
            metrics_time = dt.now() - metrics_time

            row.append(init_time.total_seconds())
            row.append(train_time.total_seconds())
            row.append(metrics_time.total_seconds())
            row.append((init_time + train_time + metrics_time).total_seconds())

            datasets = [(RpredTrain, predTrain, train, training_frame),
                        (RpredValid, predValid, valid, validation_frame),
                        (RpredTest, predTest, test, test_frame)]

            append = row.append
            for pred_r_ptr, pred_py_ptr, data_r_ptr, data_py_ptr in datasets:
                acc = None
                err = None
                mse = ((pred_py_ptr - data_py_ptr[y])**2).mean()[0]
                if training_frame[y].isfactor()[0]:
                    acc = (pred_py_ptr == data_py_ptr[y]).mean()[0]
                    err = 1.0 - acc

                auc = cvauc.AUC(base.attr(pred_r_ptr.rx2("pred"),"data")[2], base.attr(data_r_ptr,"data").rx2(y))[0]

                # TODO: Add more metrics
                append(acc)
                append(err)
                append(None) # F1()
                append(None) # fnr()
                append(None) # fpr()
                append(None) # tnr()
                append(None) # tpr()
                append(None) # precision()
                append(None) # recall()
                append(None) # sensitivity()
                append(None) # specificity()
                append(None) # aic()
                append((auc))  # auc()
                append(None) # logloss()
                append(None) # mean_residual_deviance()
                append(mse)  # mse()
                append(None) # null_degrees_of_freedom()
                append(None) # null_deviance()
                append(None) # r2()
                append(None) # residual_degrees_of_freedom()
                append(None) # residual_deviance()

                h2o.remove(pred_py_ptr)

            row = map(lambda x: None if isinstance(x, numbers.Number) and (x is None or np.isnan(x))
                                        or x == u"NaN" or x == "NaN" else x, row)
            persist(row)
            results.append(row)
            for [frame] in h2o.ls().as_matrix():
                if not keep_frames.match(frame):
                    h2o.remove(frame)

        df = pd.DataFrame(results, columns=config.Names)
        return df