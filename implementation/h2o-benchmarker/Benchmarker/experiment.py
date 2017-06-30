import yaml
import matplotlib.pyplot as plt
import pandas as pd
import warnings
import numpy as np
import ast
from config import Label, Names
from utils import label_metrics

with warnings.catch_warnings():
    warnings.filterwarnings('ignore', category=DeprecationWarning)
    import seaborn as sns
    import h2o

class Experiment(yaml.YAMLObject):
    yaml_tag = u"Experiment"

    @classmethod
    def load(cls,file):
        f = open(file, "r")
        e = yaml.load(f)
        f.close()
        return e

    def __init__(self, name,x, y, filename, train_ratio, validation_ratio, subsets):
        self.models = dict()
        self.name = name
        self.x = x
        self.y = y
        self.filename = filename
        self.train_ratio = train_ratio
        self.validation_ratio = train_ratio + validation_ratio
        if subsets:
            self.subsets = subsets
        else:
            self.subsets = {
                "min_fraction_denom":1,
                "max_fraction_denom": 4,
                "number_of_samples": 20
            }

        # Sanity checks
        if self.validation_ratio > 0.95:
            warnings.warn("train_ratio + validation_ratio should be substantially less than 1 because "
                          " train_ratio + validation_ratio = 1 - test_ratio")
        if self.subsets["min_fraction_denom"] < 1:
            warnings.warn("min_fraction_denom must be greater or equal to 1")
            self.subsets["min_fraction_denom"] = 1
        if self.subsets["number_of_samples"] < 1:
            warnings.warn("number_of_samples must be integer greater or equal to 1")
            self.subsets["number_of_samples"] = 1
        if self.subsets["max_fraction_denom"] < self.subsets["min_fraction_denom"]:
            warnings.warn("max_fraction_denom must be greater than min_fraction_denom")
            self.subsets["max_fraction_denom"] = self.subsets["min_fraction_denom"] + 1

    def add(self, name, model):
        self.models[name] = model

    def execute(self):
        print("* Executing experiment >>{}<<".format(self.name))
        print("* Importing file >>{}<<".format(self.filename))
        fr = h2o.import_file(path=self.filename, na_strings=[])
        results = pd.DataFrame(columns=Names)
        sub = self.subsets

        r = fr[0].runif()
        try:
            #for sub_frac in reversed(np.linspace(sub["min_fraction_denom"], sub["max_fraction_denom"], sub["number_of_samples"])):
            # linear sampling
            for ratio in np.linspace(1.0/sub["max_fraction_denom"], 1.0/sub["min_fraction_denom"], sub["number_of_samples"]):
                sub_frac = 1.0/ratio
                train = fr[r < self.train_ratio / float(sub_frac)]
                valid = fr[(self.train_ratio <= r) &
                           (r < self.train_ratio + (self.validation_ratio - self.train_ratio)/float(sub_frac))]
                test = fr[1.0 - (1.0 - self.validation_ratio)/float(sub_frac) <= r]
                try:
                    print("")
                    print(":: Subsampling into training, validation and test sets with {}, {} and {} samples respectively. (ratio: {})".format(
                        len(train), len(valid), len(test), 1.0/sub_frac))

                    for _, modelCfg in self.models.items():
                        print("")
                        print(":::: Benchmarking {} model...".format(modelCfg.name))
                        results = results.append(modelCfg.execute(self.name, self.x, self.y, train, valid, test, ratio))
                finally:
                    h2o.remove(train)
                    h2o.remove(valid)
                    h2o.remove(test)
        finally:
            h2o.remove(fr)
        return results

    def plot(self, file_path, results):
        res = results.groupby(["model", "params"])
        i_max = len(res) + 1
        metrics_idx = Names.index("accuracy_train")
        for time in ["initTime", "trainTime", "metricsTime", "totalTime"]:
            for metrics in Names[metrics_idx:]:
                if all(results[metrics].isnull()):
                    continue
                plt.figure()
                plt.title("{} x {}".format(label_metrics(metrics, True), Label[time]))
                i = 1

                for lbl, g in res:
                    try:
                        g = g.sort_values(time)
                        if lbl[1] != "default":
                            params = ast.literal_eval(lbl[1])
                            lbl = [lbl[0], ""]
                            for k, v in params.items():
                                lbl[1] += ", {}={}".format(k,v)
                        plt.plot(g[time], g[metrics], "o-", c=sns.color_palette("hls", n_colors=i_max)[i],
                                label=lbl[0] if lbl[1] == "default" else lbl[0] + lbl[1])
                    except:
                        print("An error occurred during ploting {}, {}, {}".format(time, metrics, lbl))
                    i += 1

                lgd=plt.legend(bbox_to_anchor=(1.05, 1), loc=2, borderaxespad=0.)
                plt.xlabel("{} [s]".format(Label[time]))
                plt.ylabel(label_metrics(metrics))
                plt.savefig("{}/{}-{}__x__{}.pdf".format(file_path,self.name, metrics,time), bbox_extra_artists=(lgd,), bbox_inches='tight')
                plt.savefig("{}/{}-{}__x__{}.png".format(file_path, self.name, metrics, time), bbox_extra_artists=(lgd,), bbox_inches='tight')
                plt.close()

