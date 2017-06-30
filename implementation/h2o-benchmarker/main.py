import h2o
from Benchmarker.experiment import Experiment
import Benchmarker.config as config
import Benchmarker.utils as utils
import warnings
from optparse import OptionParser
from Benchmarker.utils import init_journal, close_journal
# needed for modelconfig deserialization
import Benchmarker.model_config
import Benchmarker.super_learner_model_config
import yaml


if __name__ == '__main__':
    opt = OptionParser()
    opt.add_option("-f", "--file", dest="filename", help="read experiment from FILE", metavar="FILE")
    opt.add_option("-i", "--ip", dest="hostname", help="ip address or host name of computer running h2o")
    opt.add_option("-p", "--port", dest="port", help="port used by h2o")
    opt.add_option("-n", "--nthreads", dest="nthreads", help="number of threads used by h2o")
    opt.add_option("-c", "--cluster", dest="cluster", help="cluster name used by h2o to establish connection")
    opt.add_option("-e", "--export", dest="dir", help="directory to which the plots and data will be exported")
    opt.add_option("-j", "--journal-file", dest="journal", help="filename of a journal, i.e., file that gets"
                                                                "appended by each result in case something goes wrong")

    (options, args) = opt.parse_args()

    if options.journal:
        utils.journal_file = options.journal

    init_journal()
    # Sanity checks
    try:
        f = open(utils.journal_file,"a")
        f.close()
    except:
        warnings.warn("An error occurred during opening the journal file. Have you set it properly? (-j)")
        exit(1)

    try:
        f = open("{}/dummy".format(options.dir), "w")
        f.write("This dummy file is used to check validity of the export folder")
        f.close()
    except:
        warnings.warn("An error occurred during creating a file in export folder. Have you set it properly? (-e)")
        exit(1)

    config.hostname = options.hostname
    config.port = int(options.port)
    config.nthreads = int(options.nthreads) if int(options.nthreads) >= 1 or options.nthreads is None else 4
    config.cluster = "one" if options.cluster == "" or options.cluster is None else options.cluster

    # Actual code to run
    h2o.init(config.hostname, config.port, nthreads=config.nthreads, cluster_name=config.cluster)
    exp = Experiment.load(options.filename)

    result = exp.execute()
    print("Saving the results into >>{}<<".format("{}/{}-data.csv".format(options.dir, exp.name)))
    result.to_csv("{}/{}-data.csv".format(options.dir, exp.name))
    close_journal()
    print("Plotting the results into >>{}<<".format(options.dir))
    exp.plot(options.dir, result)
