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
public class If extends GPNode {
    @Override
    public String toString() {
        return "if";
    }

    @Override
    public int expectedChildren() {
        return 3;
    }

    @Override
    public void eval(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual, Problem problem) {
        DoubleData doubleData = new DoubleData();
        children[0].eval(state, thread, doubleData, stack, individual, problem);
        double test = doubleData.val;

        children[1].eval(state, thread, doubleData, stack, individual, problem);
        double trueVal = doubleData.val;

        children[2].eval(state, thread, doubleData, stack, individual, problem);
        double falseVal = doubleData.val;

        ((DoubleData) input).val = test > 0 ? trueVal : falseVal;

    }
}
