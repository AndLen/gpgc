package gp.locum.advanced;

import data.Instance;
import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import gp.DoubleData;
import gp.locum.LocumClusteringProblem;

/**
 * Created by Andrew on 8/04/2015.
 */
public abstract class DistanceTerminal extends GPNode {

    public abstract String toString();

    @Override
    public int expectedChildren() {
        return 0;
    }

    @Override
    public void eval(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual, Problem problem) {

        LocumClusteringProblem problem1 = (LocumClusteringProblem) problem;
        ((DoubleData) input).val = getDistance(problem1.instance1, problem1.instance2);


    }

    public abstract double getDistance(Instance instance1, Instance instance2);

}
