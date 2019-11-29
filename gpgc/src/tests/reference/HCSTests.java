package tests.reference;

/**
 * Created by lensenandr on 4/04/16.
 */

import tests.Tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HCSTests extends Tests {

    public List<String> getTestConfig() {
        return new ArrayList<>(Arrays.asList("featureSubsetForFitness=false", "preprocessing=scale", "fitnessFunction=clusterFitness.LocumFitness", "logPrefix=hcs/", "featureMin=0", "featureMax=1", "methods=HCS", "methodNames=HCS", "runs=1"));
    }
}
