"""
This module implements grid search class. All grid search things inherit from this class.
"""
from __future__ import print_function
from __future__ import absolute_import
from builtins import zip
from builtins import range
import h2o
from h2o.connection import H2OConnection
from h2o.job import H2OJob
from h2o.frame import H2OFrame
from h2o.estimators.estimator_base import H2OEstimator
from h2o.two_dim_table import H2OTwoDimTable
from h2o.display import H2ODisplay
import itertools
from .metrics import *


class H2OGridSearch(object):


  def __init__(self, model, hyper_params, grid_id=None, search_criteria=None):
    """
    Grid Search of a Hyper-Parameter Space for a Model
  
    Parameters
    ----------
    model : H2OEstimator
      The type of model to be explored initialized with optional parameters that will be
      unchanged across explored models.
    hyper_params: dict
      A dictionary of string parameters (keys) and a list of values to be explored by grid
      search (values).
    grid_id : str, optional
      The unique id assigned to the resulting grid object. If none is given, an id will
      automatically be generated.
    search_criteria: dict, optional
      A dictionary of directives which control the search of the hyperparameter space.
      The default strategy 'Cartesian' covers the entire space of hyperparameter combinations.
      Specify the 'RandomDiscrete' strategy to get random search of all the combinations 
      of your hyperparameters.  RandomDiscrete should usually be combined with at least one early 
      stopping criterion, max_models and/or max_runtime_secs, e.g. 
      search_criteria = {strategy: 'RandomDiscrete', max_models: 42, max_runtime_secs: 28800} or
      search_criteria = {strategy: 'RandomDiscrete', stopping_metric: 'AUTO', stopping_tolerance: 0.001, stopping_rounds: 10} or
      search_criteria = {strategy: 'RandomDiscrete', stopping_metric: 'misclassification', stopping_tolerance: 0.00001, stopping_rounds: 5}.
     
    Returns
    -------
      A new H2OGridSearch instance.
    
    Examples
    --------
      >>> from h2o.grid.grid_search import H2OGridSearch
      >>> from h2o.estimators.glm import H2OGeneralizedLinearEstimator
      >>> hyper_parameters = {'alpha': [0.01,0.5], 'lambda': [1e-5,1e-6]}
      >>> gs = H2OGridSearch(H2OGeneralizedLinearEstimator(family='binomial'), hyper_parameters)
      >>> training_data = h2o.import_file("smalldata/logreg/benign.csv")
      >>> gs.train(x=range(3) + range(4,11),y=3, training_frame=training_data)
      >>> gs.show()
    """
    self._id = grid_id
    self.model = model() if model.__class__.__name__ == 'type' else model  # H2O Estimator child class
    self.hyper_params = dict(hyper_params)
    self.search_criteria = None if None == search_criteria else dict(search_criteria)
    self._grid_json = None
    self.models = None # list of H2O Estimator instances
    self._parms = {} # internal, for object recycle #
    self.parms = {}    # external#
    self._future = False  # used by __repr__/show to query job state#
    self._job = None      # used when _future is True#

  @property
  def grid_id(self):
    """
    Returns
    -------
      A key that identifies this grid search object in H2O.
    """
    return self._id

  @grid_id.setter
  def grid_id(self, value):
    oldname = self.grid_id
    self._id = value
    h2o.rapids("(rename \"{}\" \"{}\")".format(oldname, value))

  @property
  def model_ids(self):
    return [i['name'] for i in self._grid_json["model_ids"]]

  @property
  def hyper_names(self):
    return self._grid_json["hyper_names"]

  @property
  def failed_params(self):
    return self._grid_json["failed_params"] if self._grid_json["failed_params"] else None

  @property
  def failure_details(self):
    return self._grid_json['failure_details'] if self._grid_json['failure_details'] else None

  @property
  def failure_stack_traces(self):
    return self._grid_json['failure_stack_traces'] if self._grid_json['failure_stack_traces'] else None

  @property
  def failed_raw_params(self):
    return self._grid_json['failed_raw_params'] if self._grid_json['failed_raw_params'] else None

  def start(self,x,y=None,training_frame=None,offset_column=None,fold_column=None,weights_column=None,validation_frame=None,**params):
    """Asynchronous model build by specifying the predictor columns, response column, and any
    additional frame-specific values.

    To block for results, call join.

    Parameters
    ----------
      x : list
        A list of column names or indices indicating the predictor columns.
      y : str
        An index or a column name indicating the response column.
      training_frame : H2OFrame
        The H2OFrame having the columns indicated by x and y (as well as any
        additional columns specified by fold, offset, and weights).
      offset_column : str, optional
        The name or index of the column in training_frame that holds the offsets.
      fold_column : str, optional
        The name or index of the column in training_frame that holds the per-row fold
        assignments.
      weights_column : str, optional
        The name or index of the column in training_frame that holds the per-row weights.
      validation_frame : H2OFrame, optional
        H2OFrame with validation data to be scored on while training.
    """
    self._future=True
    self.train(x=x,
               y=y,
               training_frame=training_frame,
               offset_column=offset_column,
               fold_column=fold_column,
               weights_column=weights_column,
               validation_frame=validation_frame,
               **params)

  def join(self):
    self._future=False
    self._job.poll()
    self._job=None

  def train(self,x,y=None,training_frame=None,offset_column=None,fold_column=None,weights_column=None,validation_frame=None,**params):
    #same api as estimator_base train
    algo_params = locals()
    parms = self._parms.copy()
    parms.update({k:v for k, v in algo_params.items() if k not in ["self","params", "algo_params", "parms"] })
    parms["search_criteria"] = self.search_criteria
    parms["hyper_parameters"] = self.hyper_params  # unique to grid search
    parms.update({k:v for k,v in list(self.model._parms.items()) if v is not None})  # unique to grid search
    parms.update(params)
    if '__class__' in parms:  # FIXME: hackt for PY3
      del parms['__class__']
    y = algo_params["y"]
    tframe = algo_params["training_frame"]
    if tframe is None: raise ValueError("Missing training_frame")
    if y is not None:
      if isinstance(y, (list, tuple)):
        if len(y) == 1: parms["y"] = y[0]
        else: raise ValueError('y must be a single column reference')
      self._estimator_type = "classifier" if tframe[y].isfactor() else "regressor"
    self.build_model(parms)

  def build_model(self, algo_params):
    if algo_params["training_frame"] is None: raise ValueError("Missing training_frame")
    x = algo_params.pop("x")
    y = algo_params.pop("y",None)
    training_frame = algo_params.pop("training_frame")
    validation_frame = algo_params.pop("validation_frame",None)
    is_auto_encoder = (algo_params is not None) and ("autoencoder" in algo_params and algo_params["autoencoder"])
    algo = self.model._compute_algo() #unique to grid search
    is_unsupervised = is_auto_encoder or algo == "pca" or algo == "svd" or algo == "kmeans" or algo == "glrm"
    if is_auto_encoder and y is not None: raise ValueError("y should not be specified for autoencoder.")
    if not is_unsupervised and y is None: raise ValueError("Missing response")
    self._model_build(x, y, training_frame, validation_frame, algo_params)

  def _model_build(self, x, y, tframe, vframe, kwargs):
    kwargs['training_frame'] = tframe
    if vframe is not None: kwargs["validation_frame"] = vframe
    if isinstance(y, int): y = tframe.names[y]
    if y is not None: kwargs['response_column'] = y
    if not isinstance(x, (list,tuple)): x=[x]
    if isinstance(x[0], int):
      x = [tframe.names[i] for i in x]
    offset = kwargs["offset_column"]
    folds  = kwargs["fold_column"]
    weights= kwargs["weights_column"]
    ignored_columns = list(set(tframe.names) - set(x + [y,offset,folds,weights]))
    kwargs["ignored_columns"] = None if ignored_columns==[] else [h2o.h2o._quoted(col) for col in ignored_columns]
    kwargs = dict([(k, kwargs[k].frame_id if isinstance(kwargs[k], H2OFrame) else kwargs[k]) for k in kwargs if kwargs[k] is not None])  # gruesome one-liner
    algo = self.model._compute_algo()  #unique to grid search
    kwargs["_rest_version"] = 99  #unique to grid search
    if self.grid_id is not None: kwargs["grid_id"] = self.grid_id 

    grid = H2OJob(H2OConnection.post_json("Grid/"+algo, **kwargs), job_type=(algo+" Grid Build"))

    if self._future:
      self._job = grid
      return

    grid.poll()
    if '_rest_version' in list(kwargs.keys()):
      grid_json = H2OConnection.get_json("Grids/"+grid.dest_key, _rest_version=kwargs['_rest_version'])

      error_index = 0
      if len(grid_json["failure_details"]) > 0:
        print("Errors/Warnings building gridsearch model\n")

        for error_message in grid_json["failure_details"]:
          if isinstance(grid_json["failed_params"][error_index], dict):
            for h_name in grid_json['hyper_names']:
              print("Hyper-parameter: {0}, {1}".format(h_name, grid_json['failed_params'][error_index][h_name]))

          if len(grid_json["failure_stack_traces"]) > error_index:
            print("failure_details: {0}\nfailure_stack_traces: "
                  "{1}\n".format(error_message, grid_json['failure_stack_traces'][error_index]))
          error_index += 1
    else:
      grid_json = H2OConnection.get_json("Grids/"+grid.dest_key)

    self.models = [h2o.get_model(key['name']) for key in grid_json['model_ids']]

    #get first model returned in list of models from grid search to get model class (binomial, multinomial, etc)
    # sometimes no model is returned due to bad parameter values provided by the user.
    if len(grid_json['model_ids']) > 0:
      first_model_json = H2OConnection.get_json("Models/"+grid_json['model_ids'][0]['name'],
                                                _rest_version=kwargs['_rest_version'])['models'][0]
      self._resolve_grid(grid.dest_key, grid_json, first_model_json)
    else:
      raise ValueError("Gridsearch returns no model due to bad parameter values or other reasons....")

  def _resolve_grid(self, grid_id, grid_json, first_model_json):
    model_class = H2OGridSearch._metrics_class(first_model_json)
    m = model_class()
    m._id = grid_id
    m._grid_json = grid_json
    # m._metrics_class = metrics_class
    m._parms = self._parms
    H2OEstimator.mixin(self,model_class)
    self.__dict__.update(m.__dict__.copy())

  def __getitem__(self, item):
    return self.models[item]

  def __iter__(self):
    nmodels = len(self.models)
    return (self[i] for i in range(nmodels))

  def __len__(self):
    return len(self.models)

  def __repr__(self):
    self.show()
    return ""

  def predict(self, test_data):
    """Predict on a dataset.

    Parameters
    ----------
    test_data : H2OFrame
      Data to be predicted on.

    Returns
    -------
      H2OFrame filled with predictions.
    """
    return {model.model_id:model.predict(test_data) for model in self.models}

  def is_cross_validated(self):
    """
    Returns
    -------
      True if the model was cross-validated.
    """
    return {model.model_id:model.is_cross_validated() for model in self.models}

  def xval_keys(self):
    """
    Returns
    -------
      The model keys for the cross-validated model.
    """
    return {model.model_id: model.xval_keys() for model in self.models}

  def get_xval_models(self,key=None):
    """Return a Model object.

    Parameters
    ----------
      key : str
        If None, return all cross-validated models; otherwise return the model that key
        points.

    Returns
    -------
      A model or list of models.
    """
    return {model.model_id: model.get_xval_models(key) for model in self.models}

  def xvals(self):
    """
    Returns
    -------
      A list of cross-validated models.
    """
    return {model.model_id:model.xvals for model in self.models}

  def deepfeatures(self, test_data, layer):
    """Obtain a hidden layer's details on a dataset.

    Parameters
    ----------
    test_data: H2OFrame
      Data to create a feature space on
    layer: int
      index of the hidden layer

    Returns
    -------
      A dictionary of hidden layer details for each model.
    """
    return {model.model_id:model.deepfeatures(test_data, layer) for model in self.models}

  def weights(self, matrix_id=0):
    """
    Return the frame for the respective weight matrix
    :param: matrix_id: an integer, ranging from 0 to number of layers, that specifies the weight matrix to return.
    :return: an H2OFrame which represents the weight matrix identified by matrix_id
    """
    return {model.model_id:model.weights(matrix_id) for model in self.models}

  def biases(self, vector_id=0):
    """
    Return the frame for the respective bias vector
    :param: vector_id: an integer, ranging from 0 to number of layers, that specifies the bias vector to return.
    :return: an H2OFrame which represents the bias vector identified by vector_id
    """
    return {model.model_id:model.biases(vector_id) for model in self.models}

  def normmul(self):
    """
    Normalization/Standardization multipliers for numeric predictors
    """
    return {model.model_id:model.normmul() for model in self.models}

  def normsub(self):
    """
    Normalization/Standardization offsets for numeric predictors
    """
    return {model.model_id:model.normsub() for model in self.models}

  def respmul(self):
    """
    Normalization/Standardization multipliers for numeric response
    """
    return {model.model_id:model.respmul() for model in self.models}

  def respsub(self):
    """
    Normalization/Standardization offsets for numeric response
    """
    return {model.model_id:model.respsub() for model in self.models}

  def catoffsets(self):
    """
    Categorical offsets for one-hot encoding
    """
    return {model.model_id:model.catoffsets() for model in self.models}

  def model_performance(self, test_data=None, train=False, valid=False, xval=False):
    """Generate model metrics for this model on test_data.

    :param test_data: Data set for which model metrics shall be computed against. All three of train, valid and xval arguments are ignored if test_data is not None.
    :param train: Report the training metrics for the model.
    :param valid: Report the validation metrics for the model.
    :param xval: Report the validation metrics for the model.
    :return: An object of class H2OModelMetrics.
    """
    return {model.model_id:model.model_performance(test_data, train, valid, xval) for model in self.models}

  def scoring_history(self):
    """Retrieve Model Score History

    Returns
    -------
      Score history (H2OTwoDimTable)
    """
    return {model.model_id:model.scoring_history() for model in self.models}

  def summary(self, header=True):
    """Print a detailed summary of the explored models."""
    table = []
    for model in self.models:
      model_summary = model._model_json["output"]["model_summary"]
      r_values = list(model_summary.cell_values[0])
      r_values[0] = model.model_id
      table.append(r_values)
        
    # if h2o.can_use_pandas():
    #  import pandas
    #  pandas.options.display.max_rows = 20
    #  print pandas.DataFrame(table,columns=self.col_header)
    #  return
    print()
    if header:
      print('Grid Summary:')
    print()    
    H2ODisplay(table, ['Model Id'] + model_summary.col_header[1:], numalign="left", stralign="left")


  def show(self):
    """Print models sorted by metric"""
    hyper_combos = itertools.product(*list(self.hyper_params.values()))
    if not self.models:
      c_values = [[idx+1, list(val)] for idx, val in enumerate(hyper_combos)]
      print(H2OTwoDimTable(col_header=['Model', 'Hyperparameters: [' + ', '.join(list(self.hyper_params.keys()))+']'],
                           table_header='Grid Search of Model ' + self.model.__class__.__name__, cell_values=c_values))
    else:
      print(self.sorted_metric_table())


  def varimp(self, use_pandas=False):
    """Pretty print the variable importances, or return them in a list/pandas DataFrame

    Parameters
    ----------
    use_pandas: boolean, optional
      If True, then the variable importances will be returned as a pandas data frame.

    Returns
    -------
      A dictionary of lists or Pandas DataFrame instances.
    """
    return {model.model_id:model.varimp(use_pandas) for model in self.models}


  def residual_deviance(self,train=False,valid=False,xval=False):
    """Retreive the residual deviance if this model has the attribute, or None otherwise.

    Parameters
    ----------
    train : boolean, optional, default=True
      Get the residual deviance for the training set. If both train and valid are False,
      then train is selected by default.
    valid: boolean, optional
      Get the residual deviance for the validation set. If both train and valid are True,
      then train is selected by default.
    xval : boolean, optional
      Get the residual deviance for the cross-validated models.

    Returns
    -------
      Return the residual deviance, or None if it is not present.
    """
    return {model.model_id:model.residual_deviance(train, valid, xval) for model in self.models}


  def residual_degrees_of_freedom(self,train=False,valid=False,xval=False):
    """
    Retreive the residual degress of freedom if this model has the attribute, or None otherwise.

    :param train: Get the residual dof for the training set. If both train and valid are False, then train is selected by default.
    :param valid: Get the residual dof for the validation set. If both train and valid are True, then train is selected by default.
    :return: Return the residual dof, or None if it is not present.
    """
    return {model.model_id:model.residual_degrees_of_freedom(train, valid, xval) for model in self.models}

  def null_deviance(self,train=False,valid=False,xval=False):
    """
    Retreive the null deviance if this model has the attribute, or None otherwise.

    :param:  train Get the null deviance for the training set. If both train and valid are False, then train is selected by default.
    :param:  valid Get the null deviance for the validation set. If both train and valid are True, then train is selected by default.
    :return: Return the null deviance, or None if it is not present.
    """
    return {model.model_id:model.null_deviance(train, valid, xval) for model in self.models}

  def null_degrees_of_freedom(self,train=False,valid=False,xval=False):
    """
    Retreive the null degress of freedom if this model has the attribute, or None otherwise.

    :param train: Get the null dof for the training set. If both train and valid are False, then train is selected by default.
    :param valid: Get the null dof for the validation set. If both train and valid are True, then train is selected by default.
    :return: Return the null dof, or None if it is not present.
    """
    return {model.model_id:model.null_degrees_of_freedom(train, valid, xval) for model in self.models}

  def pprint_coef(self):
    """
    Pretty print the coefficents table (includes normalized coefficients)
    :return: None
    """
    for i, model in enumerate(self.models):
      print('Model', i)
      model.pprint_coef()
      print()

  def coef(self):
    """
    :return: Return the coefficients for this model.
    """
    return {model.model_id:model.coef() for model in self.models}

  def coef_norm(self):
    """
    :return: Return the normalized coefficients
    """
    return {model.model_id:model.coef_norm() for model in self.models}

  def r2(self,  train=False, valid=False, xval=False):
    """
    Return the R^2 for this regression model.

    The R^2 value is defined to be 1 - MSE/var,
    where var is computed as sigma*sigma.

    If all are False (default), then return the training metric value.
    If more than one options is set to True, then return a dictionary of metrics where the keys are "train", "valid",
    and "xval"

    :param train: If train is True, then return the R^2 value for the training data.
    :param valid: If valid is True, then return the R^2 value for the validation data.
    :param xval:  If xval is True, then return the R^2 value for the cross validation data.
    :return: The R^2 for this regression model.
    """
    return {model.model_id:model.r2(train, valid, xval) for model in self.models}

  def mse(self, train=False, valid=False, xval=False):
    """
    Get the MSE(s).
    If all are False (default), then return the training metric value.
    If more than one options is set to True, then return a dictionary of metrics where the keys are "train", "valid",
    and "xval"

    :param train: If train is True, then return the MSE value for the training data.
    :param valid: If valid is True, then return the MSE value for the validation data.
    :param xval:  If xval is True, then return the MSE value for the cross validation data.
    :return: The MSE for this regression model.
    """
    return {model.model_id:model.mse(train, valid, xval) for model in self.models}

  def logloss(self, train=False, valid=False, xval=False):
    """
    Get the Log Loss(s).
    If all are False (default), then return the training metric value.
    If more than one options is set to True, then return a dictionary of metrics where the keys are "train", "valid",
    and "xval"

    :param train: If train is True, then return the Log Loss value for the training data.
    :param valid: If valid is True, then return the Log Loss value for the validation data.
    :param xval:  If xval is True, then return the Log Loss value for the cross validation data.
    :return: The Log Loss for this binomial model.
    """
    return {model.model_id:model.logloss(train, valid, xval) for model in self.models}

  def mean_residual_deviance(self, train=False, valid=False, xval=False):
    """
    Get the Mean Residual Deviances(s).
    If all are False (default), then return the training metric value.
    If more than one options is set to True, then return a dictionary of metrics where the keys are "train", "valid",
    and "xval"

    :param train: If train is True, then return the Mean Residual Deviance value for the training data.
    :param valid: If valid is True, then return the Mean Residual Deviance value for the validation data.
    :param xval:  If xval is True, then return the Mean Residual Deviance value for the cross validation data.
    :return: The Mean Residual Deviance for this regression model.
    """
    return {model.model_id:model.mean_residual_deviance(train, valid, xval) for model in self.models}

  def auc(self, train=False, valid=False, xval=False):
    """
    Get the AUC(s).
    If all are False (default), then return the training metric value.
    If more than one options is set to True, then return a dictionary of metrics where the keys are "train", "valid",
    and "xval"

    :param train: If train is True, then return the AUC value for the training data.
    :param valid: If valid is True, then return the AUC value for the validation data.
    :param xval:  If xval is True, then return the AUC value for the validation data.
    :return: The AUC.
    """
    return {model.model_id:model.auc(train, valid, xval) for model in self.models}

  def aic(self, train=False, valid=False, xval=False):
    """
    Get the AIC(s).
    If all are False (default), then return the training metric value.
    If more than one options is set to True, then return a dictionary of metrics where the keys are "train", "valid",
    and "xval"

    :param train: If train is True, then return the AIC value for the training data.
    :param valid: If valid is True, then return the AIC value for the validation data.
    :param xval:  If xval is True, then return the AIC value for the validation data.
    :return: The AIC.
    """
    return {model.model_id:model.aic(train, valid, xval) for model in self.models}


  def giniCoef(self, train=False, valid=False, xval=False):
    """
    Get the Gini Coefficient(s).
    If all are False (default), then return the training metric value.
    If more than one options is set to True, then return a dictionary of metrics where the keys are "train", "valid",
    and "xval"

    :param train: If train is True, then return the Gini Coefficient value for the training data.
    :param valid: If valid is True, then return the Gini Coefficient value for the validation data.
    :param xval:  If xval is True, then return the Gini Coefficient value for the cross validation data.
    :return: The Gini Coefficient for this binomial model.
    """
    return {model.model_id:model.giniCoef(train, valid, xval) for model in self.models}


  def sort_by(self, metric, increasing=True):
    """
    Sort the models in the grid space by a metric.
    
    Parameters
    ----------
    metric: str
      A metric ('logloss', 'auc', 'r2') by which to sort the models. If addtional arguments are desired,
      they can be passed to the metric, for example 'logloss(valid=True)'
    increasing: boolean, optional
      Sort the metric in increasing (True) (default) or decreasing (False) order.
      
    Returns
    -------
      An H2OTwoDimTable of the sorted models showing model id, hyperparameters, and metric value. The best model can 
      be selected and used for prediction.
     
    Examples
    --------
      >>> grid_search_results = gs.sort_by('F1', False)
      >>> best_model_id = grid_search_results['Model Id'][0]
      >>> best_model = h2o.get_model(best_model_id)
      >>> best_model.predict(test_data)
    """

    if metric[-1] != ')': metric += '()'
    c_values = [list(x) for x in zip(*sorted(eval('self.' + metric + '.items()'), key = lambda k_v: k_v[1]))]
    c_values.insert(1,[self.get_hyperparams(model_id, display=False) for model_id in c_values[0]])
    if not increasing:
      for col in c_values: col.reverse()
    if metric[-2] == '(': metric = metric[:-2]
    return H2OTwoDimTable(col_header=['Model Id', 'Hyperparameters: [' + ', '.join(list(self.hyper_params.keys()))+']', metric],
                              table_header='Grid Search Results for ' + self.model.__class__.__name__, cell_values=list(zip(*c_values)))


  def get_hyperparams(self, id, display=True):
    """
    Get the hyperparameters of a model explored by grid search.
    
    Parameters
    ----------    
    id: str
      The model id of the model with hyperparameters of interest.
    display: boolean 
      Flag to indicate whether to display the hyperparameter names.
      
    Returns
    -------
      A list of the hyperparameters for the specified model.
    """
    idx = id if isinstance(id, int) else self.model_ids.index(id)
    model = self[idx]

    # if cross-validation is turned on, parameters in one of the fold model actuall contains the max_runtime_secs
    # parameter and not the main model that is returned.
    if model._is_xvalidated:
      model = h2o.get_model(model._xval_keys[0])

    res = [model.params[h]['actual'][0] if isinstance(model.params[h]['actual'],list)
           else model.params[h]['actual']
           for h in self.hyper_params]
    if display: print('Hyperparameters: [' + ', '.join(list(self.hyper_params.keys()))+']')
    return res

  def get_hyperparams_dict(self, id, display=True):
    """
    Derived and returned the model parameters used to train the particular grid search model.

    Parameters
    ----------
    id: str
      The model id of the model with hyperparameters of interest.
    display: boolean
      Flag to indicate whether to display the hyperparameter names.

    Returns
    -------
      A dict of model pararmeters derived from the hyper-parameters used to train this particular model.
    """
    idx = id if isinstance(id, int) else self.model_ids.index(id)
    model = self[idx]

    model_params = dict()

    # if cross-validation is turned on, parameters in one of the fold model actual contains the max_runtime_secs
    # parameter and not the main model that is returned.
    if model._is_xvalidated:
      model = h2o.get_model(model._xval_keys[0])

    for param_name in self.hyper_names:
      model_params[param_name] = model.params[param_name]['actual'][0] if \
        isinstance(model.params[param_name]['actual'], list) else model.params[param_name]['actual']

    if display: print('Hyperparameters: [' + ', '.join(list(self.hyper_params.keys()))+']')
    return model_params

  def sorted_metric_table(self):
    """
    Retrieve Summary Table of an H2O Grid Search
    
    Returns
    -------
      The summary table as an H2OTwoDimTable or a Pandas DataFrame.
    """
    summary = self._grid_json["summary_table"]
    if summary is not None: return summary.as_data_frame()
    print("No sorted metric table for this grid search")

  @staticmethod
  def _metrics_class(model_json):
    model_type = model_json["output"]["model_category"]
    if model_type=="Binomial":      model_class = H2OBinomialGridSearch
    elif model_type=="Clustering":  model_class = H2OClusteringGridSearch
    elif model_type=="Regression":  model_class = H2ORegressionGridSearch
    elif model_type=="Multinomial": model_class = H2OMultinomialGridSearch
    elif model_type=="AutoEncoder": model_class = H2OAutoEncoderGridSearch
    elif model_type=="DimReduction":model_class = H2ODimReductionGridSearch
    else: raise NotImplementedError(model_type)
    return model_class

  def get_grid(self, sort_by=None, decreasing=None):
    """
    Retrieve an H2OGridSearch instance. Optionally specify a metric by which to sort models and a sort order.  
    
    Parameters
    ----------    
    sort_by : str, optional
      A metric by which to sort the models in the grid space. Choices are "logloss", "residual_deviance", "mse", "auc", "r2", "accuracy", "precision", "recall", "f1", etc.
    decreasing : bool, optional
      Sort the models in decreasing order of metric if true, otherwise sort in increasing order (default).
    Returns
    -------
      A new H2OGridSearch instance optionally sorted on the specified metric.

    """
    if sort_by is None and decreasing is None: return self

    grid_json = H2OConnection.get_json("Grids/"+self._id, sort_by=sort_by, decreasing=decreasing, _rest_version=99)
    grid = H2OGridSearch(self.model, self.hyper_params, self._id)
    grid.models = [h2o.get_model(key['name']) for key in grid_json['model_ids']] #reordered
    first_model_json = H2OConnection.get_json("Models/"+grid_json['model_ids'][0]['name'], _rest_version=99)['models'][0]
    model_class = H2OGridSearch._metrics_class(first_model_json)
    m = model_class()
    m._id = self._id
    m._grid_json = grid_json
    # m._metrics_class = metrics_class
    m._parms = grid._parms
    H2OEstimator.mixin(grid,model_class)
    grid.__dict__.update(m.__dict__.copy())
    return grid
