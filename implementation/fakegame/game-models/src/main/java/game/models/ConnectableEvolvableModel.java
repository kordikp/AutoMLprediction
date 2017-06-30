package game.models;

import configuration.CfgTemplate;
import game.data.OutputProducer;
import game.evolution.Dna;
import game.evolution.Genome;
import game.evolution.ObjectEvolvable;
import game.models.evolution.EvolvableModel;

import java.util.Vector;


/**
 * Wrapper class for models that can be connected to input data and hierarchically, it contains another wrapper class
 * selecting inputs by a genome
 */
public class ConnectableEvolvableModel extends ConnectableModel implements ObjectEvolvable {


    public void init(CfgTemplate modelConfig, Vector<OutputProducer> inputs, Genome genome) {
        this.inputs = reduceInputsbyGenome(inputs, genome);
        EvolvableModel m = new EvolvableModel();
        m.init(modelConfig);
        m.setDna(genome);
        model = m;
    }

    private Vector<OutputProducer> reduceInputsbyGenome(Vector<OutputProducer> inputs, Genome genome) {
        Vector<OutputProducer> features = new Vector<OutputProducer>();
        for (int i = 0; i < inputs.size(); i++)
            if ((Integer) genome.getGene(i) == 1) features.add(inputs.get(i));
        return features;
    }

    public void init(CfgTemplate modelConfig, Genome dna) {
        ((EvolvableModel) model).init(modelConfig);
        ((EvolvableModel) model).setDna(dna);
    }

    public int compareTo(ObjectEvolvable m) {
        return ((EvolvableModel) model).compareTo(m);
    }

    public Dna getDna() {
        return ((EvolvableModel) model).getDna();
    }

    public void setDna(Dna dna) {
        ((EvolvableModel) model).setDna(dna);
    }

    public double getFitness() {
        return ((EvolvableModel) model).getFitness();
    }

    public void setFitness(double fitness) {
        ((EvolvableModel) model).setFitness(fitness);
    }
}