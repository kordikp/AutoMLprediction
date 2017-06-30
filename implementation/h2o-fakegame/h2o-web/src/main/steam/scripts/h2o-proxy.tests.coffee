return unless SYSTEM_TEST

test 'empty cloud', (t) ->
  t.plan 6
  createCloud (_, go) ->
    logAndEcho = (go) ->
      _.requestLogAndEcho (error, result) ->
        if error
          t.fail 'log and echo request failed ' + pp error
          go error
        else
          t.pass 'got log and echo reply'
          go null

    removeAll = (go) ->
      _.requestRemoveAll (error, result) ->
        if error
          t.fail 'remove all request failed ' + pp error
          go error
        else
          t.pass 'got remove all reply'
          go null

    ensureNoFramesExist = (go) ->
      _.requestFrames (error, result) ->
        if error
          t.fail 'frames request failed ' + pp error
          go error
        else
          t.pass 'got frames reply'
          tdiff t, (readGoldJson 'frames-empty.json'), result
          go null

    ensureNoJobsExist = (go) ->
      _.requestJobs (error, result) ->
        if error
          t.fail 'request failed ' + pp error
          go error
        else
          t.pass 'got jobs reply'
          tdiff t, (readGoldJson 'jobs-empty.json'), result
          go null

    operations = [
      logAndEcho
      removeAll
      ensureNoFramesExist
      ensureNoJobsExist
    ]
    async.waterfall operations, -> go t

test 'airlines ingest and model building flow', (t) ->
  t.plan 45

  createCloud (_, go) ->
    logAndEcho = (go) ->
      _.requestLogAndEcho (error, result) ->
        if error
          t.fail 'log and echo request failed ' + pp error
          go error
        else
          t.pass 'got log and echo reply'
          go null

    removeAll = (go) ->
      _.requestRemoveAll (error, result) ->
        if error
          t.fail 'remove all request failed ' + pp error
          go error
        else
          t.pass 'got remove all reply'
          go null

    ensureNoFramesExist = (go) ->
      _.requestFrames (error, result) ->
        if error
          t.fail 'frames request failed ' + pp error
          go error
        else
          t.pass 'got frames reply'
          tdiff t, (readGoldJson 'frames-empty.json'), result
          go null


    findNonExistentFile = (go) ->
      _.requestFileGlob '/non/existent/path', 10, (error, result) ->
        if error
          t.fail 'glob request failed ' + pp error
          go error
        else
          t.pass 'got glob reply'
          tdiff t, (readGoldJson 'glob-empty.json'), result
          go null

    findAirlines = (go) ->
      _.requestFileGlob (path.join DATA_PATH, 'airlines'), 10, (error, result) ->
        if error
          t.fail 'glob request failed ' + pp error
          go error
        else
          t.pass 'got glob reply'
          expectedGlobResponse =
            src: null
            limit: 0
          tdiff t, expectedGlobResponse, result, exclude: [ 'matches' ]
          t.ok (isArray result.matches), 'has matches'
          t.ok (result.matches.length > 0), 'has matches'
          airlinesZip = find result.matches, (match) -> (/allyears2k_headers\.zip$/).test match
          t.ok (airlinesZip isnt null), 'found airlines zip'
          go null, airlinesZip

    importAirlinesFrame = (airlinesZip, go) ->
      _.requestImportFile airlinesZip, (error, result) ->
        if error
          t.fail 'import request failed ' + pp error
          go error
        else
          t.pass 'got import reply'
          expectedImportResponse =
            dels: []
            fails: []
            path: airlinesZip
            files: [ airlinesZip ]
            keys: [ "nfs://#{airlinesZip.substr 1}" ]
          tdiff t, expectedImportResponse, result

          go null, result.keys[0]

    parseSetupAirlinesFrame = (airlinesZipKey, go) ->
      _.requestParseSetup [ airlinesZipKey ], (error, result) ->
        if error
          t.fail 'parse setup request failed ' + pp error
          go error
        else
          t.pass 'got parse setup reply'
          tdiff t, (readGoldJson 'parse-setup-allyears2k_headers-zip.json'), result, exclude: [ 'srcs' ]
          t.ok (isArray result.srcs), 'has srcs'
          t.equal result.srcs.length, 1, 'has 1 src'
          t.ok (isString result.srcs[0].name), 'has src name'
          go null, result

    parseAirlinesFrame = (parseSetup, go) ->
      sourceKeys = map parseSetup.srcs, (src) -> src.name
      _.requestParseFiles sourceKeys, parseSetup.hexName, parseSetup.pType, parseSetup.sep, parseSetup.ncols, parseSetup.singleQuotes, parseSetup.columnNames, yes, parseSetup.checkHeader, (error, result) ->
        if error
          t.fail 'parse request failed ' + pp error
          go error
        else
          t.pass 'got parse reply'
          tdiff t, (readGoldJson 'parse-allyears2k_headers-zip.json'), result, exclude: [ 'job' ]
          t.ok (isString result.job.key.name), 'has job name'
          console.log("HEY!!!")
          console.log(JSON.stringify(result))
          go null, result.job.key.name

    fetchJobs = (jobKey, go) ->
      _.requestJobs (error, result) ->
        if error
          t.fail 'jobs request failed ' + pp error
          go error
        else
          t.pass 'got jobs reply'
          console.log(result)
          t.ok result.jobs.length is 1
          t.equal result.jobs[0].key.name, jobKey
          go null, jobKey

    pollJob = (jobKey, go) ->
      poll = (go) ->
        _.requestJob jobKey, (error, job) ->
          if error
            t.fail 'job poll failed ' + pp error
            go error
          else
            if job.status is 'CREATED' or job.status is 'RUNNING'
              delay poll, 1000, go
            else
              go null, job

      poll (error, job) ->
        if error
          go error
        else
          t.equal job.status, 'DONE', 'job status ok'
          go null, job.dest.name

    # allyears2k_headers.hex
    inspectAirlinesFrame = (frameKey, go) -> 
      _.requestInspect frameKey, (error, result) ->
        if error
          t.fail 'frame inspect request failed ' + pp error
          go error
        else
          t.pass 'got frame inspect reply'
          tdiff t, (readGoldJson 'inspect-allyears2k_headers-zip.json'), result
          go null, frameKey


    fetchAirlinesFrame = (frameKey, go) ->
      _.requestFrame frameKey, (error, result) ->
        if error
          t.fail 'frame request failed ' + pp error
          go error
        else
          t.pass 'got frame reply'
          tdiff t, (readGoldJson 'frames-allyears2k_headers-zip.json'), result
          go null, frameKey

    fetchKmeansModelBuilder = (frameKey, go) ->
      _.requestModelBuilder 'kmeans', (error, result) ->
        if error
          t.fail 'model builders request failed ' + pp error
          go error
        else
          t.pass 'got model builders reply'
          tdiff t, (readGoldJson 'model-builders-kmeans.json'), result, exclude: [ 'model_builders.kmeans.job', 'model_builders.kmeans.parameters.#.default_value', 'model_builders.kmeans.parameters.#.actual_value', 'model_builders.kmeans.parameters.#.required' ]
          go null, frameKey

    buildAirlinesKmeansModel = (frameKey, go) ->
      parameters = 
        training_frame: frameKey
        K: 2
        max_iterations: 1000
        normalize: 'true'
        seed: 1410214121289766000
        init: 'Furthest'
      _.requestModelBuild 'kmeans', parameters, (error, result) ->
        if error
          t.fail 'model build request failed ' + pp error
          go error
        else
          t.pass 'got model build reply'
          tdiff t, (readGoldJson 'kmeans-allyears2k_headers-zip.json'), result, include: [ 'jobs.#.description', 'jobs.#.dest', 'jobs.#.exception' ]
          t.ok (isString result.key.name), 'has job name'
          go null, result.key.name

    inspectAirlinesKmeansModel = (modelKey, go) ->
      _.requestInspect modelKey, (error, result) ->
        if error
          t.fail 'model inspect request failed ' + pp error
          go error
        else
          t.pass 'got model inspect reply'
          #result.schema.parameters[3].actual_value = "(random)"
          #tdiff t, (readGoldJson 'inspect-kmeans-allyears2k_headers-zip.json'), result, exclude: [ 'schema.output.centers', 'schema.output.rows', 'schema.output.mses', 'schema.output.mse', 'schema.output.iterations' ]
          tdiff t, (readGoldJson 'inspect-kmeans-allyears2k_headers-zip.json'), result, exclude: [ 'key.name', 'schema.key' ]
          go null

    fetchDeepLearningModelBuilder = (go) ->
      _.requestModelBuilder 'deeplearning', (error, result) ->
        if error
          t.fail 'model builders request failed ' + pp error
          go error
        else
          t.pass 'got model builders reply'
          parameters = result.model_builders.deeplearning.parameters
          seedParameter = find parameters, (parameter) -> parameter.name is 'seed'
          seedParameter.default_value = seedParameter.actual_value = 'random'
          tdiff t, (readGoldJson 'model-builders-deeplearning.json'), result, exclude: [ 'model_builders.deeplearning.job', 'model_builders.deeplearning.parameters.#.default_value', 'model_builders.deeplearning.parameters.#.actual_value', 'model_builders.deeplearning.parameters.#.required' ]
          go null

    buildAirlinesDeepLearningModel = (go) ->
      parameters = 
        training_frame: 'allyears2k_headers.hex'
        response_column: 'IsDepDelayed'
        reproducible: true
        epochs: 0.01
        hidden: '[10,10]'
        adaptive_rate: false
        rate: 0.05
        activation: 'Tanh'
      _.requestModelBuild 'deeplearning', parameters, (error, result) ->
        if error
          t.fail 'model build request failed ' + pp error
          go error
        else
          t.pass 'got model build reply'
          tdiff t, (readGoldJson 'deeplearning-allyears2k_headers-zip.json'), result, include: [ 'jobs.#.description', 'jobs.#.dest', 'jobs.#.exception' ]
          t.ok (isString result.key.name), 'has job name'
          go null, result.key.name

    inspectAirlinesDeepLearningModel = (modelKey, go) ->
      _.requestInspect modelKey, (error, result) ->
        if error
          t.fail 'model inspect request failed ' + pp error
          go error
        else
          t.pass 'got model inspect reply'
          seedParameter = find result.schema.parameters, (parameter) -> parameter.name is 'seed'
          seedParameter.default_value = seedParameter.actual_value = 'random'
          #tdiff t, (readGoldJson 'inspect-deeplearning-allyears2k_headers-zip.json'), result, exclude: [ 'schema.output.centers', 'schema.output.rows', 'schema.output.mses', 'schema.output.mse', 'schema.output.iterations' ]
          tdiff t, (readGoldJson 'inspect-deeplearning-allyears2k_headers-zip.json'), result, exclude: [ 'key.name', 'schema.key' ]
          go null, modelKey
    
    scoreAirlinesDeepLearningModel = (modelKey, go) ->
      _.requestModelMetrics modelKey, 'allyears2k_headers.hex', (error, result) ->
        if error
          t.fail 'model metrics request failed ' + pp error
          go error
        else
          t.pass 'got model metrics reply'
          metric = result.model_metrics[0]
          parameters = metric.model.parameters
          seedParameter = find parameters, (parameter) -> parameter.name is 'seed'
          seedParameter.default_value = seedParameter.actual_value = 'random'
          t.equal metric.model.key, modelKey
          t.ok (isNumber metric.model_checksum), 'isNumber metric.model_checksum'
          t.ok (isNumber metric.frame_checksum), 'isNumber metric.frame_checksum'
          t.ok (isNumber metric.duration_in_ms), 'isNumber metric.duration_in_ms'
          t.ok (isNumber metric.scoring_time), 'isNumber metric.scoring_time'
          t.ok (isArray metric.auc.thresholds), 'isArray metric.auc.thresholds'
          t.ok (isNumber metric.auc.AUC), 'isNumber metric.auc.AUC'
          t.ok (isNumber metric.auc.Gini), 'isNumber metric.auc.Gini'
          t.ok (isArray metric.auc.confusion_matrices), 'isArray metric.auc.confusion_matrices'
          t.ok (isArray metric.auc.F1), 'isArray metric.auc.F1'
          t.ok (isArray metric.auc.F2), 'isArray metric.auc.F2'
          t.ok (isArray metric.auc.F0point5), 'isArray metric.auc.F0point5'
          t.ok (isArray metric.auc.accuracy), 'isArray metric.auc.accuracy'
          t.ok (isArray metric.auc.errorr), 'isArray metric.auc.errorr'
          t.ok (isArray metric.auc.precision), 'isArray metric.auc.precision'
          t.ok (isArray metric.auc.recall), 'isArray metric.auc.recall'
          t.ok (isArray metric.auc.specificity), 'isArray metric.auc.specificity'
          t.ok (isArray metric.auc.mcc), 'isArray metric.auc.mcc'
          t.ok (isArray metric.auc.max_per_class_error), 'isArray metric.auc.max_per_class_error'

          tdiff t, (readGoldJson 'score-deeplearning-allyears2k_headers-zip.json'), result, exclude: [ 'model_metrics.#.model.key', 'model_metrics.#.model_checksum', 'model_metrics.#.frame_checksum', 'model_metrics.#.duration_in_ms', 'model_metrics.#.scoring_time', 'model_metrics.#.auc.thresholds', 'model_metrics.#.auc.AUC', 'model_metrics.#.auc.Gini' , 'model_metrics.#.auc.confusion_matrices', 'model_metrics.#.auc.F1', 'model_metrics.#.auc.F2', 'model_metrics.#.auc.F0point5', 'model_metrics.#.auc.accuracy', 'model_metrics.#.auc.errorr', 'model_metrics.#.auc.precision', 'model_metrics.#.auc.recall', 'model_metrics.#.auc.specificity', 'model_metrics.#.auc.mcc', 'model_metrics.#.auc.max_per_class_error', "model_metrics.#.auc.threshold_for_criteria", "model_metrics.#.auc.F1_for_criteria", "model_metrics.#.auc.F2_for_criteria", "model_metrics.#.auc.F0point5_for_criteria", "model_metrics.#.auc.accuracy_for_criteria", "model_metrics.#.auc.error_for_criteria", "model_metrics.#.auc.precision_for_criteria", "model_metrics.#.auc.recall_for_criteria", "model_metrics.#.auc.specificity_for_criteria", "model_metrics.#.auc.mcc_for_criteria", "model_metrics.#.auc.max_per_class_error_for_criteria", "model_metrics.#.auc.confusion_matrix_for_criteria" ]
          go null, modelKey

    operations = [
      logAndEcho
      removeAll
      ensureNoFramesExist
      findNonExistentFile
      findAirlines
      importAirlinesFrame
      parseSetupAirlinesFrame
      parseAirlinesFrame
      fetchJobs
      pollJob
      inspectAirlinesFrame
      fetchAirlinesFrame
      fetchKmeansModelBuilder
      buildAirlinesKmeansModel
      pollJob
      inspectAirlinesKmeansModel
      fetchDeepLearningModelBuilder
      buildAirlinesDeepLearningModel
      pollJob
      inspectAirlinesDeepLearningModel
      #scoreAirlinesDeepLearningModel
    ]
    async.waterfall operations, -> go t

  return

###
t_h2o_response = T
  h2o_response:
    h2o: T.str
    node: T.str
    status: T.str
    time: T.num

t_h2o_frame = T
  h2o_frame:
    id: T.str
    key: T.str
    column_names: T.arr T.str
    #TODO handle recursive references in typedef
    #compatible_models: T.arr t_h2o_model
    creation_epoch_time_millis: T.num
    is_raw_frame: T.bool

t_h2o_frames_response = T
  h2o_frames_response:
    response: t_h2o_response
    frames: T.arr t_h2o_frame

t_h2o_model = T
  h2o_model:
    id: T.str
    key: T.str
    model_algorithm: T.str
    model_category: T.str
    state: T.str
    input_column_names: T.arr T.str
    response_column_name: T.str
    critical_parameters: T.obj
    secondary_parameters: T.obj
    expert_parameters: T.obj
    #TODO handle recursive references in typedef
    #compatible_frames: T.arr t_h2o_frame
    creation_epoch_time_millis: T.num

t_h2o_models_response = T
  h2o_models_response:
    response: t_h2o_response
    models: T.arr t_h2o_model

typecheckFramesResult = (t, error, result) ->
  t.equal error, null
  { response, frames } = result
  t.equal response.status, 'done'
  t.ok frames.length > 0
  t.ok typecheck result, t_h2o_frames_response
  t.ok every frames, (frame) ->
    #TODO handle recursive references in typedef
    for compatibleModel in frame.compatible_models
      t.ok typecheck compatibleModel, t_h2o_model
  return

typecheckModelsResult = (t, error, result) ->
  t.equal error, null
  { response, models } = result
  t.equal response.status, 'done'
  t.ok models.length > 0
  t.ok typecheck result, t_h2o_models_response
  t.ok every models, (model) ->
    #TODO handle recursive references in typedef
    for compatibleFrame in model.compatible_frames
      t.ok typecheck compatibleFrame, t_h2o_frame
  return

test 'requestFrames', (t) ->
  _ = Steam.ApplicationContext()
  Steam.Xhr _
  Steam.H2OProxy _
  t.plan 6
  _.requestFrames (error, result) ->
    typecheckFramesResult t, error, result
    { frames } = result
    t.ok every frames, (frame) -> frame.compatible_models.length is 0
    t.end()

test 'requestFrame', (t) ->
  _ = Steam.ApplicationContext()
  Steam.Xhr _
  Steam.H2OProxy _
  t.plan 8
  _.requestFrame 'airlines_train.hex', (error, result) ->
    typecheckFramesResult t, error, result
    { frames } = result
    t.equal frames.length, 1
    frame0 = head frames
    t.ok frame0.key, 'airlines_train.hex'
    t.equal frame0.compatible_models.length, 0
    t.end()

test 'requestFramesAndCompatibleModels', (t) ->
  _ = Steam.ApplicationContext()
  Steam.Xhr _
  Steam.H2OProxy _
  t.plan 23
  _.requestFramesAndCompatibleModels (error, result) ->
    typecheckFramesResult t, error, result
    { frames } = result
    t.ok frames.length > 0
    t.ok every frames, (frame) -> frame.compatible_models.length > 0
    t.end()

test 'requestFrameAndCompatibleModels', (t) ->
  _ = Steam.ApplicationContext()
  Steam.Xhr _
  Steam.H2OProxy _
  t.plan 15
  _.requestFrameAndCompatibleModels 'airlines_train.hex', (error, result) ->
    typecheckFramesResult t, error, result
    { response, frames } = result
    t.equal frames.length, 1
    frame0 = head frames
    t.ok frame0.key, 'airlines_train.hex'
    t.ok frame0.compatible_models.length > 0
    t.end()

test 'requestModels', (t) ->
  _ = Steam.ApplicationContext()
  Steam.Xhr _
  Steam.H2OProxy _
  t.plan 7
  _.requestModels (error, result) ->
    typecheckModelsResult t, error, result
    { models } = result
    t.ok models.length > 0
    t.ok every models, (model) -> model.compatible_frames.length is 0
    t.end()

test 'requestModel', (t) ->
  _ = Steam.ApplicationContext()
  Steam.Xhr _
  Steam.H2OProxy _
  t.plan 8
  _.requestFrameAndCompatibleModels 'airlines_train.hex', (error, result) ->
    { response, frames } = result
    frame0 = head frames
    model0 = head frame0.compatible_models
    modelKey = model0.key
    _.requestModel modelKey, (error, result) ->
      typecheckModelsResult t, error, result
      { response, models } = result
      t.equal models.length, 1
      model = head models
      t.equal model.key, modelKey
      t.equal model.compatible_frames.length, 0 
      t.end()

test 'requestModelsAndCompatibleFrames', (t) ->
  _ = Steam.ApplicationContext()
  Steam.Xhr _
  Steam.H2OProxy _
  t.plan 23
  _.requestModelsAndCompatibleFrames (error, result) ->
    typecheckModelsResult t, error, result
    { models } = result
    t.ok models.length > 0
    t.ok every models, (model) -> model.compatible_frames.length > 0
    t.end()

test 'requestModelAndCompatibleFrames', (t) ->
  _ = Steam.ApplicationContext()
  Steam.Xhr _
  Steam.H2OProxy _
  t.plan 10
  _.requestFrameAndCompatibleModels 'airlines_train.hex', (error, result) ->
    { response, frames } = result
    frame0 = head frames
    model0 = head frame0.compatible_models
    modelKey = model0.key
    _.requestModelAndCompatibleFrames modelKey, (error, result) ->
      typecheckModelsResult t, error, result
      { response, models } = result
      t.equal models.length, 1
      model = head models
      t.equal model.key, modelKey
      t.ok model.compatible_frames.length > 0
      t.end()

test 'FrameListView: should display all frames when no predicate is applied', (t) ->
  _ = createContext()
  frameListView = Steam.FrameListView _

  link$ _.framesLoaded, ->
    t.equal frameListView.items().length, 3, 'frameListView.items() array lengths match'
    t.equal frameListView.items()[0].title, 'airlines_test.hex', 'String frameListView.items()[0].title equals [airlines_test.hex]'
    t.equal frameListView.items()[0].caption, '13 columns: fYear, fMonth, fDayofMonth, fDayOfWeek, DepTime', 'String frameListView.items()[0].caption equals [13 columns: fYear, fMonth, fDayofMonth, fDayOfWeek, DepTime]'
    t.ok (isFunction frameListView.items()[0].display), 'frameListView.items()[0].display is a function'
    t.equal frameListView.items()[0].isActive(), true, 'Boolean frameListView.items()[0].isActive() equals [true]'
    t.equal frameListView.items()[1].title, 'airlines_train.hex', 'String frameListView.items()[1].title equals [airlines_train.hex]'
    t.equal frameListView.items()[1].caption, '13 columns: fYear, fMonth, fDayofMonth, fDayOfWeek, DepTime', 'String frameListView.items()[1].caption equals [13 columns: fYear, fMonth, fDayofMonth, fDayOfWeek, DepTime]'
    t.ok (isFunction frameListView.items()[1].display), 'frameListView.items()[1].display is a function'
    t.equal frameListView.items()[1].isActive(), false, 'Boolean frameListView.items()[1].isActive() equals [false]'
    t.equal frameListView.items()[2].title, 'prostate.hex', 'String frameListView.items()[2].title equals [prostate.hex]'
    t.equal frameListView.items()[2].caption, '9 columns: ID, CAPSULE, AGE, RACE, DPROS', 'String frameListView.items()[2].caption equals [9 columns: ID, CAPSULE, AGE, RACE, DPROS]'
    t.ok (isFunction frameListView.items()[2].display), 'frameListView.items()[2].display is a function'
    t.equal frameListView.items()[2].isActive(), false, 'Boolean frameListView.items()[2].isActive() equals [false]'
    t.equal frameListView.predicateCaption(), 'Showing\nall datasets', 'String frameListView.predicateCaption() equals [Showing\nall datasets]'
    t.ok (isFunction frameListView.clearPredicate), 'frameListView.clearPredicate is a function'
    t.equal frameListView.canClearPredicate(), false, 'Boolean frameListView.canClearPredicate() equals [false]'
    t.equal frameListView.hasItems(), true, 'Boolean frameListView.hasItems() equals [true]'
    t.equal frameListView.template, 'frame-list-view', 'String frameListView.template equals [frame-list-view]'
    t.end()

  _.loadFrames type: 'all'

test 'FrameListView: should display compatible frames when a model predicate is applied', (t) ->
  _ = Steam.ApplicationContext()
  Steam.Xhr _
  Steam.H2OProxy _
  _.requestFrameAndCompatibleModels 'airlines_train.hex', (error, data) ->
    throw Error if error
    frameListView = Steam.FrameListView _
    link$ _.framesLoaded, ->
      t.equal frameListView.items().length, 2, 'frameListView.items() array lengths match'
      t.equal frameListView.items()[0].title, 'airlines_test.hex', 'String frameListView.items()[0].title equals [airlines_test.hex]'
      t.equal frameListView.items()[0].caption, '13 columns: fYear, fMonth, fDayofMonth, fDayOfWeek, DepTime', 'String frameListView.items()[0].caption equals [13 columns: fYear, fMonth, fDayofMonth, fDayOfWeek, DepTime]'
      t.ok (isFunction frameListView.items()[0].display), 'frameListView.items()[0].display is a function'
      t.equal frameListView.items()[0].isActive(), true, 'Boolean frameListView.items()[0].isActive() equals [true]'
      t.equal frameListView.items()[1].title, 'airlines_train.hex', 'String frameListView.items()[1].title equals [airlines_train.hex]'
      t.equal frameListView.items()[1].caption, '13 columns: fYear, fMonth, fDayofMonth, fDayOfWeek, DepTime', 'String frameListView.items()[1].caption equals [13 columns: fYear, fMonth, fDayofMonth, fDayOfWeek, DepTime]'
      t.ok (isFunction frameListView.items()[1].display), 'frameListView.items()[1].display is a function'
      t.equal frameListView.items()[1].isActive(), false, 'Boolean frameListView.items()[1].isActive() equals [false]'
      t.equal frameListView.predicateCaption(), 'Showing datasets compatible with\ndl_AirlinesTrain_binary_1', 'String frameListView.predicateCaption() equals [Showing datasets compatible with\ndl_AirlinesTrain_binary_1]'
      t.ok (isFunction frameListView.clearPredicate), 'frameListView.clearPredicate is a function'
      t.equal frameListView.canClearPredicate(), true, 'Boolean frameListView.canClearPredicate() equals [true]'
      t.equal frameListView.hasItems(), true, 'Boolean frameListView.hasItems() equals [true]'
      t.equal frameListView.template, 'frame-list-view', 'String frameListView.template equals [frame-list-view]'

      t.end()

    frame = head data.frames
    model = head frame.compatible_models
    _.loadFrames type: 'compatibleWithModel', modelKey: model.key

test 'FrameListView: Clicking on a frame item marks it as active and launches the frame', (t) ->
  _ = Steam.ApplicationContext()
  Steam.Xhr _
  Steam.H2OProxy _
  frameListView = Steam.FrameListView _

  t.plan 9

  item0 = item1 = item2 = null
  activeItemData = null

  link$ _.displayFrame, (itemData) ->
    t.ok itemData isnt null, 'launched a frame'
    activeItemData = itemData

  link$ _.framesLoaded, ->
    item0 = frameListView.items()[0]
    item1 = frameListView.items()[1]
    item2 = frameListView.items()[2]

    t.ok activeItemData is item0.data, 'launched frame0'
    t.ok item0.isActive() is yes and item1.isActive() is no and item2.isActive() is no, 'item0 is active'
    
    item1.display() # click on item1
    t.ok activeItemData is item1.data, 'launched frame1'
    t.ok item0.isActive() is no and item1.isActive() is yes and item2.isActive() is no, 'item1 is active'

    _.loadFrames null # switch to frame list from the top level menu while on frames view
    t.ok activeItemData is item1.data, 'launched frame1 again'
    t.ok item0.isActive() is no and item1.isActive() is yes and item2.isActive() is no, 'item1 is active'

  _.loadFrames type: 'all'
###

