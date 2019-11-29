package clustering;

import data.Instance;
import gp.locum.LocumClusteringProblem;
import other.DatasetUtils;
import other.Main;

import java.util.*;

/**
 * Created by lensenandr on 30/03/17.
 */
public class NaiveGraphNN {
    public static List<? extends Cluster> cluster(List<? extends Instance> instances) {

        Map<Instance, Set<Instance>> edges = new HashMap<>();

        for (Instance instance : instances) {
            List<Instance> neighbours = DatasetUtils.NEAREST_NEIGHBOURS.get(instance);
            int numNN = Main.CONFIG.getInt("numNN");
            numNN = Math.min(numNN, neighbours.size());
            edges.put(instance, new HashSet<>(neighbours.subList(0, numNN)));
        }
        return LocumClusteringProblem.graphToClusters(edges);
    }


}
