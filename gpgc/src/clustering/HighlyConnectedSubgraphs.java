package clustering;

import data.Instance;
import edu.princeton.cs.algs4.Edge;
import edu.princeton.cs.algs4.EdgeWeightedGraph;
import org.jgrapht.alg.StoerWagnerMinimumCut;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import other.DatasetUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class HighlyConnectedSubgraphs {
    static ExecutorService forkJoinPool = Executors.newWorkStealingPool(Math.max(1, Runtime.getRuntime().availableProcessors() / 2));

    public static List<CentroidCluster> doHCS(List<Instance> instances) throws ExecutionException, InterruptedException {
        Map<Instance, Set<Instance>> edgesBasedOnMaxThreshold = NaiveGraphThreshold.getEdgesBasedOnThreshold(instances,
                NaiveGraphThreshold.getMaxDist(instances));

        List<List<Instance>> clusters = doMinCut(instances, edgesBasedOnMaxThreshold);
        List<CentroidCluster> centroidClusters = new ArrayList<>();
        for (List<Instance> cluster : clusters) {
            CentroidCluster cluster1 = new CentroidCluster(DatasetUtils.getSampleMean(cluster));
            cluster1.addAllInstances(cluster);
            centroidClusters.add(cluster1);
        }
        return centroidClusters;

    }

    private static EdgeWeightedGraph turnIntoGraph(List<Instance> instances, Map<Instance, Set<Instance>> edges) {
        EdgeWeightedGraph graph = new EdgeWeightedGraph(instances.size());
        for (Instance instance : instances) {
            //As we have subgraphs now, don't want hanging edges
            edges.get(instance).stream().filter(instances::contains).forEach(neighbour -> {
                graph.addEdge(new Edge(instances.indexOf(instance), instances.indexOf(neighbour), DatasetUtils.EUCLIDEAN_DISSIM_MAP.getDissim(instance, neighbour)));
            });
        }
        return graph;
    }


    private static SimpleWeightedGraph<Instance, DefaultWeightedEdge> turnIntoGraph2(List<Instance> instances, Map<Instance, Set<Instance>> edges) {
        SimpleWeightedGraph<Instance, DefaultWeightedEdge> g =
                new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

        for (Instance instance : instances) {
            g.addVertex(instance);
            //As we have subgraphs now, don't want hanging edges
            edges.get(instance).stream().filter(instances::contains).forEach(neighbour -> {
                g.addVertex(neighbour);
                if (!g.containsEdge(instance, neighbour)) {
                    DefaultWeightedEdge edge = g.addEdge(instance, neighbour);
                    double dissim = DatasetUtils.EUCLIDEAN_DISSIM_MAP.getDissim(instance, neighbour);
                    g.setEdgeWeight(edge, dissim);
                }
            });
        }
        return g;
    }

    private static List<List<Instance>> doMinCut(List<Instance> thisGraph, Map<Instance, Set<Instance>> edges) throws ExecutionException, InterruptedException {

        if (thisGraph.size() == 1) {
            List<List<Instance>> singleCluster = new ArrayList<>(1);
            singleCluster.add(thisGraph);
            return singleCluster;
        }
        SimpleWeightedGraph<Instance, DefaultWeightedEdge> graph = turnIntoGraph2(thisGraph, edges);
        StoerWagnerMinimumCut<Instance, DefaultWeightedEdge> stoerWagnerMinimumCut = new StoerWagnerMinimumCut<>(graph);
        Set<Instance> oneSide = stoerWagnerMinimumCut.minCut();
        List<Instance> subgraph1 = new ArrayList<>(oneSide);
        List<Instance> subgraph2 = new ArrayList<>(thisGraph);
        subgraph2.removeAll(oneSide);
        //figure out the size of the cut in #edges
        long cuts = 0;
        for (Instance instance : thisGraph) {
            if (subgraph1.contains(instance)) {
                for (Instance neighbour : edges.get(instance)) {
                    if (thisGraph.contains(neighbour)) {
                        if (!subgraph1.contains(neighbour)) {
                            cuts++;
                        }
                    }
                }
            }
        }
        System.out.println(cuts);
        if (cuts >= thisGraph.size() / 2) {
            List<List<Instance>> singleCluster = new ArrayList<>(1);
            singleCluster.add(thisGraph);
            return singleCluster;
        } else {
            Future<List<List<Instance>>> future = forkJoinPool.submit(() -> doMinCut(subgraph1, edges));
            Future<List<List<Instance>>> future1 = forkJoinPool.submit(() -> doMinCut(subgraph2, edges));
            List<List<Instance>> lists = future.get();
            lists.addAll(future1.get());
            return lists;
        }
    }

}
