from Benchmarker.experiment import Experiment
import Benchmarker.config as config
import warnings
import pandas as pd
from optparse import OptionParser

# needed for modelconfig deserialization
import Benchmarker.model_config
import yaml


if __name__ == '__main__':
    opt = OptionParser()
    opt.add_option("-f", "--file", dest="filename", help="read experiment from FILE", metavar="FILE")
    opt.add_option("-e", "--export", dest="dir", help="directory to which the plots and data will be exported")

    (options, args) = opt.parse_args()

    # Sanity check
    try:
        f = open("{}/dummy".format(options.dir), "w")
        f.write("This dummy file is used to check validity of the export folder")
        f.close()
    except:
        warnings.warn("An error occurred during creating a file in export folder. Have you set it properly? (-e)")
        exit(1)

    # Actual code to run

    exp = Experiment.load(options.filename)

    results = pd.read_csv("{}/{}-data.csv".format(options.dir, exp.name))
    exp.plot(options.dir, results)
