/**
 * @author Pavel Kordik
 * @version 0.90
 */
package game.evolution;

/**
 * This class is used to encode inputs of models in ensemble.
 * The topology is encoded in "chromosome" with binary representation of inputs utilised by a model.
 */
public class Genome implements Dna {
    private int[] x;


    private int maxInputsSetOne;
    private int inputsPossible;
    static java.util.Random rnd = new java.util.Random();

    /**
     * @param allInputsNumber The number of all inputs available.
     * @param maxInputsSetOne The maximum number of unit's inputs (max number of ones in the chromosome).
     */
    public Genome(int allInputsNumber, int maxInputsSetOne) {
        x = new int[allInputsNumber];
        for (int i = 0; i < allInputsNumber; i++)
            x[i] = 0;
        inputsPossible = allInputsNumber;
        this.maxInputsSetOne = maxInputsSetOne;
    }

    /**
     * @return Maximal number of ones in the chromosome.
     */
    public int getMaxInputsSetOne() {
        return maxInputsSetOne;
    }

    /**
     * @param pos Locus in the chromosome.
     * @param val The value to store in the locus.
     */
    public void setGene(int pos, Object val) {
        x[pos] = (Integer) val;
    }

    /**
     * @param pos Position of the locus in the chromosome.
     * @return The value stored in the locus given by position pos.
     */
    public Object getGene(int pos) {
        return x[pos];
    }

    /**
     * @return Counts number of inputs (ones in the chromosome)
     */
    public int countInputs() {
        int sum = 0;
        for (int i = 0; i < inputsPossible; i++)
            if (x[i] == 1) sum++;
        return sum;
    }

    /**
     * Corrects number of inputs (if less or more than allowed)
     */
    void correctInputs() {
        int inp = countInputs();
        while ((inp > maxInputsSetOne) || (inp < 1)) {
            if (inp > maxInputsSetOne) {
                int dir = rnd.nextInt(2) == 0 ? -1 : 1;
                int sp = rnd.nextInt(inputsPossible);
                while (x[sp] != 1) {
                    sp += dir;
                    sp %= inputsPossible;
                    if (sp < 0) sp = inputsPossible - 1;
                }
                x[sp] = 0;
                inp--;
            } else if (inp < 1) {
                inp = 1;
                setGene(rnd.nextInt(inputsPossible), 1);
            }
        }
    }

    /**
     * Crosses this chromosome with the one given by p1
     *
     * @param p1 parent 2
     * @return two individuals (childrens) of p1 and this
     */
    public Dna[] cross(Dna p1) {
        Genome[] c = new Genome[2];
        c[0] = new Genome(inputsPossible, this.getMaxInputsSetOne());
        c[1] = new Genome(inputsPossible, ((Genome) p1).getMaxInputsSetOne());
        int r = rnd.nextInt(inputsPossible);
        for (int i = 0; i < inputsPossible; i++) {
            if (i < r) {
                c[0].setGene(i, this.getGene(i));
                c[1].setGene(i, p1.getGene(i));
            } else {
                c[0].setGene(i, p1.getGene(i));
                c[1].setGene(i, this.getGene(i));
            }
        }
        c[0].correctInputs();
        c[1].correctInputs();
        return c;
    }

    /**
     * Mutates this chromosome
     *
     * @param probability The probability that mutation occurs (1 = 100%)
     */
    public void mutate(double probability) {
        for (int i = 0; i < inputsPossible; i++) {
            if (rnd.nextDouble() < probability) {
                setGene(i, rnd.nextInt(2));

            }

        }
        correctInputs();
    }

    /**
     * Counts the distance of this and p1 genotypes.
     *
     * @param p1 The second chromosome.
     * @return The distance computed from the difference of chromosomes.
     */
    public double distance(Dna p1) {
        int sum = 0;
        for (int i = 0; i < inputsPossible; i++)
            if (this.getGene(i) != p1.getGene(i)) sum++;
        return sum;
    }

    /**
     * Returns the string representing binary encoded chromosome
     *
     * @see Object#toString()
     */
    public String toString() {
        String s = "";
        for (int i = 0; i < inputsPossible; i++)
            s = s + ((x[i] == 0) ? "0" : "1");
        return s;
    }

    /**
     * Compares two chromosomes
     *
     * @param p1 The chromosome to compare with this
     * @return True if both chromosomes have the same values in corresponding loci.
     */
    public boolean equals(Dna p1) {
        for (int i = 0; i < inputsPossible; i++)
            if (this.getGene(i) != p1.getGene(i)) return false;
        return true;
    }

    /**
     * Initializes all inputs to 0 or 1, then randomly clears chromosomes unit the maxInputsSetToOne
     */
    public void initializeRandomly() {
        for (int i = 0; i < inputsPossible; i++) x[i] = rnd.nextInt(2);
        correctInputs();
    }

    /**
     * Gets the number of all inputs available.
     *
     * @return The number of all inputs available.
     */
    public int genes() {
        return inputsPossible;
    }

    /**
     * Sets the number of all inputs available.
     *
     * @param genes
     */
    public void setGenesNumber(int genes) {
        this.inputsPossible = genes;
    }
}