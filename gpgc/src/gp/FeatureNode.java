package gp;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.*;

/**
 * Created by lensenandr on 5/07/16.
 */
public class FeatureNode extends ERC {
    private int val;

    public int getVal() {
        return val;
    }

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
        if (node instanceof FeatureNode) {
            FeatureNode fvN = (FeatureNode) node;
            return fvN.val == val;
        } else return false;
    }

    @Override
    public void eval(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual, Problem problem) {
        ((DoubleData) input).val = ((ClusteringProblem) problem).currentInstance.getFeatureValue(this.val);
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