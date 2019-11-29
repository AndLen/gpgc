package tests.reference;

import tests.Tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by lensenandr on 28/11/16.
 */
public class NaiveGraph2NNTests extends Tests {

    public List<String> getTestConfig() {
        return new ArrayList<>(Arrays.asList("featureSubsetForFitness=false", "preprocessing=scale", "methodNames=NG-2NN", "fitnessFunction=clusterFitness.LocumFitness", "methods=NAIVE_GRAPH_NN", "logPrefix=naiveGraph2N/", "featureMin=0", "featureMax=1", "runs=1", "numNN=2"));
    }
}
