package gp.locum;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.*;
import gp.DoubleData;
import gp.GPUtils;

/**
 * Created by lensenandr on 5/07/16.
 */
public class LocumMaxFeatureNode extends ERC {
    private int val;

    @Override
    public String toString() {
        return "maxT F" + val;
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
        if (node instanceof LocumMaxFeatureNode) {
            LocumMaxFeatureNode fvN = (LocumMaxFeatureNode) node;
            return fvN.val == val;
        } else return false;
    }

    @Override
    public void eval(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual, Problem problem) {
        DoubleData doubleDataPair = (DoubleData) input;
        LocumClusteringProblem locumProblem = (LocumClusteringProblem) problem;
        doubleDataPair.val = Math.max(locumProblem.instance1.getFeatureValue(this.val), locumProblem.instance2.getFeatureValue(this.val));

    }

    public int nodeHashCode() {
        // a reasonable hash code
        return this.getClass().hashCode() + val;
    }


}