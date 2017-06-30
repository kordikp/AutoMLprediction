import Benchmarker.config as config
import warnings
from optparse import OptionParser
import pandas as pd
import sqlite3 as db
# needed for modelconfig deserialization
import Benchmarker.plot_set as pss
from Benchmarker.plot import *
import yaml
import Benchmarker.utils as utils

if __name__ == '__main__':
    opt = OptionParser()
    opt.add_option("-e", "--export", dest="dir", help="directory to which the plots and data will be exported")
    opt.add_option("-j", "--journal-file", dest="journal", help="filename of a journal, i.e., file that gets"
                                                                "appended by each result in case something goes wrong")

    (options, args) = opt.parse_args()

    # Sanity checks
    try:
        f = open("{}/dummy".format(options.dir), "w")
        f.write("This dummy file is used to check validity of the export folder")
        f.close()
    except:
        warnings.warn("An error occurred during creating a file in export folder. Have you set it properly? (-e)")
        exit(1)

    con = db.connect(options.journal)

    for file in args:
        print("Plotting {}".format(file))
        with open(file,"r") as f:
            ps=yaml.load(f)

        res = pd.read_sql(ps.query, con)
        ps.plot(options.dir, res)
