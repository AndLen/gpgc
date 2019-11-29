package gp.subspace;

import clustering.CentroidCluster;
import clustering.Cluster;
import clustering.SubspaceCentroidCluster;
import data.Instance;
import data.UnlabelledInstance;
import ec.EvolutionState;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import ec.gp.GPNodeGatherer;
import ec.gp.GPTree;
import gp.ClusteringProblem;
import gp.DoubleData;
import gp.FeatureNode;
import other.DatasetUtils;
import other.Util;

import java.util.*;

import static gp.GPUtils.instances;


/**
 * Created by lensenandr on 8/08/16.
 */
public class SubspaceClusteringProblem extends ClusteringProblem {


    public static final GPNodeGatherer FEATURE_GATHERER = new GPNodeGatherer() {
        @Override
        public boolean test(GPNode thisNode) {
            return thisNode instanceof FeatureNode;
        }
    };

    public static boolean[] getUsedFeatures(GPTree tree, int numFeatures) {
        boolean[] usedFeatures = new boolean[numFeatures];

        Iterator treeIterator = tree.child.iterator(FEATURE_GATHERER);
        treeIterator.forEachRemaining(n -> usedFeatures[((FeatureNode) n).getVal()] = true);

        return usedFeatures;
    }

    public List<? extends Cluster> getClusters(EvolutionState state, GPIndividual ind, int threadnum, Random random) {
        DoubleData input = (DoubleData) (this.input);
        GPTree[] trees = ind.trees;

        int numTrees = trees.length;

        List<Set<Instance>> clusters = new ArrayList<>();
        for (int t = 0; t < numTrees; t++) {
            clusters.add(new HashSet<>());
        }
        for (Instance instance : instances) {

            double highestVote = Util.MOST_NEGATIVE_VAL;
            int highestTree = -1;
            for (int t = 0; t < numTrees; t++) {

                currentInstance = instance;
                GPTree tree = trees[t];
                tree.child.eval(
                        state, threadnum, input, stack, ind, this);
                double result = input.val;
                if (result > highestVote) {
                    highestVote = result;
                    highestTree = t;
                }
            }
            clusters.get(highestTree).add(instance);
        }
        List<CentroidCluster> finalClusters = new ArrayList<>(numTrees);

        int numFeatures = instances.get(0).numFeatures();


        for (int t = 0; t < numTrees; t++) {
            Set<Instance> cluster = clusters.get(t);
            UnlabelledInstance clusterCentroid;
            if (!cluster.isEmpty()) {
                clusterCentroid = DatasetUtils.getSampleMean(cluster);
                CentroidCluster centroidCluster = new SubspaceCentroidCluster(clusterCentroid, getUsedFeatures(ind.trees[t], numFeatures));
                centroidCluster.addAllInstances(cluster);
                finalClusters.add(centroidCluster);
            }

        }
        return finalClusters;

    }


}
