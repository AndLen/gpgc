package gp.locum.advanced;

import data.Instance;
import other.DatasetUtils;

/**
 * Created by lensenandr on 10/02/17.
 */
public class EuclideanDistance extends DistanceTerminal {
    @Override
    public String toString() {
        return "euclideanDist";
    }

    @Override
    public double getDistance(Instance instance1, Instance instance2) {
        return DatasetUtils.EUCLIDEAN_DISSIM_MAP.getDissim(instance1, instance2);
    }
}
