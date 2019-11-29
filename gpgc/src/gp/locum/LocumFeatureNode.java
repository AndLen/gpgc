package gp.locum;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.*;
import gp.DoubleData;
import gp.GPUtils;

/**
 * Created by lensenandr on 5/07/16.
 */
public class LocumFeatureNode extends ERC {
    public int val;

    @Override
    public String toString() {
        return String.format("I%dF%d", val / GPUtils.numFeatures, getFeatureVal());
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
        val = state.random[thread].nextInt(GPUtils.numFeatures * 2);
    }


    @Override
    public boolean nodeEquals(GPNode node) {
        if (node instanceof LocumFeatureNode) {
            LocumFeatureNode fvN = (LocumFeatureNode) node;
            return fvN.val == val;
        } else return false;
    }

    @Override
    public void eval(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual, Problem problem) {
        boolean firstInstance = isFirstInstance();
        int featureVal = getFeatureVal();
        // System.out.println(firstInstance + " " + featureVal);
        LocumClusteringProblem problem1 = (LocumClusteringProblem) problem;
        ((DoubleData) input).val = firstInstance ? problem1.instance1.getFeatureValue(featureVal) : problem1.instance2.getFeatureValue(featureVal);
    }

    public int getFeatureVal() {
        return val % GPUtils.numFeatures;
    }

    public boolean isFirstInstance() {
        return val / GPUtils.numFeatures == 0;
    }

    public int nodeHashCode() {
        // a reasonable hash code
        return this.getClass().hashCode() + val;
    }
//    public GPNode clone(){
//        FeatureNode fn = (FeatureNode) super.clone();
//        fn.val = val;
//        return fn;
//    }

}