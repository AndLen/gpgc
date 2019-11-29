package clustering;

import data.Instance;
import other.DatasetUtils;
import other.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Created by lensenandr on 9/03/16.
 */
public class KMeansClustering {
    public static List<CentroidCluster> doKMeans(List<? extends Instance> instances, int numClusters, int numIterations) {
        List<Instance> means = initialiseMeans(instances, numClusters);
        List<CentroidCluster> kMeansClustering = doKMeans(instances, numIterations, means, Util.getDistanceMeasureForClustering());
        return kMeansClustering;
    }

    public static List<CentroidCluster> doKMeans(List<? extends Instance> instances, int numIterations, List<Instance> means, Util.DistanceMeasure distanceMeasure) {
        List<CentroidCluster> clusters = new ArrayList<>();
        for (int i = 0; i < numIterations; i++) {
            clusters = Cluster.nearestPrototypeCluster(instances, DatasetUtils.ALL_FEATURES, means, distanceMeasure);

            List<Instance> newMeans = new ArrayList<>();
            boolean meanChanged = false;
            for (CentroidCluster cluster : clusters) {
                Instance newMean = recomputeCentre(cluster);
                if (newMean.distanceTo(cluster.getPrototype(), DatasetUtils.ALL_FEATURES, distanceMeasure) > 0.0001) {
                    meanChanged = true;
                }
                newMeans.add(newMean);
            }
            means = newMeans;
            if (!meanChanged) {
                // System.out.println("Exiting on iter " + i);
                break;
            }
        }
        return clusters;
    }


    public static List<Instance> initialiseMeans(List<? extends Instance> instances, int numClusters) {
        //Random initial means, choosing each instanceCl at most once
        List<Instance> means = new ArrayList<>();
        while (means.size() != numClusters) {
            Instance nextInstance = instances.get(Util.randomInt(instances.size()));
            if (!means.contains(nextInstance)) {
                means.add(nextInstance);
            }
        }
        return means;
    }

    public static Instance recomputeCentre(CentroidCluster cluster) {
        List<Instance> instances = cluster.getInstancesInCluster();
        int numInstances = instances.size();
        if (numInstances == 0) {
            //Clearly can't recompute the centroid... so return prototype
            return cluster.getPrototype();
        } else {
            return cluster.getMean();
        }
    }

    public static List<CentroidCluster> doFGKMeans(List<Instance> instances, int numIterations, List<Instance> means, double[] features, Util.DistanceMeasure distanceMeasure) {
        List<CentroidCluster> clusters = new ArrayList<>();
        for (int i = 0; i < numIterations; i++) {
            clusters = cluster(instances, features, means);

            List<Instance> newMeans = new ArrayList<>();
            boolean meanChanged = false;
            for (CentroidCluster cluster : clusters) {
                Instance newMean = recomputeCentre(cluster);
                if (findDistance(newMean, cluster.getPrototype(), features) > 0.0001) {
                    meanChanged = true;
                }
                newMeans.add(newMean);
            }
            means = newMeans;
            if (!meanChanged) {
                System.out.println("Exiting on iter " + i);
                break;
            }
        }
        return clusters;
    }

    private static List<CentroidCluster> cluster(List<Instance> instances, double[] featureWeights, List<Instance> prototypes) {
        List<CentroidCluster> clusters = prototypes.stream().map(CentroidCluster::new).collect(Collectors.toList());
        for (Instance instance : instances) {
            double closestDistance = Double.MAX_VALUE;
            CentroidCluster bestCluster = null;
            for (CentroidCluster cluster : clusters) {

                double distance = findDistance(instance, cluster.getPrototype(), featureWeights);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    bestCluster = cluster;
                }
            }
            if (bestCluster == null) {
                System.err.println("HALP");
            } else {
                bestCluster.addInstance(instance);
            }

        }
        return clusters;
    }

    public static double findDistance(Instance i1, Instance i2, double[] featureWeights) {
        double runningSum = 0;
        int numFeatures = featureWeights.length;
        for (int i = 0; i < numFeatures; i++) {
            if (featureWeights[i] != 0) {
                //feature is selected
                double featureDistance = i1.getFeatureValue(i) - i2.getFeatureValue(i);
                double v = (featureDistance * featureDistance) / DatasetUtils.FEATURE_RANGE2;
                runningSum += (v);// * featureWeights[i]);
            } else {
            }

        }
        double sqrt = Math.sqrt(runningSum);
        //  LOG.println(sqrt);
        return sqrt;
    }
}
