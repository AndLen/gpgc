package tests.reference;

import tests.Tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by lensenandr on 28/11/16.
 */
public class GPGCMTWVAICD5 extends Tests {

    public List<String> getTestConfig() {
        return new ArrayList<>(Arrays.asList("featureSubsetForFitness=false", "preprocessing=scale", "fitnessFunction=clusterFitness.LocumFitness", "methods=GP_LOCUM", "logPrefix=gpgcWVNumTrees/", "featureMin=0", "featureMax=1", "methodNames=GPGC MT7 WV AIC D5", "numEval=cubeRoot", "numConnect=1", "multitreeType=weightedVote", "allIndexCrossover=true", "treeDepth=5", "numtrees=7"));
    }
}
