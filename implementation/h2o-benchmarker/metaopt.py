import h2o
from Benchmarker.experiment import Experiment
import Benchmarker.config as config
import Benchmarker.utils as utils
import warnings
from optparse import OptionParser
from Benchmarker.utils import init_journal, close_journal
import numbers
import Benchmarker.metaopt.fakegame as fg
import Benchmarker.metaopt.params as p
from Benchmarker.utils import persist
import datetime
import re
import numpy as np
from numpy.random import rand, randint
import sys
import pysmac

dt = datetime.datetime

## Settings
experiment_name = ""
x_cols = []
y_col = None
data_file = ""
steps = int(sys.argv[1])

experiment_name = "Airlines 10k"
x_cols = ["Year", "Month", "DayofMonth", "DayOfWeek", "DepTime", "CRSDepTime", "ArrTime", "CRSArrTime", "UniqueCarrier",
          "FlightNum", "TailNum", "Origin", "Dest", "Distance", "TaxiIn"]
y_col = "IsDepDelayed"
data_file = "/home/frydatom/Sync/School/Vylet_2016/data/airlines_imputed_10k.csv"
# data_file = "/home/ubuntu/frydatom-vylet-2016/data/airlines_imputed_10k.csv"
## ================================================== Script ===========================================================
keep_files = None
optStart = dt.now()

experimentName = experiment_name


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


def get_classification_error(mod):
    err = None
    cm = mod.confusion_matrix()
    try:
        err = float(cm.as_data_frame()['Error'].tail(1))
    except:
        acc = mod.accuracy()
        if hasattr(acc, '__contains__'):
            err = 1.0 - acc[0][1]
        else:
            err = 1.0 - acc
    return err


def benchmark(model, model_name, params, initTime, trainTime):
    row = [config.cluster, config.nthreads, experimentName, -1, model_name, str(params),
           initTime.total_seconds(), trainTime.total_seconds(), 0,0]
    metricsIdx = len(row) - 2
    metricsTime = dt.now()
    for data in [trdata, vadata, tedata]:
        metrics = model.model_performance(test_data=data)
        err = get_classification_error(metrics)
        appendVal(row, lambda: 1 - err)
        appendVal(row, lambda: err)
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
    metricsTime = dt.now() - metricsTime
    row[metricsIdx] = metricsTime.total_seconds()
    row[metricsIdx+1] = (initTime + trainTime + metricsTime).total_seconds()
    row = map(lambda x: None if isinstance(x, numbers.Number) and (x is None or np.isnan(x))
                                or x == u"NaN" or x == "NaN" else x, row)
    persist(row)
    for [frame] in h2o.ls().as_matrix():
        if not keep_frames.match(frame):
            h2o.remove(frame)


## =================================================== Trainers ========================================================
trdata = None
vadata = None
tedata = None
keep_files = None


def GBMtrainer(**params):
    import h2o
    h2o.init()
    try:
        model = h2o.H2OGradientBoostingEstimator(**params)
        model_name = "Gradient Boosting"
        trainTime = dt.now()
        model.train(x=x_cols, y=y_col, training_frame=trdata, validation_frame=vadata)
        initTime = dt.now() - optStart
        trainTime = dt.now() - trainTime
        mse = model.mse(valid=True)
        benchmark(model, model_name, params, initTime, trainTime)
        return mse
    except:
        print("Error in {} with params {}".format(model_name, str(params)))
        return 10e15


def GLMtrainer(**params):
    import h2o
    h2o.init()
    try:
        model = h2o.H2OGeneralizedLinearEstimator(**params)
        model_name = "Generalized Linear Model"
        trainTime = dt.now()
        model.train(x=x_cols, y=y_col, training_frame=trdata, validation_frame=vadata)
        initTime = dt.now() - optStart
        trainTime = dt.now() - trainTime
        mse = model.mse(valid=True)
        benchmark(model, model_name, params, initTime, trainTime)
        return mse
    except:
        print("Error in {} with params {}".format(model_name, str(params)))
        return 10e15


def DRFtrainer(**params):
    import h2o
    h2o.init()
    try:
        model = h2o.H2ORandomForestEstimator(**params)
        model_name = "Distributed Random Forest"
        trainTime = dt.now()
        model.train(x=x_cols, y=y_col, training_frame=trdata, validation_frame=vadata)
        initTime = dt.now() - optStart
        trainTime = dt.now() - trainTime
        mse = model.mse(valid=True)
        benchmark(model, model_name, params, initTime, trainTime)
        return mse
    except:
        print("Error in {} with params {}".format(model_name, str(params)))
        return 10e15


def DLtrainer(**params):
    import h2o
    h2o.init()
    layers = ["hidden_l{}".format(x) for x in range(1, 5)]
    params["hidden"] = [params[x] for x in layers if params.get(x, 0) > 0]
    for x in layers:
        try:
            del params[x]
        except:
            pass
    try:
        model = h2o.H2ODeepLearningEstimator(**params)
        model_name = "Deep Learning"
        trainTime = dt.now()
        model.train(x=x_cols, y=y_col, training_frame=trdata, validation_frame=vadata)
        initTime = dt.now() - optStart
        trainTime = dt.now() - trainTime
        mse = model.mse(valid=True)
        benchmark(model, model_name, params, initTime, trainTime)
        return mse
    except:
        print("Error in {} with params {}".format(model_name, str(params)))
        return 10e15


def BPtrainer(**params):
    import h2o
    h2o.init()
    try:
        model = h2o.H2OFakeGameEstimator(model_config=fg.backPropagation(**params))
        model_name = "Fakegame - Backpropagation"
        trainTime = dt.now()
        model.train(x=x_cols, y=y_col, training_frame=trdata, validation_frame=vadata)
        initTime = dt.now() - optStart
        trainTime = dt.now() - trainTime
        mse = model.mse(valid=True)
        benchmark(model, model_name, params, initTime, trainTime)
        return mse
    except:
        print("Error in {} with params {}".format(model_name, str(params)))
        return 10e15


def CCtrainer(**params):
    import h2o
    h2o.init()
    try:
        model = h2o.H2OFakeGameEstimator(model_config=fg.cascadeCorrelation(**params))
        model_name = "Fakegame - Cascade Correlation"
        trainTime = dt.now()
        model.train(x=x_cols, y=y_col, training_frame=trdata, validation_frame=vadata)
        initTime = dt.now() - optStart
        trainTime = dt.now() - trainTime
        mse = model.mse(valid=True)
        benchmark(model, model_name, params, initTime, trainTime)
        return mse
    except:
        print("Error in {} with params {}".format(model_name, str(params)))
        return 10e15


def QPtrainer(**params):
    import h2o
    h2o.init()
    try:
        model = h2o.H2OFakeGameEstimator(model_config=fg.quickProp(**params))
        model_name = "Fakegame - QuickProp"
        trainTime = dt.now()
        model.train(x=x_cols, y=y_col, training_frame=trdata, validation_frame=vadata)
        initTime = dt.now() - optStart
        trainTime = dt.now() - trainTime
        mse = model.mse(valid=True)
        benchmark(model, model_name, params, initTime, trainTime)
        return mse
    except:
        print("Error in {} with params {}".format(model_name, str(params)))
        return 10e15


def RPtrainer(**params):
    import h2o
    h2o.init()
    try:
        model = h2o.H2OFakeGameEstimator(model_config=fg.rProp(**params))
        model_name = "Fakegame - RProp"
        trainTime = dt.now()
        model.train(x=x_cols, y=y_col, training_frame=trdata, validation_frame=vadata)
        initTime = dt.now() - optStart
        trainTime = dt.now() - trainTime
        mse = model.mse(valid=True)
        benchmark(model, model_name, params, initTime, trainTime)
        return mse
    except:
        print("Error in {} with params {}".format(model_name, str(params)))
        return 10e15


def sampleFrom(dictionary):
    res = {}
    for k, (typ, vals, default) in dictionary.iteritems():
        if typ == "categorical":
            res[k] = vals[randint(0, len(vals))]
        elif typ == "integer":
            res[k] = randint(vals[0], vals[1])
        elif typ == "real":
            scale = (min(10e300, vals[1]) - max(-10e300, vals[0]))
            if default == -1 and vals[0] > -1:
                if abs(rand()) < 1.0 / (scale + 1):
                    res[k] = -1
                    continue
            res[k] = rand() * scale + max(vals[0], -10e300)
    return res


def parse_params(params):
    par = {}
    forb = []
    for k,(typ, interval, default) in params.iteritems():
        if typ == "integer" or typ == "real":
            (lo, hi) = interval
            if lo > default:
                par[k] = (typ, [default, hi], default)
                forb.append("{"+"{k} > {default} && {k} < {lo}".format(k=k, default=default, lo=lo)+"}")
                continue
        par[k] = (typ, interval, default)
    print(forb)
    print(par)
    return par, forb


def randomSearch(trainer, params, steps):
    global experimentName, optStart
    optStart = dt.now()
    experimentName = experiment_name + " - Random Search"
    for i in range(steps):
        cur_conf = sampleFrom(params)
        try:
            trainer(**cur_conf)
        except:
            print(cur_conf)
            import traceback
            traceback.print_tb(sys.exc_traceback)


def smac(trainer, params, steps):
    global experimentName, optStart
    optStart = dt.now()
    experimentName = experiment_name + " - SMAC"
    opt = pysmac.SMAC_optimizer()
    parms, forbidden = parse_params(params)
    opt.minimize(trainer, steps, parms, forbidden_clauses = forbidden)


algs = [(GLMtrainer, p.glm_params),(CCtrainer, p.fakegame_cascadeCorrelation_params), (DRFtrainer, p.drf_params),(GBMtrainer, p.gbm_params), 
    (BPtrainer, p.fakegame_backprop_params),(DLtrainer, p.dl_params), (QPtrainer, p.fakegame_quickprop_params), (RPtrainer, p.fakegame_rprop_params)]

##################################################### Run ##############################################################

if __name__ == '__main__':
    opt = OptionParser()
    opt.add_option("-n", "--nthreads", dest="nthreads", help="number of threads used by h2o")
    opt.add_option("-c", "--cluster", dest="cluster", help="cluster name used by h2o to establish connection")
    opt.add_option("-j", "--journal-file", dest="journal", help="filename of a journal, i.e., file that gets"
                                                                "appended by each result in case something goes wrong")

    (options, args) = opt.parse_args()

    if options.journal:
        utils.journal_file = options.journal

    init_journal()
    # Sanity checks
    try:
        f = open(utils.journal_file, "a")
        f.close()
    except:
        warnings.warn("An error occurred during opening the journal file. Have you set it properly? (-j)")
        exit(1)

    config.hostname = "127.0.0.1"
    config.port = 54321
    config.nthreads = int(options.nthreads) if int(options.nthreads) >= 1 or options.nthreads is None else 4
    config.cluster = "one" if options.cluster == "" or options.cluster is None else options.cluster

    # Actual code to run
    h2o.init(config.hostname, config.port, nthreads=config.nthreads, cluster_name=config.cluster)
    h2o.remove_all()
    data = h2o.import_file(data_file)
    r = data.runif()
    trdata = data[r < 0.5]
    vadata = data[(r >= 0.5) & (r < 0.75)]
    tedata = data[r >= 0.75]

    keep_frames = re.compile("|".join([trdata.frame_id, vadata.frame_id, tedata.frame_id]) + "|.*\\.hex|py_.*")

    for (tr, par) in algs:
        print("random search")
        randomSearch(tr, par, steps)
        print("smac")
        smac(tr, par, steps)
