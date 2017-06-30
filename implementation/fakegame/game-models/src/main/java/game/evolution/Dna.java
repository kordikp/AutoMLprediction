package game.evolution;

/**
 * This interface represents encoded solution of the problem together with operators to manipulate it
 */
public interface Dna extends java.io.Serializable {

    public Dna[] cross(Dna p1);

    public Object getGene(int index);

    public void setGene(int index, Object gene);

    /**
     * @return returns length of the dna (number of genes)
     */
    public int genes();

    public void setGenesNumber(int genes);

    public void mutate(double probability);

    public double distance(Dna p1);

    public boolean equals(Dna p1);

    public void initializeRandomly();
}
