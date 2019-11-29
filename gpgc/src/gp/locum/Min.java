package gp.locum;

/**
 * Created by Andrew on 8/04/2015.
 */
public class Min extends AbstractLocumArithmeticNode {
    @Override
    public String toString() {
        return "min";
    }

    @Override
    protected double performOperation(double child1, double child2) {
        return Math.min(child1, child2);
    }
}
