package gp.locum;

import clustering.CentroidCluster;
import clustering.Cluster;
import data.Instance;
import data.UnlabelledInstance;
import ec.EvolutionState;
import ec.gp.GPIndividual;
import ec.util.Parameter;
import gp.ClusteringProblem;
import gp.DoubleData;
import other.DatasetUtils;
import other.Main;
import other.Util;

import java.util.*;

import static gp.GPUtils.instances;


/**
 * Created by lensenandr on 8/08/16.
 */
public class LocumClusteringProblem extends ClusteringProblem {
    public static int NUM_NEIGHBOURS_TO_EVALUATE = 10;
    public static int NUM_NEIGHBOURS_TO_CONNECT = 1;
    private static String MULTI_TREE_TYPE;
    private static boolean SELF_CONN_ALLOWED;
    public Instance instance1, instance2;

    public static List<? extends Cluster> graphToClusters(Map<Instance, Set<Instance>> locums) {
        Set<TemporaryCluster> clusters = new HashSet<>();
        for (Instance instance : locums.keySet()) {
            Set<Instance> neighbours = locums.get(instance);
            Map<Instance, TemporaryCluster> neighbourClusters = new HashMap<>();

            //this instance could already be in
            neighbours.add(instance);

            neighbours.forEach(n -> {
                //What if instance linked to itself...this seems fine

                TemporaryCluster cluster = instanceIsClustered(clusters, n);
                if (cluster != null) {
                    neighbourClusters.put(n, cluster);
                }
            });

            //Yes this could all be quicker, but doing it simply avoids a lot of nasty bugs (neighbours w/ same cluster etc) -- besides, O(n^2) part is the locum bit.
            TemporaryCluster cluster = new TemporaryCluster();
            clusters.add(cluster);

            if (!neighbourClusters.isEmpty()) {
                //Add all remaining clusters into the first
                for (TemporaryCluster nextCluster : neighbourClusters.values()) {
                    cluster.addAll(nextCluster);
                    //Prevents issues with neighbours having same cluster
                    nextCluster.clear();
                    clusters.remove(nextCluster);
                }

            }
            cluster.addAll(neighbours);


        }//
        List<Cluster> finalClusters = new ArrayList<>();
        for (TemporaryCluster cluster : clusters) {
            Set<Instance> mergedCluster = cluster.mergeAll();
            UnlabelledInstance clusterCentroid = DatasetUtils.getSampleMean(mergedCluster);

            CentroidCluster centroidCluster = new CentroidCluster(clusterCentroid);
            centroidCluster.addAllInstances(mergedCluster);
            finalClusters.add(centroidCluster);
        }

        return finalClusters;
    }

    private static TemporaryCluster instanceIsClustered(Set<TemporaryCluster> clusters, Instance instance) {
        for (TemporaryCluster cluster : clusters) {
            if (cluster.contains(instance)) return cluster;

        }
        return null;
    }

    /**
     * Fully explores graph from given node, not re-visting nodes...directed tho
     */
    private static void explore(Instance instance, Set<Instance> cluster, Map<Instance, Set<Instance>> locums) {
        System.out.println(instance + " " + cluster.contains(instance));
        if (!cluster.contains(instance)) {
            cluster.add(instance);
            for (Instance neighbour : locums.get(instance)) {
                explore(neighbour, cluster, locums);
            }
        }
    }

    public void setup(final EvolutionState state, final Parameter base) {
        super.setup(state, base);
        String numEval = Main.CONFIG.getProperty("numEval");
        switch (numEval) {
            case "squareRoot":
                NUM_NEIGHBOURS_TO_EVALUATE = (int) Math.ceil(Math.pow(instances.size(), 1d / 2d));
                break;
            case "cubeRoot":
                NUM_NEIGHBOURS_TO_EVALUATE = (int) Math.ceil(Math.pow(instances.size(), 1d / 3d));
                break;
            default:
                NUM_NEIGHBOURS_TO_EVALUATE = Integer.parseInt(numEval);
                break;
        }
        String numConn = Main.CONFIG.getProperty("numConnect");
        switch (numConn) {
            case "squareRoot":
                NUM_NEIGHBOURS_TO_CONNECT = (int) Math.ceil(Math.pow(instances.size(), 1d / 2d));
                break;
            case "cubeRoot":
                NUM_NEIGHBOURS_TO_CONNECT = (int) Math.ceil(Math.pow(instances.size(), 1d / 3d));
                break;
            default:
                NUM_NEIGHBOURS_TO_CONNECT = Integer.parseInt(numConn);
                break;
        }
        String multitreeType = Main.CONFIG.getProperty("multitreeType");
        if (multitreeType != null) {
            MULTI_TREE_TYPE = multitreeType;
        }
        SELF_CONN_ALLOWED = Main.CONFIG.getBoolean("selfConn");
        Util.LOG.printf("Neighbours to eval: %d, Neighbours to connect: %d, MT type: %s\n", NUM_NEIGHBOURS_TO_EVALUATE, NUM_NEIGHBOURS_TO_CONNECT, MULTI_TREE_TYPE == null ? "none" : MULTI_TREE_TYPE);
    }

    public List<? extends Cluster> getClusters(EvolutionState state, GPIndividual ind, int threadnum, Random random) {
        //This way to prevent NPEs

        boolean isSingleVote = "singleVote".equals(MULTI_TREE_TYPE);
        boolean isWeightedVote = "weightedVote".equals(MULTI_TREE_TYPE);

        DoubleData input = (DoubleData) (this.input);
        //O(n^2)
        Map<Instance, Set<Instance>> locums = new HashMap<>();
        for (int i = 0; i < instances.size(); i++) {
            Instance instance = instances.get(i);

            instance1 = instance;
            //GPTree tree = ;
            List<Instance> neighbours = DatasetUtils.NEAREST_NEIGHBOURS.get(instance);
            Set<Instance> connections = new HashSet<>();

            int numNeighboursToUse = Math.min(NUM_NEIGHBOURS_TO_EVALUATE, neighbours.size());
            if (SELF_CONN_ALLOWED) {
                neighbours.add(instance1);
                numNeighboursToUse++;
            }

            int numConnections = Math.min(numNeighboursToUse, NUM_NEIGHBOURS_TO_CONNECT);
            int numTrees = ind.trees.length;

            PriorityQueue<InstanceWeight> queue = new PriorityQueue<>(Comparator.reverseOrder());

            //Legacy GPGC -- keep code identical just in case!!
            if (numTrees == 1) {
                for (int j = 0; j < numNeighboursToUse; j++) {
                    instance2 = neighbours.get(j);
                    ind.trees[0].child.eval(
                            state, threadnum, input, stack, ind, this);
                    double result = input.val;
                    queue.offer(new InstanceWeight(instance2, result));
                }

            } else {
                //Array then sort, more flexible than priority queue for MT etc...
                double[] neighbourWeights = new double[numNeighboursToUse];
                for (int t = 0; t < numTrees; t++) {
                    double[] thisNeighbourWeights = new double[numNeighboursToUse];
                    //First we just compute them all
                    for (int j = 0; j < numNeighboursToUse; j++) {
                        instance2 = neighbours.get(j);
                        ind.trees[t].child.eval(
                                state, threadnum, input, stack, ind, this);
                        thisNeighbourWeights[j] = input.val;
                    }
                    if (isSingleVote) {
                        neighbourWeights[Util.getMaxArg(thisNeighbourWeights)]++;
                    } else if (isWeightedVote) {
                        //Can this be sexier...
                        for (int k = 0; k < thisNeighbourWeights.length; k++) {
                            neighbourWeights[k] += thisNeighbourWeights[k];
                        }
                    } else {
                        throw new IllegalArgumentException(MULTI_TREE_TYPE);
                    }
                }

                for (int n = 0; n < neighbourWeights.length; n++) {
                    queue.add(new InstanceWeight(neighbours.get(n), neighbourWeights[n]));
                }


            }
            for (int k = 0; k < numConnections; k++) {
                connections.add(queue.poll().instance);
            }

            locums.put(instance, connections);
        }
        return graphToClusters(locums);
    }


    private static class InstanceWeight implements Comparable<InstanceWeight> {
        private final Instance instance;
        private final double weight;

        private InstanceWeight(Instance instance, double weight) {
            this.instance = instance;
            this.weight = weight;
        }

        @Override
        public int compareTo(InstanceWeight o) {
            return Double.compare(weight, o.weight);
        }
    }

    /**
     * Don't actually merge clusters until the last minute.
     */
    private static class TemporaryCluster {
        Set<Instance> cluster = new HashSet<>();
//        Set<Set<Instance>> groupedClusters = new HashSet<>();


        public boolean contains(Instance instance) {
            return cluster.contains(instance);
//            for (Set<Instance> groupedCluster : groupedClusters) {
//                if (groupedCluster.contains(instance)) return true;
//            }
//            return false;
        }

        public void addAll(TemporaryCluster cluster) {
            this.cluster.addAll(cluster.cluster);
            // groupedClusters.addAll(nextCluster.groupedClusters);
        }

        public void clear() {
            cluster.clear();
        }


        public void addAll(Set<Instance> neighbours) {
            cluster.addAll(neighbours);

        }

        public Set<Instance> mergeAll() {
            return cluster;
//            Set<Instance> mergedClusters = new HashSet<>();
//            groupedClusters.forEach(mergedClusters::addAll);
//            return mergedClusters;
        }
    }

}
