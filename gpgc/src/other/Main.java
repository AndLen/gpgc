package other;

import clusterFitness.ClusterFitnessFunction;
import clustering.KEstimator;
import clustering.SilhouetteKMeans;
import clustering.SilhouettePAM;
import data.Instance;
import javafx.util.Pair;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static other.DatasetUtils.*;
import static other.Util.DIVIDER;
import static other.Util.LOG;

/**
 * Created by lensenandr on 2/03/16.
 */
public class Main {

    public static final BetterProperties CONFIG = new BetterProperties();
    public static final int BOARD_WIDTH = 200;
    public static int RUN = 0;
    public static String[] METHOD_NAMES;

    public static void main(String args[]) throws IOException, ExecutionException, InterruptedException {
        SetupSystem setupSystem = new SetupSystem(args).invoke();
        List<Instance> processedInstances = setupSystem.getProcessedInstances();
        int numClusters = setupSystem.getNumClusters();
        int numFeatures = setupSystem.getNumFeatures();


        METHOD[] methods = getMethods();
        METHOD_NAMES = getMethodNames(methods);


        ResultsKeeper resultsKeeper = new ResultsKeeper();

        if (CONFIG.getBoolean("parallelRuns")) {
            //This only works if: (1) each run is independent (and you don't care about output ordering) and
            // (2) your run has no OTHER parallelism using the shared threadPool (or you'll deadlock...) AND
            // (3) You don't use the RUN parameter...
            RUN = -1;
            List<Future<Void>> futures = new ArrayList<>();
            for (int i = 0; i < CONFIG.getInt("runs"); i++) {
                int finalNumClusters = numClusters;
                Future<Void> voidFuture = Util.submitJob(() -> {
                    doSingleRun(new ArrayList<>(processedInstances), finalNumClusters, numFeatures, methods, resultsKeeper);
                    return null;
                });
                futures.add(voidFuture);
            }
            for (Future<Void> future : futures) {
                future.get();
            }


        } else {
            for (RUN = 0; RUN < CONFIG.getInt("runs"); RUN++) {
                numClusters = doSingleRun(processedInstances, numClusters, numFeatures, methods, resultsKeeper);
            }
        }
        LOG.println(CONFIG.getProperty("dataset").toUpperCase() + " with " + CONFIG.toString() + " final results:");
        resultsKeeper.printFinalResults(methods);
        Util.shutdownThreads();
        LOG.closeStreams();
    }

    public static int doSingleRun(List<Instance> processedInstances, int numClusters, int numFeatures, METHOD[] methods, ResultsKeeper resultsKeeper) {
        if (CONFIG.getBoolean("silNumClusters")) {
            //Diff number for each of 30 runs
            if (CONFIG.getBoolean("silNumClustersKMeans")) {
                numClusters = SilhouetteKMeans.getMedianKFromSil(processedInstances);
            } else {
                numClusters = SilhouettePAM.getMedianKFromSil(processedInstances);
            }
        }
        StringBuilder runResultOutput = new StringBuilder(String.format("RUN %2d: ", RUN));
        StringBuilder runRepResultOutput = new StringBuilder();
        for (int j = 0; j < methods.length; j++) {
            METHOD method = methods[j];
            Util.resetRunParams();
            //New FF for every independent run
            ClusterFitnessFunction fitnessFunction = getFitnessFunction();

            TestResult testResult = method.doTest(numFeatures, numClusters, processedInstances, fitnessFunction);
            String testName = METHOD_NAMES[j];
            //Just in case?
            Util.removeEmptyClusters(testResult.partition);
            double fitness = testResult.fitness;//fitnessFunction.fitness(testResult.partition, scaledInstances, testResult.featureSubset,DatasetUtils.featuresToUseForFitness(testResult.featureSubset));

            UsedFeatures uf = new UsedFeatures(testResult.featureSubset);

            runRepResultOutput.append(String.format(DIVIDER + "%s:\nFitness over Run:%s\n Best partition of run:\n", testName, Util.formatList(testResult.fitnessesOverRun, PerformanceEvaluation::format)));
            runRepResultOutput.append(String.format("Used %d features: %s\n", uf.numFeaturesUsed(), uf.usedFeatures().toString()));
            runRepResultOutput.append(Util.printUserFriendly(testResult.partition, fitness, processedInstances));

            Pair<String, Map<String, Double>> evaluationResult = PerformanceEvaluation.evaluateOnAll(testResult.partition, processedInstances, testResult.featureSubset);
            runResultOutput.append(String.format("%n%s: Fitness: %s%nPerformance measures:  %s%n", testName, PerformanceEvaluation.format(fitness), evaluationResult.getKey()));
            synchronized (resultsKeeper) {
                resultsKeeper.addEvaluationResult(testName, evaluationResult);
                resultsKeeper.addFeatureSubsetResult(testName, testResult.featureSubset);
                resultsKeeper.addClusterNumResult(testName, testResult.partition.size());
                resultsKeeper.addFitnessFunctionResult(testName, fitness);
            }
        }
        LOG.println(runResultOutput.toString());
        LOG.printf(runRepResultOutput.toString());
        return numClusters;
    }

    private static String[] getMethodNames(METHOD[] methods) {
        String methodNamesStr = CONFIG.getProperty("methodNames");
        String[] methodNames = new String[methods.length];
        if (methodNamesStr != null) {
            String[] split = methodNamesStr.split(",");
            if (split.length == methods.length) {
                for (int i = 0; i < split.length; i++) {
                    String s = split[i].trim();
                    methodNames[i] = s;
                }
                return methodNames;

            } else {
                LOG.println("Not enough method names");
            }
        }
        LOG.println("Using default method names");
        for (int i = 0; i < methods.length; i++) {
            methodNames[i] = methods[i].humanFriendly();

        }
        return methodNames;

    }

    private static METHOD[] getMethods() {
        String methodsString = CONFIG.getProperty("methods");
        String[] methodNames = methodsString.split(",");
        METHOD[] methods = new METHOD[methodNames.length];
        for (int i = 0; i < methodNames.length; i++) {
            String methodName = methodNames[i].trim();
            methods[i] = METHOD.valueOf(methodName);

        }
        return methods;
    }

    private static ClusterFitnessFunction getFitnessFunction() {
        String ffName = CONFIG.getProperty("fitnessFunction");
        try {
            return (ClusterFitnessFunction) Class.forName(ffName).newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException(ffName);
        }
    }


    public static class SetupSystem {
        private String[] args;
        private List<Instance> processedInstances;
        private int numFeatures;
        private int numClusters;

        public SetupSystem(String... args) {
            this.args = args;
        }

        public static List<Instance> getRawInstances(List<String> lines, String[] header) {
            String classLabelPosition = header[0];
            int numInitialFeatures = Integer.parseInt(header[1]);
            String splitString = ",";
            if (header[3].equals("space")) splitString = "\\s+";
            else if (header[3].equals("tab")) splitString = "\t";
            //Remove bad features
            List<Instance> rawInstances = getInstances(lines, classLabelPosition, numInitialFeatures, splitString);


            return rawInstances;
        }

        public List<Instance> getProcessedInstances() {
            return processedInstances;
        }

        public int getNumFeatures() {
            return numFeatures;
        }

        public int getNumClusters() {
            return numClusters;
        }

        public SetupSystem invoke() throws IOException {
            CONFIG.build(args);
            Util.initLogging();
            String dataset = CONFIG.getProperty("dataset");
            return getSetupSystem(dataset);
        }

        SetupSystem getSetupSystem(String dataset) throws IOException {
            List<String> lines = Files.readAllLines(Paths.get(System.getProperty("user.dir"), "/datasets", dataset));
            String[] header = lines.get(0).split(",");

            numClusters = Integer.parseInt(header[2]);

            List<Instance> rawInstances = getRawInstances(lines, header);

            DatasetUtils.FEATURE_MIN = CONFIG.getInt("featureMin");
            DatasetUtils.FEATURE_MAX = CONFIG.getInt("featureMax");
            DatasetUtils.FEATURE_RANGE = (FEATURE_MAX - FEATURE_MIN);
            DatasetUtils.FEATURE_RANGE2 = (FEATURE_RANGE) * (FEATURE_RANGE);

            String preprocessingType = CONFIG.getProperty("preprocessing", "scale");
            switch (preprocessingType) {
                case "scale":
                    processedInstances = scaleInstances(rawInstances);
                    break;
                case "normalise":
                    processedInstances = normaliseInstances(rawInstances);
                    break;
                case "none":
                    processedInstances = rawInstances.stream().map(Instance::clone).collect(Collectors.toList());
                    break;
                default:
                    throw new IllegalArgumentException(preprocessingType);
            }
            DatasetUtils.initialise(processedInstances, true);
            numFeatures = processedInstances.get(0).numFeatures();


            LOG.printf("System set up:%n%s%n%n", CONFIG.toString());
            return this;
        }
    }
}
