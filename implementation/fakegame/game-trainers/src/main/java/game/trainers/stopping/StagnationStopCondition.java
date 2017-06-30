package game.trainers.stopping;

public class StagnationStopCondition implements java.io.Serializable {
    protected double prevfx;
    protected int stagnation;
    protected boolean result;

    public int getMaxStagnation() {
        return maxStagnation;
    }

    public void setMaxStagnation(int maxStagnation) {
        this.maxStagnation = maxStagnation;
    }

    int maxStagnation;

    public StagnationStopCondition(int maxStagnation) {
        this.maxStagnation = maxStagnation;
    }

    public void init(double ofx) {
        prevfx = ofx;
        stagnation = 0;
    }

    public void set(double ofx) {
        if (ofx == prevfx) stagnation++;
        else {
            prevfx = ofx;
            stagnation = 0;
        }

        result = (stagnation > maxStagnation);
    }

    public boolean stop() {
        return result;
    }
}
