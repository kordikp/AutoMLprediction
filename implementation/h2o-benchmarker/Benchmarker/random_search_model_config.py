import yaml
import sklearn.grid_search as grid
import datetime
import pandas as pd
import config
import numpy as np
import numbers
import re
import h2o
import metaopt.params as parameters
import metaopt.fakegame as fg
import metaopt.utils as u
from utils import persist


def appendVal(row, lmbda):
    try:
        res = lmbda()
        if hasattr(res, '__contains__'):
            print("{} has more than one value: {}".format(config.Names[len(row)], res))
            res = res[0][1]
        row.append(res)
    except:
        row.append(None)
    return row


def get_classification_error(mod, data, type):
    cm = None
    err = None
    if type == "train":
        try:
            cm = mod.confusion_matrix(train=True)
        except:
            cm = mod.confusion_matrix(data)
    elif type == "valid":
        try:
            cm = mod.confusion_matrix(valid=True)
        except:
            cm = mod.confusion_matrix(data)
    elif type == "test":
        cm = mod.confusion_matrix()
    try:
        err = float(cm.as_data_frame()['Error'].tail(1))
    except:
        acc = None
        if type == "train":
            acc = mod.accuracy(train=True)
        elif type == "valid":
            acc = mod.accuracy(valid=True)
        elif type == "test":
            acc = mod.accuracy()
        if hasattr(acc, '__contains__'):
            err = 1.0 - acc[0][1]
        else:
            err = 1.0 - acc
    return err

## CAVEAT EMPTOR 
class RandomSearchModelConfig(yaml.YAMLObject):
    yaml_tag = u"RandomSearchModelConfig"

    def __init__(self, name, steps, model_type):
        self.name = name
        self.steps = steps
        self.model_type = model_type


    def execute(self, name, x, y, training_frame, validation_frame, test_frame, subset_coef):
        results = []
        dt = datetime.datetime

        keep_frames = re.compile("|".join([training_frame.frame_id, validation_frame.frame_id, test_frame.frame_id]) +
                                 "|.*\\.hex|py_.*")


        # Initialize the model
        init_time = dt.now()
        p = u.randomSearch(u.getTrainer(self.model_type), parameters.getParameters(self.model_type), self.steps)
        row = [config.cluster, config.nthreads, name, subset_coef, self.name, str(p)]
        model = self.base_model(**p)
        init_time = dt.now() - init_time

        for [frame] in h2o.ls().as_matrix():
            if not keep_frames.match(frame):
                h2o.remove(frame)

        # Train the model
        train_time = dt.now()
        model.train(x, y, training_frame=training_frame, validation_frame=validation_frame)
        train_time = dt.now() - train_time

        # Model metrics
        metrics_time = dt.now()
        metrics = model.model_performance(test_data=test_frame)
        err_tr = get_classification_error(model, training_frame, "train")
        err_va = get_classification_error(model, validation_frame, "valid")
        err_te = get_classification_error(metrics, test_frame, "test")
        metrics_time = dt.now() - metrics_time

        # results
        row.append(init_time.total_seconds())
        row.append(train_time.total_seconds())
        row.append(metrics_time.total_seconds())
        row.append((init_time + train_time + metrics_time).total_seconds())

        # on training data
        appendVal(row, lambda: 1 - err_tr)
        appendVal(row, lambda: err_tr)
        appendVal(row, lambda: model.F1())
        appendVal(row, lambda: model.fnr())
        appendVal(row, lambda: model.fpr())
        appendVal(row, lambda: model.tnr())
        appendVal(row, lambda: model.tpr())
        appendVal(row, lambda: model.precision())
        appendVal(row, lambda: model.recall())
        appendVal(row, lambda: model.sensitivity())
        appendVal(row, lambda: model.specificity())
        appendVal(row, lambda: model.aic())
        appendVal(row, lambda: model.auc())
        appendVal(row, lambda: model.logloss())
        appendVal(row, lambda: model.mean_residual_deviance())
        appendVal(row, lambda: model.mse())
        appendVal(row, lambda: model.null_degrees_of_freedom())
        appendVal(row, lambda: model.null_deviance())
        appendVal(row, lambda: model.r2())
        appendVal(row, lambda: model.residual_degrees_of_freedom())
        appendVal(row, lambda: model.residual_deviance())

        # on validation data
        appendVal(row, lambda: 1 - err_va)
        appendVal(row, lambda: err_va)
        appendVal(row, lambda: model.F1(valid=True))
        appendVal(row, lambda: model.fnr(valid=True))
        appendVal(row, lambda: model.fpr(valid=True))
        appendVal(row, lambda: model.tnr(valid=True))
        appendVal(row, lambda: model.tpr(valid=True))
        appendVal(row, lambda: model.precision(valid=True))
        appendVal(row, lambda: model.recall(valid=True))
        appendVal(row, lambda: model.sensitivity(valid=True))
        appendVal(row, lambda: model.specificity(valid=True))
        appendVal(row, lambda: model.aic(valid=True))
        appendVal(row, lambda: model.auc(valid=True))
        appendVal(row, lambda: model.logloss(valid=True))
        appendVal(row, lambda: model.mean_residual_deviance(valid=True))
        appendVal(row, lambda: model.mse(valid=True))
        appendVal(row, lambda: model.null_degrees_of_freedom(valid=True))
        appendVal(row, lambda: model.null_deviance(valid=True))
        appendVal(row, lambda: model.r2(valid=True))
        appendVal(row, lambda: model.residual_degrees_of_freedom(valid=True))
        appendVal(row, lambda: model.residual_deviance(valid=True))

        # on test data
        appendVal(row, lambda: 1 - err_te)
        appendVal(row, lambda: err_te)
        appendVal(row, lambda: metrics.F1())
        appendVal(row, lambda: metrics.fnr())
        appendVal(row, lambda: metrics.fpr())
        appendVal(row, lambda: metrics.tnr())
        appendVal(row, lambda: metrics.tpr())
        appendVal(row, lambda: metrics.precision())
        appendVal(row, lambda: metrics.recall())
        appendVal(row, lambda: metrics.sensitivity())
        appendVal(row, lambda: metrics.specificity())
        appendVal(row, lambda: metrics.aic())
        appendVal(row, lambda: metrics.auc())
        appendVal(row, lambda: metrics.logloss())
        appendVal(row, lambda: metrics.mean_residual_deviance())
        appendVal(row, lambda: metrics.mse())
        appendVal(row, lambda: metrics.null_degrees_of_freedom())
        appendVal(row, lambda: metrics.null_deviance())
        appendVal(row, lambda: metrics.r2())
        appendVal(row, lambda: metrics.residual_degrees_of_freedom())
        appendVal(row, lambda: metrics.residual_deviance())

        row = map(lambda x: None if isinstance(x, numbers.Number) and (x is None or np.isnan(x))
                                    or x == u"NaN" or x == "NaN" else x, row)

        persist(row)
        results.append(row)
        for [frame] in h2o.ls().as_matrix():
            if not keep_frames.match(frame):
                h2o.remove(frame)

        df = pd.DataFrame(results, columns=config.Names)
        return df
