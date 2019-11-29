package tests.reference;

/**
 * Created by lensenandr on 4/04/16.
 */

import tests.Tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DBSCANTests extends Tests {

    public List<String> getTestConfig() {
        return new ArrayList<>(Arrays.asList("featureSubsetForFitness=false", "preprocessing=scale", "fitnessFunction=clusterFitness.LocumFitness", "logPrefix=dbscan/", "featureMin=0", "featureMax=1", "methods=DBSCAN", "methodNames=DBSCAN"));
    }
}
