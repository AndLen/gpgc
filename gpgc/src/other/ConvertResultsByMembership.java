package other;

import clusterFitness.LimitedConnectednessFitness;
import clusterFitness.LocumFitness;
import clustering.CentroidCluster;
import clustering.Cluster;
import data.Instance;
import javafx.util.Pair;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static other.DatasetUtils.*;
import static other.Main.SetupSystem.getRawInstances;
import static other.Util.LOG;

/**
 * Created by lensenandr on 6/03/18.
 * Used for ECJ journal to convert existing results to using the Adjusted Rand Index (ARI) to save a lot of computation in regenerating...
 * <p>
 * VERY HACKY
 */
public class ConvertResultsByMembership {
    static final Pattern newResult = Pattern.compile("Partition with (.*?) clusters.*");
    static final Pattern newCluster = Pattern.compile("Cluster (.*?):.*");
    static final Pattern clusterIndicies = Pattern.compile(".*Instance Indices: \\[(.*?)\\].*");
    private static final boolean RR = true;
    private static Pattern endOfResult = Pattern.compile("==============================");

    public static void main(String[] args) throws IOException {
        //Not the right way to do this.
        DatasetUtils.FEATURE_MIN = 0;
        DatasetUtils.FEATURE_MAX = 1;
        DatasetUtils.FEATURE_RANGE = (FEATURE_MAX - FEATURE_MIN);
        DatasetUtils.FEATURE_RANGE2 = (FEATURE_RANGE) * (FEATURE_RANGE);
        PerformanceEvaluation.evaluations = new LinkedHashMap<>();
        PerformanceEvaluation.evaluations.put("Conn.", (p, i, fs, fse) -> LimitedConnectednessFitness.avgClusterConnectivity(p));
        PerformanceEvaluation.evaluations.put("Spar.", (p, i, fS, fSE) -> PerformanceEvaluation.meanMaxMinIntra(p));
        PerformanceEvaluation.evaluations.put("Sep.", (p, i, fS, fSE) -> PerformanceEvaluation.meanMinMinInter(p));
        PerformanceEvaluation.evaluations.put("ARI", (p, i, fS, fSE) -> PerformanceEvaluation.adjustedRandIndex(p, i));
        PerformanceEvaluation.evaluations.put("FM", (p, i, fS, fSE) -> PerformanceEvaluation.fMeasure(p, i));

        Path path = Paths.get(args[0]);
        clearDirectories(path);
        traverse(path);


    }

    public static void clearDirectories(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            if (path.getFileName().toString().equals("ecjREFORMATTED")) {
                for (Path dataPath : Files.newDirectoryStream(path)) {
                    System.out.println("Deleting: " + dataPath);
                    Files.delete(dataPath);
                }
                System.out.println("Deleting: " + path);
                Files.delete(path);
            } else {
                for (Path dataPath : Files.newDirectoryStream(path)) {
                    clearDirectories(dataPath);
                }
            }


        }
    }


    static void traverse(Path path) throws IOException {
        System.out.println(path);
        if (Files.isDirectory(path)) {
            for (Path dataPath : Files.newDirectoryStream(path)) {
                traverse(dataPath);
            }
        } else {
            if (path.toString().toLowerCase().endsWith(RR ? ".outrr" : ".out"))
                try {
                    readInClustersFromMemberships(path);
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                    //Try the next file anyway
                }
        }
    }

    public static void readInClustersFromMemberships(Path toConvert) throws IOException {

        String dataset = toConvert.getFileName().toString().split("-")[0].trim();
        if (dataset.endsWith(".out")) {
            //Lovely. Wrong type of out file... (the combined ones)
            return;
        }
        System.out.println("Dataset: " + dataset);
        Util.LOG = new Util.LoggerStream("ecjREFORMATTED/rf-" + dataset, toConvert.getParent());
        //This probably can't handle more than 1 method
        String methodName = null;
        List<Instance> goldStandard = getGoldStandard(dataset);

        DoConversion callback = new DoECJMeasures(goldStandard);

        int numClusters = -1;
        int clusterIndex = -1;
        List<String> lines = Files.readAllLines(toConvert);
        List<Cluster> clusterMemberships = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String l = lines.get(i);
            Matcher matcher = newResult.matcher(l);
            if (matcher.matches()) {
                numClusters = Integer.parseInt(matcher.group(1).trim());
                clusterIndex = 0;
                clusterMemberships = new ArrayList<>();
                continue;
            }
            matcher = newCluster.matcher(l);
            if (matcher.matches()) {
                int thisIndex = Integer.parseInt(matcher.group(1).trim());
                if (thisIndex != clusterIndex + 1) {
                    throw new IllegalStateException("Uh oh");
                } else clusterIndex++;

                matcher = clusterIndicies.matcher(l);
                if (!matcher.matches()) {
                    throw new IllegalStateException();
                }
                String[] instances = matcher.group(1).split(",");
                List<Instance> thisMemberships = new ArrayList<>();
                for (String instance : instances) {
                    thisMemberships.add(goldStandard.get(Integer.valueOf(instance.trim())));
                }
                CentroidCluster centroidCluster = new CentroidCluster(DatasetUtils.getSampleMean(thisMemberships));
                centroidCluster.addAllInstances(thisMemberships);
                clusterMemberships.add(centroidCluster);
                continue;
            }
            matcher = endOfResult.matcher(l);
            if (matcher.matches()) {
                if (clusterIndex != numClusters) {
                    System.err.println("Parse error: " + clusterIndex + " " + numClusters);
                    throw new Error();

                }                             //Can get a "====" at the start of the file --> skip for now.
                else if (numClusters != -1) {
                    callback.outputConversion(clusterMemberships, goldStandard, methodName);
                    //Fail quick
                    numClusters = -1;
                    clusterIndex = -1;
                    clusterMemberships = null;

                } else if (methodName == null) {
                    String s = lines.get(++i);
                    methodName = s.substring(0, s.indexOf(":"));
                }
                continue;
            }
            //Some rubbish
            if (l.contains("methodNames=")) {
                int fromIndex = l.indexOf("methodNames=");
                int end = l.indexOf(",", fromIndex);
                methodName = l.substring(fromIndex + 12, end);
            }

        }
        callback.doFinalOutput(methodName);
    }

    private static void doARI(List<? extends Cluster> clusterMemberships, List<Instance> goldStandard) {
        System.out.printf("FM: %.3f; ARI: %.3f\n", PerformanceEvaluation.fMeasure(clusterMemberships, goldStandard), PerformanceEvaluation.adjustedRandIndex(clusterMemberships, goldStandard));
    }

    private static List<Instance> getGoldStandard(String dataset) throws IOException {
        System.out.println(dataset);
        List<Path> paths = Files.find(Paths.get("/home/lensenandr/IdeaProjects/phd/datasets"), 10, (path, basicFileAttributes) -> {
            String fileName = path.getFileName().toString();
            return fileName.startsWith(dataset) && (fileName.endsWith(".andrew") || fileName.endsWith(".dat") || fileName.endsWith(".data") || fileName.endsWith(".ssAndrew"));
        }).collect(Collectors.toList());
        if (paths.size() != 1) {
            throw new IllegalStateException(String.valueOf(paths));
        } else {
            List<String> lines = Files.readAllLines(paths.get(0));
            String[] header = lines.get(0).split(",");


            List<Instance> rawInstances = scaleInstances(getRawInstances(lines, header));
            rawInstances.sort((a, b) -> Integer.compare(a.instanceID, b.instanceID));
            return rawInstances;

        }

    }

//    private static List<List<Integer>> getListClusters(List<Instance> rawInstances) {
//        Map<String, List<Integer>> goldStandard = new HashMap<>();
//
//        for (Instance instance : rawInstances) {
//            String classLabel = instance.getClassLabel();
//            List<Integer> thisCluster = goldStandard.getOrDefault(classLabel, new ArrayList<>());
//            thisCluster.add(instance.instanceID);
//            goldStandard.put(classLabel, thisCluster);
//        }
//        //Don't care about labels
//        return new ArrayList<>(goldStandard.values());
//    }

    public interface DoConversion {
        void outputConversion(List<? extends Cluster> partition, List<Instance> instances, String testName);

        void doFinalOutput(String methodName);
    }

    private static class DoECJMeasures implements DoConversion {
        private final ResultsKeeper resultsKeeper;

        public DoECJMeasures(List<Instance> goldStandard) {
            Main.CONFIG.put("dissimMapToUse", "euclidean");
            DatasetUtils.initialise(goldStandard, true);
            resultsKeeper = new ResultsKeeper();


        }

        @Override
        public void outputConversion(List<? extends Cluster> partition, List<Instance> instances, String testName) {

            boolean[] allSel = new boolean[instances.get(0).numFeatures()];
            Arrays.fill(allSel, true);

            Pair<String, Map<String, Double>> evaluationResult = PerformanceEvaluation.evaluateOnAll(partition, instances, allSel);
            synchronized (resultsKeeper) {
                resultsKeeper.addEvaluationResult(testName, evaluationResult);
                resultsKeeper.addFeatureSubsetResult(testName, allSel);
                resultsKeeper.addClusterNumResult(testName, partition.size());
                //hmm
                resultsKeeper.addFitnessFunctionResult(testName, LocumFitness.gpgcFitness(partition, instances));
            }
        }

        @Override
        public void doFinalOutput(String testName) {
            int numRuns = resultsKeeper.evalResults.get(testName).get("Conn.").size();
            System.out.println("Runs: " + numRuns);
            Main.CONFIG.put("runs", Integer.toString(numRuns));
            String[] methodNames = {testName};

            resultsKeeper.printFeatureHistogram();
            resultsKeeper.printRunAverages(true, methodNames);
            resultsKeeper.printEvalFinal(true, methodNames);
            LOG.println("Latex:");
            resultsKeeper.printFullLatex();
            resultsKeeper.printFinalResults(methodNames);

        }
    }
}
