from __future__ import print_function
import sys
sys.path.insert(1,"../../")
import h2o
from tests import pyunit_utils



import pandas as pd
import numpy as np

def group_by():
    # Connect to a pre-existing cluster
    

    h2o_iris = h2o.import_file(path=pyunit_utils.locate("smalldata/iris/iris_wheader.csv"))
    pd_iris = pd.read_csv(pyunit_utils.locate("smalldata/iris/iris_wheader.csv"))

    na_handling = ["rm","all"]
    col_names = h2o_iris.col_names[0:4]

    print("Running smoke test")

    # smoke test
    for na in na_handling:
      grouped = h2o_iris.group_by("class")
      grouped \
        .count(na=na) \
        .min(  na=na) \
        .max(  na=na) \
        .mean( na=na) \
        .var(  na=na) \
        .sd(   na=na) \
        .ss(   na=na) \
        .sum(  na=na)
      print(grouped.get_frame())


if __name__ == "__main__":
    pyunit_utils.standalone_test(group_by)
else:
    group_by()
