package gp.locum;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import gp.DoubleData;

/**
 * Created by Andrew on 8/04/2015.
 */
public abstract class AbstractLocumArithmeticNode extends GPNode {
    @Override
    public int expectedChildren() {
        return 1;
    }

    @Override
    public abstract String toString();

    @Override
    public void eval(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual, Problem problem) {
        DoubleDataPair doubleData = new DoubleDataPair();
        children[0].eval(state, thread, doubleData, stack, individual, problem);
        double child1 = doubleData.val1;
        double child2 = doubleData.val2;
        ((DoubleData) input).val = performOperation(child1, child2);
    }

    protected abstract double performOperation(double child1, double child2);
}
