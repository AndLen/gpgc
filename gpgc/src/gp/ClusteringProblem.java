package gp;

import clustering.CentroidCluster;
import clustering.Cluster;
import data.Instance;
import data.UnlabelledInstance;
import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPIndividual;
import ec.gp.GPProblem;
import ec.gp.GPTree;
import ec.simple.SimpleFitness;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import other.DatasetUtils;
import other.Main;
import other.Util;

import java.util.*;
import java.util.stream.Collectors;

import static gp.GPUtils.fitnessFunction;
import static gp.GPUtils.instances;

/**
 * Created by lensenandr on 5/07/16.
 */
public class ClusteringProblem extends GPProblem {
    public static final double NUM_ITERATIONS = 100;
    public static boolean K_MEANS = true;
    public static boolean SINGLE_LINKAGE = true;
    public Instance currentInstance;

    public static List<? extends Cluster> kCluster(List<Instance> instances, Map<Instance, List<Double>> constructedFeatures, Random random) {
        //Kmeans...
        if (Main.CONFIG.getBoolean("singleLinkage")) {
            return getClustersSingleLinkage(instances, constructedFeatures);
        }
        List<Instance> startInstances = initialiseMeans(instances, GPUtils.numClusters, random);
        List<List<Double>> means = new ArrayList<>();
        for (Instance i : startInstances) {
            means.add(constructedFeatures.get(i));
        }

        boolean manhattan = Main.CONFIG.getProperty("gpDist", "eucledian").equalsIgnoreCase("manhattan");
        List<BasicCluster> clusters = new ArrayList<>();
        for (int i = 0; i < NUM_ITERATIONS; i++) {
            clusters = prototypeCluster(instances, means, constructedFeatures);

            List<List<Double>> newMeans = new ArrayList<>();
            boolean meanChanged = false;
            for (BasicCluster cluster : clusters) {
                List<Double> newMean;
                if (Main.CONFIG.getBoolean("kMedoid")) {
                    double minIntraSum = Double.MAX_VALUE;
                    newMean = cluster.val;

                    for (Instance thisMedoid : cluster.instances) {
                        List<Double> medoidCF = constructedFeatures.get(thisMedoid);
                        double intraSum = 0;
                        for (Instance instance : cluster.instances) {
                            intraSum += manhattan ? getManhattanDistance(constructedFeatures.get(instance), medoidCF) : getDistance(constructedFeatures.get(instance), medoidCF);
                        }
                        if (intraSum < minIntraSum) {
                            minIntraSum = intraSum;
                            newMean = medoidCF;
                        }
                    }

                } else {
                    double[] newMeanArray = new double[means.get(0).size()];

                    for (Instance instance : cluster.instances) {
                        List<Double> thisCFs = constructedFeatures.get(instance);
                        for (int j = 0; j < thisCFs.size(); j++) {
                            newMeanArray[j] += thisCFs.get(j);
                        }

                    }
                    //Check
                    for (int j = 0; j < newMeanArray.length; j++) {
                        newMeanArray[j] /= cluster.instances.size();

                    }
                    newMean = new ArrayList<>();
                    for (double v : newMeanArray) {
                        newMean.add(v);
                    }

                }
                if ((manhattan ? getManhattanDistance(cluster.val, newMean) : getDistance(cluster.val, newMean)) > 0.001) {
                    meanChanged = true;
                }
                newMeans.add(newMean);
            }
            means = newMeans;
            if (!meanChanged) {
                //  System.out.println("Exiting on iter " + i);
                break;
            }
        }
        List<CentroidCluster> finalClusters = new ArrayList<>();
        for (BasicCluster cluster : clusters) {
            CentroidCluster centroidCluster = new CentroidCluster(UnlabelledInstance.fromList(cluster.val));
            centroidCluster.addAllInstances(cluster.instances);
            finalClusters.add(centroidCluster);
        }

        return finalClusters;
        //   return getClustersDynamicRange(constructedFeatures);
        //  return getQuickishMeanLinkage(instances, constructedFeatures);

        //return getMeanNNLinkage(instances, constructedFeatures);
        //  return getMedianLinkage(instances, constructedFeatures);
        //return getClustersSingleLinkage(instances, constructedFeatures);
    }


    static double getManhattanDistance(List<Double> thisCFs, List<Double> clusterCFs) {
        double distance = 0;
        for (int i = 0; i < thisCFs.size(); i++) {
            double diff = thisCFs.get(i) - clusterCFs.get(i);
            distance += Math.abs(diff);
        }

        return distance;
    }

    public static List<Instance> initialiseMeans(List<? extends Instance> instances, int numClusters, Random random) {
        //Random initial means, choosing each instance at most once
        List<Instance> means = new ArrayList<>();
        while (means.size() != numClusters) {
            Instance nextInstance = instances.get(random.nextInt(instances.size()));
            if (!means.contains(nextInstance)) {
                means.add(nextInstance);
            }
        }
        return means;
    }

    public static List<BasicCluster> prototypeCluster(List<? extends Instance> instances, List<List<Double>> means, Map<Instance, List<Double>> cFs) {
        List<BasicCluster> clusters = means.stream().map(BasicCluster::new).collect(Collectors.toList());
        boolean manhattan = Main.CONFIG.getProperty("gpDist", "eucledian").equalsIgnoreCase("manhattan");

        for (Instance instance : instances) {
            double closestDistance = Double.MAX_VALUE;
            BasicCluster bestCluster = null;
            for (BasicCluster cluster : clusters) {
                List<Double> thisCFs = cFs.get(instance);
                List<Double> clusterCFs = cluster.val;

                double distance = manhattan ? getManhattanDistance(thisCFs, clusterCFs) : getDistance(thisCFs, clusterCFs);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    bestCluster = cluster;
                }
            }
            if (bestCluster == null) {
                System.err.println("HALP");
            } else {
                bestCluster.addInstance(instance);
            }

        }
        return clusters;
    }

    static double getDistance(List<Double> thisCFs, List<Double> clusterCFs) {
        double distance = 0;
        for (int i = 0; i < thisCFs.size(); i++) {
            double diff = thisCFs.get(i) - clusterCFs.get(i);
            distance += (diff * diff);
        }

        distance = Math.sqrt(distance);
        return distance;
    }


    static List<? extends Cluster> getMedianLinkageSimple(List<Instance> instances, Map<Instance, Double> constructedFeatures) {
        Map<Instance, Group> groupings = new HashMap<>();
        Set<Group> groups = new HashSet<>();
        int index = 0;
        for (Instance i : instances) {
            Group group = new Group(i, index++);
            groups.add(group);
            groupings.put(i, group);
        }


        PriorityQueue<GroupEdge> edges = new PriorityQueue<>();
        for (int i = 0; i < instances.size(); i++) {
            Instance instance = instances.get(i);
            double thisConstructed = constructedFeatures.get(instance);
            for (int j = i + 1; j < instances.size(); j++) {
                Instance otherInstance = instances.get(j);
                double distance = Math.abs(thisConstructed - constructedFeatures.get(otherInstance));
                edges.offer(new GroupEdge(distance, groupings.get(instance), groupings.get(otherInstance)));
            }
        }

        while (groups.size() > GPUtils.numClusters) {
            //  System.out.println(groups.size());

            GroupEdge edge = edges.poll();
            Group group1 = edge.g1;
            Group group2 = edge.g2;
            //Haven't already merged
            if (!group1.visited && !group2.visited) {
                Group newGroup = new Group(index++);
                newGroup.addInstances(group1.instances);
                newGroup.addInstances(group2.instances);
                group1.visited = true;
                group2.visited = true;

                groups.remove(group1);
                groups.remove(group2);
                for (Group group : groups) {
                    //New group isn't in here
                    double medianLinkage = findMedianLinkage(newGroup, group, constructedFeatures);
                    edges.offer(new GroupEdge(medianLinkage, newGroup, group));
                }
                groups.add(newGroup);
            }
        }
        List<CentroidCluster> clusters = new ArrayList<>();
        for (Group group : groups) {
            //TODO check
            CentroidCluster cluster = new CentroidCluster(group.instances.get(0));
            cluster.addAllInstances(group.instances);
            clusters.add(cluster);
        }
        return clusters;

    }
//
//    static List<? extends Cluster> getMeanNNLinkage(List<Instance> instances, Map<Instance, Double> constructedFeatures) {
//        int numInstances = instances.size();
//
//        Set<Grouping> groups = new HashSet<>();
//        int index = 0;
//
//        for (Instance i : instances) { // O(n)
//            Grouping group = new Grouping(i, index++);
//            groups.add(group);
//        }
//        for (Grouping group : groups) {
//            group.findNearestNeighbour(groups);
//        }
//
//
//        //do
//    }


    static List<? extends Cluster> getQuickishMeanLinkage(List<Instance> instances, Map<Instance, Double> constructedFeatures) {
        Map<Instance, Group> groupings = new HashMap<>();
        int numInstances = instances.size();

        Set<Group> groups = new HashSet<>();
        int index = 0;
        Map<Group, Map<Group, Double>> distances = new HashMap<>();

        for (Instance i : instances) { // O(n)
            Group group = new Group(i, index++);
            groups.add(group);
            groupings.put(i, group);
            distances.put(group, new HashMap<>());
        }

        //double[][] dissims = new double[numInstances][numInstances];

        PriorityQueue<GroupEdge> edges = new PriorityQueue<>();
        for (int i = 0; i < numInstances; i++) { //O(n^2)
            Instance instance = instances.get(i);
            Group g1 = groupings.get(instance);
            double thisConstructed = constructedFeatures.get(instance);
            Map<Group, Double> thisDistances = distances.get(g1);
            for (int j = i + 1; j < numInstances; j++) {
                Instance otherInstance = instances.get(j);
                Group g2 = groupings.get(otherInstance);
                double g2constructed = constructedFeatures.get(otherInstance);
                double distance = Math.abs(thisConstructed - g2constructed);
                thisDistances.put(g2, distance);
                distances.get(g2).put(g1, distance);
                edges.offer(new GroupEdge(distance, g1, g2));
            }
        }

        while (groups.size() > GPUtils.numClusters) { //O(n)
            //  System.out.println(groups.size());

            GroupEdge edge = edges.poll(); //O(log n)
            Group group1 = edge.g1;
            Group group2 = edge.g2;
            //Haven't already merged
            if (!group1.visited && !group2.visited) {
                Group newGroup = new Group(index++);
                newGroup.addInstances(group1.instances);
                newGroup.addInstances(group2.instances);
                group1.visited = true;
                group2.visited = true;

                groups.remove(group1);
                groups.remove(group2);
                Map<Group, Double> thisDistances = new HashMap<>();
                for (Group group : groups) { //O(n)
                    //New group isn't in here
                    double meanLinkage = findMeanLinkage(group1, group2, group, distances);
                    thisDistances.put(group, meanLinkage);
                    distances.get(group).put(newGroup, meanLinkage);
                    edges.offer(new GroupEdge(meanLinkage, newGroup, group)); //O(log n)
                }
                distances.put(newGroup, thisDistances);
                groups.add(newGroup);
            }
        }
        List<CentroidCluster> clusters = new ArrayList<>();
        for (Group group : groups) {
            //TODO check
            CentroidCluster cluster = new CentroidCluster(group.instances.get(0));
            cluster.addAllInstances(group.instances);
            clusters.add(cluster);
        }
        return clusters;

    }

    private static double findMeanLinkage(Group group1, Group group2, Group neighbour, Map<Group, Map<Group, Double>> dissims) {
        //Derp
        double group1Dist;
        if (group1.id < neighbour.id) {
            group1Dist = dissims.get(group1).get(neighbour);
        } else {
            group1Dist = dissims.get(neighbour).get(group1);
        }
        double group2Dist;
        if (group2.id < neighbour.id) {
            group2Dist = dissims.get(group2).get(neighbour);
        } else {
            group2Dist = dissims.get(neighbour).get(group2);
        }
        int g1Size = group1.instances.size();
        int g2Size = group2.instances.size();
        return ((g1Size * group1Dist) + (g2Size * group2Dist)) / (g1Size + g2Size);
    }

    private static double findMedianLinkage(Group newGroup, Group group, Map<Instance, Double> constructedFeatures) {
        List<Double> dists = new ArrayList<>(((newGroup.instances.size() * group.instances.size()) + 1) / 2);
        for (Instance i1 : newGroup.instances) {
            for (Instance i2 : group.instances) {
                dists.add(Math.abs(constructedFeatures.get(i1) - constructedFeatures.get(i2)));
            }

        }
        Collections.sort(dists);
        return dists.get(dists.size() / 2);
    }

    static List<? extends Cluster> getMedianLinkage(List<Instance> instances, Map<Instance, Double> constructedFeatures) {
        int numInstances = instances.size();
        Set<Integer> remainingClusters = new HashSet<>();
        double[][] d = new double[numInstances][numInstances];
        int[] nearestNeighbours = new int[numInstances];
        double[] minDists = new double[numInstances];
        Arrays.fill(minDists, Double.MAX_VALUE);
        PriorityQueue<IndexDist> queue = new PriorityQueue<>();
        Map<Integer, Group> clusters = new HashMap<>();
        for (int x = 0; x < numInstances - 1; x++) {
            Instance instance = instances.get(x);
            clusters.put(x, new Group(instance, x));
            remainingClusters.add(x);
            d[x][x] = 0;
            for (int y = x + 1; y < numInstances; y++) {
                Instance other = instances.get(y);
                double dissim = Math.abs(constructedFeatures.get(instance) - constructedFeatures.get(other));
                d[x][y] = dissim;
                d[y][x] = dissim;

                //TODO: Check reflectiveness
                if (dissim < minDists[x]) {
                    minDists[x] = dissim;
                    nearestNeighbours[x] = y;
                }
            }
            queue.offer(new IndexDist(x, minDists[x]));

        }

        while (clusters.values().size() > GPUtils.numClusters) {
            IndexDist next = queue.peek();
            int a = next.instanceIndex;
            int b = nearestNeighbours[a];
            double delta = minDists[a];

            while (delta != d[a][b]) {
                //Needed?
                //   System.out.println("Blah");
                double minDist = Double.MAX_VALUE;
                for (int x = a + 1; x < numInstances; x++) {
                    double dissim = d[a][x];
                    if (dissim < minDist) {
                        minDist = dissim;
                        nearestNeighbours[a] = x;
                    }
                    //This is O(n)....
                    queue.remove(next);
                    minDists[a] = minDist;
                    queue.offer(new IndexDist(a, minDist));
                    next = queue.peek();
                    a = next.instanceIndex;
                    b = nearestNeighbours[a];
                    delta = minDists[a];
                }
            }
            IndexDist poll = queue.poll();
            //This is O(n)....
            queue.remove(next);
            clusters.get(b).addInstances(clusters.get(a).instances);
            clusters.remove(a);
            remainingClusters.remove(a);

            for (int x : remainingClusters) {
                if (x != b) {
                    double newDist = Math.sqrt(d[a][x] / 2 + d[b][x / 2] - d[a][b]);
                    d[x][b] = newDist;
                    d[b][x] = newDist;
                }
            }
            for (int x : remainingClusters) {
                if (x < a) {
                    if (nearestNeighbours[x] == a) {
                        nearestNeighbours[x] = b;
                    }
                }
            }
            for (int x : remainingClusters) {
                if (x < b) {
                    if (d[x][b] < minDists[x]) {
                        nearestNeighbours[x] = b;
                        //FIXME Correct?
                        queue.remove(new IndexDist(x, minDists[x]));
                        queue.offer(new IndexDist(x, d[x][b]));
                        minDists[x] = d[x][b];

                    }

                }
            }
            double minDist = Double.MAX_VALUE;
            int nearestIndex = -1;
            for (int x : remainingClusters) {
                if (x > b) {
                    double dissim = d[x][b];
                    if (dissim < minDist) {
                        minDist = dissim;
                        nearestIndex = x;
                    }
                }
            }
            nearestNeighbours[b] = nearestIndex;

            queue.remove(new IndexDist(nearestIndex, minDists[b]));
            queue.offer(new IndexDist(nearestIndex, minDist));
            minDists[b] = minDist;


        }

        List<CentroidCluster> clusterList = new ArrayList<>();
        for (Group group : clusters.values()) {
            //TODO check
            CentroidCluster cluster = new CentroidCluster(group.instances.get(0));
            cluster.addAllInstances(group.instances);
            clusterList.add(cluster);
        }
        return clusterList;
    }

    static List<? extends Cluster> getClustersSingleLinkage(List<Instance> instances, Map<Instance, List<Double>> constructedFeatures) {
        List<ClusterEdge> edges = new ArrayList<>((instances.size() * instances.size()) / 2);
        for (int i = 0; i < instances.size(); i++) {
            Instance instance = instances.get(i);
            List<Double> thisConstructed = constructedFeatures.get(instance);
            for (int j = i + 1; j < instances.size(); j++) {
                Instance otherInstance = instances.get(j);
                double distance = getDistance(thisConstructed, constructedFeatures.get(otherInstance));
                edges.add(new ClusterEdge(distance, instance, otherInstance));
            }
        }
        //O(n log(n))
        Collections.sort(edges);
        Map<Instance, Group> groupings = new HashMap<>();
        Set<Group> groups = new HashSet<>();
        int index = 0;
        for (Instance i : instances) {
            Group group = new Group(i, index++);
            groups.add(group);
            groupings.put(i, group);
        }

        int nextIndex = 0;
        while (groups.size() > GPUtils.numClusters) {
            //  System.out.println(groups.size());

            ClusterEdge edge = edges.get(nextIndex++);
            Group group1 = groupings.get(edge.i1);
            Group group2 = groupings.get(edge.i2);
            //Haven't already merged
            if (!group1.equals(group2)) {
                List<Instance> toMove = group2.instances;
                group1.addInstances(toMove);
                toMove.forEach(i -> groupings.put(i, group1));
                //RIP
                groups.remove(group2);
            }
        }
        List<CentroidCluster> clusters = new ArrayList<>();
        for (Group group : groups) {
            //TODO check
            CentroidCluster cluster = new CentroidCluster(group.instances.get(0));
            cluster.addAllInstances(group.instances);
            clusters.add(cluster);
        }
        return clusters;
    }

    static List<? extends Cluster> getClustersDynamicRange(List<Instance> instances, Map<Instance, Double> constructedFeatures) {
        //instanceIndex, cF
        List<Double> cFs = new ArrayList<>();
        List<InstanceVal> instanceCFs = new ArrayList<>();
        constructedFeatures.forEach((k, v) -> {
            cFs.add(v);
            instanceCFs.add(new InstanceVal(k, v));
        });
        Collections.sort(cFs);
        Collections.sort(instanceCFs);

        List<DistCF> distCFs = new ArrayList<>();
        for (int i = 1; i < cFs.size(); i++) {
            Double prevCF = cFs.get(i - 1);
            Double cF = cFs.get(i);
            distCFs.add(new DistCF(cF - prevCF, prevCF));

        }
        //Biggest -> smallest
        Collections.sort(distCFs);

        List<Double> chosenBreaks = new ArrayList<>();
        for (int i = 0; i < GPUtils.numClusters - 1; i++) {
            chosenBreaks.add(distCFs.get(i).cF);
        }
        Collections.sort(chosenBreaks);

        List<Cluster> clusters = new ArrayList<>();
        List<Instance> clusterToBe = new ArrayList<>();
        Iterator<Double> breaks = chosenBreaks.iterator();
        double nextBreak = breaks.next();
        double prevBreak = instanceCFs.get(0).val;

        for (InstanceVal x : instanceCFs) {

            if (x.val > nextBreak) {
                UnlabelledInstance centroid = new UnlabelledInstance(new double[]{prevBreak});
                CentroidCluster cluster = new CentroidCluster(centroid);
                cluster.addAllInstances(clusterToBe);
                clusters.add(cluster);
                clusterToBe = new ArrayList<>();
                prevBreak = nextBreak;
                if (breaks.hasNext()) {
                    nextBreak = breaks.next();
                } else {
                    nextBreak = Double.POSITIVE_INFINITY;
                }
            }
            clusterToBe.add(x.instance);

        }
        UnlabelledInstance centroid = new UnlabelledInstance(new double[]{prevBreak});
        CentroidCluster cluster = new CentroidCluster(centroid);
        cluster.addAllInstances(clusterToBe);
        clusters.add(cluster);

        return clusters;
    }

    @Override
    public void evaluate(EvolutionState state, Individual ind, int subpopulation, int threadnum) {

        int seed = GPUtils.getSeed(ind);
        Random random = new Random(seed);
        if (!ind.evaluated)  // don't bother reevaluating
        {
            List<? extends Cluster> clusters = getClusters(state, (GPIndividual) ind, threadnum, random);
            Util.removeEmptyClusters(clusters);
            double fitness;
            // fitness = PerformanceEvaluation.fMeasure(clusters,instances);

                SimpleFitness f = ((SimpleFitness) ind.fitness);

                //TODO: Fix
                if (clusters.size() == GPUtils.numClusters) {
                    fitness = fitnessFunction.fitness(clusters, instances, DatasetUtils.ALL_FEATURES, DatasetUtils.ALL_FEATURES);
                } else {
                    fitness = fitnessFunction.worstPossibleFitness();
                }
                //TODO
                f.setFitness(state, fitness, false);
            ind.evaluated = true;

        }
    }

    public List<? extends Cluster> getClusters(EvolutionState state, GPIndividual ind, int threadnum, Random random) {
        DoubleData input = (DoubleData) (this.input);
        Map<Instance, List<Double>> constructedFeatures = new HashMap<>();
        for (Instance instance : instances) {
            List<Double> thisCf = new ArrayList<>();

            currentInstance = instance;
            GPTree[] trees = ind.trees;
            for (GPTree tree : trees) {
                tree.child.eval(
                        state, threadnum, input, stack, ind, this);
                double constructedFeature = input.val;
                thisCf.add(constructedFeature);
            }

            constructedFeatures.put(instance, thisCf);

        }

        //return getQuickishMeanLinkage(instances,constructedFeatures);
        //return getClustersDynamicRange(GPUtils.instances, constructedFeatures);

        return internalCluster(instances, constructedFeatures, random);


    }

    protected List<? extends Cluster> internalCluster(List<Instance> instances, Map<Instance, List<Double>> constructedFeatures, Random random) {
        switch (Main.CONFIG.getProperty("gpClusterAlgorithm", "kmeans")) {
            case "dbscan":
                return dbscan(instances, constructedFeatures);

            default:
                return kCluster(instances, constructedFeatures, random);

        }
    }

    private List<? extends Cluster> dbscan(List<Instance> instances, Map<Instance, List<Double>> constructedFeatures) {
        List<DBScanPoint> points = new ArrayList<>(instances.size());
        instances.forEach(i -> points.add(new DBScanPoint(i, constructedFeatures.get(i))));
        //double sumAvgNNDist = 0;
        List<Double> avgNNDists = new ArrayList<>();
        for (Instance i : instances) {
            avgNNDists.add(findAvgDistNN(i, instances, 10, constructedFeatures));
        }
        Collections.sort(avgNNDists);

        double v = instances.size() * 0.75;
        double epsilon = avgNNDists.get((int) v);//sumAvgNNDist / instances.size();//DatasetUtils.EUCLIDEAN_DISSIM_MAP.medianDissim() / 5;
        //epsilon /= 2;
        //wut
        int minPoints = 3;
        DBSCANClusterer<DBScanPoint> dbscanClusterer = new DBSCANClusterer<>(epsilon, minPoints);
        List<org.apache.commons.math3.ml.clustering.Cluster<DBScanPoint>> clusters = dbscanClusterer.cluster(points);

        List<CentroidCluster> finalClusters = new ArrayList<>(clusters.size());
        for (org.apache.commons.math3.ml.clustering.Cluster<DBScanPoint> cluster : clusters) {
            List<DBScanPoint> clusterPoints = cluster.getPoints();
            List<Instance> clusterInstances = new ArrayList<>(clusterPoints.size());
            clusterPoints.forEach(c -> clusterInstances.add(c.instance));
            UnlabelledInstance centre = Cluster.computeCentre(clusterInstances.get(0).numFeatures(), clusterInstances);
            CentroidCluster centroidCluster = new CentroidCluster(centre);
            centroidCluster.addAllInstances(clusterInstances);
            finalClusters.add(centroidCluster);
        }

        LinkedHashMap<Instance, Cluster> instanceClusterMap = new LinkedHashMap<>();
        for (Cluster cluster : finalClusters) {
            for (Instance instance : cluster.getInstancesInCluster()) {
                instanceClusterMap.put(instance, cluster);
            }

        }

        for (Instance instance : instances) {
            //It's a noise point...own cluster?
            if (!instanceClusterMap.containsKey(instance)) {
                CentroidCluster centroidCluster = new CentroidCluster(instance);
                centroidCluster.addInstance(instance);
                finalClusters.add(centroidCluster);
            }
        }


        return finalClusters;

    }

    private double findAvgDistNN(Instance i, List<Instance> instances, int numNNs, Map<Instance, List<Double>> constructedFeatures) {
        PriorityQueue<InstanceVal> nns = new PriorityQueue<>();
        List<Double> cFs = constructedFeatures.get(i);

        for (Instance instance : instances) {
            if (!i.equals(instance)) {
                nns.add(new InstanceVal(instance, getDistance(cFs, constructedFeatures.get(instance))));
            }
        }
        double distSum = 0;
        for (int j = 0; j < numNNs; j++) {
            distSum += nns.poll().val;
        }
        return distSum / numNNs;

    }

    public static class DBScanPoint implements Clusterable {
        private final Instance instance;
        private final double[] points;

        public DBScanPoint(Instance instance, List<Double> cFs) {

            this.instance = instance;
            this.points = new double[cFs.size()];
            for (int i = 0; i < cFs.size(); i++) {
                points[i] = cFs.get(i);
            }
        }

        @Override
        public double[] getPoint() {
            return points;
        }
    }


    public static class ClusterEdge implements Comparable<ClusterEdge> {
        public final double distance;
        public final Instance i1;
        public final Instance i2;

        private ClusterEdge(double distance, Instance i1, Instance i2) {
            this.distance = distance;
            this.i1 = i1;
            this.i2 = i2;
        }

        @Override
        public int compareTo(ClusterEdge o) {
            return Double.compare(distance, o.distance);
        }
    }

    public static class GroupEdge implements Comparable<GroupEdge> {
        public final double distance;
        public final Group g1;
        public final Group g2;

        private GroupEdge(double distance, Group g1, Group g2) {
            this.distance = distance;
            this.g1 = g1;
            this.g2 = g2;
        }

        @Override
        public int compareTo(GroupEdge o) {
            return Double.compare(distance, o.distance);
        }
    }

    public static class Group {
        private final List<Instance> instances;
        private final int id;
        public boolean visited = false;

        public Group(Instance initial, int id) {
            this.id = id;
            instances = new ArrayList<>();
            instances.add(initial);
        }

        public Group(int id) {
            this.id = id;
            instances = new ArrayList<>();
        }

        void addInstance(Instance instance) {
            instances.add(instance);
        }

        void addInstances(List<Instance> instanceList) {
            instances.addAll(instanceList);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Group group = (Group) o;

            return id == group.id;

        }

        @Override
        public int hashCode() {
            return id;
        }
    }

    private static class InstanceVal implements Comparable<InstanceVal> {
        private final Instance instance;
        private final double val;

        public InstanceVal(Instance instance, double val) {

            this.instance = instance;
            this.val = val;
        }

        @Override
        public int compareTo(InstanceVal o) {
            return Double.compare(val, o.val);
        }
    }

    private static class DistCF implements Comparable<DistCF> {
        private final double dist;
        private final double cF;

        private DistCF(double dist, double cF) {
            this.dist = dist;
            this.cF = cF;
        }

        @Override
        public int compareTo(DistCF o) {
            return Double.compare(o.dist, dist);
        }
    }

    private static class IndexDist implements Comparable<IndexDist> {
        private final int instanceIndex;
        private final double val;

        public IndexDist(int instanceIndex, double val) {

            this.instanceIndex = instanceIndex;
            this.val = val;
        }

        @Override
        public int compareTo(IndexDist o) {
            return Double.compare(val, o.val);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            IndexDist indexDist = (IndexDist) o;

            if (instanceIndex != indexDist.instanceIndex) return false;
            return Double.compare(indexDist.val, val) == 0;

        }

        @Override
        public int hashCode() {
            int result;
            long temp;
            result = instanceIndex;
            temp = Double.doubleToLongBits(val);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            return result;
        }

        @Override
        public String toString() {
            return "IndexDist{" +
                    "instanceIndex=" + instanceIndex +
                    ", val=" + val +
                    '}';
        }
    }

    private static class BasicCluster {
        private final List<Double> val;
        private final List<Instance> instances;

        public BasicCluster(List<Double> val) {
            this.val = val;
            this.instances = new ArrayList<>();
        }

        public void addInstance(Instance instance) {
            instances.add(instance);
        }
    }
}
