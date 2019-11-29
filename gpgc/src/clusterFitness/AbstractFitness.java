package clusterFitness;

import clustering.Cluster;
import data.Instance;
import other.DatasetUtils;

import java.util.List;

/**
 * Created by lensenandr on 12/04/16.
 */
public abstract class AbstractFitness implements ClusterFitnessFunction {
    static boolean tooManyClusters(List<? extends Cluster> clusters, List<Instance> instances) {
        int kMax = (int) Math.sqrt(instances.size());
        return clusters.size() > kMax;
    }

    public double fitness(List<? extends Cluster> clusters, List<Instance> instances, boolean[] featureSubset, boolean[] featureSubsetForFitness) {
        int numInstances = clusters.stream().mapToInt(Cluster::size).sum();
        if (numInstances != instances.size()) {
            throw new IllegalStateException("Incorrect number of instances clustered: " + numInstances + " instead of: " + instances.size());
        }
        if (clusters.size() < 2 || DatasetUtils.numFeaturesUsed(featureSubset) < 1) {
            return worstPossibleFitness();
        } else {
            return internalFitness(clusters, instances, featureSubset, featureSubsetForFitness);
        }
    }

    protected abstract double internalFitness(List<? extends Cluster> clusters, List<Instance> instances, boolean[] actualFeatureSubset, boolean[] featureSubsetForFitness);

}
