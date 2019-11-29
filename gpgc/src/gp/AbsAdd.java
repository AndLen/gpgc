package gp;

/**
 * Created by Andrew on 8/04/2015.
 */
public class AbsAdd extends AbstractArithmeticNode {
    @Override
    public String toString() {
        return "|add|";
    }

    @Override
    protected double performOperation(double child1, double child2) {
        return Math.abs(child1+child2);
    }
}