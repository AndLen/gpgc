package clustering;

import data.Instance;
import other.DissimilarityMap;
import other.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static other.DatasetUtils.EUCLIDEAN_DISSIM_MAP;
import static other.Util.LOG;
import static other.Util.MOST_NEGATIVE_VAL;

/**
 * Created by lensenandr on 19/05/16.
 */
public class SilhouettePAM implements KEstimator {

    public static final int NUM_ITERATIONS = 100;

    private static double averageDissim(Cluster cluster, Instance instance, DissimilarityMap dissimMap) {
        double sumDissim = 0;
        for (Instance i2 : cluster.getInstancesInCluster()) {
            sumDissim += dissimMap.getDissim(instance, i2);
        }
        return sumDissim / cluster.size();

    }

    public static int getMedianKFromSil(List<Instance> instances) {
        List<Future<Integer>> estimates = new ArrayList<>(30);
        for (int i = 0; i < 30; i++) {
            estimates.add(Util.submitJob(() -> new SilhouettePAM().estimatedNumberOfClusters(instances)));
        }
        List<Integer> kS = new ArrayList<>();
        for (Future<Integer> estimate : estimates) {
            try {
                kS.add(estimate.get());
            } catch (InterruptedException | ExecutionException e) {
                throw new Error(e);
            }
        }

        Collections.sort(kS);
        LOG.println(kS);
        int median = kS.get(14);
        LOG.println(median);
        return median;
    }

    public static double getAverageSilhouette(DissimilarityMap dissimMap, List<? extends Cluster> clusters, int numInstances) {
        double sumSilhouette = 0;
        int numClusters = clusters.size();
        for (int clusterI = 0; clusterI < numClusters; clusterI++) {
            Cluster cluster = clusters.get(clusterI);
            List<Instance> clusterInstances = cluster.getInstancesInCluster();
            int clusterSize = clusterInstances.size();

            for (int instanceI = 0; instanceI < clusterSize; instanceI++) {
                Instance instance = clusterInstances.get(instanceI);
                double sumAI = 0;
                for (int otherInstanceI = 0; otherInstanceI < clusterSize; otherInstanceI++) {
                    if (instanceI != otherInstanceI) {
                        Instance otherInstance = clusterInstances.get(otherInstanceI);
                        sumAI += dissimMap.getDissim(instance, otherInstance);
                    }
                }
                double aI = clusterSize == 1 ? 0 : sumAI / (clusterSize - 1);
                double bI = Double.MAX_VALUE;
                for (int otherClusterI = 0; otherClusterI < numClusters; otherClusterI++) {
                    if (clusterI != otherClusterI) {
                        double clusterDissim = averageDissim(clusters.get(otherClusterI), instance, dissimMap);
                        if (clusterDissim < bI) {
                            bI = clusterDissim;
                        }
                    }

                }
                if (bI == 0 && aI == 0) {
                    //TODO: What makes sense?
                } else {
                    sumSilhouette += (bI - aI) / (Math.max(aI, bI));
                }
            }

        }
        double result = sumSilhouette / numInstances;
        if (Double.isNaN(result)) {
            System.err.println("Shit");
        }
        return result;
    }

    @Override
    public int estimatedNumberOfClusters(List<Instance> instances) {
        int numInstances = instances.size();
        int kMax = (int) Math.sqrt(numInstances);
        double[] critVal = new double[kMax];
        for (int k = 2; k <= kMax; k++) {
            List<? extends Cluster> clusters = PartitionAroundMedoids.doPAM(instances, k, NUM_ITERATIONS);
            double averageSilhouette = getAverageSilhouette(EUCLIDEAN_DISSIM_MAP, clusters, numInstances);
            critVal[k - 1] = averageSilhouette;
        }
        double maxCrit = MOST_NEGATIVE_VAL;
        int bestK = 1;
        for (int k = 2; k <= kMax; k++) {
            if (critVal[k - 1] > maxCrit) {
                maxCrit = critVal[k - 1];
                bestK = k;
            }
        }
        return bestK;

    }

}
