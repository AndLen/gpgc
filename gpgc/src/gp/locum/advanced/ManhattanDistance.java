package gp.locum.advanced;

import data.Instance;
import other.DatasetUtils;

/**
 * Created by lensenandr on 10/02/17.
 */
public class ManhattanDistance extends DistanceTerminal {
    @Override
    public String toString() {
        return "manhattanDist";
    }

    @Override
    public double getDistance(Instance instance1, Instance instance2) {
        return DatasetUtils.MANHATTAN_DISSIM_MAP.getDissim(instance1, instance2);
    }
}
