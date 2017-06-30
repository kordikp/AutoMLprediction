# -*- coding: utf-8 -*-
# import numpy    no numpy cuz windoz
from __future__ import print_function
from __future__ import absolute_import

import requests
from six import iteritems, itervalues
import collections
from io import StringIO
import csv
import imp
import os
import tempfile
import sys
import traceback
import random
from .utils.shared_utils import _quoted, can_use_pandas, can_use_numpy, _handle_python_lists, _is_list, _is_str_list, _handle_python_dicts, _handle_numpy_array, _handle_pandas_data_frame, quote
from .display import H2ODisplay
from .connection import H2OConnection
from .job import H2OJob
from .expr import ExprNode
from .group_by import GroupBy
import h2o
from past.builtins import basestring
from functools import reduce
import warnings
warnings.filterwarnings("ignore", category=DeprecationWarning, module="pandas", lineno=7)


# TODO: Automatically convert column names into Frame properties!
class H2OFrame(object):
  def __init__(self, python_obj=None):
    self._ex = ExprNode()
    self._ex._children=None
    if python_obj is not None:
      self._upload_python_object(python_obj)

  @property
  def columns(self):
    """
    Returns
    -------
      A list of column names.
    """
    return self.names

  @columns.setter
  def columns(self, value):
    """Set the column names of this H2OFrame.

    Parameters
    ----------
      value : list
    """
    self.set_names(value)

  @property
  def col_names(self):
    """
    Returns
    -------
      A list of column names.
    """
    return self.names

  @col_names.setter
  def col_names(self, value):
    """Set the column names of this H2OFrame.

    Parameters
    ----------
      value : list
    """
    self.set_names(value)

  @property
  def names(self):
    """Retrieve the column names (one name per H2OVec) for this H2OFrame.

    Returns
    -------
      A str list of column names
    """
    if not self._ex._cache.names_valid():
      self._ex._cache.flush()
      self._frame(True)
    return self._ex._cache.names

  @names.setter
  def names(self,value):
    """Set the column names of this H2OFrame.

    Parameters
    ----------
      value : list
    """
    self.set_names(value)

  @property
  def nrow(self):
    """
    Returns
    -------
      The number of rows in the H2OFrame.
    """
    if not self._ex._cache.nrows_valid():
      self._ex._cache.flush()
      self._frame(True)
    return self._ex._cache.nrows

  @property
  def ncol(self):
    """
    Returns
    -------
      The number of columns in the H2OFrame.
    """
    if not self._ex._cache.ncols_valid():
      self._ex._cache.flush()
      self._frame(True)
    return self._ex._cache.ncols

  @property
  def dim(self):
    """
    Returns
    -------
      The number of rows and columns in the H2OFrame as a list [rows, cols].
    """
    return [self.nrow, self.ncol]

  @property
  def shape(self):
    """
    Returns
    -------
      A tuple (nrow, ncol)
    """
    return self.nrow, self.ncol

  @property
  def types(self):
    """
    Returns
    -------
      A dictionary of column_name-type pairs.
    """
    if not self._ex._cache.types_valid():
      self._ex._cache.flush()
      self._frame(True)
    return self._ex._cache.types

  @property
  def frame_id(self):
    """
    Returns
    -------
      Get the name of this frame.
    """
    return self._frame()._ex._cache._id

  @frame_id.setter
  def frame_id(self, value):
    if self._ex._cache._id is None:
      h2o.assign(self, value)
    else:
      oldname = self.frame_id
      self._ex._cache._id = value
      h2o.rapids("(rename \"{}\" \"{}\")".format(oldname, value))

  @staticmethod
  def _expr(expr,cache=None):
    fr = H2OFrame()
    fr._ex = expr
    if cache is not None:
      fr._ex._cache.fill_from(cache)
    return fr

  @staticmethod
  def get_frame(frame_id):
    """Create an H2OFrame mapped to an existing id in the cluster.

    Returns
    -------
      H2OFrame that points to a pre-existing big data H2OFrame in the cluster
    """
    fr = H2OFrame()
    fr._ex._cache._id = frame_id
    try:
      fr._ex._cache.fill()
    except EnvironmentError:
      return None
    return fr

  def _import_parse(self, path, destination_frame, header, separator, column_names, column_types, na_strings):
    rawkey = h2o.lazy_import(path)
    self._parse(rawkey,destination_frame, header, separator, column_names, column_types, na_strings)
    return self

  def _upload_parse(self, path, destination_frame, header, sep, column_names, column_types, na_strings):
    fui = {"file": os.path.abspath(path)}
    rawkey = H2OConnection.post_json(url_suffix="PostFile", file_upload_info=fui)["destination_frame"]
    self._parse(rawkey,destination_frame, header, sep, column_names, column_types, na_strings)
    return self

  def _upload_python_object(self, python_obj, destination_frame="", header=(-1, 0, 1), separator="", column_names=None, column_types=None, na_strings=None):
    # [] and () cases -- folded together since H2OFrame is mutable
    if isinstance(python_obj, (list, tuple)): col_header, data_to_write = _handle_python_lists(python_obj, header)

    # {} and collections.OrderedDict cases
    elif isinstance(python_obj, (dict, collections.OrderedDict)): col_header, data_to_write = _handle_python_dicts(python_obj)

    # handle a numpy.ndarray, pandas.DataFrame
    else:
      if can_use_numpy() and can_use_pandas():
        import numpy
        import pandas
        if isinstance(python_obj, numpy.ndarray): col_header, data_to_write = _handle_numpy_array(python_obj, header)
        elif isinstance(python_obj, pandas.DataFrame): col_header, data_to_write = _handle_pandas_data_frame(python_obj, header)
        else: raise ValueError("`python_obj` must be a tuple, list, dict, collections.OrderedDict, numpy.ndarray, or "
                               "pandas.DataFrame. Got: " + str(type(python_obj)))
      elif can_use_numpy():
        import numpy
        if isinstance(python_obj, numpy.ndarray): col_header, data_to_write = _handle_numpy_array(python_obj, header)
        else: raise ValueError("`python_obj` must be a tuple, list, dict, collections.OrderedDict, numpy.ndarray, or "
                               "pandas.DataFrame. Got: " + str(type(python_obj)))
      elif can_use_pandas():
        import pandas
        if isinstance(python_obj, pandas.DataFrame): col_header, data_to_write = _handle_pandas_data_frame(python_obj, header)
        else: raise ValueError("`python_obj` must be a tuple, list, dict, collections.OrderedDict, numpy.ndarray, or "
                               "pandas.DataFrame. Got: " + str(type(python_obj)))
      else:
        raise ValueError("`python_obj` must be a tuple, list, dict, collections.OrderedDict, numpy.ndarray, or "
                         "pandas.DataFrame. Got: " + str(type(python_obj)))

    if col_header is None or data_to_write is None: raise ValueError("No data to write")

    #
    ## write python data to file and upload
    #

    # create a temporary file that will be written to
    tmp_handle,tmp_path = tempfile.mkstemp(suffix=".csv")
    tmp_file = os.fdopen(tmp_handle,'w')
    # create a new csv writer object thingy
    csv_writer = csv.DictWriter(tmp_file, fieldnames=col_header, restval=None, dialect="excel", extrasaction="ignore", delimiter=",", quoting=csv.QUOTE_ALL)
    csv_writer.writeheader()              # write the header
    #because we have written the header, header in this newly created tmp csv file must be 1
    header = 1
    if column_names is None: column_names = col_header
    csv_writer.writerows(data_to_write)    # write the data
    tmp_file.close()                       # close the streams
    self._upload_parse(tmp_path, destination_frame, header, separator, column_names, column_types, na_strings)  # actually upload the data to H2O
    os.remove(tmp_path)                    # delete the tmp file

  @staticmethod
  def from_python(python_obj, destination_frame="", header=(-1, 0, 1), separator="", column_names=None, column_types=None, na_strings=None):
    """Properly handle native python data types. For a discussion of the rules and
    permissible data types please refer to the main documentation for H2OFrame.

    Parameters
    ----------
      python_obj : tuple, list, dict, collections.OrderedDict
        If a nested list/tuple, then each nested collection is a row.

      destination_frame : str, optional
        The unique hex key assigned to the imported file. If none is given, a key will
        automatically be generated.

      header : int, optional
        -1 means the first line is data, 0 means guess, 1 means first line is header.

      sep : str, optional
        The field separator character. Values on each line of the file are separated by
        this character. If sep = "", the parser will automatically detect the separator.

      col_names : list, optional
        A list of column names for the file.

      col_types : list or dict, optional
        A list of types or a dictionary of column names to types to specify whether
        columns should be forced to a certain type upon import parsing. If a list, the
        types for elements that are None will be guessed. The possible types a column may
        have are.

      na_strings : list or dict, optional
        A list of strings, or a list of lists of strings (one list per column), or a
        dictionary of column names to strings which are to be interpreted as missing values.

    Returns
    -------
      A new H2OFrame instance.

    Examples
    --------
      >>> l = [[1,2,3,4,5], [99,123,51233,321]]
      >>> l = H2OFrame(l)
      >>> l
    """
    fr = H2OFrame()
    fr._upload_python_object(python_obj, destination_frame, header, separator, column_names, column_types, na_strings)
    return fr

  def _parse(self, rawkey, destination_frame="", header=None, separator=None, column_names=None, column_types=None, na_strings=None):
    setup = h2o.parse_setup(rawkey, destination_frame, header, separator, column_names, column_types, na_strings)
    return self._parse_raw(setup)

  def _parse_raw(self, setup):
    # Parse parameters (None values provided by setup)
    p = { "destination_frame" : None,
          "parse_type"        : None,
          "separator"         : None,
          "single_quotes"     : None,
          "check_header"      : None,
          "number_columns"    : None,
          "chunk_size"        : None,
          "delete_on_done"    : True,
          "blocking"          : False,
          "column_types"      : None,
          }

    if setup["column_names"]: p["column_names"] = None
    if setup["na_strings"]: p["na_strings"] = None

    p.update({k: v for k, v in iteritems(setup) if k in p})

    # Extract only 'name' from each src in the array of srcs
    p['source_frames'] = [_quoted(src['name']) for src in setup['source_frames']]

    H2OJob(H2OConnection.post_json(url_suffix="Parse", **p), "Parse").poll()
    # Need to return a Frame here for nearly all callers
    # ... but job stats returns only a dest_key, requiring another REST call to get nrow/ncol
    self._ex._cache._id = p["destination_frame"]
    self._ex._cache.fill()

  def filter_na_cols(self, frac=0.2):
    """Filter columns with proportion of NAs >= frac.

    Parameters
    ----------
      frac : float
        Fraction of NAs in the column.

    Returns
    -------
      A list of column indices that have a fewer count of NAs.
      If all columns are filtered, None is returned.
    """
    return ExprNode("filterNACols", self, frac)._eager_scalar()

  def type(self, name):
    """
    Returns
    -------
      The type for a named column
    """
    return self.types[name]

  def __iter__(self):
    return (self[i] for i in range(self.ncol))
  def __str__(self):
    if sys.gettrace() is None:
      if self._ex is None: return "This H2OFrame has been removed."
      row_string = ' rows x ' if self.nrow != 1 else ' row x '
      column_string = ' columns]' if self.ncol != 1 else ' column]'
      return self._frame()._ex._cache._tabulate("simple",False) + '\n\n[' + str(self.nrow) \
    + row_string + str(self.ncol) + column_string
    return ""
  def __len__(self):
    return self.nrow

  def __repr__(self):
    if sys.gettrace() is None:
      # PUBDEV-2278: using <method>? from IPython caused everything to dump
      stk = traceback.extract_stack()
      if not ("IPython" in stk[-2][0] and "info" == stk[-2][2]):
        self.show()
    return ""

  def show(self, use_pandas=False):
    """Used by the H2OFrame.__repr__ method to print or display a snippet of the data frame.
    If called from IPython, displays an html'ized result
    Else prints a tabulate'd result
    """
    if self._ex is None:
      print("This H2OFrame has been removed.")
      return
    if not self._ex._cache.is_valid(): self._frame()._ex._cache.fill()
    if H2ODisplay._in_ipy():
      import IPython.display
      if use_pandas and can_use_pandas():
        IPython.display.display(self.head().as_data_frame(True))
      else:
        IPython.display.display_html(self._ex._cache._tabulate("html",False),raw=True)
    else:
      if use_pandas and can_use_pandas():
        print(self.head().as_data_frame(True))
      else:
        print(self)

  def summary(self):
    """Summary includes min/mean/max/sigma and other rollup data.
    """
    if not self._ex._cache.is_valid(): self._frame()._ex._cache.fill()
    if H2ODisplay._in_ipy():
      import IPython.display
      IPython.display.display_html(self._ex._cache._tabulate("html",True),raw=True)
    else:
      print(self._ex._cache._tabulate("simple",True))

  def describe(self):
    """Generate an in-depth description of this H2OFrame. Everything in summary(), plus
    the data layout.
    """
    # Force a fetch of 10 rows; the chunk & distribution summaries are not
    # cached, so must be pulled.  While we're at it, go ahead and fill in
    # the default caches if they are not already filled in

    res = H2OConnection.get_json("Frames/"+self.frame_id+"?row_count="+str(10))["frames"][0]
    self._ex._cache._fill_data(res)
    print("Rows:{:,}".format(self.nrow), "Cols:{:,}".format(self.ncol))
    res["chunk_summary"].show()
    res["distribution_summary"].show()
    print("\n")
    self.summary()

  def _frame(self, fill_cache=False):
    self._ex._eager_frame()
    if fill_cache:
      self._ex._cache.fill()
    return self

  def head(self,rows=10,cols=200):
    """Analogous to Rs `head` call on a data.frame.

    Parameters
    ----------
      rows : int, default=10
        Number of rows starting from the topmost

      cols : int, default=200
        Number of columns starting from the leftmost

    Returns
    -------
      An H2OFrame.
    """
    nrows = min(self.nrow, rows)
    ncols = min(self.ncol, cols)
    return self[:nrows,:ncols]

  def tail(self, rows=10, cols=200):
    """Analogous to Rs `tail` call on a data.frame.

    Parameters
    ----------
      rows : int, default=10
        Number of rows starting from the bottommost

      cols: int, default=200
        Number of columns starting from the leftmost

    Returns
    -------
      An H2OFrame.
    """
    nrows = min(self.nrow, rows)
    ncols = min(self.ncol, cols)
    start_idx = self.nrow - nrows
    return self[start_idx:start_idx+nrows,:ncols]

  def logical_negation(self): return H2OFrame._expr(expr=ExprNode("not", self), cache=self._ex._cache)

  # ops
  def __add__ (self, i): return H2OFrame._expr(expr=ExprNode("+",   self,i), cache=self._ex._cache)
  def __sub__ (self, i): return H2OFrame._expr(expr=ExprNode("-",   self,i), cache=self._ex._cache)
  def __mul__ (self, i): return H2OFrame._expr(expr=ExprNode("*",   self,i), cache=self._ex._cache)
  def __div__(self, i):  return H2OFrame._expr(expr=ExprNode("/",   self,i), cache=self._ex._cache)
  def __truediv__ (self, i): return H2OFrame._expr(expr=ExprNode("/",   self,i), cache=self._ex._cache)
  def __floordiv__(self, i): return H2OFrame._expr(expr=ExprNode("intDiv",self,i), cache=self._ex._cache)
  def __mod__ (self, i): return H2OFrame._expr(expr=ExprNode("%",   self,i), cache=self._ex._cache)
  def __or__  (self, i): return H2OFrame._expr(expr=ExprNode("|",   self,i), cache=self._ex._cache)
  def __and__ (self, i): return H2OFrame._expr(expr=ExprNode("&",   self,i), cache=self._ex._cache)
  def __ge__  (self, i): return H2OFrame._expr(expr=ExprNode(">=",  self,i), cache=self._ex._cache)
  def __gt__  (self, i): return H2OFrame._expr(expr=ExprNode(">",   self,i), cache=self._ex._cache)
  def __le__  (self, i): return H2OFrame._expr(expr=ExprNode("<=",  self,i), cache=self._ex._cache)
  def __lt__  (self, i): return H2OFrame._expr(expr=ExprNode("<",   self,i), cache=self._ex._cache)
  def __eq__  (self, i):
    if i is None: i = float("nan")
    return H2OFrame._expr(expr=ExprNode("==",  self,i), cache=self._ex._cache)
  def __ne__  (self, i):
    if i is None: i = float("nan")
    return H2OFrame._expr(expr=ExprNode("!=",  self,i), cache=self._ex._cache)
  def __pow__ (self, i): return H2OFrame._expr(expr=ExprNode("^",   self,i), cache=self._ex._cache)
  def __contains__(self, i): return all([(t==self).any() for t in i]) if _is_list(i) else (i==self).any()
  # rops
  def __rmod__(self, i): return H2OFrame._expr(expr=ExprNode("%",i,self), cache=self._ex._cache)
  def __radd__(self, i): return self.__add__(i)
  def __rsub__(self, i): return H2OFrame._expr(expr=ExprNode("-",i,  self), cache=self._ex._cache)
  def __rand__(self, i): return self.__and__(i)
  def __ror__ (self, i): return self.__or__ (i)
  def __rtruediv__(self, i): return H2OFrame._expr(expr=ExprNode("/",i,  self), cache=self._ex._cache)
  def __rdiv__(self, i): return H2OFrame._expr(expr=ExprNode("/",i,  self), cache=self._ex._cache)
  def __rfloordiv__(self, i): return H2OFrame._expr(expr=ExprNode("intDiv",i,self), cache=self._ex._cache)
  def __rmul__(self, i): return self.__mul__(i)
  def __rpow__(self, i): return H2OFrame._expr(expr=ExprNode("^",i,  self), cache=self._ex._cache)
  # unops
  def __abs__ (self):    return H2OFrame._expr(expr=ExprNode("abs",self), cache=self._ex._cache)
  def __invert__(self):  return H2OFrame._expr(expr=ExprNode("!!", self), cache=self._ex._cache)
  def __nonzero__(self):
    if self.nrow > 1 or self.ncol > 1:
      raise ValueError('This operation is not supported on an H2OFrame. Try using parantheses. Did you mean & (logical and), | (logical or), or ~ (logical not)?')
    else:
      return self.__len__()

  def flatten(self):
    return ExprNode("flatten",self)._eager_scalar()

  def mult(self, matrix):
    """Perform matrix multiplication.

    Parameters
    ----------
      matrix : H2OFrame
        The right-hand-side matrix

    Returns
    -------
      H2OFrame result of the matrix multiplication
    """
    return H2OFrame._expr(expr=ExprNode("x", self, matrix))

  def cos(self)     :    return H2OFrame._expr(expr=ExprNode("cos", self), cache=self._ex._cache)
  def sin(self)     :    return H2OFrame._expr(expr=ExprNode("sin", self), cache=self._ex._cache)
  def tan(self)     :    return H2OFrame._expr(expr=ExprNode("tan", self), cache=self._ex._cache)
  def acos(self)    :    return H2OFrame._expr(expr=ExprNode("acos", self), cache=self._ex._cache)
  def asin(self)    :    return H2OFrame._expr(expr=ExprNode("asin", self), cache=self._ex._cache)
  def atan(self)    :    return H2OFrame._expr(expr=ExprNode("atan", self), cache=self._ex._cache)
  def cosh(self)    :    return H2OFrame._expr(expr=ExprNode("cosh", self), cache=self._ex._cache)
  def sinh(self)    :    return H2OFrame._expr(expr=ExprNode("sinh", self), cache=self._ex._cache)
  def tanh(self)    :    return H2OFrame._expr(expr=ExprNode("tanh", self), cache=self._ex._cache)
  def acosh(self)   :    return H2OFrame._expr(expr=ExprNode("acosh", self), cache=self._ex._cache)
  def asinh(self)   :    return H2OFrame._expr(expr=ExprNode("asinh", self), cache=self._ex._cache)
  def atanh(self)   :    return H2OFrame._expr(expr=ExprNode("atanh", self), cache=self._ex._cache)
  def cospi(self)   :    return H2OFrame._expr(expr=ExprNode("cospi", self), cache=self._ex._cache)
  def sinpi(self)   :    return H2OFrame._expr(expr=ExprNode("sinpi", self), cache=self._ex._cache)
  def tanpi(self)   :    return H2OFrame._expr(expr=ExprNode("tanpi", self), cache=self._ex._cache)
  def abs(self)     :    return H2OFrame._expr(expr=ExprNode("abs", self), cache=self._ex._cache)
  def sign(self)    :    return H2OFrame._expr(expr=ExprNode("sign", self), cache=self._ex._cache)
  def sqrt(self)    :    return H2OFrame._expr(expr=ExprNode("sqrt", self), cache=self._ex._cache)
  def trunc(self)   :    return H2OFrame._expr(expr=ExprNode("trunc", self), cache=self._ex._cache)
  def ceil(self)    :    return H2OFrame._expr(expr=ExprNode("ceiling", self), cache=self._ex._cache)
  def floor(self)   :    return H2OFrame._expr(expr=ExprNode("floor", self), cache=self._ex._cache)
  def log(self)     :    return H2OFrame._expr(expr=ExprNode("log", self), cache=self._ex._cache)
  def log10(self)   :    return H2OFrame._expr(expr=ExprNode("log10", self), cache=self._ex._cache)
  def log1p(self)   :    return H2OFrame._expr(expr=ExprNode("log1p", self), cache=self._ex._cache)
  def log2(self)    :    return H2OFrame._expr(expr=ExprNode("log2", self), cache=self._ex._cache)
  def exp(self)     :    return H2OFrame._expr(expr=ExprNode("exp", self), cache=self._ex._cache)
  def expm1(self)   :    return H2OFrame._expr(expr=ExprNode("expm1", self), cache=self._ex._cache)
  def gamma(self)   :    return H2OFrame._expr(expr=ExprNode("gamma", self), cache=self._ex._cache)
  def lgamma(self)  :    return H2OFrame._expr(expr=ExprNode("lgamma", self), cache=self._ex._cache)
  def digamma(self) :    return H2OFrame._expr(expr=ExprNode("digamma", self), cache=self._ex._cache)
  def trigamma(self):    return H2OFrame._expr(expr=ExprNode("trigamma", self), cache=self._ex._cache)

  def diff(self):
    """Computes the lag1 diff on a numeric column.

    Returns
    -------
      The lag1 difference for a numeric column (expects operation to occur over H2OFrame
      of a single column).
    """
    return H2OFrame._expr(expr=ExprNode("difflag1", self))

  @staticmethod
  def mktime(year=1970,month=0,day=0,hour=0,minute=0,second=0,msec=0):
    """All units are zero-based (including months and days).
    Missing year is 1970.

    Parameters
    ----------
      year : int, H2OFrame
        the year

      month: int, H2OFrame
        the month

      day : int, H2OFrame
        the day

      hour : int, H2OFrame
        the hour

      minute : int, H2OFrame
        the minute

      second : int, H2OFrame
        the second

      msec : int, H2OFrame
        the milisecond

    Returns
    -------
      H2OFrame of one column containing the date in millis since the epoch.
    """
    return ExprNode("mktime", year,month,day,hour,minute,second,msec)._eager_frame()

  def unique(self):
    """Extract the unique values in the column.

    Returns
    -------
      H2OFrame of just the unique values in the column.
    """
    return H2OFrame._expr(expr=ExprNode("unique", self))

  def levels(self):
    """Get the factor levels.

    Returns
    -------
      A list of lists, one list per column, of levels.
    """
    lol = H2OFrame._expr(expr=ExprNode("levels", self)).as_data_frame(False)
    lol.pop(0)  # Remove column headers
    lol = list(zip(*lol))
    return [[ll for ll in l if ll!=''] for l in lol]


  def nlevels(self):
    """Get the number of factor levels for this frame.

    Returns
    -------
      A list of the number of levels per column.
    """
    levels = self.levels()
    return [len(l) for l in levels] if levels else 0

  def set_level(self, level):
    """A method to set all column values to one of the levels.

    Parameters
    ----------
      level : str
        The level at which the column will be set (a string)

    Returns
    -------
      H2OFrame with entries set to the desired level.
    """
    return H2OFrame._expr(expr=ExprNode("setLevel", self, level), cache=self._ex._cache)

  def set_levels(self, levels):
    """Works on a single categorical column.
    New domains must be aligned with the old domains. This call has copy-on-write semantics.

    Parameters
    ----------
      levels : list
        A list of strings specifying the new levels. The number of new levels must match
        the number of old levels.

    Returns
    -------
      A single-column H2OFrame with the desired levels.
    """
    return H2OFrame._expr(expr=ExprNode("setDomain",self,levels), cache=self._ex._cache)

  def set_names(self,names):
    """Change all of this H2OFrame instance's column names.

    Parameters
    ----------
      names : list
        A list of strings equal to the number of columns in the H2OFrame.
    """
    self._ex = ExprNode("colnames=",self, range(self.ncol), names)  # Update-in-place, but still lazy
    return self

  def set_name(self,col=None,name=None):
    """Set the name of the column at the specified index.

    Parameters
    ----------
      col : int, str
        Index of the column whose name is to be set; may be skipped for 1-column frames

      name : str
        The new name of the column to set

    Returns
    -------
      Returns self.
    """
    if isinstance(col, basestring): col = self.names.index(col)  # lookup the name
    if not isinstance(col, int) and self.ncol > 1: raise ValueError("`col` must be an index. Got: " + str(col))
    if self.ncol == 1: col = 0
    old_cache = self._ex._cache
    self._ex = ExprNode("colnames=",self,col,name)  # Update-in-place, but still lazy
    self._ex._cache.fill_from(old_cache)
    if self.names is None:
      self._frame()._ex._cache.fill()
    else:
      self._ex._cache.names = self.names[:col] + [name] + self.names[col+1:]

  def as_date(self, format):
    """Return the column with all elements converted to millis since the epoch.

    Parameters
    ----------
      format : str
        A datetime format string (e.g. "YYYY-mm-dd")

    Returns
    -------
      An H2OFrame instance.
    """
    fr = H2OFrame._expr(expr=ExprNode("as.Date",self,format), cache=self._ex._cache)
    if fr._ex._cache.types_valid():
      fr._ex._cache.types = {k:"int" for k in self._ex._cache.types.keys()}
    return fr

  def cumsum(self):
    """
    Returns
    -------
      The cumulative sum over the column.
    """
    return H2OFrame._expr(expr=ExprNode("cumsum",self), cache=self._ex._cache)

  def cumprod(self):
    """
    Returns
    -------
      The cumulative product over the column.
    """
    return H2OFrame._expr(expr=ExprNode("cumprod",self), cache=self._ex._cache)

  def cummin(self):
    """
    Returns
    -------
      The cumulative min over the column.
    """
    return H2OFrame._expr(expr=ExprNode("cummin",self), cache=self._ex._cache)

  def cummax(self):
    """
    Returns
    -------
      The cumulative max over the column.
    """
    return H2OFrame._expr(expr=ExprNode("cummax",self), cache=self._ex._cache)

  def prod(self,na_rm=False):
    """
    Parameters
    ----------
      na_rm : bool, default=False
        True or False to remove NAs from computation.

    Returns
    -------
      The product of the column.
    """
    return ExprNode("prod.na" if na_rm else "prod",self)._eager_scalar()

  def any(self):
    """
    Returns
    -------
      True if any element is True or NA in the column.
    """
    return bool(ExprNode("any",self)._eager_scalar())

  def any_na_rm(self):
    """
    Returns
    -------
      True if any element is True in the column.
    """
    return bool(ExprNode("any.na",self)._eager_scalar())

  def all(self):
    """
    Returns
    -------
      True if every element is True or NA in the column.
    """
    return bool(ExprNode("all",self)._eager_scalar())

  def isnumeric(self):
    """
    Returns
    -------
      True if the column is numeric, otherwise return False
    """
    if self._ex._cache.types_valid():
      return [str(list(itervalues(self._ex._cache.types))[0]) in ["numeric", "int", "real"]]
    return [bool(o) for o in ExprNode("is.numeric",self)._eager_scalar()]

  def isstring(self):
    """
    Returns
    -------
      True if the column is a string column, otherwise False (same as ischaracter)
    """
    return  [bool(o) for o in ExprNode("is.character",self)._eager_scalar()]

  def ischaracter(self):
    """
    Returns
    -------
      True if the column is a character column, otherwise False (same as isstring)
    """
    return self.isstring()

  def isin(self, item):
    """Test whether elements of an H2OFrame are contained in the item.

    Parameters
    ----------
      items : any element or a list of elements
        An item or a list of items to compare the H2OFrame against.

    Returns
    -------
      An H2OFrame of 0s and 1s showing whether each element in the original H2OFrame is contained in item.
    """
    return reduce(H2OFrame.__or__, (self == i for i in item)) if _is_list(item) else self == item

  def kfold_column(self, n_folds=3, seed=-1):
    """Build a fold assignments column for cross-validation. This call will produce a
    column having the same data layout as the calling object.

    Parameters
    ----------
      n_folds : int
        An integer specifying the number of validation sets to split the training data
        into.

      seed : int, optional
        Seed for random numbers as fold IDs are randomly assigned.

    Returns
    -------
      A single column H2OFrame with the fold assignments.
    """
    return H2OFrame._expr(expr=ExprNode("kfold_column",self,n_folds,seed))._frame()  # want this to be eager!

  def modulo_kfold_column(self, n_folds=3):
    """Build a fold assignments column for cross-validation. Rows are assigned a fold
    according to the current row number modulo n_folds.

    Parameters
    ----------
      n_folds : int
        An integer specifying the number of validation sets to split the training data
        into.

    Returns
    -------
      A single column H2OFrame with the fold assignments.
    """
    return H2OFrame._expr(expr=ExprNode("modulo_kfold_column",self,n_folds))._frame()  # want this to be eager!

  def stratified_kfold_column(self, n_folds=3, seed=-1):
    """
    Build a fold assignment column with the constraint that each fold has the same class
    distribution as the fold column.

    Parameters
    ----------
      n_folds: int
        The number of folds to build.

      seed: int
        A random seed.

    Returns
    -------
      A single column H2OFrame with the fold assignments.
    """
    return H2OFrame._expr(expr=ExprNode("stratified_kfold_column",self,n_folds,seed))._frame()  # want this to be eager!

  def structure(self):
    """Similar to R's str method: Compactly Display the Structure of this H2OFrame.
    """
    df = self.as_data_frame(use_pandas=False)
    cn = df.pop(0)
    nr = self.nrow
    nc = self.ncol
    width = max([len(c) for c in cn])
    isfactor = [c.isfactor() for c in self]
    numlevels  = self.nlevels()
    lvls = self.levels()
    print("self._newExpr '{}': \t {} obs. of {} variables(s)".format(self.frame_id,nr,nc))
    for i in range(nc):
      print("$ {} {}: ".format(cn[i], ' '*(width-max(0,len(cn[i])))), end=' ')
      if isfactor[i]:
        nl = numlevels[i]
        print("Factor w/ {} level(s) {}: ".format(nl, '"' + '","'.join(lvls[i]) + '"'), end=' ')
        print(" ".join(list(map(lambda x: str(lvls[i].index(x)), list(zip(*df))[i]))))
      else:
        print("num {}".format(" ".join(it[0] for it in h2o.as_list(self[:10,i], False)[1:])))

  def as_data_frame(self, use_pandas=True):
    """Obtain the dataset as a python-local object.

    Parameters
    ----------
      use_pandas : bool, default=True
        A flag specifying whether or not to return a pandas DataFrame.

    Returns
    -------
      A local python object (a list of lists of strings, each list is a row, if
      use_pandas=False, otherwise a pandas DataFrame) containing this H2OFrame instance's
      data.
    """
    if can_use_pandas() and use_pandas:
      import pandas
      return pandas.read_csv(StringIO(self.get_frame_data()), low_memory=False)
    return [row for row in csv.reader(StringIO(self.get_frame_data()))]

  def get_frame_data(self):
    """Get frame data as str in csv format

    Returns
    -------
      A local python string, each line is a row and each element separated by commas,
      containing this H2OFrame instance's data.
    """
    url = H2OConnection.make_url("DownloadDataset",3) + "?frame_id={}&hex_string=false".format(self.frame_id)
    return requests.get(url, headers = {'User-Agent': 'H2O Python client/'+sys.version.replace('\n','')},
                        auth = (H2OConnection.username(), H2OConnection.password()),
                        verify = not H2OConnection.insecure(), stream = True).text

  def __getitem__(self, item):
    """Frame slicing. Supports R-like row and column slicing.

    Parameters
    ----------
      item : tuple, list, string, int
        If a tuple, then this indicates both row and column selection. The tuple
        must be exactly length 2.
        If a list, then this indicates column selection.
        If a int, the this indicates a single column to be retrieved at the index.
        If a string, then slice on the column with this name.

    Returns
    -------
      An instance of H2OFrame.

    Examples
    --------
      fr[2]              # All rows, column 2
      fr[-2]             # All rows, 2nd column from end
      fr[:,-1]           # All rows, last column
      fr[0:5,:]          # first 5 rows, all columns
      fr[fr[0] > 1, :]   # all rows greater than 1 in the first column, all columns
      fr[[1,5,6]]        # columns 1, 5, and 6
      fr[0:50, [1,2,3]]  # first 50 rows, columns 1,2, and 3
    """
    # Select columns based on a string, a list of strings, an int or a slice.
    # Note that the python column selector handles the case of negative
    # selections, or out-of-range selections - without having to compute
    # self._ncols in the front-end - which would force eager evaluation just to
    # range check in the front-end.
    new_ncols=-1
    new_nrows=-1
    new_names=None
    new_types=None
    fr=None
    flatten=False
    if isinstance(item, (basestring,list,int,slice)):
      new_ncols,new_names,new_types,item = self._compute_ncol_update(item)
      new_nrows = self.nrow
      fr = H2OFrame._expr(expr=ExprNode("cols_py",self,item))
    elif isinstance(item, (ExprNode, H2OFrame)):
      new_ncols = self.ncol
      new_names = self.names
      new_types = self.types
      new_nrows = -1  # have a "big" predicate column -- update cache later on...
      fr = H2OFrame._expr(expr=ExprNode("rows",self,item))
    elif isinstance(item, tuple):
      rows, cols = item
      allrows = allcols = False
      if isinstance(cols, slice):  allcols = all([a is None for a in [cols.start,cols.step,cols.stop]])
      if isinstance(rows, slice):  allrows = all([a is None for a in [rows.start,rows.step,rows.stop]])

      if allrows and allcols: return self               # fr[:,:]    -> all rows and columns.. return self
      if allrows:
        new_ncols,new_names,new_types,cols = self._compute_ncol_update(cols)
        new_nrows = self.nrow
        fr = H2OFrame._expr(expr=ExprNode("cols_py",self,cols))  # fr[:,cols] -> really just a column slice
      if allcols:
        new_ncols = self.ncol
        new_names = self.names
        new_types = self.types
        new_nrows,rows = self._compute_nrow_update(rows)
        fr = H2OFrame._expr(expr=ExprNode("rows",self,rows))  # fr[rows,:] -> really just a row slices

      if not allrows and not allcols:
        new_ncols,new_names,new_types,cols = self._compute_ncol_update(cols)
        new_nrows,rows = self._compute_nrow_update(rows)
        fr = H2OFrame._expr(expr=ExprNode("rows", ExprNode("cols_py",self,cols),rows))

      flatten = isinstance(rows, int) and isinstance(cols, (basestring,int))
    else:
      raise ValueError("Unexpected __getitem__ selector: "+str(type(item))+" "+str(item.__class__))

    assert fr is not None
    # Pythonic: if the row & col selector turn into ints (or a single col
    # name), then extract the single element out of the Frame.  Otherwise
    # return a Frame, EVEN IF the selectors are e.g. slices-of-1-value.
    if flatten:
      return fr.flatten()

    fr._ex._cache.ncols = new_ncols
    fr._ex._cache.nrows = new_nrows
    fr._ex._cache.names = new_names
    fr._ex._cache.types = new_types
    return fr

  def _compute_ncol_update(self, item):  # computes new ncol, names, and types
    try :
      new_ncols=-1
      if isinstance(item, list):
        new_ncols = len(item)
        if _is_str_list(item):
          new_types = {k:self.types[k] for k in item}
          new_names = item
        else:
          new_names = [self.names[i] for i in item]
          new_types = {name:self.types[name] for name in new_names}
      elif isinstance(item, slice):
        start = 0 if item.start is None else item.start
        end   = min(self.ncol, self.ncol if item.stop is None else item.stop)
        if end < 0:
          end = self.ncol+end
        if item.start is not None or item.stop is not None:
          new_ncols = end - start
        range_list = range(start,end)
        new_names = [self.names[i] for i in range_list]
        new_types = {name:self.types[name] for name in new_names}
        item = slice(start,end)
      elif isinstance(item, (basestring,int)):
        new_ncols = 1
        if isinstance(item, basestring):
          new_names = [item]
          new_types = None if item not in self.types else {item:self.types[item]}
        else:
          new_names = [self.names[item]]
          new_types = {new_names[0]:self.types[new_names[0]]}
      else:
        raise ValueError("Unexpected type: " + str(type(item)))
      return [new_ncols,new_names,new_types,item]
    except:
      return [-1,None,None,item]

  def _compute_nrow_update(self, item):
    try:
      new_nrows=-1
      if isinstance(item, list):
        new_nrows = len(item)
      elif isinstance(item, slice):
        start = 0 if item.start is None else item.start
        end   = self.nrow if item.stop is None else item.stop
        if end < 0:
          end = self.nrow+end
        if item.start is not None or item.stop is not None:
          new_nrows = end - start
        item = slice(start,end)
      elif isinstance(item, H2OFrame):
        new_nrows=-1
      else:
        new_nrows=1
      return [new_nrows,item]
    except:
      return [-1,item]

  def __setitem__(self, b, c):
    """Replace or update column(s) in an H2OFrame.

    Parameters
    ----------
      b : int, str
        A 0-based index or a column name.

      c : int, H2OFrame, str
        The value replacing 'b'

    Returns
    -------
      Returns this H2OFrame.
    """
    col_expr=None
    row_expr=None
    colname=None  # When set, we are doing an append

    if isinstance(b, basestring):  # String column name, could be new or old
      if b in self.names:
        col_expr = self.names.index(b)  # Old, update
      else:
        col_expr = self.ncol
        colname = b  # New, append
    elif isinstance(b, int):    col_expr = b  # Column by number
    elif isinstance(b, tuple):     # Both row and col specifiers
      row_expr = b[0]
      col_expr = b[1]
      if isinstance(col_expr, basestring):    # Col by name
        if col_expr not in self.names:  # Append
          colname = col_expr
          col_expr = self.ncol
      elif isinstance(col_expr, slice):    # Col by slice
        if col_expr.start is None and col_expr.stop is None:
          col_expr = slice(0,self.ncol)    # Slice of all
    elif isinstance(b, (ExprNode, H2OFrame)): row_expr = b # Row slicing
    elif isinstance(b, list): col_expr = b

    src = float("nan") if c is None else c
    src_in_self = self.is_src_in_self(src)
    old_cache = self._ex._cache
    if colname is None:
      self._ex = ExprNode(":=",self,src,col_expr,row_expr)
      self._ex._cache.fill_from(old_cache)
      if isinstance(src, H2OFrame)    and \
        src._ex._cache.types_valid()  and \
        self._ex._cache.types_valid():
          self._ex._cache._types.update(src._ex._cache.types)
      else:
        self._ex._cache.types = None
    else:
      self._ex = ExprNode("append",self,src,colname)
      self._ex._cache.fill_from(old_cache)
      self._ex._cache.names = self.names + [colname]
      if not self._ex._cache.types_valid() or \
         not isinstance(src, H2OFrame)     or \
         not src._ex._cache.types_valid():
          self._ex._cache.types = None
      else:
        self._ex._cache._types[colname] = list(itervalues(src._ex._cache.types))[0]
    if isinstance(src, H2OFrame) and src_in_self:
      src._ex=None  # wipe out to keep ref counts correct
    # self._frame()  # setitem is eager

  def is_src_in_self(self,src):
    # src._ex._children[0]._children[0] is self._ex
    if isinstance(src, H2OFrame):
      if self._ex is src._ex:
        return True
      else:
        if src._ex._children is not None:
          for ch in src._ex._children:
            if self.is_src_in_self(ch): return True
    elif isinstance(src, ExprNode):
      if self._ex is src: return True
      else:
        if src._children is not None:
          for ch in src._children:
            if self.is_src_in_self(ch): return True
    return False

  def __int__(self):
    if self.ncol != 1 or self.nrow != 1: raise ValueError("Not a 1x1 Frame")
    return int(self.flatten())

  def __float__(self):
    if self.ncol != 1 or self.nrow != 1: raise ValueError("Not a 1x1 Frame")
    return float(self.flatten())

  def drop(self, i):
    """Drop a column from the current H2OFrame.

    Parameters
    ----------
      i : str, int
        The column to be dropped

    Returns
    -------
      H2OFrame with the column at index i dropped. Returns a new H2OFrame.
    """
    if isinstance(i, basestring): i = self.names.index(i)
    fr = H2OFrame._expr(expr=ExprNode("cols", self,-(i+1)), cache=self._ex._cache)
    fr._ex._cache.ncols -= 1
    fr._ex._cache.names = self.names[:i] + self.names[i+1:]
    fr._ex._cache.types = {name:self.types[name] for name in fr._ex._cache.names}
    return fr

  def pop(self,i):
    """Pop a column from the H2OFrame at index i

    Parameters
    ----------
    i : int, str
      The index or name of the column to pop.

    Returns
    -------
      The column dropped from the frame; the frame is side-effected to lose the column.
    """
    if isinstance(i, basestring): i=self.names.index(i)
    col = H2OFrame._expr(expr=ExprNode("cols",self,i))
    old_cache = self._ex._cache
    self._ex = ExprNode("cols", self,-(i+1))
    self._ex._cache.ncols -= 1
    self._ex._cache.names = old_cache.names[:i] + old_cache.names[i+1:]
    self._ex._cache.types = {name:old_cache.types[name] for name in self._ex._cache.names}
    self._ex._cache._data = None
    col._ex._cache.ncols = 1
    col._ex._cache.names = [old_cache.names[i]]
    return col

  def quantile(self, prob=None, combine_method="interpolate", weights_column=None):
    """Compute quantiles.

    Parameters
    ----------
      prob : list, default=[0.01,0.1,0.25,0.333,0.5,0.667,0.75,0.9,0.99]
        A list of probabilities of any length.

      combine_method : str, default="interpolate"
        For even samples, how to combine quantiles.
        Should be one of ["interpolate", "average", "low", "high"]

      weights_column : str, default=None
        Name of column with optional observation weights in this H2OFrame or a 1-column H2OFrame of observation weights.

    Returns
    -------
      A new H2OFrame containing the quantiles and probabilities.
    """
    if len(self) == 0: return self
    if prob is None: prob=[0.01,0.1,0.25,0.333,0.5,0.667,0.75,0.9,0.99]
    if weights_column is None: weights_column="_"
    else:
      if not (isinstance(weights_column, basestring) or (isinstance(weights_column, H2OFrame)
                                                        and weights_column.ncol == 1
                                                        and weights_column.nrow == self.nrow)):
        raise ValueError("`weights_column` must be a column name in x or an H2OFrame object with 1 column and same row count as x")
      if isinstance(weights_column, H2OFrame):
        merged = self.cbind(weights_column)
        weights_column = merged.names[-1]
        return H2OFrame._expr(expr=ExprNode("quantile",merged,prob,combine_method,weights_column))
    return H2OFrame._expr(expr=ExprNode("quantile",self,prob,combine_method,weights_column))

  def cbind(self,data):
    """Append data to this H2OFrame column-wise.

    Parameters
    ----------
      data : H2OFrame
        H2OFrame to be column bound to the right of this H2OFrame.

    Returns
    -------
      H2OFrame of the combined datasets.
    """
    fr = H2OFrame._expr(expr=ExprNode("cbind", self, data), cache=self._ex._cache)
    fr._ex._cache.ncols = self.ncol + data.ncol
    fr._ex._cache.names = None  # invalidate for possibly duplicate names
    fr._ex._cache.types = None  # invalidate for possibly duplicate names
    return fr

  def rbind(self, data):
    """Combine H2O Datasets by rows.
    Takes a sequence of H2O data sets and combines them by rows.

    Parameters
    ----------
      data : H2OFrame

    Returns
    -------
      Returns this H2OFrame with data appended row-wise.
    """
    if not isinstance(data, H2OFrame): raise ValueError("`data` must be an H2OFrame, but got {0}".format(type(data)))
    fr = H2OFrame._expr(expr=ExprNode("rbind", self, data), cache=self._ex._cache)
    fr._ex._cache.nrows = self.nrow + data.nrow
    return fr

  def split_frame(self, ratios=None, destination_frames=None, seed=None):
    """Split a frame into distinct subsets of size determined by the given ratios.
    The number of subsets is always 1 more than the number of ratios given. Note that
    this does not give an exact split. H2O is designed to be efficient on big data
    using a probabilistic splitting method rather than an exact split. For example
    when specifying a split of 0.75/0.25, H2O will produce a test/train split with
    an expected value of 0.75/0.25 rather than exactly 0.75/0.25. On small datasets,
    the sizes of the resulting splits will deviate from the expected value more than
    on big data, where they will be very close to exact.

    Parameters
    ----------
      ratios : list
        The fraction of rows for each split.

      destination_frames : list
        The names of the split frames.

      seed : int
        Used for selecting which H2OFrame a row will belong to.

    Returns
    -------
      A list of H2OFrame instances
    """

    if ratios is None:
      ratios = [0.75]

    if len(ratios) < 1:
      raise ValueError("Ratios must have length of at least 1")

    if destination_frames is not None:
      if (len(ratios)+1) != len(destination_frames):
        raise ValueError("The number of provided destination_frames must be one more than the number of provided ratios")

    num_slices = len(ratios) + 1
    boundaries = []

    last_boundary = 0
    i = 0
    while i < num_slices-1:
      ratio = ratios[i]
      if ratio < 0:
        raise ValueError("Ratio must be greater than 0")
      boundary = last_boundary + ratio
      if boundary >= 1.0:
        raise ValueError("Ratios must add up to less than 1.0")
      boundaries.append(boundary)
      last_boundary = boundary
      i += 1

    splits = []
    tmp_runif = self.runif(seed)

    i = 0
    while i < num_slices:
      if i == 0:
        # lower_boundary is 0.0
        upper_boundary = boundaries[i]
        tmp_slice = self[(tmp_runif <= upper_boundary), :]
      elif i == num_slices-1:
        lower_boundary = boundaries[i-1]
        # upper_boundary is 1.0
        tmp_slice = self[(tmp_runif > lower_boundary), :]
      else:
        lower_boundary = boundaries[i-1]
        upper_boundary = boundaries[i]
        tmp_slice = self[((tmp_runif > lower_boundary) & (tmp_runif <= upper_boundary)), :]

      if destination_frames is None:
        splits.append(tmp_slice)
      else:
        destination_frame_id = destination_frames[i]
        tmp_slice.frame_id = destination_frame_id
        splits.append(tmp_slice)

      i += 1

    return splits

  def ddply(self,cols,fun):
    """Unimplemented
    """
    raise ValueError("unimpl")

  def group_by(self,by):
    """Returns a new GroupBy object using this frame and the desired grouping columns.
       The returned groups are sorted by the natural group-by column sort.

    Parameters
    ----------
      by : list
          The columns to group on.

    Returns
    -------
      A new GroupBy object.
    """
    return GroupBy(self,by)

  def impute(self, column=-1, method="mean", combine_method="interpolate", by=None, group_by_frame=None, values=None):
    """Impute in place.

    Parameters
    ----------
      column: int, default=-1
          The column to impute, if -1 then impute the whole frame

      method : str, default="mean"
          The method of imputation: mean, median, mode

      combine_method : str, default="interpolate"
          When method is "median", dictates how to combine quantiles for even samples.

      by : list, default=None
          The columns to group on.

      group_by_frame : H2OFrame, default=None
          Impute the column col with this pre-computed grouped frame.

      values : list
          A list of impute values (one per column). NaN indicates to skip the column.

    Returns
    -------
      A list of values used in the imputation or the group by result used in imputation.
    """
    if isinstance(column, basestring): column = self.names.index(column)
    if isinstance(by, basestring):     by     = self.names.index(by)


    if values is None: values="_"
    if group_by_frame is None: group_by_frame="_"

    if by is not None or group_by_frame is not "_":
      res = H2OFrame._expr(expr=ExprNode("h2o.impute", self, column, method, combine_method, by, group_by_frame, values))._frame()
    else:
      res = ExprNode("h2o.impute", self, column, method, combine_method, by, group_by_frame, values)._eager_scalar()

    self._ex._cache.flush(); self._ex._cache.fill(10)
    return res

  def merge(self, other, all_x=False, all_y=False, by_x=None, by_y=None, method="auto"):
    """Merge two datasets based on common column names

    Parameters
    ----------
      other: H2OFrame
        Other dataset to merge.  Must have at least one column in common with self,
        and all columns in common are used as the merge key.  If you want to use only a
        subset of the columns in common, rename the other columns so the columns are unique
        in the merged result.

      all_x: bool, default=False
        If True, include all rows from the left/self frame

      all_y: bool, default=False
        If True, include all rows from the right/other frame

    Returns
    -------
      Original self frame enhanced with merged columns and rows
    """

    common_names = set(self.names).intersection(set(other.names))
    common_names = list(common_names)
    if (len(common_names) == 0): raise ValueError("No columns in common to merge on!")
    if (by_x==None): by_x = [self.names.index(c) for c in common_names]
    if (by_y==None): by_y = [other.names.index(c) for c in common_names]
    return H2OFrame._expr(expr=ExprNode("merge", self, other, all_x, all_y, by_x, by_y, method))

  def relevel(self,y):
    """ Reorders levels of an H2O factor, similarly to standard R's relevel().
    The levels of a factor are reordered such that the reference level is at level 0, remaining levels are moved down
    as needed.

    Parameters
    ----------
     x: Column
      Column in H2O Frame

     y : String
      Reference level

    Returns
    -------
     New reordered factor column
    """

    return H2OFrame._expr(expr=ExprNode("relevel", self, quote(y)))

  def insert_missing_values(self, fraction=0.1, seed=None):
    """Inserting Missing Values into an H2OFrame.
    Randomly replaces a user-specified fraction of entries in a H2O dataset with missing
    values.

    WARNING! This will modify the original dataset.  Unless this is intended, this
    function should only be called on a subset of the original.

    Parameters
    ----------
      fraction : float
        A number between 0 and 1 indicating the fraction of entries to replace with missing.

      seed : int
        A random number used to select which entries to replace with missing values.

    Returns
    -------
      H2OFrame with missing values inserted.
    """
    kwargs = {}
    kwargs['dataset'] = self.frame_id  # Eager; forces eval now for following REST call
    kwargs['fraction'] = fraction
    if seed is not None: kwargs['seed'] = seed
    job = {}
    job['job'] = H2OConnection.post_json("MissingInserter", **kwargs)
    H2OJob(job, job_type=("Insert Missing Values")).poll()
    self._ex._cache.flush()
    return self

  def min(self):
    """

    Returns
    -------
      The minimum value of all frame entries
    """
    return ExprNode("min", self)._eager_scalar()

  def max(self):
    """

    Returns
    -------
      The maximum value of all frame entries
    """
    return ExprNode("max", self)._eager_scalar()

  def sum(self, na_rm=False):
    """

    Returns
    -------
      The sum of all frame entries
    """
    return ExprNode("sumNA" if na_rm else "sum", self)._eager_scalar()

  def mean(self,na_rm=False):
    """Compute the mean.

    Parameters
    ----------
      na_rm: bool, default=False
        If True, then remove NAs from the computation.

    Returns
    -------
      A list containing the mean for each column (NaN for non-numeric columns).
    """
    return ExprNode("mean", self, na_rm)._eager_scalar()

  def nacnt(self):
    """Count of NAs for each column in this H2OFrame.

      Returns
      -------
        A list of the na cnts (one entry per column).
    """
    return ExprNode("naCnt",self)._eager_scalar()

  def median(self, na_rm=False):
    """Compute the median.

    Parameters
    ----------
      na_rm: bool, default=False
        If True, then remove NAs from the computation.

    Returns
    -------
      A list containing the median for each column (NaN for non-numeric columns).
    """
    return ExprNode("median", self, na_rm)._eager_scalar()


  def var(self,y=None,na_rm=False, use=None):
    """Compute the variance or covariance matrix of one or two H2OFrames.
    Parameters
    ----------
    y : H2OFrame, default=None
      If y is None and self is a single column, then the variance is computed for self. If self has 
      multiple columns, then its covariance matrix is returned. Single rows are treated as single columns. 
      If y is not None, then a covariance matrix between the columns of self and the columns of y is computed. 
    na_rm : bool, default=False
      Remove NAs from the computation.
    use : str, default=None, which acts as "everything" if na_rm is False, and "complete.obs" if na_rm is True
      A string indicating how to handle missing values. This must be one of the following: 
        "everything"            - outputs NaNs whenever one of its contributing observations is missing
        "all.obs"               - presence of missing observations will throw an error
        "complete.obs"          - discards missing values along with all observations in their rows so that only complete observations are used
    Returns
    -------
      An H2OFrame of the covariance matrix of the columns of this H2OFrame with itself (if y is not given), or with the columns of y 
      (if y is given). If self and y are single rows or single columns, the variance or covariance is given as a scalar.
    """
    symmetric = False
    if y is None:
      y = self
      symmetric = True
    if use is None: use = "complete.obs" if na_rm else "everything"
    if self.nrow==1 or (self.ncol==1 and y.ncol==1): return ExprNode("var",self,y,use,symmetric)._eager_scalar()
    return H2OFrame._expr(expr=ExprNode("var",self,y,use,symmetric))._frame()


  def sd(self, na_rm=False):
    """Compute the standard deviation.

    Parameters
    ----------
      na_rm : bool, default=False
        Remove NAs from the computation.

    Returns
    -------
      A list containing the standard deviation for each column (NaN for non-numeric
      columns).
    """
    return ExprNode("sd", self, na_rm)._eager_scalar()

  def cor(self,y = None,na_rm=False,use=None):
    """Compute the correlation matrix of one or two H2OFrames.

    Parameters
    ----------
    y : H2OFrame, default=None
      If y is None and self is a single column, then the correlation is computed for self.
      If self has multiple columns, then its correlation matrix is returned. Single rows are treated as single columns.
      If y is not None, then a correlation matrix between the columns of self and the columns of y is computed.
    na_rm : bool, default=False
      Remove NAs from the computation.
    use : str, default=None, which acts as "everything" if na_rm is False, and "complete.obs" if na_rm is True
      A string indicating how to handle missing values. This must be one of the following:
        "everything"            - outputs NaNs whenever one of its contributing observations is missing
        "all.obs"               - presence of missing observations will throw an error
        "complete.obs"          - discards missing values along with all observations in their rows so that only complete observations are used
    Returns
    -------
      An H2OFrame of the correlation matrix of the columns of this H2OFrame with itself (if y is not given), or with the columns of y
      (if y is given). If self and y are single rows or single columns, the correlation is given as a scalar.
    """
    if y is None:
      y = self
    if use is None: use = "complete.obs" if na_rm else "everything"
    if self.nrow==1 or (self.ncol==1 and y.ncol==1): return ExprNode("cor",self,y,use)._eager_scalar()
    return H2OFrame._expr(expr=ExprNode("cor",self,y,use))._frame()

  def asfactor(self):
    """
    Returns
    -------
      H2Oframe of one column converted to a factor.
    """
    fr = H2OFrame._expr(expr=ExprNode("as.factor",self), cache=self._ex._cache)
    if fr._ex._cache.types_valid():
      fr._ex._cache.types = {list(fr._ex._cache.types)[0]:"enum"}
    return fr

  def isfactor(self):
    """Test if the selection is a factor column.

    Returns
    -------
      True if the column is categorical; otherwise False. For String columns, the result
      is False.
    """
    #TODO: list for fr.ncol > 1 ?
    if self._ex._cache.types_valid():
      return [str(list(itervalues(self._ex._cache.types))[0]) == "enum"]
    return [bool(o) for o in ExprNode("is.factor", self)._eager_scalar()]

  def anyfactor(self):
    """Test if H2OFrame has any factor columns.

    Returns
    -------
      True if there are any categorical columns; False otherwise.
    """
    return bool(ExprNode("any.factor", self)._eager_scalar())

  def transpose(self):
    """Transpose rows and columns of H2OFrame.

    Returns
    -------
      The transpose of the input frame.
    """
    return H2OFrame._expr(expr=ExprNode("t", self))

  def strsplit(self, pattern):
    """Split the strings in the target column on the given pattern

    Parameters
    ----------
      pattern : str
        The split pattern.

    Returns
    -------
      H2OFrame containing columns of the split strings.
    """
    fr = H2OFrame._expr(expr=ExprNode("strsplit", self, pattern))
    fr._ex._cache.nrows = self.nrow
    return fr

  def countmatches(self, pattern):
    """For each string in the column, count the occurrences of pattern.

    Parameters
    ----------
      pattern : str
        The pattern to count matches on in each string.

    Returns
    -------
      A single-column H2OFrame containing the counts for the per-row occurrences of
      pattern in the input column.
    """
    fr = H2OFrame._expr(expr=ExprNode("countmatches", self, pattern))
    fr._ex._cache.nrows = self.nrow
    fr._ex._cache.ncols = self.ncol
    return fr

  def trim(self):
    """Trim white space on the left and right of strings in a single-column H2OFrame.

    Returns
    -------
      H2OFrame with trimmed strings.
    """
    fr = H2OFrame._expr(expr=ExprNode("trim", self))
    fr._ex._cache.nrows = self.nrow
    fr._ex._cache.ncol = self.ncol
    return fr

  def substring(self, start_index, end_index=None):
    """For each string, return a new string that is a substring of the original string. If end_index is not
    specified, then the substring extends to the end of the original string. If the start_index is longer than
    the length of the string, or is greater than or equal to the end_index, an empty string is returned. Negative
    start_index is coerced to 0.

    Parameters
    ----------
      start_index : int
        The index of the original string at which to start the substring, inclusive.
      end_index: int, optional
        The index of the original string at which to end the substring, exclusive.

    Returns
    -------
      An H2OFrame containing the specified substrings.
    """
    fr = H2OFrame._expr(expr=ExprNode("substring", self, start_index, end_index))
    fr._ex._cache.nrows = self.nrow
    fr._ex._cache.ncol = self.ncol
    return fr

  def lstrip(self, set = " "):
    """Return a copy of the column with leading characters removed.
    The set argument is a string specifying the set of characters to be removed.
    If omitted, the set argument defaults to removing whitespace.

    Parameters
    ----------
      set : str
        Set of characters to lstrip from strings in column

    Returns
    -------
      H2OFrame with lstripped strings.
    """

    # work w/ None; parity with python lstrip
    if set is None: set = " "

    fr = H2OFrame._expr(expr=ExprNode("lstrip", self, set))
    fr._ex._cache.nrows = self.nrow
    fr._ex._cache.ncol = self.ncol
    return fr

  def rstrip(self, set = " "):
    """Return a copy of the column with trailing characters removed.
    The set argument is a string specifying the set of characters to be removed.
    If omitted, the set argument defaults to removing whitespace.

    Parameters
    ----------
      set : str
        Set of characters to rstrip from strings in column

    Returns
    -------
      H2OFrame with rstripped strings.
    """

    # work w/ None; parity with python rstrip
    if set is None: set = " "

    fr = H2OFrame._expr(expr=ExprNode("rstrip", self, set))
    fr._ex._cache.nrows = self.nrow
    fr._ex._cache.ncol = self.ncol
    return fr

  def entropy(self):
    """For each string, return the Shannon entropy. If the string is empty, the entropy is 0.

    Returns
    -------
      An H2OFrame of Shannon entropies. 
    """
    fr = H2OFrame._expr(expr=ExprNode("entropy", self))
    fr._ex._cache.nrows = self.nrow
    fr._ex._cache.ncol = self.ncol
    return fr

  def num_valid_substrings(self, path_to_words):
    """For each string, find the count of all possible substrings >= 2 characters that are contained in 
    the line-separated text file whose path is given.
    
    Parameters
    ----------
      path_to_words : str
        Path to file that contains a line-separated list of strings considered valid. 
        
    Returns
    -------
      An H2OFrame with the number of substrings that are contained in the given word list. 
    """
    fr = H2OFrame._expr(expr=ExprNode("num_valid_substrings", self, path_to_words))
    fr._ex._cache.nrows = self.nrow
    fr._ex._cache.ncol = self.ncol
    return fr

  def nchar(self):
    """Count the number of characters in each string of single-column H2OFrame.

    Returns
    -------
      A single-column H2OFrame containing the per-row character count.
    """
    return H2OFrame._expr(expr=ExprNode("strlen", self))

  def table(self, data2=None, dense=True):
    """Compute the counts of values appearing in a column, or co-occurence counts between
    two columns.

    Parameters
    ----------
      data2 : H2OFrame
        Default is None, can be an optional single column to aggregate counts by.
      dense : bool
        Default is True, for dense representation, which lists only non-zero counts, 1 combination per row. Set to False
        to expand counts across all combinations.

    Returns
    -------
      H2OFrame of the counts at each combination of factor levels
    """
    return H2OFrame._expr(expr=ExprNode("table",self,data2,dense)) if data2 is not None else H2OFrame._expr(expr=ExprNode("table",self,dense))

  def hist(self, breaks="Sturges", plot=True, **kwargs):
    """Compute a histogram over a numeric column.

    Parameters
    ----------
      breaks: str, int, list
        Can be one of "Sturges", "Rice", "sqrt", "Doane", "FD", "Scott."
        Can be a single number for the number of breaks.
        Can be a list containing sthe split points, e.g., [-50,213.2123,9324834]
        If breaks is "FD", the MAD is used over the IQR in computing bin width.
      plot : bool, default=True
        If True, then a plot is generated

    Returns
    -------
      If plot is False, return H2OFrame with these columns: breaks, counts, mids_true,
      mids, and density; otherwise produce the plot.
    """
    frame = H2OFrame._expr(expr=ExprNode("hist", self, breaks))._frame()
    total = frame["counts"].sum(True)
    densities = [[(frame[i,"counts"]/total)*(1/(frame[i,"breaks"]-frame[i-1,"breaks"]))] for i in range(1,frame["counts"].nrow)]
    densities.insert(0,[0])
    densities_frame = H2OFrame(densities)
    densities_frame.set_names(["density"])
    frame = frame.cbind(densities_frame)

    if plot:
      try:
        imp.find_module('matplotlib')
        import matplotlib
        if 'server' in kwargs.keys() and kwargs['server']: matplotlib.use('Agg', warn=False)
        import matplotlib.pyplot as plt
      except ImportError:
        print("matplotlib is required to make the histogram plot. Set `plot` to False, if a plot is not desired.")
        return

      lower = float(frame[0,"breaks"])
      clist = h2o.as_list(frame["counts"], use_pandas=False)
      clist.pop(0)
      clist.pop(0)
      mlist = h2o.as_list(frame["mids"], use_pandas=False)
      mlist.pop(0)
      mlist.pop(0)
      counts = [float(c[0]) for c in clist]
      counts.insert(0,0)
      mids = [float(m[0]) for m in mlist]
      mids.insert(0,lower)
      plt.xlabel(self.names[0])
      plt.ylabel('Frequency')
      plt.title('Histogram of {0}'.format(self.names[0]))
      plt.bar(mids, counts)
      if not ('server' in kwargs.keys() and kwargs['server']): plt.show()

    else: return frame

  def sub(self, pattern, replacement, ignore_case=False):
    """Substitute the first occurrence of pattern in a string with replacement.

    Parameters
    ----------
      pattern : str
        A regular expression.

      replacement : str
        A replacement string.

      ignore_case : bool
        If True then pattern will match against upper and lower case.

    Returns
    -------
      H2OFrame
    """
    return H2OFrame._expr(expr=ExprNode("replacefirst", self, pattern, replacement, ignore_case))

  def gsub(self, pattern, replacement, ignore_case=False):
    """Globally substitute occurrences of pattern in a string with replacement.

    Parameters
    ----------
      pattern : str
        A regular expression.

      replacement : str
        A replacement string.

      ignore_case : bool
        If True then pattern will match against upper and lower case.

    Returns
    -------
      H2OFrame
    """
    return H2OFrame._expr(expr=ExprNode("replaceall", self, pattern, replacement, ignore_case))

  def interaction(self, factors, pairwise, max_factors, min_occurrence, destination_frame=None):
    """Categorical Interaction Feature Creation in H2O.
    Creates a frame in H2O with n-th order interaction features between categorical columns, as specified by
    the user.

    Parameters
    ----------
      factors : list
          factors Factor columns (either indices or column names).
      pairwise : bool
        Whether to create pairwise interactions between factors (otherwise create one
        higher-order interaction). Only applicable if there are 3 or more factors.
      max_factors: int
        Max. number of factor levels in pair-wise interaction terms (if enforced, one extra
        catch-all factor will be made)
      min_occurrence: int
        Min. occurrence threshold for factor levels in pair-wise interaction terms
      destination_frame: str, optional
        A string indicating the destination key.

    Returns
    -------
      H2OFrame
    """
    return h2o.interaction(data=self, factors=factors, pairwise=pairwise, max_factors=max_factors,
                           min_occurrence=min_occurrence, destination_frame=destination_frame)

  def toupper(self):
    """Translate characters from lower to upper case for a particular column

    Returns
    -------
      H2OFrame
    """
    return H2OFrame._expr(expr=ExprNode("toupper", self), cache=self._ex._cache)

  def tolower(self):
    """Translate characters from upper to lower case for a particular column

    Returns
    -------
      H2OFrame
    """
    return H2OFrame._expr(expr=ExprNode("tolower", self), cache=self._ex._cache)

  def rep_len(self, length_out):
    """Replicates the values in `data` in the H2O backend

    Parameters
    ----------
    length_out : int
      Number of columns of the resulting H2OFrame

    Returns
    -------
      H2OFrame
    """
    return H2OFrame._expr(expr=ExprNode("rep_len", self, length_out))

  def scale(self, center=True, scale=True):
    """Centers and/or scales the columns of the self._newExpr

    Parameters
    ----------
      center : bool, list
        If True, then demean the data by the mean. If False, no shifting is done.
        If a list, then shift each column by the given amount in the list.
      scale : bool, list
        If True, then scale the data by the column standard deviation. If False, no scaling
        is done.
        If a list, then scale each column by the given amount in the list.

    Returns
    -------
      H2OFrame
    """
    return H2OFrame._expr(expr=ExprNode("scale", self, center, scale), cache=self._ex._cache)

  def signif(self, digits=6):
    """Round doubles/floats to the given number of significant digits.

    Parameters
    ----------
      digits : int, default=6
        Number of significant digits to round doubles/floats.

    Returns
    -------
      H2OFrame
    """
    return H2OFrame._expr(expr=ExprNode("signif", self, digits), cache=self._ex._cache)

  def round(self, digits=0):
    """Round doubles/floats to the given number of decimal places.

    Parameters
    ----------
    digits : int, default=0
      Number of decimal places to round doubles/floats. Rounding to a negative number of decimal places is not
      supported. For rounding off a 5, the IEC 60559 standard is used, ‘go to the even digit’. Therefore rounding 2.5
      gives 2 and rounding 3.5 gives 4.

    Returns
    -------
      H2OFrame
    """
    return H2OFrame._expr(expr=ExprNode("round", self, digits), cache=self._ex._cache)

  def asnumeric(self):
    """All factor columns converted to numeric.

    Returns
    -------
      H2OFrame
    """
    fr = H2OFrame._expr(expr=ExprNode("as.numeric", self), cache=self._ex._cache)
    if fr._ex._cache.types_valid():
      fr._ex._cache.types = {k:"real" for k in fr._ex._cache.types.keys()}
    return fr

  def ascharacter(self):
    """All columns converted to String columns

    Returns
    -------
      H2OFrame
    """
    fr = H2OFrame._expr(expr=ExprNode("as.character", self), cache=self._ex._cache)
    if fr._ex._cache.types_valid():
      fr._ex._cache.types = {k:"string" for k in fr._ex._cache.types.keys()}
    return fr

  def na_omit(self):
    """Remove rows with NAs from the H2OFrame.

    Returns
    -------
      H2OFrame
    """
    fr = H2OFrame._expr(expr=ExprNode("na.omit", self), cache=self._ex._cache)
    fr._ex._cache.nrows=-1
    return fr

  def isna(self):
    """For each element in an H2OFrame, determine if it is NA or not.

    Returns
    -------
      H2OFrame of 1s and 0s. 1 means the value was NA.
    """
    fr = H2OFrame._expr(expr=ExprNode("is.na", self))
    fr._ex._cache.nrows = self._ex._cache.nrows
    return fr

  def year(self):
    """
    Returns
    -------
      Year column from a msec-since-Epoch column
    """
    fr = H2OFrame._expr(expr=ExprNode("year", self), cache=self._ex._cache)
    if fr._ex._cache.types_valid():
      fr._ex._cache.types = {k:"int" for k in self._ex._cache.types.keys()}
    return fr

  def month(self):
    """
    Returns
    -------
      Month column from a msec-since-Epoch column
    """
    fr = H2OFrame._expr(expr=ExprNode("month", self), cache=self._ex._cache)
    if fr._ex._cache.types_valid():
      fr._ex._cache.types = {k:"int" for k in self._ex._cache.types.keys()}
    return fr

  def week(self):
    """
    Returns
    -------
      Week column from a msec-since-Epoch column
    """
    fr = H2OFrame._expr(expr=ExprNode("week", self), cache=self._ex._cache)
    if fr._ex._cache.types_valid():
      fr._ex._cache.types = {k:"int" for k in self._ex._cache.types.keys()}
    return fr

  def day(self):
    """
    Returns
    -------
      Day column from a msec-since-Epoch column
    """
    fr = H2OFrame._expr(expr=ExprNode("day", self), cache=self._ex._cache)
    if fr._ex._cache.types_valid():
      fr._ex._cache.types = {k:"int" for k in self._ex._cache.types.keys()}
    return fr

  def dayOfWeek(self):
    """
    Returns
    -------
      Day-of-Week column from a msec-since-Epoch column
    """
    fr = H2OFrame._expr(expr=ExprNode("dayOfWeek", self), cache=self._ex._cache)
    if fr._ex._cache.types_valid():
      fr._ex._cache.types = {k:"int" for k in self._ex._cache.types.keys()}
    return fr

  def hour(self):
    """
    Returns
    -------
      Hour-of-Day column from a msec-since-Epoch column
    """
    fr = H2OFrame._expr(expr=ExprNode("hour", self), cache=self._ex._cache)
    if fr._ex._cache.types_valid():
      fr._ex._cache.types = {k:"int" for k in self._ex._cache.types.keys()}
    return fr

  def runif(self, seed=None):
    """Generate a column of random numbers drawn from a uniform distribution [0,1) and
    having the same data layout as the calling H2OFrame instance.

    Parameters
    ----------
      seed : int, optional
        A random seed. If None, then one will be generated.

    Returns
    -------
      Single-column H2OFrame filled with doubles sampled uniformly from [0,1).
    """
    fr = H2OFrame._expr(expr=ExprNode("h2o.runif", self, -1 if seed is None else seed))
    fr._ex._cache.ncols=1
    fr._ex._cache.nrows=self.nrow
    return fr

  def stratified_split(self,test_frac=0.2,seed=-1):
    """Construct a column that can be used to perform a random stratified split.

    Parameters
    ----------
      test_frac : float, default=0.2
        The fraction of rows that will belong to the "test".
      seed      : int
        For seeding the random splitting.

    Returns
    -------
      A categorical column of two levels "train" and "test".

    Examples
    --------
      >>> my_stratified_split = my_frame["response"].stratified_split(test_frac=0.3,seed=12349453)
      >>> train = my_frame[my_stratified_split=="train"]
      >>> test  = my_frame[my_stratified_split=="test"]

      # check the distributions among the initial frame, and the train/test frames match
      >>> my_frame["response"].table()["Count"] / my_frame["response"].table()["Count"].sum()
      >>> train["response"].table()["Count"] / train["response"].table()["Count"].sum()
      >>> test["response"].table()["Count"] / test["response"].table()["Count"].sum()
    """
    return H2OFrame._expr(expr=ExprNode('h2o.random_stratified_split', self, test_frac, seed))

  def match(self, table, nomatch=0):
    """
    Makes a vector of the positions of (first) matches of its first argument in its second.

    Parameters
    ----------
      table : list
        list of items to match against

      nomatch : optional

    Returns
    -------
      H2OFrame of one boolean column
    """
    return H2OFrame._expr(expr=ExprNode("match", self, table, nomatch, None))

  def cut(self, breaks, labels=None, include_lowest=False, right=True, dig_lab=3):
    """Cut a numeric vector into factor "buckets". Similar to R's cut method.

    Parameters
    ----------
      breaks : list
        The cut points in the numeric vector (must span the range of the col.)

      labels: list
        Factor labels, defaults to set notation of intervals defined by breaks.

      include_lowest : bool
        By default,  cuts are defined as (lo,hi]. If True, get [lo,hi].

      right : bool
        Include the high value: (lo,hi]. If False, get (lo,hi).

      dig_lab: int
        Number of digits following the decimal point to consider.

    Returns
    -------
      Single-column H2OFrame of categorical data.
    """
    fr = H2OFrame._expr(expr=ExprNode("cut",self,breaks,labels,include_lowest,right,dig_lab), cache=self._ex._cache)
    fr._ex._cache.ncols = 1
    fr._ex._cache.nrows = self.nrow
    fr._ex._cache.types = {k:"enum" for k in self.names}
    return fr

  def which(self):
    """Equivalent to [ index for index,value in enumerate(self) if value ]

    Returns
    -------
      Single-column H2OFrame filled with 0-based indices for which the elements are not
      zero.
    """
    return H2OFrame._expr(expr=ExprNode("which",self))

  def ifelse(self,yes,no):
    """Equivalent to [y if t else n for t,y,n in zip(self,yes,no)]

    Based on the booleans in the test vector, the output has the values of the
    yes and no vectors interleaved (or merged together).  All Frames must have
    the same row count.  Single column frames are broadened to match wider
    Frames.  Scalars are allowed, and are also broadened to match wider frames.

    Parameters
    ----------
      test : H2OFrame (self)
        Frame of values treated as booleans; may be a single column

      yes : H2OFrame
        Frame to use if [test] is true ; may be a scalar or single column

      no : H2OFrame
        Frame to use if [test] is false; may be a scalar or single column

    Returns
    -------
      H2OFrame of the merged yes/no Frames/scalars according to the test input frame.
    """
    return H2OFrame._expr(expr=ExprNode("ifelse",self,yes,no))

  def apply(self, fun=None, axis=0):
    """Apply a lambda expression to an H2OFrame.

    Parameters
    ----------
      fun: lambda
        A lambda expression to be applied per row or per column

      axis: int
        0: apply to each column; 1: apply to each row

    Returns
    -------
      H2OFrame
    """
    from .astfun import _bytecode_decompile_lambda
    if axis not in [0,1]:
      raise ValueError("margin must be either 0 (cols) or 1 (rows).")
    if fun is None:
      raise ValueError("No function to apply.")
    if isinstance(fun, type(lambda:0)) and fun.__name__ == (lambda:0).__name__:  # have lambda
      res = _bytecode_decompile_lambda(fun.__code__)
      return H2OFrame._expr(expr=ExprNode("apply",self, 1+(axis==0),*res))
    else:
      raise ValueError("unimpl: not a lambda")
