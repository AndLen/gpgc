package gp;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;

/**
 * Created by Andrew on 8/04/2015.
 */
public abstract class AbstractArithmeticNode extends GPNode {
    @Override
    public int expectedChildren() {
        return 2;
    }

    @Override
    public abstract String toString();

    @Override
    public void eval(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual, Problem problem) {
        DoubleData doubleData = new DoubleData();
        children[0].eval(state, thread, doubleData, stack, individual, problem);
        double child1 = doubleData.val;

        children[1].eval(state, thread, doubleData, stack, individual, problem);
        double child2 = doubleData.val;
        ((DoubleData) input).val = performOperation(child1, child2);
    }

    protected abstract double performOperation(double child1, double child2);
}
