import h2o
import Benchmarker.experiment as e
import Benchmarker.model_config as m
import yaml


h2o.init("127.0.0.1", 54321, False)

x = ['C'+str(i) for i in range(1,5)]

exp = e.Experiment("Iris",filename="/home/frydatom/Sync/School/Diplomka/implementace/h2o/iris.data",
                   x=x, y='C5', train_ratio=0.6, validation_ratio=0.3, subsets= {
                "min_fraction_denom":1,
                "max_fraction_denom": 3,
                "number_of_samples": 5
            })

mod = m.ModelConfig("deeplearning",h2o.H2ODeepLearningEstimator,{})
exp.add("deeplearning", mod)

mod2 = m.ModelConfig("gbm", h2o.H2OGradientBoostingEstimator, {"ntrees":[10, 100, 1000, 10000]})
exp.add("gbm",mod2)

mod3 = m.ModelConfig("rf", h2o.H2ORandomForestEstimator, None)
exp.add("rf", mod3)

try: # Needed in case something goes wrong so h2o can clean it up
    results = exp.execute()
    #results.to_csv("/tmp/some_dir/{}-data.csv".format(exp.name)) # In case we want to save measured data
    #exp.plot("/tmp/some_dir", result) # In case we want to save some plots
except e:
    print(e)


print(yaml.dump(exp)) # In case we want to print out configuration in YAML
