package tests.reference;

/**
 * Created by lensenandr on 4/04/16.
 */

import tests.Tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MOCKTests extends Tests {

    public List<String> getTestConfig() {
        return new ArrayList<>(Arrays.asList("featureSubsetForFitness=false", "preprocessing=scale", "fitnessFunction=clusterFitness.LocumFitness", "logPrefix=mock/", "featureMin=0", "featureMax=1", "methods=MOCK", "methodNames=MOCK", "runs=30", "parallelRuns=true"));
    }
}
