"""
A confusion matrix from H2O.
"""
from __future__ import division
from builtins import zip
from builtins import str
from builtins import range
from builtins import object
from past.utils import old_div

from ..two_dim_table import H2OTwoDimTable


class ConfusionMatrix(object):
  ROUND = 4  # round count_errs / sum

  def __init__(self, cm, domains=None, table_header=None):
    if not cm: raise ValueError("Missing data, `cm_raw` is None")
    if not isinstance(cm, list):  raise ValueError("`cm` is not a list. Got: " + type(cm))

    if len(cm)==2: cm=list(zip(*cm))  # transpose if 2x2
    nclass = len(cm)
    class_errs = [0] * nclass
    class_sums = [0] * nclass
    class_err_strings = [0] * nclass
    cell_values = [[0] * (1 + nclass)] * (1 + nclass)
    totals = [sum(c) for c in cm]
    total_errs=0
    for i in range(nclass):
      class_errs[i] = sum([v[i] for v in cm[:i] + cm[(i + 1):]])
      total_errs += class_errs[i]
      class_sums[i] = sum([v[i] for v in cm])  # row sums
      class_err_strings[i] = \
          " (" + str(class_errs[i]) + "/" + str(class_sums[i]) + ")"
      class_errs[i] = float("nan") if class_sums[i] == 0 else round(old_div(float(class_errs[i]), float(class_sums[i])), self.ROUND)
      # and the cell_values are
      cell_values[i] = [v[i] for v in cm] + [str(class_errs[i])] + [class_err_strings[i]]

    # tally up the totals
    class_errs += [sum(class_errs)]
    totals += [sum(class_sums)]
    class_err_strings += [" (" + str(total_errs) + "/" + str(totals[-1]) + ")"]

    class_errs[-1] = float("nan") if totals[-1] == 0 else round(old_div(float(total_errs), float(totals[-1])), self.ROUND)

    # do the last row of cell_values ... the "totals" row
    cell_values[-1] = totals[0:-1] + [str(class_errs[-1])] + [class_err_strings[-1]]

    if table_header is None: table_header = "Confusion Matrix (Act/Pred)"
    col_header = [""]  # no column label for the "rows" column
    if domains is not None:
        import copy
        row_header = copy.deepcopy(domains)
        col_header += copy.deepcopy(domains)
    else:
        row_header = [str(i) for i in range(nclass)]
        col_header += [str(i) for i in range(nclass)]

    row_header += ["Total"]
    col_header += ["Error", "Rate"]

    for i in range(len(row_header)):
      cell_values[i].insert(0, row_header[i])

    self.table = H2OTwoDimTable(row_header=row_header, col_header=col_header,
                                table_header=table_header, cell_values=cell_values)

  def show(self):
    self.table.show()

  def __repr__(self):
    self.show()
    return ""

  def to_list(self):
    return [ [int(self.table.cell_values[0][1]), int(self.table.cell_values[0][2])],
             [int(self.table.cell_values[1][1]), int(self.table.cell_values[1][2])] ]

  @staticmethod
  def read_cms(cms=None, domains=None):
    if cms is None:  raise ValueError("Missing data, no `cms`.")
    if not isinstance(cms, list):  raise ValueError("`cms` must be a list of lists")
    lol_all = all(isinstance(l, (tuple, list)) for l in cms)
    if not lol_all: raise ValueError("`cms` must be a list of lists")
    return [ConfusionMatrix(cm, domains) for cm in cms]
