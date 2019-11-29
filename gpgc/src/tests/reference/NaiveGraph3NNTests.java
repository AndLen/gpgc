package tests.reference;

import tests.Tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by lensenandr on 28/11/16.
 */
public class NaiveGraph3NNTests extends Tests {

    public List<String> getTestConfig() {
        return new ArrayList<>(Arrays.asList("featureSubsetForFitness=false", "preprocessing=scale", "fitnessFunction=clusterFitness.LocumFitness", "methods=NAIVE_GRAPH_NN", "methodNames=NG-3NN", "logPrefix=naiveGraph2N/", "featureMin=0", "featureMax=1", "runs=1", "numNN=3"));
    }
}
