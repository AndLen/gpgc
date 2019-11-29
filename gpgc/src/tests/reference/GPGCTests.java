package tests.reference;

import tests.Tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by lensenandr on 28/11/16.
 */
public class GPGCTests extends Tests {

    public List<String> getTestConfig() {
        return new ArrayList<>(Arrays.asList("featureSubsetForFitness=false", "preprocessing=scale", "fitnessFunction=clusterFitness.LocumFitness", "methods=GP_LOCUM", "logPrefix=GPGC/", "featureMin=0", "featureMax=1", "methodNames=GP Locum", "numtrees=1", "numEval=cubeRoot", "numConnect=1"));
    }
}
