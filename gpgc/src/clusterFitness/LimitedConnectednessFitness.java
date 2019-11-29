package clusterFitness;

import clustering.Cluster;
import data.Instance;
import other.DatasetUtils;
import other.Util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * From Handl and Knowles 2007
 * Created by lensenandr on 14/04/16.
 */
public class LimitedConnectednessFitness extends AbstractFitness {
    public static <T extends Cluster> Map<Instance, T> instancesToClusters(List<T> clusters) {
        HashMap<Instance, T> instancesToClusters = new HashMap<>();
        for (T cluster : clusters) {
            for (Instance instance : cluster.getInstancesInCluster()) {
                instancesToClusters.put(instance, cluster);
            }
        }
        return instancesToClusters;
    }

    public static double avgClusterConnectivity(List<? extends Cluster> clusters) {
        Map<Instance, ? extends Cluster> instanceClusters = instancesToClusters(clusters);
        double clusterSum = 0;
        for (Cluster cluster : clusters) {
            double sum = 0;
            List<Instance> instancesInCluster = cluster.getInstancesInCluster();

            for (Instance instance : instancesInCluster) {
                List<Instance> nns = DatasetUtils.NEAREST_NEIGHBOURS.get(instance);
                double thisinstance = 0;
                int numNNs = Math.min(10, nns.size());
                for (int i = 0; i < numNNs; i++) {
                    Instance nn = nns.get(i);
                    double dissim;
                    dissim = DatasetUtils.DEFAULT_MAP.getDissim(instance, nn);
                    //this is wrong.
                    double inversedist = dissim <= 0.1 ? 10 : 1 / dissim;
//NNs only in order when fixed number atm...
                    boolean sameCluster = cluster.equals(instanceClusters.get(nn));
                    if (sameCluster) thisinstance += (inversedist);

                }
                //if (nns.size() > 0) {
                //    thisinstance /= nns.size();
                sum += thisinstance;
                //}

            }
            sum /= instancesInCluster.size();
            clusterSum += sum;
        }
        clusterSum /= clusters.size();
        return clusterSum;
    }

    @Override
    public double internalFitness(List<? extends Cluster> clusters, List<Instance> instances, boolean[] actualFeatureSubset, boolean[] featureSubsetForFitness) {
        //  if (tooManyClusters(clusters, instances)) {
        //    return worstPossibleFitness();
        //}
        return avgClusterConnectivity(clusters);
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
