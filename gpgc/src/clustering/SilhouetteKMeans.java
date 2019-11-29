package clustering;

import data.Instance;
import other.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static other.DatasetUtils.EUCLIDEAN_DISSIM_MAP;
import static other.Util.*;

/**
 * Created by lensenandr on 19/05/16.
 */
public class SilhouetteKMeans extends SilhouettePAM{

    public static int getMedianKFromSil(List<Instance> instances) {
        List<Future<Integer>> estimates = new ArrayList<>(30);
        for (int i = 0; i < 30; i++) {
            estimates.add(Util.submitJob(() -> new SilhouetteKMeans().estimatedNumberOfClusters(instances)));
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


    @Override
    public int estimatedNumberOfClusters(List<Instance> instances) {
        int numInstances = instances.size();
        int kMax = (int) Math.sqrt(numInstances);
        double[] critVal = new double[kMax];
        for (int k = 2; k <= kMax; k++) {
            List<Instance> means = KMeansClustering.initialiseMeans(instances, k);
            List<? extends Cluster> clusters = KMeansClustering.doKMeans(instances, NUM_ITERATIONS, means, EUCLIDEAN_DISTANCE);
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
