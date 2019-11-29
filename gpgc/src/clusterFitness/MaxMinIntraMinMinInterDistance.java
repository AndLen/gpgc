package clusterFitness;

import clustering.Cluster;
import data.Instance;
import other.DatasetUtils;
import other.Util;

import java.util.List;

/**
 * Similar to Dunn maybe?
 */
public class MaxMinIntraMinMinInterDistance extends AbstractFitness {
    public static double maxMinIntraMinMinInterDistance(List<? extends Cluster> clusters, List<Instance> instances, double sparsityWeight, double separabilityWeight) {
        //We want SMALL dist within, BIG between

        double sum = 0;
        int numClusters = clusters.size();
        for (int k = 0; k < numClusters; k++) {
            Cluster cluster = clusters.get(k);
            double maxMinIntra = 0;
            double minMinInter = Double.MAX_VALUE;
            List<Instance> clusterInstances = cluster.getInstancesInCluster();
            int thisClusterSize = clusterInstances.size();
            for (int i = 0; i < thisClusterSize; i++) {
                Instance instance = clusterInstances.get(i);
                double distToClosestNeighbour = distToClosestNeighbour(clusterInstances, i, instance);
                maxMinIntra = Math.max(distToClosestNeighbour, maxMinIntra);

                double distToClosestNeighourCluster = distToClosestNeighourCluster(clusters, k, instance);
                minMinInter = Double.min(minMinInter, distToClosestNeighourCluster);
            }
            sum += ((maxMinIntra * sparsityWeight) / (minMinInter * separabilityWeight));


        }
        //avg?
        return sum / numClusters;
    }

    public static double distToClosestNeighourCluster(List<? extends Cluster> clusters, int clusterIndex, Instance instance) {
        double thisMinInter = Double.MAX_VALUE;
        int numClusters = clusters.size();
        for (int l = 0; l < numClusters; l++) {
            if (l != clusterIndex) {
                Cluster otherCluster = clusters.get(l);
                List<Instance> otherClusterInstances = otherCluster.getInstancesInCluster();
                int otherClusterSize = otherCluster.size();
                for (int m = 0; m < otherClusterSize; m++) {
                    thisMinInter = Double.min(thisMinInter, DatasetUtils.DEFAULT_MAP.getDissim(instance, otherClusterInstances.get(m)));
                }
            }
        }
        return thisMinInter;
    }

    public static double distToClosestNeighbour(List<Instance> clusterInstances, int instanceIndex, Instance instance) {
        double minDistToNeighbour = Double.MAX_VALUE;
        int thisClusterSize = clusterInstances.size();
        for (int j = 0; j < thisClusterSize; j++) {
            if (instanceIndex != j) {
                minDistToNeighbour = Double.min(minDistToNeighbour, DatasetUtils.DEFAULT_MAP.getDissim(instance, clusterInstances.get(j)));
            }
        }
        return minDistToNeighbour;
    }

    @Override
    public double internalFitness(List<? extends Cluster> clusters, List<Instance> instances, boolean[] actualFeatureSubset, boolean[] featureSubsetForFitness) {
        return 1 / maxMinIntraMinMinInterDistance(clusters, instances, 1.0, 0.5);
    }

    @Override
    public boolean fitnessBetterThan(double oldFitness, double newFitness) {
        if (Double.isNaN(oldFitness)) {
            return !Double.isNaN(newFitness);
        }
        return newFitness > oldFitness;
    }

    @Override
    public double worstPossibleFitness() {
        return Util.MOST_NEGATIVE_VAL;
    }

}
