import tabulate


class H2ODisplay(object):
  """Pretty printing for H2O Objects.

  Handles both IPython and vanilla console display
  """
  THOUSANDS = "{:,}"
  def __init__(self,table=None,header=None,table_header=None,**kwargs):
    self.table_header=table_header
    self.header=header
    self.table=table
    self.kwargs=kwargs
    self.do_print=True

    # one-shot display... never return an H2ODisplay object (or try not to)
    # if holding onto a display object, then may have odd printing behavior
    # the __repr__ and _repr_html_ methods will try to save you from many prints,
    # but just be WARNED that your mileage may vary!
    #
    # In other words, it's better to just new one of these when you're ready to print out.

    if self.table_header is not None:
      print()
      print(self.table_header + ":")
      print()
    if H2ODisplay._in_ipy():
      from IPython.display import display
      display(self)
      self.do_print=False
    else:
      self.pprint()
      self.do_print=False

  # for Ipython
  def _repr_html_(self):
    if self.do_print:
      return H2ODisplay._html_table(self.table,self.header)

  def pprint(self):
    r = self.__repr__()
    print(r)

  # for python REPL console
  def __repr__(self):
    if self.do_print or not H2ODisplay._in_ipy():
      if self.header is None: return tabulate.tabulate(self.table,**self.kwargs)
      else:                   return tabulate.tabulate(self.table,headers=self.header,**self.kwargs)
    self.do_print=True
    return ""

  @staticmethod
  def _in_ipy():  # are we in ipy? then pretty print tables with _repr_html
    try:
      __IPYTHON__
      return True
    except NameError:
      return False

  # some html table builder helper things
  @staticmethod
  def _html_table(rows, header=None):
    table= "<div style=\"overflow:auto\"><table style=\"width:50%\">{}</table></div>"  # keep table in a div for scroll-a-bility
    table_rows=[]
    if header is not None:
      table_rows.append(H2ODisplay._html_row(header, bold=True))
    for row in rows:
      table_rows.append(H2ODisplay._html_row(row))
    return table.format("\n".join(table_rows))

  @staticmethod
  def _html_row(row, bold=False):
    res = "<tr>{}</tr>"
    entry = "<td><b>{}</b></td>"if bold else "<td>{}</td>"
    #format full floating point numbers to 7 decimal places
    entries = "\n".join([entry.format(str(r))
                         if len(str(r)) < 10 or not _is_number(str(r))
                         else entry.format("{0:.7f}".format(float(str(r)))) for r in row])
    return res.format(entries)

def _is_number(s):
  try:
    float(s)
    return True
  except ValueError:
    return False