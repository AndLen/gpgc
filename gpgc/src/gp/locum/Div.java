package gp.locum;

/**
 * Created by Andrew on 8/04/2015.
 */
public class Div extends AbstractLocumArithmeticNode {
    @Override
    public String toString() {
        return "div";
    }

    @Override
    protected double performOperation(double child1, double child2) {
        return child2 == 0 ? 1 : child1 / child2;
    }
}
