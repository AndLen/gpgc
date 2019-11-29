package other;

import clustering.Cluster;

import java.util.List;

/**
 * Created by lensenandr on 14/03/16.
 */
public class TestResult {
    public final List<? extends Cluster> partition;
    public final boolean[] featureSubset;
    public final double fitness;
    public final List<Double> fitnessesOverRun;

    public TestResult(List<? extends Cluster> partition, double fitness, List<Double> fitnessesOverRun) {
        this.partition = partition;
        this.fitness = fitness;
        this.fitnessesOverRun = fitnessesOverRun;
        this.featureSubset = DatasetUtils.ALL_FEATURES;
    }
}
