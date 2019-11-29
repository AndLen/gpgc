package clustering;

import data.Instance;
import data.UnlabelledInstance;
import other.Util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by lensenandr on 2/03/16.
 */
public abstract class Cluster implements Serializable {
    private static int nextID = 0;
    protected final List<Instance> instancesInCluster;
    private final int clusterID;

    public Cluster() {
        instancesInCluster = new ArrayList<>();
        clusterID = getNextID();
    }

    private static synchronized int getNextID() {
        return nextID++;
    }

    public static List<CentroidCluster> nearestPrototypeCluster(List<? extends Instance> instances, boolean[] featureSubset, List<Instance> prototypes, Util.DistanceMeasure distanceMeasure) {
        List<CentroidCluster> clusters = prototypes.stream().map(CentroidCluster::new).collect(Collectors.toList());
        for (Instance instance : instances) {
            double closestDistance = Double.MAX_VALUE;
            CentroidCluster bestCluster = null;
            for (CentroidCluster cluster : clusters) {

                double distance = cluster.findDistance(instance, featureSubset, distanceMeasure);
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

    public static UnlabelledInstance computeCentre(int numFeatures, List<Instance> instances) {

        double[] centroid = new double[numFeatures];
        int numInstancesInCluster = instances.size();

        for (Instance instance : instances) {
            for (int i = 0; i < numFeatures; i++) {
                centroid[i] += instance.getFeatureValue(i);
            }
        }
        for (int i = 0; i < numFeatures; i++) {
            centroid[i] /= numInstancesInCluster;
        }
        return new UnlabelledInstance(centroid);




    }
//
//    public static List<CentroidCluster> kdTreeNearestPrototypeCluster(List<Instance> instancesCl, boolean[] featureSubset, List<Instance> prototypes) {
//        List<CentroidCluster> clusters = prototypes.stream().map(CentroidCluster::new).collect(Collectors.toList());
//        int counter = 0;
//        for (boolean b : featureSubset) {
//            if (b) counter++;
//        }
//        //lol lambdas
//        final int numSelectedFeatures = counter;
//
    //KDTree kdTree = new KDTree(numSelectedFeatures);
//        clusters.forEach(c -> kdTree.insert(denseSubsetInstance(c.getPrototype(), featureSubset, numSelectedFeatures), c));
//
//        for (Instance instanceCl : instancesCl) {
//            final CentroidCluster nearestCluster = (CentroidCluster) kdTree.nearest(denseSubsetInstance(instanceCl, featureSubset, numSelectedFeatures));
//            nearestCluster.addInstance(instanceCl);
//
//        }
//
//        return clusters;
//
//    }
//
//    private static double[] denseSubsetInstance(Instance instanceCl, boolean[] featureSubset, int numSelectedFeatures) {
//        double[] denseSubset = new double[numSelectedFeatures];
//        int index = 0;
//        for (int i = 0; i < featureSubset.length; i++) {
//            boolean b = featureSubset[i];
//            if (b) {
//                denseSubset[index] = instanceCl.getFeatureValue(i);
//                index++;
//            }
//        }
//        return denseSubset;
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Cluster cluster = (Cluster) o;

        return this.clusterID == cluster.clusterID;

    }

    @Override
    public int hashCode() {
        return clusterID;
    }

    public List<Instance> getInstancesInCluster() {
        return instancesInCluster;
    }

    public void addInstance(Instance instance) {
        instancesInCluster.add(instance);
    }

    public void addAllInstances(Collection<? extends Instance> instances) {
        instancesInCluster.addAll(instances);
    }

    public abstract double findDistance(Instance instance, boolean[] featureSubset, Util.DistanceMeasure distanceMeasure);

    /**
     * @return whatever the cluster defines as its centre - could be the medoid, centroid, .....
     */
    public abstract Instance getPrototype();

    /**
     * @return The centroid of the cluster, defined as the average of all values in the cluster. Cluster must be
     * fully populated to give the intended result!
     */
    public final Instance getMean() {
        return computeCentre(instancesInCluster.get(0).numFeatures(),instancesInCluster);
    }

    public abstract String getPrototypeToString(List<Instance> allInstances);


    public int size() {
        return instancesInCluster.size();
    }
}
