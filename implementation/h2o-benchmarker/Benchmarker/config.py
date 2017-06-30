import re


hostname="localhost"
port=54321
nthreads=4
cluster="one"


Names=["cluster_name", "nthreads","name", "subset", "model", "params", "initTime", "trainTime", "metricsTime", "totalTime",
       "accuracy_train", "error_train", "F1_train", "fnr_train", "fpr_train", "tnr_train", "tpr_train",
       "precision_train", "recall_train", "sensitivity_train", "specificity_train",
       "aic_train", "auc_train", "logloss_train", "mean_res_dev_train", "mse_train",
       "null_dof_train", "null_dev_train", "r2_train", "res_dof_train", "res_dev_train", "accuracy_validation",
       "error_validation", "F1_validation", "fnr_validation", "fpr_validation", "tnr_validation", "tpr_validation",
       "precision_validation", "recall_validation", "sensitivity_validation", "specificity_validation",
       "aic_validation", "auc_validation", "logloss_validation", "mean_res_dev_validation", "mse_validation",
       "null_dof_validation", "null_dev_validation", "r2_validation", "res_dof_validation", "res_dev_validation",
       "accuracy_test", "error_test", "F1_test", "fnr_test", "fpr_test", "tnr_test", "tpr_test", "precision_test",
       "recall_test", "sensitivity_test", "specificity_test","aic_test", "auc_test", "logloss_test",
       "mean_res_dev_test", "mse_test", "null_dof_test", "null_dev_test", "r2_test", "res_dof_test", "res_dev_test"]

Label={
    "initTime" : "Initialization duration",
    "trainTime" : "Training duration",
    "metricsTime" : "Scoring duration",
    "totalTime" : "Total duration",
    "accuracy" : "Accuracy",
    "error" : "Classification error",
    "F1" : "F1 metric",
    "fnr" : "False negative rate",
    "fpr" : "False positive rate",
    "tnr" : "True positive rate",
    "sensitivity" : "Sensitivity",
    "specificity" : "Specificity",
    "precision" : "Precision",
    "recall" : "Recall",
    "aic" : "Akaike information criterion",
    "auc" : "Area under the curve",
    "logloss" : "Logarithmic loss",
    "mean_res_dev" : "Mean residual deviation",
    "mse" : "Mean squared error",
    "null_dof" : "Null degrees of freedom",
    "null_dev" : "Null deviation",
    "r2" : "$R^2$",
    "res_dof" : "Residiual degrees of freedom",
    "res_dev" : "Residual deviation",
    "train" : "training",
    "test" : "test",
    "validation" : "validation"
 }

text_fields = re.compile("cluster_name|name|model|params")

