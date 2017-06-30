import readline # anaconda bug workaround
import rpy2.rinterface as ri
import rpy2.robjects as ro
from config import *
import ast
import sqlite3 as db


journal_file = "journal"
journal_cur = None
journal_con = None

def toR(something):
    if isinstance(something, list):
        if isinstance(something, float):
            return ri.FloatSexpVector(something)
        elif isinstance(something, int):
            return ri.IntSexpVector(something)
        else:
            return ri.StrSexpVector(something)
    elif isinstance(something, dict):
        return ro.ListVector(something)
    return something


def label_metrics(name, print_phase=False):
    splitted_name = name.split("_")
    name = "_".join(splitted_name[:-1])
    mode = splitted_name[-1]
    if print_phase:
        return "{} in {} phase".format(Label.get(name,name), Label.get(mode,mode))
    else:
        return Label.get(name,name)


def init_journal():
    global journal_con, journal_cur, journal_file
    journal_con = db.connect("{}.db".format(journal_file))
    journal_cur = journal_con.cursor()
    query = ""
    for n in Names:
        if text_fields.match(n):
            query += ",\n  {} TEXT NOT NULL".format(n)
        else:
            query += ",\n  {} REAL".format(n)
    journal_cur.executescript("""
    CREATE TABLE IF NOT EXISTS results (
     resId INTEGER PRIMARY KEY{}
     );
    """.format(query))
    journal_con.commit()

def close_journal():
    global journal_con
    journal_con.close()

def escape(text):
    text = text.replace("\"","")
    return text

def persist(row):
    global journal_con, journal_cur
    try:
        query = ""
        for i, val in enumerate(row):
            if val is None:
                query += ", NULL"
            elif text_fields.match(Names[i]):
                query += ", \"{}\"".format(escape(val))
            else:
                query += ", {}".format(val)

        q="INSERT INTO results VALUES(NULL{});".format(
           query
        )

        journal_cur.execute(q)
        journal_con.commit()
    except:
        pass
    try:
        with open(journal_file, "a") as f:
            f.write(str(row) + "\n")
    except:
        pass


def legend_label(lbl):
    label = lbl[0]
    for i in range(1,len(lbl)):
        if isinstance(lbl[i], (str, basestring)):
            if "{" in lbl[i]:
                params = ast.literal_eval(lbl[i])
                for k, v in params.items():
                    label += ", {}={}".format(k, v)
            elif not lbl[i] == "default":
                label += ", {}".format(lbl[i])
        else:
            label += ", {}".format(str(lbl[i]))
    return label


def legend_label_mpcn(lbl):
    label = ""
    if isinstance(lbl[1], (str, basestring)):
        if "{" in lbl[1]:
            params = ast.literal_eval(lbl[1])
            for k, v in params.items():
                label += ", {}={}".format(k, v)
        elif not lbl[1] == "default":
             label += ", {}".format(lbl[1])
    else:
        label += ", {}".format(str(lbl[i]))
    return "{}{}, cluster={}, nthreads={}".format(lbl[0], label, lbl[2], lbl[3])

def legend_label_fg(lbl):
    label = ""
    if isinstance(lbl[1], (str, basestring)):
        if "{" in lbl[1]:
            params = ast.literal_eval(lbl[1])
            if "model_config" in params.keys() and "<" in params["model_config"]:
                try:
                    m = re.search("\<description\>(.*)\<\/description\>", params["model_config"])        
                    return "{} - {}".format(lbl[0], m.group(1))
                except:
                    return "Error - {}".format(lbl[0])
            else:
                for k, v in params.items():
                    label += ", {}={}".format(k, v)
        elif not lbl[1] == "default":
             label += ", {}".format(lbl[1])
    else:
        label += ", {}".format(str(lbl[i]))
    return "{}{}, cluster={}, nthreads={}".format(lbl[0], label, lbl[2], lbl[3])
