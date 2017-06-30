package hex.fakegame;

import game.classifiers.Classifier;

import game.configuration.Configurable;
import hex.*;
import org.apache.commons.collections.ArrayStack;
import org.apache.commons.lang.NotImplementedException;
import water.H2O;
import water.Key;
import water.util.ArrayUtils;
import water.util.Log;

import java.util.*;

public class FakeGameModel extends Model<FakeGameModel, FakeGameModel.FakeGameParameters, FakeGameModel.FakeGameOutput> {

    public static class FakeGameParameters extends Model.Parameters {
        @Override
        public String algoName() {
            return "FakeGame";
        }

        @Override
        public String fullName() {
            return "FakeGame";
        }

        @Override
        public String javaName() {
            return FakeGameModel.class.getName();
        }

        @Override
        protected boolean defaultDropConsCols() { return false; }

        @Override
        public long progressUnits() {
            return 1;
        }

        public String _model_config;
    }

    public static class FakeGameOutput extends Model.Output {

        public LinkedList<Configurable> _models;

        public FakeGameOutput(FakeGame b) {
            super(b);
        }

        @Override
        public ModelCategory getModelCategory() {
            if (isClassifier()) {
                return (nclasses() > 2) ? ModelCategory.Multinomial : ModelCategory.Binomial;
            }
            return ModelCategory.Regression;
        }

        @Override
        public boolean isSupervised() {
            return true;
        }
    }

    FakeGameModel(Key selfKey, FakeGameParameters parms, FakeGameOutput output) {
        super(selfKey, parms, output);
    }



    @Override
    public ModelMetrics.MetricBuilder makeMetricBuilder(String[] domain) {
        switch (_output.getModelCategory()) {
            case Binomial:
                return new ModelMetricsBinomial.MetricBuilderBinomial(domain);
            case Multinomial:
                return new ModelMetricsMultinomial.MetricBuilderMultinomial(domain.length, domain);
            default:
                throw H2O.unimpl();
        }
    }

    private double median(double[] ds){
        Arrays.sort(ds);
        if (ds.length % 2 == 1){
            return ds[ds.length/2];
        } else {
            return (ds[ds.length/2]+ds[ds.length/2 - 1])/2.0;
        }
    }

    private double mean(double[] ds){
        double sum = 0.0;
        for (double d : ds){
            sum += d;
        }
        return sum/ds.length;
    }

    @Override
    protected double[] score0(double data[/*ncols*/], double preds[/*nclasses+1*/]) {
       // System.out.println("before clone");
        //Log.info("SCORING >> "+data.length+" cols with ensemble of "+_output._models.size() +" models");
        LinkedList<Configurable> models = (LinkedList<Configurable>) _output._models.clone();
       // System.out.println("after clone");

        for (int i = 0; i < _output.nclasses() + 1; i++) {
            preds[i] = 0.0;
        }


        if (_output.isClassifier()) {
        //    System.out.println("IsClassifier<< >>"+_output+" << >> "+models+" << >> "+models.size());
            for (Configurable m : models) {
                Classifier c = (Classifier) m;
                preds[1 +  c.getOutput(data)] += 1.0 /models.size();
 //               double d[] =c.getOutputProbabilities(data);
//                for (int i =0; i<d.length; i++){
//                    System.out.println("<"+i+"> = "+d[i]);
//                }
//                preds[1 + ArrayUtils.maxIndex(d)] += 1.0 / _output._models.size();
            }
            //find maximum
            preds[0] = ArrayUtils.maxIndex(preds);

        //    for (int i=0; i < preds.length; i++){
        //        System.out.println("<"+i+">  =  "+preds[i]);
        //    }
            return preds;
        } else {
            double ds[] = new double[models.size()];
            int idx = 0;
            for (Configurable c : models) {
                game.models.Model m = (game.models.Model) c;
                ds[idx] = m.getOutput(data);
                idx ++;
            }
            //FIXME: Mean, median? how to choose best regression
            preds[0] = mean(ds);
            return preds;
        }
    }

}
