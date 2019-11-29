package tests.reference;

/**
 * Created by lensenandr on 4/04/16.
 */

import tests.Tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KMeansPlusPlusTests extends Tests {

    public List<String> getTestConfig() {
        return new ArrayList<>(Arrays.asList("featureSubsetForFitness=false", "preprocessing=scale", "fitnessFunction=clusterFitness.FF2", "methods=K_MEANS_PLUS_PLUS", "logPrefix=KMeans++/", "featureMin=0", "featureMax=1", "methodNames=KMeans++"));
    }
}
