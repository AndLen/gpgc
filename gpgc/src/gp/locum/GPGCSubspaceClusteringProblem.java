package gp.locum;

import clustering.Cluster;
import clustering.SubspaceCentroidCluster;
import ec.EvolutionState;
import ec.gp.GPIndividual;
import ec.gp.GPTree;
import gp.GPUtils;
import gp.subspace.SubspaceClusteringProblem;
import other.DatasetUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * Created by lensenandr on 8/08/16.
 */
public class GPGCSubspaceClusteringProblem extends LocumClusteringProblem {


    public List<? extends Cluster> getClusters(EvolutionState state, GPIndividual ind, int threadnum, Random random) {
        List<? extends Cluster> clusters = super.getClusters(state, ind, threadnum, random);

        boolean[] allUsedFeatures = new boolean[GPUtils.numFeatures];
        for (GPTree tree : ind.trees) {
            boolean[] usedFeatures = SubspaceClusteringProblem.getUsedFeatures(tree, GPUtils.numFeatures);
            for (int i = 0; i < usedFeatures.length; i++) {
                allUsedFeatures[i] = allUsedFeatures[i] || usedFeatures[i];

            }

        }

        List<SubspaceCentroidCluster> convertedClusters = new ArrayList<>();
        for (Cluster cluster : clusters) {
            SubspaceCentroidCluster sCC = new SubspaceCentroidCluster(cluster.getMean(), DatasetUtils.ALL_FEATURES);
            sCC.addAllInstances(cluster.getInstancesInCluster());
            convertedClusters.add(sCC);
        }
        return convertedClusters;

    }


}
