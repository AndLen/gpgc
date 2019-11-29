package clustering;

import data.Instance;
import other.DatasetUtils;
import other.Util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by lensenandr on 9/03/16.
 */
public class PartitionAroundMedoids {
    public static List<MedoidCluster> doPAM(List<? extends Instance> instances, int numClusters, int numIterations) {
        List<Instance> means = KMeansClustering.initialiseMeans(instances, numClusters);
        List<MedoidCluster> pamClustering = doPAM(instances, numIterations, new HashSet<>(means), DatasetUtils.ALL_FEATURES, Util.getDistanceMeasureForClustering());
        return pamClustering;
    }

    public static List<MedoidCluster> doPAM(List<? extends Instance> instances, int numIterations, Set<Instance> medoids, boolean[] featureSubset, Util.DistanceMeasure distanceMeasure) {
        List<MedoidCluster> clusters = null;
        for (int i = 0; i < numIterations; i++) {
            clusters = MedoidCluster.clusterify(instances, DatasetUtils.ALL_FEATURES, medoids, distanceMeasure);
            boolean changed = false;
            Set<Instance> newMedoids = new HashSet<>();
            for (MedoidCluster cluster : clusters) {
                Instance newMedoid = chooseNewMedoid(cluster, featureSubset, distanceMeasure);
                newMedoids.add(newMedoid);
                if (!newMedoid.equals(cluster.getPrototype())) {
                    changed = true;
                }
            }
            if (!changed) {
                //   LOG.println("Exiting early on iter " + i);
                break;
            }
            medoids = newMedoids;
        }
        return clusters;
    }

    private static Instance chooseNewMedoid(MedoidCluster cluster, boolean[] featureSubset, Util.DistanceMeasure distanceMeasure) {
        List<Instance> instances = cluster.getInstancesInCluster();
        double minIntraSum = Double.MAX_VALUE;
        Instance newMedoid = cluster.getPrototype();

        for (Instance thisMedoid : instances) {
            double intraSum = 0;
            for (Instance instance : instances) {
                intraSum += instance.distanceTo(thisMedoid, featureSubset, distanceMeasure);
            }
            if (intraSum < minIntraSum) {
                minIntraSum = intraSum;
                newMedoid = thisMedoid;
            }
        }
        return newMedoid;

    }

}
