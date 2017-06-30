
parserTypes = map [ 'AUTO', 'XLS', 'CSV', 'SVMLight' ], (type) -> type: type, caption: type

parseDelimiters = do ->
  whitespaceSeparators = [
    'NULL'
    'SOH (start of heading)'
    'STX (start of text)'
    'ETX (end of text)'
    'EOT (end of transmission)'
    'ENQ (enquiry)'
    'ACK (acknowledge)'
    "BEL '\\a' (bell)"
    "BS  '\\b' (backspace)"
    "HT  '\\t' (horizontal tab)"
    "LF  '\\n' (new line)"
    "VT  '\\v' (vertical tab)"
    "FF  '\\f' (form feed)"
    "CR  '\\r' (carriage ret)"
    'SO  (shift out)'
    'SI  (shift in)'
    'DLE (data link escape)'
    'DC1 (device control 1) '
    'DC2 (device control 2)'
    'DC3 (device control 3)'
    'DC4 (device control 4)'
    'NAK (negative ack.)'
    'SYN (synchronous idle)'
    'ETB (end of trans. blk)'
    'CAN (cancel)'
    'EM  (end of medium)'
    'SUB (substitute)'
    'ESC (escape)'
    'FS  (file separator)'
    'GS  (group separator)'
    'RS  (record separator)'
    'US  (unit separator)'
    "' ' SPACE"
  ]

  createDelimiter = (caption, charCode) ->
    charCode: charCode
    caption: "#{caption}: '#{('00' + charCode).slice(-2)}'"

  whitespaceDelimiters = map whitespaceSeparators, createDelimiter

  characterDelimiters = times (126 - whitespaceSeparators.length), (i) ->
    charCode = i + whitespaceSeparators.length
    createDelimiter (String.fromCharCode charCode), charCode

  otherDelimiters = [ charCode: -1, caption: 'AUTO' ]

  concat whitespaceDelimiters, characterDelimiters, otherDelimiters

Steam.ImportFilesDialog = (_, _go) ->

  _isImportMode = node$ yes
  _isParseMode = node$ no

  #
  # Search files/dirs
  #
  _specifiedPath = node$ ''
  _errorMessage = node$ ''
  _hasErrorMessage = lift$ _errorMessage, isTruthy

  tryImportFiles = ->
    specifiedPath = _specifiedPath()
    _.requestFileGlob specifiedPath, 0, (error, result) ->
      if error
        _errorMessage error.data.errmsg
      else
        _errorMessage ''
        #_go 'confirm', result
        processImportResult result


  #
  # File selection 
  #
  _importedFiles = nodes$ []
  _importedFileCount = lift$ _importedFiles, (files) -> "Found #{describeCount files.length, 'file'}."
  _hasImportedFiles = lift$ _importedFiles, (files) -> files.length > 0
  _selectedFiles = nodes$ []
  _selectedFilesDictionary = lift$ _selectedFiles, (files) ->
    dictionary = {}
    for file in files
      dictionary[file.path] = yes
    dictionary
  _selectedFileCount = lift$ _selectedFiles, (files) -> "#{describeCount files.length, 'file'} selected."
  _hasSelectedFiles = lift$ _selectedFiles, (files) -> files.length > 0

  importFiles = (files) ->
    paths = map files, (file) -> file.path
    _.requestImportFiles paths, (responses) ->
      passedResponses = filter responses, (response) -> not response.error
      sourceKeys = flatten map passedResponses, (response) -> response.result.keys
      _.requestParseSetup sourceKeys, (error, result) ->
        if error
          #TODO handle this properly
          _.fail 'Error', error, null, noop
        else
          _isImportMode no
          processParseSetupResult result
          _isParseMode yes
    return

  importSelectedFiles = -> importFiles _selectedFiles()

  createSelectedFileItem = (path) ->
    self =
      path: path
      deselect: ->
        _selectedFiles.remove self
        for file in _importedFiles() when file.path is path
          file.isSelected no
        return

  createFileItem = (path, isSelected) ->
    self =
      path: path
      isSelected: node$ isSelected
      toggle: ->
        if self.isSelected()
          self.deselect()
        else
          self.select()
      select: ->
        _selectedFiles.push createSelectedFileItem self.path
        self.isSelected yes 
      deselect: ->
        file.deselect() if file = (find _selectedFiles(), (file) -> file.path is path)

  listPathHints = (query, process) ->
    _.requestFileGlob query, 10, (error, result) ->
      if error
        # swallow
      else
        process map result.matches, (value) -> value: value

  selectAllFiles = ->
    _selectedFiles map _importedFiles(), (file) ->
      createSelectedFileItem file.path
    for file in _importedFiles()
      file.isSelected yes
    return

  deselectAllFiles = ->
    _selectedFiles []
    for file in _importedFiles()
      file.isSelected no
    return
  
  createFileItems = (result) ->
    map result.matches, (path) ->
      createFileItem path, _selectedFilesDictionary()[path]

  processImportResult = (result) -> 
    files = createFileItems result
    _importedFiles files

  #
  # Parsing
  #
  _parserType = node$ null
  _delimiter = node$ null
  _useSingleQuotes = node$ no
  _columns = node$ []
  _rows = node$ []
  _columnCount = node$ 0
  _hasColumns = lift$ _columnCount, (count) -> count > 0
  _destinationKey = node$ ''
  _parsedFiles = nodes$ []
  _headerOption = node$ 'auto'
  _deleteOnDone = node$ yes
  _headerOptions = auto: 0, header: 1, data: -1

  processParseSetupResult = (result) ->
    _parserType find parserTypes, (parserType) -> parserType.type is result.pType
    _delimiter find parseDelimiters, (delimiter) -> delimiter.charCode is result.sep 
    _useSingleQuotes result.singleQuotes
    _destinationKey result.hexName
    _headerOption if result.checkHeader is 0 then 'auto' else if result.checkHeader is -1 then 'data' else 'header'
    _columnCount result.ncols
    _parsedFiles result.srcs
    _columns map result.columnNames, (name) -> name: node$ name
    _rows result.data

  parseFiles = ->
    sourceKeys = map _parsedFiles(), (file) -> file.name
    columnNames = map _columns(), (column) -> column.name()
    _.requestParseFiles sourceKeys, _destinationKey(), _parserType().type, _delimiter().charCode, _columnCount(), _useSingleQuotes(), columnNames, _deleteOnDone(), _headerOptions[_headerOption()], (error, result) -> 
      if error
        #TODO handle this properly
        _.fail 'Error', error, null, noop
      else
        _go 'confirm', result.job

  backToImport = ->
    _isParseMode no
    _isImportMode yes

  cancel = -> _go 'cancel'

  isImportMode: _isImportMode
  isParseMode: _isParseMode

  specifiedPath: _specifiedPath
  hasErrorMessage: _hasErrorMessage
  errorMessage: _errorMessage
  tryImportFiles: tryImportFiles

  listPathHints: throttle listPathHints, 100
  hasImportedFiles: _hasImportedFiles
  importedFiles: _importedFiles
  importedFileCount: _importedFileCount
  selectedFiles: _selectedFiles
  selectAllFiles: selectAllFiles
  deselectAllFiles: deselectAllFiles
  hasSelectedFiles: _hasSelectedFiles
  selectedFileCount: _selectedFileCount
  importSelectedFiles: importSelectedFiles

  parserTypes: parserTypes
  delimiters: parseDelimiters
  parserType: _parserType
  delimiter: _delimiter
  useSingleQuotes: _useSingleQuotes
  columns: _columns
  rows: _rows
  columnCount: _columnCount
  hasColumns: _hasColumns
  destinationKey: _destinationKey
  headerOption: _headerOption
  parsedFiles: _parsedFiles
  deleteOnDone: _deleteOnDone
  backToImport: backToImport
  parseFiles: parseFiles

  cancel: cancel
  template: 'import-files-dialog'
