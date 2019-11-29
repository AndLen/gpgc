package clustering;

import data.Instance;

import java.util.List;

/**
 * Created by lensenandr on 27/04/16.
 */
public interface KEstimator {
    public int estimatedNumberOfClusters(List<Instance> instances);
}
