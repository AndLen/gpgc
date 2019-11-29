package tests.naiveGraph;

import tests.Tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by lensenandr on 28/11/16.
 */
public class NaiveGraphThresholdTests extends Tests {

    public List<String> getTestConfig() {
        return new ArrayList<>(Arrays.asList("featureSubsetForFitness=false", "preprocessing=scale", "fitnessFunction=clusterFitness.LocumFitness", "methods=NAIVE_GRAPH_THRESHOLD", "logPrefix=naiveGraphThreshold/", "featureMin=0", "featureMax=1", "runs=1"));
    }
}
