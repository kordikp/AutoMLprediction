#!/usr/bin/env python
# -*- encoding: utf-8 -*-
#
# This file is auto-generated by h2o-3/h2o-bindings/bin/gen_python.py
# Copyright 2016 H2O.ai;  Apache License Version 2.0 (see LICENSE for details)
#
from .estimator_base import H2OEstimator


class H2OFakeGameEstimator(H2OEstimator):
    """
    FakeGame
    --------

    Parameters (optional, unless specified otherwise)
    ----------
      training_frame : str
        Id of the training data frame (Not required, to allow initial validation of model parameters).

      validation_frame : str
        Id of the validation data frame.

      response_column : VecSpecifier
        Response variable column.

      model_config : str
        Model configuration

      ignored_columns : list(str)
        Names of columns to ignore for training.

    """
    def __init__(self, **kwargs):
        super(H2OFakeGameEstimator, self).__init__()
        self._parms = {}
        for name in ["training_frame", "validation_frame", "response_column", "model_config", "ignored_columns"]:
            pname = name[:-1] if name[-1] == '_' else name
            self._parms[pname] = kwargs[name] if name in kwargs else None

    @property
    def training_frame(self):
        return self._parms["training_frame"]

    @training_frame.setter
    def training_frame(self, value):
        self._parms["training_frame"] = value

    @property
    def validation_frame(self):
        return self._parms["validation_frame"]

    @validation_frame.setter
    def validation_frame(self, value):
        self._parms["validation_frame"] = value

    @property
    def response_column(self):
        return self._parms["response_column"]

    @response_column.setter
    def response_column(self, value):
        self._parms["response_column"] = value

    @property
    def model_config(self):
        return self._parms["model_config"]

    @model_config.setter
    def model_config(self, value):
        self._parms["model_config"] = value

    @property
    def ignored_columns(self):
        return self._parms["ignored_columns"]

    @ignored_columns.setter
    def ignored_columns(self, value):
        self._parms["ignored_columns"] = value

