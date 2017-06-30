package hex.fakegame;

import configuration.CfgTemplate;
import configuration.ConfigurationFactory;
import game.classifiers.Classifier;
import game.classifiers.ClassifierFactory;
import game.configuration.Configurable;
import game.data.ArrayGameData;
import game.models.Model;
import game.models.ModelFactory;
import hex.DataInfo;
import hex.ModelBuilder;
import hex.ModelCategory;
import hex.ModelMetrics;
import hex.fakegame.FakeGameModel.FakeGameOutput;
import hex.fakegame.FakeGameModel.FakeGameParameters;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.NotImplementedException;
import water.MRTask;
import water.Scope;
import water.fvec.Chunk;
import water.util.Log;

import java.io.StringReader;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public class FakeGame extends ModelBuilder<FakeGameModel, FakeGameParameters, FakeGameOutput> {
    @Override
    public boolean isSupervised() {
        return true;
    }

    @Override
    public ModelCategory[] can_build() {
        return new ModelCategory[]{
                ModelCategory.Regression,
                ModelCategory.Binomial,
                ModelCategory.Multinomial
        };
    }


    @Override
    public BuilderVisibility builderVisibility() {
        return BuilderVisibility.Stable;
    }


    // Called from Nano thread; start the FakeGame Job on a F/J thread
    public FakeGame(boolean startup_once) {
        super(new FakeGameParameters(), startup_once);
    }

    public FakeGame(FakeGameParameters parms) {
        super(parms);
        init(false);
    }

    @Override
    protected FakeGameDriver trainModelImpl() {
        return new FakeGameDriver();
    }

    @Override
    public void init(boolean expensive) {
        super.init(expensive);
    }

    // ----------------------
    private class FakeGameDriver extends Driver {
        @Override
        public void computeImpl() {
            FakeGameModel model = null;
            try {
                Scope.enter();
                _parms.read_lock_frames(_job); // Fetch & read-lock source frame
                init(true);

                // The model to be built
                model = new FakeGameModel(_job._result, _parms, new FakeGameOutput(FakeGame.this));
                model.delete_and_lock(_job);


                String[] names = _train._names;
                int resp_col = 0;
                for (int i = 0; i < names.length; i++) {
                //    System.out.println("<" + i + "> name = " + names[i]);
                    if (_parms._response_column.equals(names[i])) {
                        resp_col = i;
                        break;
                    }
                }

                _job.update(1, "Training models");       // One unit of work
                LinkedList<Configurable> models = (new FakeGameLearner(resp_col, isClassifier(), _parms._model_config)).doAll(_train)._lfg;
                //Log.info("After doAll cls contains " + models.size() + " elements");
                // Fill in the model
                model._output._models = models;
                //System.out.println("after set model");
                model.update(_job);   // Update model in K/V store
                //System.out.println("after update job");

                //System.out.println("after updating by 1 job");

                _job.update(1, "Scoring and computing metrics on training data");

                model.score(_parms.train()).delete(); // This scores on the training data and appends a ModelMetrics
                model._output._training_metrics = ModelMetrics.getFromDKV(model, _parms.train());

                _job.update(1, "Scoring and computing metrics on validation data");

                if (_valid != null) {
                    model.score(_parms.valid()).delete(); //this appends a ModelMetrics on the validation set
                    model._output._validation_metrics = ModelMetrics.getFromDKV(model, _parms.valid());
                }

            } finally {
                if (model != null) model.unlock(_job);
                _parms.read_unlock_frames(_job);
                Scope.exit(model == null ? null : model._key);
            }
            //System.out.println("after finally");
            //tryComplete();
            //System.out.println("after tryComplete");
        }


    }


    private static class FakeGameLearner extends MRTask<FakeGameLearner> {
        // IN
        int resp_col;
        boolean isClassifier;
        String classifier_config;
        // OUT
        LinkedList<Configurable> _lfg;

        FakeGameLearner(int resp_col, boolean isClassifier, String classifier_cfg) {
            this.resp_col = resp_col;
            this.isClassifier = isClassifier;
            this.classifier_config = classifier_cfg;
            _lfg = new LinkedList<Configurable>();
        }

        @Override
        public void map(Chunk[] cs) {
            if (cs.length == 0) {
                System.out.println("Chunk size == 0");
                return;
            } else if (cs[0]._len == 0) {
                System.out.println("Chunk row size == 0");
                return;
            }

            double[][] inputVect = new double[cs[0]._len][cs.length - 1];
            double[][] target;
            if (isClassifier) {
                target = new double[cs[0]._len][cs[resp_col].vec().cardinality()];
            } else {
                target = new double[cs[0]._len][1];
            }

            for (int col = 0; col < resp_col; col++)
                for (int row = 0; row < cs[col]._len; row++)
                    inputVect[row][col] = cs[col].atd(row);

            if (isClassifier) {
                for (int row = 0; row < cs[resp_col]._len; row++)
                    target[row][(int) cs[resp_col].at8(row)] = 1;
            } else {
                for (int row = 0; row < cs[resp_col]._len; row++)
                    target[row][0] = cs[resp_col].atd(row);
            }

            for (int col = resp_col + 1; col < cs.length; col++)
                for (int row = 0; row < cs[col]._len; row++)
                    inputVect[row][col] = cs[col].atd(row);

//            if (cs.length >= 0) {
//                Log.info("Creating ArrayGameData with " + inputVect.length + "x" + inputVect[0].length + " data and 1x" +
//                        target.length + " target");
//            }
            ArrayGameData data = new ArrayGameData(inputVect, target);

            if (isClassifier) {
                StringReader sr = new StringReader(classifier_config);
                CfgTemplate cfg = ConfigurationFactory.readConfiguration(sr);
                Classifier c = ClassifierFactory.createNewClassifier(cfg, data, true);
                _lfg.add(c);
            } else {
                StringReader sr = new StringReader(classifier_config);
                CfgTemplate cfg = ConfigurationFactory.readConfiguration(sr);
                Model m = ModelFactory.createNewConnectableModel(cfg, data, true);
                _lfg.add(m);
            }
        }

        @Override
        public void reduce(FakeGameLearner mrt) {
       //     Log.info("FakeGame attempting to reduce " + _lfg.size() + " + " + mrt._lfg.size());
            if (mrt._lfg.size() > 0 && this._lfg != mrt._lfg) {
       //         Log.info("FakeGame reducing " + _lfg.size() + " + " + mrt._lfg.size());
                _lfg.addAll(mrt._lfg);
            }
        }
    }
}
