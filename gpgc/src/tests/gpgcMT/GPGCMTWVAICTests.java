package tests.gpgcMT;

import tests.Tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by lensenandr on 28/11/16.
 */
public class GPGCMTWVAICTests extends Tests {

    public List<String> getTestConfig() {
        return new ArrayList<>(Arrays.asList("featureSubsetForFitness=false", "preprocessing=scale", "fitnessFunction=clusterFitness.LocumFitness", "methods=GP_LOCUM", "logPrefix=gpgcWVAICTEST/", "featureMin=0", "featureMax=1", "methodNames=GPGC-AIC-D5", "numEval=cubeRoot", "numConnect=1", "multitreeType=weightedVote", "allIndexCrossover=true", "treeDepth=5", "numtrees=1"));
    }
}
