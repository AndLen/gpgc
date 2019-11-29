package gp.locum;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.*;
import gp.DoubleData;
import gp.GPUtils;

/**
 * Created by lensenandr on 5/07/16.
 */
public class LocumDivFeatureNode extends ERC {
    private int val;

    @Override
    public String toString() {
        return "divT F" + val;
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
        if (node instanceof LocumDivFeatureNode) {
            LocumDivFeatureNode fvN = (LocumDivFeatureNode) node;
            return fvN.val == val;
        } else return false;
    }

    @Override
    public void eval(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual, Problem problem) {
        DoubleData doubleDataPair = (DoubleData) input;
        LocumClusteringProblem locumProblem = (LocumClusteringProblem) problem;
        double child1 = locumProblem.instance1.getFeatureValue(val);
        double child2 = locumProblem.instance2.getFeatureValue(val);
        doubleDataPair.val = child2 == 0 ? 1 : child1 / child2;

    }

    public int nodeHashCode() {
        // a reasonable hash code
        return this.getClass().hashCode() + val;
    }


}