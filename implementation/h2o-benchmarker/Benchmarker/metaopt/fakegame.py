
def switch(options, selected):
    return "\n          ".join(["<boolean>{}</boolean>".format("true" if x == selected else "false") for x in options])

def backPropagation(hidden_l1=5, hidden_l2=0, trainingCycles=600, acceptableError=0.0, learningRate=0.2,
                    momentum = 0.3, activationFunction = "sigmoid"):
    return """
<configuration.classifiers.single.ClassifierModelConfig>
  <classRef>game.classifiers.single.ClassifierModel</classRef>
  <description>BackPropagation classifier</description>
  <maxLearningVectors>-1</maxLearningVectors>
  <maxInputsNumber>-1</maxInputsNumber>
  <baseModelsDef>UNIFORM</baseModelsDef>
  <baseModelCfgs>
    <configuration.models.single.neural.BackPropagationModelConfig>
      <classRef>game.models.single.neural.BackPropagationModel</classRef>
      <maxLearningVectors>-1</maxLearningVectors>
      <maxInputsNumber>-1</maxInputsNumber>
      <firstLayerNeurons>{hidden_l1}</firstLayerNeurons>
      <secondLayerNeurons>{hidden_l2}</secondLayerNeurons>
      <trainingCycles>{trainingCycles}</trainingCycles>
      <acceptableError>{acceptableError}</acceptableError>
      <activationFunction>
        <elements class="string-array">
          <string>sigmoid</string>
          <string>sigmoid_offset</string>
        </elements>
        <elementEnabled>
          {activationFunction}
        </elementEnabled>
      </activationFunction>
      <learningRate>{learningRate}</learningRate>
      <momentum>{momentum}</momentum>
    </configuration.models.single.neural.BackPropagationModelConfig>
  </baseModelCfgs>
</configuration.classifiers.single.ClassifierModelConfig>
    """.format(hidden_l1 = hidden_l1, hidden_l2 = hidden_l2, trainingCycles = trainingCycles,
               acceptableError = acceptableError, learningRate = learningRate, momentum = momentum,
               activationFunction=switch(["sigmoid", "sigmoid_offset"], activationFunction))


def cascadeCorrelation(acceptableError = 0.001, maxLayersNumber = 5, candNumber = 1, usedAlg = "Rprop",
                       activationFunction="sigmoid_offset"):
    return """
<configuration.classifiers.single.ClassifierModelConfig>
  <classRef>game.classifiers.single.ClassifierModel</classRef>
  <description>Cascade Correlation classifier</description>
  <maxLearningVectors>-1</maxLearningVectors>
  <maxInputsNumber>-1</maxInputsNumber>
  <baseModelsDef>UNIFORM</baseModelsDef>
  <baseModelCfgs>
    <configuration.models.single.neural.CascadeCorrelationModelConfig>
      <classRef>game.models.single.neural.CascadeCorrelationModel</classRef>
      <maxLearningVectors>-1</maxLearningVectors>
      <maxInputsNumber>-1</maxInputsNumber>
      <acceptableError>{acceptableError}</acceptableError>
      <maxLayersNumber>{maxLayersNumber}</maxLayersNumber>
      <candNumber>{candNumber}</candNumber>
      <usedAlg>
        <elements class="string-array">
          <string>Quickprop</string>
          <string>Rprop</string>
        </elements>
        <elementEnabled>
          {usedAlg}
        </elementEnabled>
      </usedAlg>
      <activationFunction>
        <elements class="string-array">
          <string>sigmoid</string>
          <string>sigmoid_offset</string>
          <string>symmetric_sigmoid</string>
        </elements>
        <elementEnabled>
          {activationFunction}
        </elementEnabled>
      </activationFunction>
    </configuration.models.single.neural.CascadeCorrelationModelConfig>
  </baseModelCfgs>
</configuration.classifiers.single.ClassifierModelConfig>
""".format(acceptableError = acceptableError, maxLayersNumber = maxLayersNumber, candNumber = candNumber,
           usedAlg=switch(["Quickprop", "Rprop"], usedAlg),
           activationFunction=switch(["sigmoid", "sigmoid_offset", "symmetric_sigmoid"], activationFunction))



def quickProp(hidden_l1 = 5, hidden_l2 = 0, trainingCycles=600, acceptableError=0.0, activationFunction="sigmoid",
              maxGrowthFactor=2.0, epsilon = 7.0e-4, splitEpsilon=False):
    return """
<configuration.classifiers.single.ClassifierModelConfig>
  <classRef>game.classifiers.single.ClassifierModel</classRef>
  <description>QuickProp classifier</description>
  <maxLearningVectors>-1</maxLearningVectors>
  <maxInputsNumber>-1</maxInputsNumber>
  <baseModelsDef>UNIFORM</baseModelsDef>
  <baseModelCfgs>
    <configuration.models.single.neural.QuickpropModelConfig>
      <classRef>game.models.single.neural.QuickpropModel</classRef>
      <maxLearningVectors>-1</maxLearningVectors>
      <maxInputsNumber>-1</maxInputsNumber>
      <firstLayerNeurons>{hidden_l1}</firstLayerNeurons>
      <secondLayerNeurons>{hidden_l2}</secondLayerNeurons>
      <trainingCycles>{trainingCycles}</trainingCycles>
      <acceptableError>{acceptableError}</acceptableError>
      <activationFunction>
        <elements class="string-array">
          <string>sigmoid</string>
          <string>sigmoid_offset</string>
          <string>symmetric_sigmoid</string>
        </elements>
        <elementEnabled>
          {activationFunction}
        </elementEnabled>
      </activationFunction>
      <maxGrowthFactor>{maxGrowthFactor}</maxGrowthFactor>
      <epsilon>{epsilon}</epsilon>
      <splitEpsilon>{splitEpsilon}</splitEpsilon>
    </configuration.models.single.neural.QuickpropModelConfig>
  </baseModelCfgs>
</configuration.classifiers.single.ClassifierModelConfig>
    """.format(hidden_l1 = hidden_l1, hidden_l2 = hidden_l2, trainingCycles=trainingCycles,
               acceptableError=acceptableError, activationFunction=switch(["sigmoid", "sigmoid_offset",
                                                                            "symmetric_sigmoid"], activationFunction),
               maxGrowthFactor=maxGrowthFactor, epsilon=epsilon, splitEpsilon = splitEpsilon)


def rProp(hidden_l1 = 5, hidden_l2 = 0, trainingCycles=600, acceptableError=0.0, activationFunction="sigmoid",
          etaMinus=0.5, etaPlus=1.2):
    return """
<configuration.classifiers.single.ClassifierModelConfig>
  <classRef>game.classifiers.single.ClassifierModel</classRef>
  <description>Rprop classifier</description>
  <maxLearningVectors>-1</maxLearningVectors>
  <maxInputsNumber>-1</maxInputsNumber>
  <baseModelsDef>UNIFORM</baseModelsDef>
  <baseModelCfgs>
    <configuration.models.single.neural.RpropModelConfig>
      <classRef>game.models.single.neural.RpropModel</classRef>
      <maxLearningVectors>-1</maxLearningVectors>
      <maxInputsNumber>-1</maxInputsNumber>
      <firstLayerNeurons>{hidden_l1}</firstLayerNeurons>
      <secondLayerNeurons>{hidden_l2}</secondLayerNeurons>
      <trainingCycles>{trainingCycles}</trainingCycles>
      <acceptableError>{acceptableError}</acceptableError>
      <activationFunction>
        <elements class="string-array">
          <string>sigmoid</string>
          <string>sigmoid_offset</string>
          <string>symmetric_sigmoid</string>
        </elements>
        <elementEnabled>
          {activationFunction}
        </elementEnabled>
      </activationFunction>
      <etaMinus>{etaMinus}</etaMinus>
      <etaPlus>{etaPlus}</etaPlus>
    </configuration.models.single.neural.RpropModelConfig>
  </baseModelCfgs>
</configuration.classifiers.single.ClassifierModelConfig>
    """.format(hidden_l1 = hidden_l1, hidden_l2 = hidden_l2, trainingCycles=trainingCycles,
               acceptableError=acceptableError, activationFunction=switch(["sigmoid", "sigmoid_offset",
                                                                            "symmetric_sigmoid"], activationFunction),
               etaMinus=etaMinus, etaPlus=etaPlus)