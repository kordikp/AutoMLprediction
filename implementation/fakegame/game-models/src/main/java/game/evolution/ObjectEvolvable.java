package game.evolution;


/**
 * This interface represents objects that can be evolved.
 */
public interface ObjectEvolvable extends Comparable<ObjectEvolvable> {

    /**
     * Each evolvable object posses a genome that contains its "DNA"
     * @param objectConfig configuration template
     */
    // public void init(ClassWithConfigBean objectConfig);

    /**
     * Objects can be compared base on their fitness
     *
     * @param m rival model
     * @return -1 if this.fitness<m.fitness , 1 otherwise
     */
    public int compareTo(ObjectEvolvable m);

    public Dna getDna();

    public void setDna(Dna dna);

    public double getFitness();

    public void setFitness(double fitness);

}