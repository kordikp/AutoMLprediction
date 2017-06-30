package game.models;


import configuration.CfgTemplate;
import configuration.models.ensemble.BaseModelsDefinition;
import game.data.OutputProducer;

import java.io.Serializable;
import java.util.List;
import java.util.Vector;

import configuration.models.ModelConfig;

/**
 * A layer of ConnectableEvolvableModels uses an evolution strategy to optimize it
 */
public class Layer implements Serializable {
    Vector<ModelConnectable> models;
    Vector<OutputProducer> inputs;
    List<CfgTemplate> baseModelsCfg;
    BaseModelsDefinition baseModelsDef;
    protected int modelsNumber;
    protected int maxLearningVectors;

    public void init(ModelConfig modelConfig, Vector<OutputProducer> inputs) {

    }

    /* protected Vector<ModelConnectable> createInitialPopulation() {


       ModelConfig cfg = (ModelConfig) classNamewithcfg.getCfgBean();
       cfg.setMaxInputsNumber(inputs.size());
       cfg.setMaxLearningVectors(maxLearningVectors);
       ConnectableModel model = new ConnectableModel();

       model.init(classNamewithcfg,inputs);
       ModelLearnable mo = (ModelLearnable)model.getModel();
       model.setName(this.getName());
       for(int j=0;j<num;j++) {
           data.publishVector(j);
           model.storeLearningVector(data.getTargetOutput(cfg.getTargetVariable()));
       }
       mo.learn();
       appendModel(model);
       System.out.println(model.toEquation());
       return true;

       ModelLearnable learnable;
       try {
           Class m = classNamewithcfg.getClassRef();
           learnable = (ModelLearnable) m.newInstance();
           ModelConfig cfg=(ModelConfig) classNamewithcfg.getCfgBean();
           if(cfg.getMaxInputsNumber()==-1) cfg.setMaxInputsNumber(this.inputsNumber);
           if(cfg.getMaxLearningVectors()==-1) cfg.setMaxLearningVectors(this.maxLearningVectors);
           cfg.setTargetVariable(this.targetVariable);
           learnable.init(cfg);
           return learnable;
       } catch (InstantiationException e1) {
           e1.printStackTrace();
       } catch (IllegalAccessException e1) {
           e1.printStackTrace();
       }
       return null;
   }
   /**
    *   Creates ensemble models according to their configuration
    *  - PREDEFINED: all base models have their own configuration bean - added to the list baseModelCfgs
    * - RANDOM: baseModelCfgs contains cfg beans of all models implemented so far. Base models are randomly selected from this list respecting the allowed flag
    * - UNIFORM: baseModelCfgs contains only one configuration bean, all generated models are of the same type
    * - UNIFORM_RANDOM: baseModelCfgs contains cfg beans of all models implemented so far, one cfg bean is randomly selected and all models are of this type
    */
    protected void createBaseModels() {
      /*  MyRandom rndGenerator = new MyRandom(baseModelsCfg.size());
         switch(baseModelsDef) {
            case PREDEFINED:
                  //for(ClassWithConfigBean cfgStruct:listCfg) {
                  for(int i=0;i<modelsNumber;i++) {
                      addModel(i,createBaseModel(baseModelsCfg.get(i)));
                  }
                break;
            case RANDOM:
                  for(int i=0;i<modelsNumber;i++)
                  addModel(i,createBaseModel(baseModelsCfg.get(rndGenerator.nextInt(baseModelsCfg.size()))));
                break;
            case UNIFORM:
                for(int i=0;i<modelsNumber;i++)
                  addModel(i,createBaseModel(baseModelsCfg.get(0)));
                break;
            case UNIFORM_RANDOM:
                int rnd = rndGenerator.nextInt(baseModelsCfg.size());
                for(int i=0;i<modelsNumber;i++)
                  addModel(i,createBaseModel(baseModelsCfg.get(rnd)));
                break;
        }

   */
    }
}
