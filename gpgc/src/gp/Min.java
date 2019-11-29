package gp;

/**
 * Created by Andrew on 8/04/2015.
 */
public class Min extends AbstractArithmeticNode {
    @Override
    public String toString() {
        return "min";
    }

    @Override
    protected double performOperation(double child1, double child2) {
        return Math.min(child1,child2);
    }
}
