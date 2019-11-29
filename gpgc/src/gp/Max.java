package gp;

/**
 * Created by Andrew on 8/04/2015.
 */
public class Max extends AbstractArithmeticNode {
    @Override
    public String toString() {
        return "max";
    }

    @Override
    protected double performOperation(double child1, double child2) {
        return Math.max(child1,child2);
    }
}
