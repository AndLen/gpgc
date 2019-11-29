package gp;

/**
 * Created by Andrew on 8/04/2015.
 */
public class AbsSub extends AbstractArithmeticNode {
    @Override
    public String toString() {
        return "|sub|";
    }

    @Override
    protected double performOperation(double child1, double child2) {
        return Math.abs(child1-child2);
    }
}
