package clusterFitness;

import clustering.Cluster;
import data.Instance;

import java.util.List;

/**
 * Created by lensenandr on 7/03/16.
 */
public interface ClusterFitnessFunction {
//    /**
//     * One central place to control if we allow clusters of size 0.
//     *
//     * @param size
//     * @return
//     */
//    public static boolean invalidClusterSize(int size) {
//        return Main.CONFIG.getBoolean("allowEmptyClusters") ? size < 0 : size <= 0;
//    }

    /**
     * @param clusters      dense list of clusters to measure fitness of
     *                      //@param featureSubset boolean representation of which features selected
     * @param instances
     * @param featureSubset
     * @param featureSubsetforFitness
     * @return fitness value. see isMaximising for whether maximising or minimizing function
     */
    public double fitness(List<? extends Cluster> clusters, List<Instance> instances, boolean[] featureSubset, boolean[] featureSubsetforFitness);

    /**
     * @param oldFitness
     * @param newFitness
     * @return true if newFitness is more fit than oldFitness; false otherwise.
     */
    public boolean fitnessBetterThan(double oldFitness, double newFitness);

    public double worstPossibleFitness();
}
