package clustering;


import data.Instance;
import other.Util;

import java.util.*;

/**
 * Created by lensenandr on 2/03/16.
 */
public class MedoidCluster extends Cluster {
    private final Instance medoid;

    public MedoidCluster(Instance medoid) {
        super();
        this.medoid = medoid;
    }

    public static List<MedoidCluster> clusterify(List<? extends Instance> instances, boolean[] featureSubset, boolean[] medoidSubset, Util.DistanceMeasure distanceMeasure) {
        Set<Instance> medoids = new HashSet<>();

        for (int i = 0; i < medoidSubset.length; i++) {
            if (medoidSubset[i]) {
                medoids.add(instances.get(i));
            }
        }
        return clusterify(instances, featureSubset, medoids, distanceMeasure);
    }

    public static List<MedoidCluster> clusterify(List<? extends Instance> instances, boolean[] featureSubset, Set<Instance> medoids, Util.DistanceMeasure distanceMeasure) {
        List<MedoidCluster> clusters = new ArrayList<>();
        for (Instance medoid : medoids) {
            MedoidCluster cluster = new MedoidCluster(medoid);
            //Must go in its own medoid
            cluster.addInstance(medoid);
            clusters.add(cluster);
        }

        for (Instance instance : instances) {
            //If is a medoid, don't process it again.
            if (!medoids.contains(instance)) {
                Cluster bestCluster = getClosestCluster(featureSubset, clusters, instance, distanceMeasure);
                if (bestCluster == null) {
                 //   System.err.println("HALP");
                } else {
                    bestCluster.addInstance(instance);
                }

            }
        }
        return clusters;
    }

    public static Cluster getClosestCluster(boolean[] featureSubset, Collection<MedoidCluster> clusters, Instance instance, Util.DistanceMeasure distanceMeasure) {
        double closestDistance = Double.MAX_VALUE;
        Cluster bestCluster = null;
        for (Cluster cluster : clusters) {
            double distance = cluster.findDistance(instance, featureSubset, distanceMeasure);
            if (distance < closestDistance) {
                closestDistance = distance;
                bestCluster = cluster;
            }
        }
        return bestCluster;
    }

    public Instance getPrototype() {
        return medoid;
    }

    @Override
    public String getPrototypeToString(List<Instance> allInstances) {
        return String.format("Index %d",allInstances.indexOf(medoid));
    }

    @Override
    public double findDistance(Instance instance, boolean[] featureSubset, Util.DistanceMeasure distanceMeasure) {
        return medoid.distanceTo(instance, featureSubset, distanceMeasure);
    }

}
