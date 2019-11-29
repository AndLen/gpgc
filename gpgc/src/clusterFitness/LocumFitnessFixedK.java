package clusterFitness;

import clustering.Cluster;
import data.Instance;
import other.DatasetUtils;
import other.Util;

import java.util.List;

/**
 * Created by lensenandr on 3/03/16.
 */
public class LocumFitnessFixedK extends AbstractFitness {

    public static double CONNECTIVITY_WEIGHT = 1.0;
    public static double SPARSITY_WEIGHT = 1.0;
    public static double SEPARABILITY_WEIGHT = 1.0;

    public static double gpgcFitness(List<? extends Cluster> clusters, List<Instance> instances) {
        return (CONNECTIVITY_WEIGHT * LimitedConnectednessFitness.avgClusterConnectivity(clusters))//;
                //  return new ConnectednessFitness().fullAvgClusterConnectivity(clusters, instances)
                // / sumNearestNeighbourFitness(clusters, instances);
                // / SumIntraFitness.getSumIntraFitness(clusters, featureSubsetForFitness);
                // / maxMinDistance(clusters, instances);
                / MaxMinIntraMinMinInterDistance.maxMinIntraMinMinInterDistance(clusters, instances, SPARSITY_WEIGHT, SEPARABILITY_WEIGHT);
    }

    @Override
    public double internalFitness(List<? extends Cluster> clusters, List<Instance> instances, boolean[] actualFeatureSubset, boolean[] featureSubsetForFitness) {
//Limited connectedness
        return gpgcFitness(clusters, instances);
        // / AverageMinIntraMinMinInterDistance.averageMinIntraMinMinInterDistance(clusters, instances);
        /// AverageMinIntraAverageMinInterDistance.averageMinIntraAverageMinInterDistance(clusters, instances);
    }

    private double sumNearestNeighbourFitness(List<? extends Cluster> clusters, List<Instance> instances) {
        double sum = 0;
        for (Cluster cluster : clusters) {
            List<Instance> clusterInstances = cluster.getInstancesInCluster();
            for (int i = 0; i < clusterInstances.size(); i++) {
                Instance instance = clusterInstances.get(i);
                double minDist = Double.MAX_VALUE;
                for (int j = 0; j < clusterInstances.size(); j++) {
                    if (i != j) {
                        minDist = Double.min(minDist, DatasetUtils.DEFAULT_MAP.getDissim(instance, clusterInstances.get(j)));
                    }
                }
                sum += minDist;
            }

        }
        return sum / instances.size();
    }

    private double maxMinDistance(List<? extends Cluster> clusters, List<Instance> instances) {
        //Or just max across all clusters? (too variable)
        double maxSum = 0;
        for (Cluster cluster : clusters) {
            double max = 0;
            List<Instance> clusterInstances = cluster.getInstancesInCluster();
            for (int i = 0; i < clusterInstances.size(); i++) {
                Instance instance = clusterInstances.get(i);
                double minDist = Double.MAX_VALUE;
                for (int j = 0; j < clusterInstances.size(); j++) {
                    if (i != j) {
                        minDist = Double.min(minDist, DatasetUtils.DEFAULT_MAP.getDissim(instance, clusterInstances.get(j)));
                    }
                }
                max = Math.max(minDist, max);
            }
            maxSum += max;

        }
        //avg?
        return maxSum / clusters.size();
    }

    private double maxMinIntraMinMinInterDistance(List<? extends Cluster> clusters, List<Instance> instances) {
        //We want SMALL dist within, BIG between

        double sum = 0;
        for (int k = 0; k < clusters.size(); k++) {
            Cluster cluster = clusters.get(k);
            double maxMinIntra = 0;
            double minMinInter = Double.MAX_VALUE;
            List<Instance> clusterInstances = cluster.getInstancesInCluster();
            for (int i = 0; i < clusterInstances.size(); i++) {
                Instance instance = clusterInstances.get(i);
                double minDistToNeighbour = Double.MAX_VALUE;
                for (int j = 0; j < clusterInstances.size(); j++) {
                    if (i != j) {
                        minDistToNeighbour = Double.min(minDistToNeighbour, DatasetUtils.DEFAULT_MAP.getDissim(instance, clusterInstances.get(j)));
                    }
                }
                maxMinIntra = Math.max(minDistToNeighbour, maxMinIntra);

                for (int l = 0; l < clusters.size(); l++) {
                    if (l != k) {
                        Cluster otherCluster = clusters.get(l);
                        List<Instance> otherClusterInstances = otherCluster.getInstancesInCluster();
                        for (int m = 0; m < otherCluster.size(); m++) {
                            minMinInter = Double.min(minMinInter, DatasetUtils.DEFAULT_MAP.getDissim(instance, otherClusterInstances.get(m)));
                        }
                    }
                }
            }
            sum += (maxMinIntra / minMinInter);


        }
        //avg?
        return sum / clusters.size();
    }


    private double neighbourDistance(List<? extends Cluster> clusters, boolean[] featureSubsetForFitness) {
        return 0;
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
