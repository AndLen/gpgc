package other;

import data.Instance;
import ec.EvolutionState;
import ec.gp.ADFStack;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import ec.gp.GPTree;
import gp.*;
import gp.locum.LocumClusteringProblem;
import gp.locum.LocumFeatureNode;
import tests.reference.GPGCMTWVAICD5;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ReadInMultiTree {
    public static final int NUM_TREES = 1;

    //USED IN PAPER
    //
    // /local/scratch/lensenandr/gpgcJournalBak/andrew/1000d20c.andrew-Tue-Apr-25-18-52-36-NZST-2017-2981794.ecj15
    // /local/scratch/lensenandr/ecjFiles/10d10cE.andrew-Tue-Apr-11-12-48-49-NZST-2017-2974543.ecj15


    // /local/scratch/lensenandr/gpgcJournalBak/handlKnowles/ellipsoid.50d40c.1.dat-Tue-Apr-25-17-42-36-NZST-2017-2981767.ecj5
    // /local/scratch/lensenandr/gpgcJournalBak/andrew/1000d100c.andrew-Tue-Apr-25-19-53-51-NZST-2017-2981800.ecj11
    // /local/scratch/lensenandr/gpgcJournalBak/handlKnowles/10d10c.0.dat-Tue-Apr-25-17-20-51-NZST-2017-2981752.ecj19
//    static String s = "Tree 0:\n" +
//            " (|sub| (|add| (min 0.37220301259636046 (if\n" +
//            "     (|sub| (max 0.24682944264655993 I1F564) (|add|\n" +
//            "         I1F458 0.3575192272844402)) (min (div 0.5966343548717762\n" +
//            "     0.6613867932034522) (min I1F91 I0F952)) (min\n" +
//            "     (sub I1F713 0.1344336792987043) (+ 0.08592964192946961\n" +
//            "     0.3267929550090082)))) (max (div 0.6358275779219201\n" +
//            "     0.5099131379573252) (sub I0F443 0.26925047479779285)))\n" +
//            "     (+ (max (sub I0F443 0.26925047479779285)\n" +
//            "         (|sub| I1F597 0.18729821312778294)) (|add|\n" +
//            "         (max 0.831252375489486 0.8365985151267916)\n" +
//            "         (max 0.831252375489486 0.8365985151267916))))\n" +
//            "Tree 1:\n" +
//            " (mul (|add| (min 0.025572726326887896 0.9431732178235404)\n" +
//            "     (|sub| I0F243 0.9480815232978089)) (|sub|\n" +
//            "     I1F385 I1F727))\n" +
//            "Tree 2:\n" +
//            " (|sub| (min (|sub| I1F39 0.4077214328187414)\n" +
//            "     (|sub| I1F39 0.4077214328187414)) (if (|sub|\n" +
//            "     I1F39 0.4077214328187414) (sub I0F874 0.984397858654988)\n" +
//            "     (max I0F34 0.49020825688109226)))\n" +
//            "Tree 3:\n" +
//            " (mul (+ (if 0.8554673501565115 0.30137129879280455\n" +
//            "     0.6310462358771802) (min I1F793 0.01594723514266161))\n" +
//            "     (mul (+ I1F793 0.6126053709355194) (if (min\n" +
//            "         I1F793 0.01594723514266161) (|add| (if 0.8894362085574663\n" +
//            "         0.06909620264578631 I1F599) (+ 0.21437339861219373\n" +
//            "         0.35715539077026226)) (if 0.11481016035145275\n" +
//            "         I1F929 I0F867))))\n" +
//            "Tree 4:\n" +
//            " (sub (div (mul (|sub| I1F861 I1F919) (|sub|\n" +
//            "     I1F861 I1F919)) 0.3173431564249276) (|sub|\n" +
//            "     (div (div 0.3705667826960245 0.6229617592943515)\n" +
//            "         (if 0.07252130957181291 0.36087304850171775\n" +
//            "             0.8194451792109824)) (|sub| (min I1F376 0.8896292735489121)\n" +
//            "     (div 0.3705667826960245 0.6229617592943515))))\n" +
//            "Tree 5:\n" +
//            " (+ 0.8766691959810412 (mul (max (|sub| I0F595\n" +
//            "     I1F28) (min I0F595 0.6545235744759307)) 0.5699747124882573))\n" +
//            "Tree 6:\n" +
//            " (sub (|sub| (min (+ I0F426 0.08840424652246082)\n" +
//            "     (sub 0.009123184411161 I0F168)) (mul (sub\n" +
//            "     0.009123184411161 I0F168) (sub 0.009123184411161\n" +
//            "     I0F168))) (sub (mul (mul I1F895 I1F424) (+\n" +
//            "     I0F426 0.08840424652246082)) (mul (mul I1F895\n" +
//            "     I1F424) (mul I1F895 I1F424))))";

    public static void main(String args[]) throws Exception {
        Path ecjFilePath = Paths.get(args[0]);
        List<String> ecjLines = Files.readAllLines(ecjFilePath);
        int startIndex = 0;
        while (!ecjLines.get(startIndex).startsWith("Best Individual of Run:")) {
            startIndex++;
        }
        while (!ecjLines.get(startIndex).startsWith("Tree 0:")) {
            startIndex++;
        }
        List<String> lines = ecjLines.subList(startIndex, ecjLines.size());

        ArrayList<String> argsList = new ArrayList<>();
        String configPath = Paths.get(System.getProperty("user.dir"), "/src/tests/config", "10d10cE.config").toString();

        Collections.addAll(argsList, configPath);
        argsList.addAll(new GPGCMTWVAICD5().getTestConfig());

        //args need to be standard w/ main
        String[] strings = argsList.toArray(new String[argsList.size()]);
        Main.SetupSystem setupSystem = new Main.SetupSystem(strings).invoke();
        GPUtils.instances = setupSystem.getProcessedInstances();
        GPUtils.numClusters = setupSystem.getNumClusters();
        GPUtils.numFeatures = setupSystem.getNumFeatures();

        final EvolutionState evolutionState = GPUtils.getEvolutionState(GPUtils.numClusters, "src/gp/locum.params");

        final GPIndividual individual = new GPIndividual();
        individual.trees = new GPTree[NUM_TREES];
        int index = 0;
        for (int treeIndex = 0; treeIndex < NUM_TREES; treeIndex++) {
            index += 1; //Skip the "Tree x" line.
            StringBuilder treeString = new StringBuilder();
            while (index < lines.size() && !lines.get(index).startsWith("Tree ")) {
                treeString.append(lines.get(index++).replaceAll("\n", "").replaceAll("\t", ""));
            }
            String oneLineTree = treeString.toString();
            System.out.println(oneLineTree);
            GPTree tree = readInGPTree(oneLineTree);
            tree.printTreeForHumans(evolutionState, 0);
            individual.trees[treeIndex] = tree;
        }

        double[][] similarities = computePairWiseSimilarities(individual, evolutionState);
        GPGCMeasure gpSimilarityAsDistance = new GPGCMeasure(similarities, individual, evolutionState);
        compareNNOrderings(GPUtils.instances, gpSimilarityAsDistance);
//        List<Instance> means = KMeansClustering.initialiseMeans(GPUtils.instances, GPUtils.numClusters);
//
//        List<CentroidCluster> clusters = KMeansClustering.doKMeans(GPUtils.instances, 100, means,gpSimilarityAsDistance);
//        for (String line : Util.printUserFriendly(clusters, 0, GPUtils.instances).split("\n")) {
//            //Otherwise it doesn't print right
//            System.out.println(line);
//        }
//        System.out.println("ARI: " + PerformanceEvaluation.adjustedRandIndex(clusters, GPUtils.instances));
    }

    private static void compareNNOrderings(List<Instance> instances, DissimilarityMap otherMap) {
        Map<Instance, List<Instance>> standardOrdering = DatasetUtils.NEAREST_NEIGHBOURS;
        Map<Instance, List<Instance>> otherOrdering = makeNNOrderings(instances, otherMap);
        double sumDiff = 0.0;
        double sumAtZeroth = 0;
        for (Instance instance : instances) {
            double indexInOriginal = getDiffBetweenNeighbours(standardOrdering.get(instance), otherOrdering.get(instance));
            if (indexInOriginal == 0) sumAtZeroth += 1;
            sumDiff += indexInOriginal;
        }
        System.out.println("Sum diff: " + sumDiff);
        sumDiff /= instances.size();
        System.out.println("Diffs per instance: " + sumDiff);
        System.out.printf("Mean Percentage Diff: %.2f%%\n", sumDiff * 100d);
        System.out.printf("Percentage time unchanged: %.2f%%\n", (sumAtZeroth / instances.size()) * 100d);

    }

    private static double getDiffBetweenNeighbours(List<Instance> standardList, List<Instance> otherList) {
        int indexInOriginal = standardList.indexOf(otherList.get(0));
        return indexInOriginal;
        //return  i1.equals(i2) ? 0: 1;
        //        return otherList.subList(0,10).contains(i1) ? 0 : 1;
//        List<Instance> list110 = standardList.subList(0, 10);
//        List<Instance> list210 = otherList.subList(0, 10);
//        int numSharedBetween = 0;
//        for (int i = 0; i < 10; i++) {
//            Instance instance = list110.get(i);
//            if (list210.contains(instance)) {
//                numSharedBetween++;
//            }
//        }
//        return 10-numSharedBetween;

    }

    private static Map<Instance, List<Instance>> makeNNOrderings(List<Instance> instances, final DissimilarityMap otherMap) {
        Map<Instance, List<Instance>> nns = new HashMap<>();
        for (int i = 0; i < instances.size(); i++) {
            Instance instance = instances.get(i);

            PriorityQueue<Instance> nnQueue = new PriorityQueue<>(Comparator.comparingDouble(i1 -> otherMap.getDissim(instance, i1)));

            List<Instance> neighbours = DatasetUtils.NEAREST_NEIGHBOURS.get(instance);
            for (int j = 0; j < LocumClusteringProblem.NUM_NEIGHBOURS_TO_EVALUATE; j++) {
                nnQueue.add(neighbours.get(j));
            }

            List<Instance> nearestNeighbours = new ArrayList<>();
            while (!nnQueue.isEmpty()) {
                nearestNeighbours.add(nnQueue.poll());
            }

            nns.put(instance, nearestNeighbours);
        }
        return nns;
    }

    private static double[][] computePairWiseSimilarities(GPIndividual ind, EvolutionState state) {
        LocumClusteringProblem problem = new LocumClusteringProblem();
        problem.stack = new ADFStack();
        //(LocumClusteringProblem) state.evaluator.p_problem;
        int numInstances = GPUtils.instances.size();
        double[][] similarities = new double[numInstances][numInstances];
        for (int i = 0; i < numInstances; i++) {
            for (int j = 0; j < numInstances; j++) {
                problem.instance1 = GPUtils.instances.get(i);
                problem.instance2 = GPUtils.instances.get(j);
                double thisSum = 0;
                for (int t = 0; t < NUM_TREES; t++) {
                    DoubleData similarity = new DoubleData();
                    ind.trees[t].child.eval(
                            state, 0, similarity, problem.stack, ind, problem);
                    thisSum += similarity.val;
                }
                similarities[problem.instance1.instanceID][problem.instance2.instanceID] = thisSum;
            }
        }
        return similarities;
    }

    private static GPTree readInGPTree(String treeString) {
        GPTree tree = new GPTree();
        tree.child = readWithOffset(treeString, 0).node;
        return tree;
    }

    private static ParsedNode readWithOffset(String treeString, int offset) {
        while (treeString.charAt(offset) == ' ') {
            offset++;
        }
        if (treeString.charAt(offset) == '(') {
            return parseFunction(treeString, offset);
        } else {
            return parseTerminal(treeString, offset);

        }
    }

    private static ParsedNode parseFunction(String treeString, int offset) {
        int nextSpaceIndex = treeString.indexOf(' ', offset);
        String functionName = treeString.substring(offset + 1, nextSpaceIndex);
        System.out.println(functionName);
//
        //Here's the nasty bit
        ParsedNode child1 = readWithOffset(treeString, nextSpaceIndex + 1);
        ParsedNode child2 = readWithOffset(treeString, child1.finalIndex + 1);
        GPNode thisNode;
        int finalChildIndex;
        if (functionName.equals("if")) {
            ParsedNode child3 = readWithOffset(treeString, child2.finalIndex + 1);
            thisNode = new If();
            thisNode.children = new GPNode[3];
            thisNode.children[0] = child1.node;
            thisNode.children[1] = child2.node;
            thisNode.children[2] = child3.node;
            finalChildIndex = child3.finalIndex;
        } else {
            thisNode = getGPNode(functionName);
            thisNode.children = new GPNode[2];
            thisNode.children[0] = child1.node;
            thisNode.children[1] = child2.node;
            finalChildIndex = child2.finalIndex;
        }
        char endOfThisFunction = treeString.charAt(finalChildIndex + 1);
        if (endOfThisFunction != ')') {
            throw new IllegalStateException(endOfThisFunction + " " + treeString.substring(offset));
        }
        return new ParsedNode(thisNode, finalChildIndex + 1);
    }

    private static GPNode getGPNode(String functionName) {
        GPNode thisNode;

        switch (functionName) {
            case "+":
                thisNode = new Add();
                break;
            case "sub":
                thisNode = new gp.Sub();
                break;
            case "mul":
                thisNode = new Mul();
                break;
            case "div":
                thisNode = new Div();
                break;
            case "|add|":
                thisNode = new AbsAdd();
                break;
            case "|sub|":
                thisNode = new AbsSub();
                break;
            case "max":
                thisNode = new Max();
                break;
            case "min":
                thisNode = new Min();
                break;
            default:
                throw new IllegalArgumentException(functionName);
        }
        return thisNode;
    }

    private static ParsedNode parseTerminal(String treeString, int offset) {
        //Terminal, so read until space or close bracket
        int nextSpace = treeString.indexOf(' ', offset);
        int nextCloseParen = treeString.indexOf(')', offset);
        int endOfTerminal;
        if (nextSpace == -1) endOfTerminal = nextCloseParen;
        else if (nextCloseParen == -1) endOfTerminal = nextSpace;
        else endOfTerminal = Math.min(nextSpace, nextCloseParen);
        return new ParsedNode(createTerminal(treeString.substring(offset, endOfTerminal)), endOfTerminal - 1);
    }

    private static GPNode createTerminal(String substring) {
        substring = substring.trim();
        GPNode terminal;
        if (substring.startsWith("I")) {
            LocumFeatureNode featureNode = new LocumFeatureNode();
            boolean firstInstance = substring.charAt(1) == '0';
            int featureIndex = Integer.parseInt(substring.substring(3, substring.length()));
            featureNode.val = firstInstance ? featureIndex : GPUtils.numFeatures + featureIndex;
            terminal = featureNode;
        } else {
            DoubleConstantNode doubleConstantNode = new DoubleConstantNode();
            doubleConstantNode.constant = Double.parseDouble(substring);
            terminal = doubleConstantNode;
        }
        terminal.children = new GPNode[0];
        return terminal;
    }


    private static class ParsedNode {
        private final GPNode node;
        private final int finalIndex;

        private ParsedNode(GPNode node, int finalIndex) {

            this.node = node;
            this.finalIndex = finalIndex;
        }
    }

    /**
     * THIS IS VERY WEIRD WHEN USE FOR NEIGHBOURS NOT TRAINED ON (I.E. >10).
     * TODO: CAREFUL
     */
    private static class GPGCMeasure implements Util.DistanceMeasure, DissimilarityMap {
        private final double[][] similarities;
        private final GPIndividual individual;
        private final EvolutionState evolutionState;
        private final double averageSimilarity;
        private final double mostNegativeSimilarity;
        private final double mostPositiveSimilarity;

        public GPGCMeasure(double[][] similarities, GPIndividual individual, EvolutionState evolutionState) {
            this.similarities = similarities;
            this.individual = individual;
            this.evolutionState = evolutionState;
            this.averageSimilarity = Arrays.stream(similarities).flatMapToDouble(Arrays::stream).average().orElseThrow(IllegalStateException::new);
            this.mostNegativeSimilarity = Arrays.stream(similarities).flatMapToDouble(Arrays::stream).min().getAsDouble();
            this.mostPositiveSimilarity = Arrays.stream(similarities).flatMapToDouble(Arrays::stream).max().getAsDouble();
        }

        @Override
        public double distance(Instance instance1, Instance instance2, boolean[] featureSubset) {
            if (instance1.instanceID >= 0 && instance2.instanceID >= 0) {
                return getDissim(instance1, instance2);
            } else {
                LocumClusteringProblem problem = new LocumClusteringProblem();
                problem.stack = new ADFStack();
                problem.instance1 = instance1;
                problem.instance2 = instance2;
                double thisSum = 0;
                for (int t = 0; t < NUM_TREES; t++) {
                    DoubleData similarity = new DoubleData();
                    individual.trees[t].child.eval(
                            evolutionState, 0, similarity, problem.stack, individual, problem);
                    thisSum += similarity.val;
                }
                return convertSimToDissim(thisSum);
            }
        }

        @Override
        public double getDissim(Instance i1, Instance i2) {
            return convertSimToDissim(similarities[i1.instanceID][i2.instanceID]);
        }

        public double convertSimToDissim(double similarity) {
            //e.g. scale -20 to [-200,150] --> [0,1] sim. then dissim is 1-sim.
            return 1 - Util.scale(similarity, mostNegativeSimilarity, mostPositiveSimilarity);
            //return 1/ similarity;
        }

        @Override
        public double averageDissim() {
            //Questionable.
            return convertSimToDissim(averageSimilarity);
        }
    }
}


//    int endOfThisFunction = offset;
//        int numOpenParens = 0;
//        while(numOpenParens >= 0){
//            if(treeString.charAt(endOfThisFunction) == '('){
//                numOpenParens++;
//            }else if(treeString.charAt(endOfThisFunction) == ')'){
//                numOpenParens--;
//            }
//            endOfThisFunction++;
//        }