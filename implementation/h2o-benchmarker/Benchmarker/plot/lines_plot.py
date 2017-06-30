import yaml
import matplotlib.pyplot as plt
import seaborn as sns
import sys
import traceback
from ..config import Names, Label
from ..utils import legend_label, label_metrics


class LinesPlot(yaml.YAMLObject):
    yaml_tag = u"Lines"

    def __init__(self, name ,title, xlab, ylab, x_col, y_col, style, legend_labels, group_by):
        self.name = name
        self.title = title
        self.xlab = xlab
        self.ylab = ylab
        self.x_col = x_col
        self.y_col = y_col
        self.legend_labels = legend_labels
        self.group_by = group_by
        self.style = style

    def plot(self, file_path, data):
        res = data.groupby(self.group_by)
        i_max = len(res) + 1
        plt.figure()
        plt.title(self.title)
        i = 1

        for lbl, g in res:
            try:
                g = g.sort_values(self.x_col)

                label = self.legend_labels(lbl)
                plt.plot(g[self.x_col], g[self.y_col], self.style, c=sns.color_palette("hls", n_colors=i_max)[i],
                         label=label)
            except:
                print("An error occurred during ploting {}, {}, {}".format(self.x_col, self.y_col, lbl))
                traceback.print_tb(sys.exc_traceback)
            i += 1

        lgd = plt.legend(bbox_to_anchor=(1.05, 1), loc=2, borderaxespad=0.)

        plt.xlabel(self.xlab)
        plt.ylabel(self.ylab)

        plt.savefig("{}/{}-{}__x__{}.pdf".format(file_path, self.name, self.x_col, self.y_col),
                    bbox_extra_artists=(lgd,), bbox_inches='tight')
        plt.savefig("{}/{}-{}__x__{}.png".format(file_path, self.name, self.x_col, self.y_col),
                    bbox_extra_artists=(lgd,), bbox_inches='tight')
        plt.close()

