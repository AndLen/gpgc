package clustering;

import data.Instance;
import gp.locum.LocumClusteringProblem;
import other.DatasetUtils;

import java.util.*;

/**
 * Created by lensenandr on 30/03/17.
 */
public class NaiveGraphThreshold {
    public static List<? extends Cluster> cluster(List<? extends Instance> instances) {
        Map<Instance, Set<Instance>> edges = getEdgesBasedOnMaxThreshold(instances);
        return LocumClusteringProblem.graphToClusters(edges);
    }

    public static Map<Instance, Set<Instance>> getEdgesBasedOnMaxThreshold(List<? extends Instance> instances) {
        double maxDist = getMaxDist(instances);

        return getEdgesBasedOnThreshold(instances, maxDist);
    }

    public static double getMaxDist(List<? extends Instance> instances) {
        double maxDist = 0d;
        for (Instance instance : instances) {
            maxDist = Double.max(maxDist, DatasetUtils.EUCLIDEAN_DISSIM_MAP.getDissim(instance, DatasetUtils.NEAREST_NEIGHBOURS.get(instance).get(0)));
        }
        System.err.println(maxDist);
        return maxDist;
    }

    public static Map<Instance, Set<Instance>> getEdgesBasedOnThreshold(List<? extends Instance> instances, double threshold) {
        Map<Instance, Set<Instance>> edges = new HashMap<>();

        for (Instance instance : instances) {
            Set<Instance> chosenNeighbours = new HashSet<>();

            List<Instance> neighbours = DatasetUtils.NEAREST_NEIGHBOURS.get(instance);
            for (Instance neighbour : neighbours) {
                if (DatasetUtils.EUCLIDEAN_DISSIM_MAP.getDissim(instance, neighbour) <= threshold) {
                    chosenNeighbours.add(neighbour);
                } else {
                    break;
                }
            }
            edges.put(instance, chosenNeighbours);
        }
        return edges;
    }


}
