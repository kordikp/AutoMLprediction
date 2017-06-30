from numpy.random import rand, randint
import fakegame as fg
import h2o
import sys


def trainerFactory(predictor):
    def trainer(x_cols, y_col, trdata, vadata, **params):
        model = predictor(**params)
        model.train(x=x_cols,y=y_col, training_frame=trdata, validation_frame=vadata)
        return model.mse(valid=True)
    return trainer


def DLtrainer(x_cols, y_col, trdata, vadata, **params):
    layers= ["hidden_l{}".format(x) for x in range(1,5)]
    params["hidden"] = [params[x] for x in layers if params.get(x,0)>0]
    for x in layers:
        try:
            del params[x]
        except:
            pass
    model = h2o.H2ODeepLearningEstimator(**params)
    model.train(x=x_cols,y=y_col, training_frame=trdata, validation_frame=vadata)
    return model.mse(valid=True)


def BPtrainer(x_cols, y_col, trdata, vadata, **params):
    model = h2o.H2OFakeGameEstimator(model_config=fg.backPropagation(**params))
    model.train(x=x_cols,y=y_col, training_frame=trdata, validation_frame=vadata)
    return model.mse(valid=True)


def CCtrainer(x_cols, y_col, trdata, vadata, **params):
    model = h2o.H2OFakeGameEstimator(model_config=fg.cascadeCorrelation(**params))
    model.train(x=x_cols,y=y_col, training_frame=trdata, validation_frame=vadata)
    return model.mse(valid=True)


def QPtrainer(x_cols, y_col, trdata, vadata, **params):
    model = h2o.H2OFakeGameEstimator(model_config=fg.quickProp(**params))
    model.train(x=x_cols,y=y_col, training_frame=trdata, validation_frame=vadata)
    return model.mse(valid=True)


def RPtrainer(x_cols, y_col, trdata, vadata, **params):
    model = h2o.H2OFakeGameEstimator(model_config=fg.rProp(**params))
    model.train(x=x_cols,y=y_col, training_frame=trdata, validation_frame=vadata)
    return model.mse(valid=True)

def getModel(model_type):
    if model_type == "gbm":     return h2o.H2OGradientBoostingEstimator
    elif model_type == "glm":   return h2o.H2OGeneralizedLinearEstimator
    elif model_type == "drf":   return h2o.H2ORandomForestEstimator
    elif model_type == "dl":    return h2o.H2ODeepLearningEstimator
    else: return h2o.H2OFakeGameEstimator

def getTrainer(model_type):
    if model_type == "gbm":    return trainerFactory(h2o.H2OGradientBoostingEstimator)
    elif model_type == "glm":  return trainerFactory(h2o.H2OGeneralizedLinearEstimator)
    elif model_type == "drf":  return trainerFactory(h2o.H2ORandomForestEstimator)
    elif model_type == "dl":   return DLtrainer
    elif model_type == "fg_bp": return BPtrainer
    elif model_type == "fg_qp": return QPtrainer
    elif model_type == "fg_rp": return RPtrainer
    elif model_type == "fg_cc": return CCtrainer


def sampleFrom(dictionary):
    res = {}
    for k,(typ, vals, default) in dictionary.iteritems():
        if typ == "categorical":
            res[k]=vals[randint(0,len(vals))]
        elif typ == "integer":
            res[k]=randint(vals[0], vals[1])
        elif typ == "real":
            scale = (min(10e300,vals[1])-max(-10e300,vals[0]))
            if default == -1 and vals[0] > -1:
                if abs(rand()) < 1.0/(scale+1):
                    res[k] = -1
                    continue
            res[k] = rand()*scale+max(vals[0],-10e300)
            if res[k] > 10e299:
                res[k] = float("+inf")
    return res


def randomSearch(trainer, params, steps):
    res = 1e15
    conf = sampleFrom(params)
    for i in range(steps):
        cur_conf = sampleFrom(params)
        try:
            tmp = trainer(**cur_conf)
            if res > tmp:
                res = tmp
                conf = cur_conf
        except:
            print(cur_conf)
            import traceback
            traceback.print_tb(sys.exc_traceback)
    return conf