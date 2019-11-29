package gp.locum;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.*;
import gp.GPUtils;

/**
 * Created by lensenandr on 5/07/16.
 */
public class LocumSameFeatureNode extends ERC {
    private int val;

    @Override
    public String toString() {
        return "F" + val;
    }

    @Override
    public String encode() {
        return toString();
    }

    @Override
    public int expectedChildren() {
        return 0;
    }

    @Override
    public void resetNode(EvolutionState state, int thread) {
        val = state.random[thread].nextInt(GPUtils.numFeatures);
    }


    @Override
    public boolean nodeEquals(GPNode node) {
        if (node instanceof LocumSameFeatureNode) {
            LocumSameFeatureNode fvN = (LocumSameFeatureNode) node;
            return fvN.val == val;
        } else return false;
    }

    @Override
    public void eval(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual, Problem problem) {
        DoubleDataPair doubleDataPair = (DoubleDataPair) input;
        LocumClusteringProblem locumProblem = (LocumClusteringProblem) problem;
        doubleDataPair.val1 = locumProblem.instance1.getFeatureValue(this.val);
        doubleDataPair.val2 = locumProblem.instance2.getFeatureValue(this.val);

    }

    public int nodeHashCode() {
        // a reasonable hash code
        return this.getClass().hashCode() + val;
    }


}