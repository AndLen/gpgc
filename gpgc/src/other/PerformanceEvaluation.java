package other;

import clusterFitness.MaxMinIntraMinMinInterDistance;
import clustering.Cluster;
import data.Instance;
import javafx.util.Pair;
import org.apache.commons.math3.util.CombinatoricsUtils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Created by lensenandr on 30/03/16.
 */
public class PerformanceEvaluation {
    public static LinkedHashMap<String, MeasureFitness> evaluations = new LinkedHashMap<>();

    static {
        // evaluations.put("Conn. ^", (p, i, fs, fse) -> new ConnectednessFitness().fitness(p, i, fs, fse));
        //   evaluations.put("Scatter ^", (p, i, fs, fsE) -> new ScatterFitness().fitness(p, i, fs, fsE));
        // evaluations.put("Silhouette ^", (p, i, fS, fSE) -> SilhouettePAM.getAverageSilhouette(DatasetUtils.EUCLIDEAN_DISSIM_MAP, p, i.size()));
        // evaluations.put("J1 ^", (p, i, fs, fsE) -> new J1ScatterFitness().fitness(p, i, fs, fsE));
        // evaluations.put("J2 ^", (p, i, fs, fsE) -> new J2ScatterFitness().fitness(p, i, fs, fsE));
        //  evaluations.put("FFBasePlusFeatures ^", (p, i, fs, fsE) -> new OldFF1().fitness(p, i, fs, fsE));

        //  evaluations.put("Dunn ^", (p, i, fs, fsE) -> new DunnFitness().fitness(p, i, fs, fsE));
        //  evaluations.put("Total Intra v", (p, i, fs, fsE) -> new SumIntraFitness().fitness(p, i, fs, fsE));
        //    evaluations.put("DB v", (p, i, fs, fsE) -> new DaviesBouldinFitness().fitness(p, i, fs, fsE));
        //  evaluations.put("RMSE v", (p, i, fs, fsE) -> new RMSEFitness().fitness(p, i, fs, fsE));
        //      evaluations.put("Class. Purity ^", (p, i, fs, fsE) -> classificationAccuracy(p, i));
        //    evaluations.put("Rand ^", (clusters, instances, fs, fsEeatureSubsetForEvaluation) -> PerformanceEvaluation.randIndex(clusters));
        //evaluations.put("Cura ER v", (p, i, fs, fsE) -> curaErrorRate(p, i));
        //evaluations.put("TPR ^", (p, i, fS, fsE) -> TPR(p, i));
        //   evaluations.put("Conn (10) ^", (p, i, fS, fsE) -> LimitedConnectednessFitness.avgClusterConnectivity(p));
        //  evaluations.put("Mean Sparsity v", (p, i, fS, fSE) -> meanMaxMinIntra(p));
        // evaluations.put("Mean Separation ^", (p, i, fS, fSE) -> meanMinMinInter(p));
        // evaluations.put("Mean CS:S ^", (p, i, fS, fsE) -> MaxMinIntraMinMinInterDistance.maxMinIntraMinMinInterDistance(p, i, 1.0, 0.5));
        evaluations.put("F-Measure ^", (p, i, fS, fSE) -> PerformanceEvaluation.fMeasure(p, i));
        evaluations.put("ARI ^", (p, i, fs, fsE) -> PerformanceEvaluation.adjustedRandIndex(p, i));
    }

    public static double meanMinMinInter(List<? extends Cluster> clusters) {
        double sum = 0;
        int numClusters = clusters.size();
        if (numClusters > 1) {
            for (int k = 0; k < numClusters; k++) {
                Cluster cluster = clusters.get(k);

                double minMinInter = Double.MAX_VALUE;
                List<Instance> clusterInstances = cluster.getInstancesInCluster();
                int thisClusterSize = clusterInstances.size();
                for (int i = 0; i < thisClusterSize; i++) {
                    Instance instance = clusterInstances.get(i);

                    double distToClosestNeighourCluster = MaxMinIntraMinMinInterDistance.distToClosestNeighourCluster(clusters, k, instance);
                    minMinInter = Double.min(minMinInter, distToClosestNeighourCluster);
                }
                sum += minMinInter;


            }
        }
        //avg?
        return sum / numClusters;
    }

    public static double meanMaxMinIntra(List<? extends Cluster> p) {
        int numClusters = p.size();
        double sumMaxMinIntra = 0;
        for (int k = 0; k < numClusters; k++) {
            Cluster cluster = p.get(k);
            double maxMinIntra = 0;
            List<Instance> clusterInstances = cluster.getInstancesInCluster();
            int thisClusterSize = clusterInstances.size();
            if (thisClusterSize > 1) {

                for (int i = 0; i < thisClusterSize; i++) {
                    Instance instance = clusterInstances.get(i);
                    double distToClosestNeighbour = MaxMinIntraMinMinInterDistance.distToClosestNeighbour(clusterInstances, i, instance);
                    maxMinIntra = Math.max(distToClosestNeighbour, maxMinIntra);
                }
            }
            sumMaxMinIntra += maxMinIntra;
        }
        return sumMaxMinIntra / numClusters;
    }

    public static double fMeasure(List<? extends Cluster> partition, List<Instance> instances) {
        LinkedHashMap<Instance, Cluster> instanceClusterMap = new LinkedHashMap<>();
        for (Cluster cluster : partition) {
            for (Instance instance : cluster.getInstancesInCluster()) {
                instanceClusterMap.put(instance, cluster);
            }

        }
        int TPs = 0;
        int FPs = 0;
        int FNs = 0;
        for (int i = 0; i < instances.size() - 1; i++) {
            Instance instance = instances.get(i);
            for (int j = i + 1; j < instances.size(); j++) {
                Instance otherInstance = instances.get(j);

                boolean sameClass = instance.getClassLabel().equals(otherInstance.getClassLabel());
                boolean sameCluster = instanceClusterMap.get(instance).equals(instanceClusterMap.get(otherInstance));

                if (sameClass) {
                    if (sameCluster) {
                        TPs++;
                    } else {
                        FNs++;
                    }
                } else {
                    if (sameCluster) {
                        FPs++;
                    }
                }
            }
        }
        double precision = TPs / (double) (TPs + FPs);
        double recall = TPs / (double) (TPs + FNs);
        double fMeasure = 2 * ((precision * recall) / (precision + recall));
        return fMeasure;
    }

    private static double TPR(List<? extends Cluster> partition, List<Instance> instances) {
        LinkedHashMap<Instance, Cluster> instanceClusterMap = new LinkedHashMap<>();
        for (Cluster cluster : partition) {
            for (Instance instance : cluster.getInstancesInCluster()) {
                instanceClusterMap.put(instance, cluster);
            }

        }
        int totalTruePairs = 0;
        int actualTruePairs = 0;
        for (int i = 0; i < instances.size() - 1; i++) {
            Instance instance = instances.get(i);
            for (int j = i + 1; j < instances.size(); j++) {
                Instance otherInstance = instances.get(j);

                boolean sameClass = instance.getClassLabel().equals(otherInstance.getClassLabel());
                if (sameClass) {
                    totalTruePairs++;
                    boolean sameCluster = instanceClusterMap.get(instance).equals(instanceClusterMap.get(otherInstance));
                    if (sameCluster) {
                        actualTruePairs++;
                    }
                }
            }
        }
        return actualTruePairs / (double) totalTruePairs;
    }

    private static double curaErrorRate(List<? extends Cluster> clusters, List<Instance> instances) {
        LinkedHashMap<Instance, Cluster> instanceClusterMap = new LinkedHashMap<>();
        for (Cluster cluster : clusters) {
            for (Instance instance : cluster.getInstancesInCluster()) {
                instanceClusterMap.put(instance, cluster);
            }

        }
        double runningSum2 = 0;
        for (int i = 0; i < instances.size() - 1; i++) {
            Instance instance = instances.get(i);
            for (int j = i + 1; j < instances.size(); j++) {
                Instance otherInstance = instances.get(j);

                boolean sameCluster = instanceClusterMap.get(instance).equals(instanceClusterMap.get(otherInstance));
                boolean sameClass = instance.getClassLabel().equals(otherInstance.getClassLabel());
                if (sameClass != sameCluster) runningSum2++;
            }
        }
        //System.out.println(runningSum2);
//        double runningSum = 0;
//
//        int i = 0;
//        for (Map.Entry<Instance, Cluster> entry : instanceClusterMap.entrySet()) {
//            int j = 0;
//            for (Map.Entry<Instance, Cluster> otherEntry : instanceClusterMap.entrySet()) {
//                if (i <= j + 1) {
//                    boolean sameCluster = entry.getValue().equals(otherEntry.getValue());
//                    boolean sameClass = entry.getKey().getClassLabel().equals(otherEntry.getKey().getClassLabel());
//                    if(sameClass!=sameCluster) runningSum++;
//
//                }
//                j++;
//            }
//            i++;
//
//        }
        double n = instances.size();
        double fraction = runningSum2 / ((n * (n - 1)) / 2);
        return fraction * 100;
    }

    private static double randIndex(List<? extends Cluster> clusters) {
        int a = 0, b = 0, c = 0, d = 0;
        LinkedHashMap<Instance, Cluster> instanceClusterMap = new LinkedHashMap<>();
        for (Cluster cluster : clusters) {
            for (Instance instance : cluster.getInstancesInCluster()) {
                instanceClusterMap.put(instance, cluster);
            }

        }

        int i = 0;
        for (Map.Entry<Instance, Cluster> entry : instanceClusterMap.entrySet()) {
            int j = 0;
            for (Map.Entry<Instance, Cluster> otherEntry : instanceClusterMap.entrySet()) {
                if (i != j) {
                    boolean sameCluster = entry.getValue().equals(otherEntry.getValue());
                    boolean sameClass = entry.getKey().getClassLabel().equals(otherEntry.getKey().getClassLabel());
                    if (sameCluster) {
                        if (sameClass) {
                            a++;
                        } else {
                            c++;
                        }
                    } else {
                        if (sameClass) {
                            d++;
                        } else {
                            b++;
                        }
                    }
                }

                j++;
            }
            i++;

        }
        return (a + b) / ((double) a + b + c + d);

    }

    public static double adjustedRandIndex(List<? extends Cluster> clusters, List<Instance> instances) {
        Map<String, List<Instance>> goldStdClusters = instances.stream().collect(Collectors.groupingBy(Instance::getClassLabel));
        List<String> goldStandardLabels = new ArrayList<>(goldStdClusters.keySet());
        int numFoundClusters = clusters.size();
        int numGoldStdClusters = goldStandardLabels.size();
        System.out.println(numFoundClusters + " " + numGoldStdClusters);

        //This is a fun one.
        //row-major (found clusters), col-minor ("gold standard clusters")
        int[][] contingencyTable = new int[numFoundClusters][numGoldStdClusters];

        int index = 0;
        //Fill in the matrix and calculate the (Rand) index as we go
        for (int i = 0; i < numFoundClusters; i++) {
            for (int j = 0; j < numGoldStdClusters; j++) {
                contingencyTable[i][j] = numInCommon(clusters.get(i).getInstancesInCluster(), goldStdClusters.get(goldStandardLabels.get(j)));
                index += getBinomialCoeffient(contingencyTable[i][j], 2);

            }
        }

        int rowAdjustedSums = 0;
        for (int i = 0; i < numFoundClusters; i++) {
            //Sum over rowI.
            int rowISum = Arrays.stream(contingencyTable[i]).sum();
            rowAdjustedSums += getBinomialCoeffient(rowISum, 2);
        }
        int colAdjustedSums = 0;

        for (int j = 0; j < numGoldStdClusters; j++) {
            //Goddamit java
            int colJSum = 0;
            for (int i = 0; i < numFoundClusters; i++) {
                colJSum += contingencyTable[i][j];
            }
            colAdjustedSums += getBinomialCoeffient(colJSum, 2);
        }

        int n = instances.size();
        double expectedIndex = (rowAdjustedSums * colAdjustedSums) / (double) getBinomialCoeffient(n, 2);
        double maxIndex = 0.5 * (rowAdjustedSums + colAdjustedSums);

        double adjustedRandIndex = (index - expectedIndex) / (maxIndex - expectedIndex);
        //I did it mum!
        return adjustedRandIndex;


    }

    public static long getBinomialCoeffient(int n, int k) {
        return n < 2 ? 0 : CombinatoricsUtils.binomialCoefficient(n, k);
    }

    private static int numInCommon(List<Instance> i1, List<Instance> i2) {
        return (int) i1.stream().filter(i2::contains).count();
    }

    private static double classificationAccuracy(List<? extends Cluster> partition, List<Instance> instances) {
        long totalNumCorrect = 0;
        long totalNumIncorrect = 0;
        for (Cluster cluster : partition) {
            List<Instance> clusterInstances = cluster.getInstancesInCluster();
            Optional<Long> max = clusterInstances.stream().collect(Collectors.groupingBy(Instance::getClassLabel, Collectors.counting()))
                    .values().stream().max(Comparator.comparingLong(v -> v));

            int numInstancesInCluster = clusterInstances.size();
            if (max.isPresent()) {
                long numCorrect = max.get();
                totalNumCorrect += numCorrect;
                totalNumIncorrect += (numInstancesInCluster - numCorrect);
            } else {
                System.err.println("WUT");
                totalNumIncorrect += numInstancesInCluster;
            }
        }
        //Accuracy
        double sum = totalNumCorrect + totalNumIncorrect;
        //Sanity check
        if (sum != instances.size()) {
            throw new IllegalStateException();
        }
        return (totalNumCorrect) / sum;
    }

    public static String format(double result) {
        if (Double.isInfinite(result) || Double.isNaN(result)) {
            return Double.toString(result);
        } else {
            return format(new BigDecimal(result));
        }
//        if (result < 10000) {
//            if (result < 1) {
//                if (result == 0) {
//                    return "0";
//                } else return String.format("%.4f", result);
//            } else return String.format("%4.3f", result);
//        } else return String.format(("%4.3e"), result);
    }

    public static String format(BigDecimal bigDecimal) {
        return bigDecimal.round(new MathContext(4, RoundingMode.HALF_UP)).toString();
    }

    public static Pair<String, Map<String, Double>> evaluateOnAll(List<? extends Cluster> partition, List<Instance> instances, boolean[] featureSubset) {
        return evaluateOnAll(partition, instances, featureSubset, PerformanceEvaluation.evaluations);
    }

    public static Pair<String, Map<String, Double>> evaluateOnAll(List<? extends Cluster> partition, List<Instance> instances, boolean[] featureSubset, Map<String, MeasureFitness> evaluations) {
        //Just in case?
        Util.removeEmptyClusters(partition);
        StringBuilder sb = new StringBuilder();
        Map<String, Double> results = new LinkedHashMap<>();
        evaluations.forEach((name, test) -> {
            double fitness = test.measureFitness(partition, instances, featureSubset, DatasetUtils.featuresToUseForEvaluation(featureSubset));
            results.put(name, fitness);
            sb.append(String.format("%s: %s, ", name, format(fitness)));
        });

        return new Pair<>(sb.toString(), results);
    }


    public static List<String> columnNames() {
        List<String> columnNames = new ArrayList<>();
        columnNames.add("Method");
        columnNames.addAll(evaluations.keySet());
        return columnNames;

    }

    public static List<Integer> columnWidths(String[] methodNames) {
        List<Integer> columnWidths = new ArrayList<>();
        int maxTextNameWidth = 0;
        for (int i = 0; i < methodNames.length; i++) {
            maxTextNameWidth = Math.max(maxTextNameWidth, methodNames[i].length());
        }
        columnWidths.add(maxTextNameWidth);
        evaluations.keySet().forEach(i -> columnWidths.add(Math.max(i.length(), 10)));
        return columnWidths;
    }


    @FunctionalInterface
    public interface MeasureFitness {
        double measureFitness(List<? extends Cluster> partition, List<Instance> instances, boolean[] featureSubset, boolean[] featureSubsetForEvaluation);
    }


}
