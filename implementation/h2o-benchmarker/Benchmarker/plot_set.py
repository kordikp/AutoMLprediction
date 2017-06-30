import yaml
from plot import *
import os

class PlotSet(yaml.YAMLObject):
    yaml_tag = u"PlotSet"

    def __init__(self, name, plots, query):
        self.name = name
        self.plots = plots
        self.query = query

    def plot(self, file_path ,data_frame):
        file_path = "{}/{}".format(file_path,self.name)
        try:
            os.mkdir(file_path)
        except OSError:
            if not os.path.isdir(file_path):
                raise
        for p in self.plots:
            if isinstance(p, (str,basestring)):
                if p.startswith("$"):
                    with open("{}/plot_config/{}.yaml".format(os.path.dirname(__file__), p[1:]), "r") as f:
                        p = yaml.load(f)
                else:
                    with open(p,"r") as f:
                        p = yaml.load(f)
            p.plot(file_path, data_frame)
