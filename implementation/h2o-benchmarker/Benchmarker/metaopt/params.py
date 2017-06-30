gbm_params = {
    "balance_classes": ("categorical", [True, False], False),
    "max_after_balance_size": ("real", [0.1, 10], 5),
    "ntrees": ("integer", [10, 500], 50),
    "max_depth": ("integer", [1, 50], 5),
    "min_rows": ("real", [1, 100], 10),
    "nbins": ("integer", [2, 200], 20),
    "nbins_top_level": ("integer", [200, 8192], 1024),
    "nbins_cats": ("integer", [4, 8192], 1024),
    "r2_stopping": ("real", [0.95, 0.9999999999], 0.999999),
    "stopping_rounds": ("integer", [0, 10], 0),
    "stopping_metric": ("categorical", ["AUTO",  # "deviance",
                                        "logloss", "MSE", "AUC", "lift_top_group", "r2", "misclassification",
                                        "mean_per_class_error"], "AUTO"),
    "stopping_tolerance": ("real", [0.000000001, 0.1], 0.001),
    "learn_rate": ("real", [0.0000000001, 1], 0.1),
    "learn_rate_annealing": ("real", [0.95, 1.0], 1),
    # "distribution": ("categorical", ["AUTO", "bernoulli", "multinomial", "gaussian", "poisson", "gamma", "tweedie", "laplace", "quantile"], "AUTO"),
    "quantile_alpha": ("real", [0, 1], 0.5),
    "tweedie_power": ("real", [1, 1.999999999], 1.5),
    "sample_rate": ("real", [0.0000000001, 1], 1),
    "col_sample_rate": ("real", [0.0000000001, 1], 1),
    "col_sample_rate_change_per_level": ("real", [0, 2], 1),
    "col_sample_rate_per_tree": ("real", [0.0000000001, 1], 1.0),
    "min_split_improvement": ("real", [1e-12, 0.1], 1e-05),
    "histogram_type": ("categorical", ["AUTO", "UniformAdaptive", "Random", "QuantilesGlobal", "RoundRobin"], "AUTO"),
   # "max_abs_leafnode_pred": ("real", [100, 1.79769313486e308], 1.79769313486e+308)
}

drf_params = {
    "balance_classes": ("categorical", [True, False], False),
    "max_after_balance_size": ("real", [0.1, 10], 5),
    "ntrees": ("integer", [10, 500], 50),
    "max_depth": ("integer", [2, 200], 20),
    "min_rows": ("real", [1, 100], 1),
    "nbins": ("integer", [2, 200], 20),
    "nbins_top_level": ("integer", [200, 8192], 1024),
    "nbins_cats": ("integer", [4, 8192], 1024),
    "r2_stopping": ("real", [0.95, 0.9999999999], 0.999999),
    "stopping_rounds": ("integer", [0, 10], 0),
    "stopping_metric": ("categorical", ["AUTO",  # "deviance",
                                        "logloss", "MSE", "AUC", "lift_top_group", "r2", "misclassification",
                                        "mean_per_class_error"], "AUTO"),
    "stopping_tolerance": ("real", [0.000000001, 0.1], 0.001),
    # "mtries": ("integer",  [-1, 16], -1), # must not be 0
    "sample_rate": ("real", [0, 1.0], 0.632000029087),
    "binomial_double_trees": ("categorical", [True, False], False),
    "col_sample_rate_change_per_level": ("real", [0, 2], 1),
    "col_sample_rate_per_tree": ("real", [0, 1], 1.0),
    "min_split_improvement": ("real", [1e-12, 0.1], 1e-05),
    "histogram_type": ("categorical", ["AUTO", "UniformAdaptive", "Random", "QuantilesGlobal", "RoundRobin"], "AUTO")
}

glm_params = {
    "family": ("categorical", [  # "gaussian",
        "binomial"  # , "multinomial", "poisson", "gamma", "tweedie"
    ], "binomial"),
    "tweedie_variance_power": ("real", [0, 5], 0),
    "tweedie_link_power": ("real", [0, 5], 1),
    "solver": ("categorical", ["AUTO", "IRLSM",  # "L_BFGS",
                               "COORDINATE_DESCENT_NAIVE", "COORDINATE_DESCENT"], "AUTO"),
    "alpha": ("real", [0, 1], 0.5),
    "lambda_": ("real", [0, 10], 0),
    "lambda_search": ("categorical", [True, False], False),
    "early_stopping": ("categorical", [True, False], True),
    "nlambdas": ("integer", [1, 10], -1),
    "standardize": ("categorical", [True, False], True),
    # "remove_collinear_columns": ("categorical",  [True, False], False),
    # "intercept": ("categorical",  [True, False], True),
    "non_negative": ("categorical", [True, False], False),
    "max_iterations": ("integer", [1, 1000], -1),
    "beta_epsilon": ("real", [0.0000000001, 0.1], 0.0001),
    # "link" : ("categorical", ["family_default", "identity", "logit", "log", "inverse", "tweedie"], "family_default"),
    "prior": ("real", [0.0000000001, 0.9999999999999], -1),
    "lambda_min_ratio": ("real", [-1, 1], -1),
    "max_active_predictors": ("integer", [1, 100], -1),
    "balance_classes": ("categorical", [True, False], False),
    "max_after_balance_size": ("real", [0.1, 10], 5),
}

dl_params = {
    "balance_classes": ("categorical", [True, False], False),
    "max_after_balance_size": ("real", [0.1, 10], 5.0),
    "overwrite_with_best_model": ("categorical", [True, False], True),
    "use_all_factor_levels": ("categorical", [True, False], True),
    "standardize": ("categorical", [True, False], True),
    "activation": (
        "categorical", ["Tanh", "TanhWithDropout", "Rectifier", "RectifierWithDropout", "Maxout", "MaxoutWithDropout"],
        "Rectifier"),
    "hidden_l1": ("integer", [10, 1000], 200),
    "hidden_l2": ("integer", [10, 1000], 200),
    # "hidden_l3" : ("integer", [0, 1000], 0),
    # "hidden_l4" : ("integer", [0, 1000], 0),
    "epochs": ("real", [2, 100], 10),
    "adaptive_rate": ("categorical", [True], True),
    "rho": ("real", [0.5, 1.0], 0.99),
    "epsilon": ("real", [1e-15, 0.2], 1e-8),
    "rate": ("real", [1e-8, 0.1], 0.005),
    "rate_annealing": ("real", [1e-15, 0.2], 1e-6),
    "rate_decay": ("real", [0.8, 1.2], 1),
    "momentum_start": ("real", [0, 1.0], 0.0),
    "momentum_ramp": ("real", [100, 100000000], 1000000),
    "momentum_stable": ("real", [0, 1], 0.0),
    "nesterov_accelerated_gradient": ("categorical", [True, False], True),
    "input_dropout_ratio": ("real", [0, 0.5], 0),
    "l1": ("real", [0, 1], 0),
    "l2": ("real", [0, 1], 0),
    # "max_w2" :("real",[1,float("+inf")],float("+inf")),
    "initial_weight_distribution": ("categorical", ["UniformAdaptive", "Uniform", "Normal"], "UniformAdaptive"),
    "initial_weight_scale": ("real", [0.1, 10], 1.0),
    "loss": ("categorical", ["Automatic", "CrossEntropy", "Quadratic", "Huber", "Absolute"  # , "Quantile"
                             ], "Automatic"),
    # "distribution" : ("categorical", ["AUTO", "bernoulli", "multinomial", "gaussian", "poisson", "gamma", "tweedie", "laplace", "huber", "quantile"], "AUTO"),
    "quantile_alpha": ("real", [0, 1], 0.5),
    "tweedie_power": ("real", [1, 1.9999999999999], 1.5),
    "stopping_rounds": ("integer", [0, 10], 5),
    "stopping_metric": ("categorical", ["AUTO",  # "deviance",
                                        "logloss", "MSE",  # "AUC",
                                        "lift_top_group", "r2", "misclassification", "mean_per_class_error"], "AUTO"),
    "stopping_tolerance": ("real", [0, 0.5], 0),
    "fast_mode": ("categorical", [True, False], True),
    "shuffle_training_data": ("categorical", [True, False], False),
    "sparse": ("categorical", [True, False], False),
    "mini_batch_size": ("integer", [1, 100], 1),
    "elastic_averaging_moving_rate": ("real", [0.5, 1], 0.9),
    "elastic_averaging_regularization": ("real", [1e-8, 0.1], 0.001)
}

fakegame_rprop_params = {
    "hidden_l1": ("integer", [1, 100], 5),
    "hidden_l2": ("integer", [1, 100], 0),
    "trainingCycles": ("integer", [10, 1000], 600),
    "acceptableError": ("real", [0,0.5], 0),
    "activationFunction": ("categorical", ["sigmoid", "sigmoid_offset","symmetric_sigmoid"], "sigmoid"),
    "etaMinus": ("real", [0, 1], 0.5),
    "etaPlus": ("real", [0,2], 1.2)
}

fakegame_quickprop_params = {
    "hidden_l1": ("integer", [1, 100], 5),
    "hidden_l2": ("integer", [1, 100], 0),
    "trainingCycles": ("integer", [10, 1000], 600),
    "acceptableError": ("real", [0,0.5], 0),
    "activationFunction": ("categorical", ["sigmoid", "sigmoid_offset","symmetric_sigmoid"], "sigmoid"),
    "maxGrowthFactor": ("real", [0, 10], 2),
    "epsilon": ("real", [0,0.1], 7e-4),
    "splitEpsilon": ("categorical", [True, False], False)
}

fakegame_backprop_params = {
    "hidden_l1": ("integer", [1, 100], 5),
    "hidden_l2": ("integer", [1, 100], 0),
    "trainingCycles": ("integer", [10, 1000], 600),
    "acceptableError": ("real", [0,0.5], 0),
    "activationFunction": ("categorical", ["sigmoid", "sigmoid_offset"], "sigmoid"),
    "learningRate": ("real", [0, 1], 0.2),
    "momentum": ("real", [0,1], 0.3),
}

fakegame_cascadeCorrelation_params = {
    "acceptableError": ("real", [0,0.5], 0.001),
    "maxLayersNumber": ("integer", [1, 10], 5),
    "candNumber": ("integer", [1, 10], 1),
    "usedAlg": ("categorical", ["Quickprop", "Rprop"], "Rprop"),
    "activationFunction": ("categorical", ["sigmoid", "sigmoid_offset","symmetric_sigmoid"], "sigmoid_offset")
}


def getParameters(model_type):
    if model_type == "gbm":     return gbm_params
    elif model_type == "glm":   return glm_params
    elif model_type == "drf":   return drf_params
    elif model_type == "dl":    return dl_params
    elif model_type == "fg_bp": return fakegame_backprop_params
    elif model_type == "fg_qp": return fakegame_quickprop_params
    elif model_type == "fg_rp": return fakegame_rprop_params
    elif model_type == "fg_cc": return fakegame_cascadeCorrelation_params