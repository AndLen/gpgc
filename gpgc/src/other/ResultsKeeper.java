package other;

import javafx.util.Pair;
import wagu.Board;
import wagu.Table;

import java.util.*;

import static other.Main.CONFIG;
import static other.Main.METHOD_NAMES;
import static other.Util.LOG;

/**
 * Created by lensenandr on 7/04/16.
 * <p>
 * TODO: REFORMAT ME!!!
 */
public class ResultsKeeper {
    private final Map<String, List<Double>> averageFFs = new LinkedHashMap<>();
    //Method to eval method to results
    public final Map<String, Map<String, List<Double>>> evalResults = new LinkedHashMap<>();
    private final Map<String, int[]> testFSResults = new LinkedHashMap<>();
    private final Map<String, Integer> averageClusterNums = new LinkedHashMap<>();

    public void addEvaluationResult(String testName, Pair<String, Map<String, Double>> evaluationResult) {
        Map<String, List<Double>> testEvalResults = evalResults.getOrDefault(testName, new LinkedHashMap<>());
        Map<String, Double> evalResult = evaluationResult.getValue();
        for (String evalMethod : evalResult.keySet()) {
            List<Double> results = testEvalResults.getOrDefault(evalMethod, new ArrayList<>());
            results.add(evalResult.get(evalMethod));
            testEvalResults.put(evalMethod, results);
        }
        evalResults.put(testName, testEvalResults);

    }

    public void addFeatureSubsetResult(String testName, boolean[] featureSubset) {
        if (featureSubset != null) {
            int[] fsHistogram = testFSResults.getOrDefault(testName, new int[featureSubset.length]);
            for (int j = 0; j < featureSubset.length; j++) {
                if (featureSubset[j]) fsHistogram[j]++;
            }
            testFSResults.put(testName, fsHistogram);
        }
    }

    public void addFitnessFunctionResult(String testName, double fitness) {
        List<Double> newVal = averageFFs.getOrDefault(testName, new ArrayList<>());
        newVal.add(fitness);
        averageFFs.put(testName, newVal);
    }


    public void addClusterNumResult(String testName, int clusterSize) {
        int newVal = averageClusterNums.getOrDefault(testName, 0) + clusterSize;
        averageClusterNums.put(testName, newVal);
    }

    /**
     * This is all so gross.
     *
     * @param csv
     * @param methods
     */
    void printEvalFinal(boolean csv, String[] methods) {
        List<List<String>> rows = new ArrayList<>();
        ArrayList<String> measures = new ArrayList<>(PerformanceEvaluation.evaluations.keySet());
        Map<String, Map<String, Double>> means = new LinkedHashMap<>();
        Map<String, Map<String, Double>> stdDevs = new LinkedHashMap<>();

        for (Map.Entry<String, Map<String, List<Double>>> resultEntry : evalResults.entrySet()) {
            Map<String, Double> theseMeans = Util.computeFinalAverages(resultEntry.getValue());
            Map<String, Double> theseStdDevs = new LinkedHashMap<>();
            List<String> row = new ArrayList<>();
            row.add(resultEntry.getKey());
            for (String evalMethod : theseMeans.keySet()) {
                double result = theseMeans.get(evalMethod);
                double stdDev = Util.getStandardDeviation(resultEntry.getValue().get(evalMethod), result);
                theseStdDevs.put(evalMethod, stdDev);
                row.add(PerformanceEvaluation.format(result));

            }
            rows.add(row);
            means.put(resultEntry.getKey(), theseMeans);
            stdDevs.put(resultEntry.getKey(), theseStdDevs);
        }
        List<String> headersList = PerformanceEvaluation.columnNames();
        if (csv) {
            printCSV(headersList, rows);
            LOG.println("Means:");
            printForSigTests(measures, means);
            LOG.println("Std Devs:");
            printForSigTests(measures, stdDevs);

        } else {
            Board board = new Board(Main.BOARD_WIDTH);
            LOG.println((board.setInitialBlock(new Table(board, Main.BOARD_WIDTH, headersList, rows, PerformanceEvaluation.columnWidths(methods)).tableToBlocks()).build()).getPreview());
        }
    }

    public void printFeatureHistogram() {
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, int[]> result : testFSResults.entrySet()) {
            int[] histogram = result.getValue();
            int sum = Arrays.stream(histogram).sum();

            sb.append(String.format("%s selected %.2f features on average. FS histogram: %s%n", result.getKey(), sum / (double) CONFIG.getInt("runs"), Arrays.toString(histogram)));
        }
        LOG.println(sb.toString());
    }

    public void printFinalResults(METHOD[] methods) {
        String[] methodNames = new String[methods.length];
        for (int i = 0; i < methods.length; i++) {
            methodNames[i] = METHOD_NAMES[i];
        }

        printFinalResults(methodNames);

    }

    public void printFinalResults(String[] methodNames) {
        printRunAverages(false, methodNames);
        printEvalFinal(false, methodNames);
        printFeatureHistogram();
        printRunAverages(true, methodNames);
        printEvalFinal(true, methodNames);
        LOG.println("Latex:");
        printFullLatex();
    }

    public void printFullLatex() {
        List<List<String>> rows = new ArrayList<>();
        double numRuns = CONFIG.getInt("runs");
        for (Map.Entry<String, Map<String, List<Double>>> resultEntry : evalResults.entrySet()) {
            List<String> row = new ArrayList<>();
            String testName = resultEntry.getKey();
            row.add(testName);
            int[] fsHistogram = testFSResults.get(testName);
            double mean = Util.getMean(averageFFs.get(testName));

            row.add(PerformanceEvaluation.format(mean));
            row.add(PerformanceEvaluation.format(Arrays.stream(fsHistogram).sum() / numRuns));
            row.add(PerformanceEvaluation.format(averageClusterNums.get(testName) / numRuns));

            Map<String, Double> results = Util.computeFinalAverages(resultEntry.getValue());
            for (String s : results.keySet()) {
                row.add(PerformanceEvaluation.format(results.get(s)));
            }
            rows.add(row);
        }

        List<String> headersList = new ArrayList<>(Arrays.asList("Method", "Fitness", "#Features", "#Clusters"));

        List<String> evalHeaderList = PerformanceEvaluation.columnNames();
        evalHeaderList.remove(0);
        headersList.addAll(evalHeaderList);
        printCSV(headersList, rows);

    }

    public void printRunAverages(boolean csv, String[] methods) {
        List<List<String>> rows = new ArrayList<>();
        double numRuns = CONFIG.getInt("runs");
        Map<String, Double> fitnessMeans = new LinkedHashMap<>();
        Map<String, Double> fitnessStdDevs = new LinkedHashMap<>();
        for (String testName : averageFFs.keySet()) {
            List<String> row = new ArrayList<>();
            row.add(testName);
            double mean = Util.getMean(averageFFs.get(testName));
            fitnessMeans.put(testName, mean);
            row.add(PerformanceEvaluation.format(mean));
            fitnessStdDevs.put(testName, Util.getStandardDeviation(averageFFs.get(testName), mean));
            int[] fsHistogram = testFSResults.get(testName);
            row.add(PerformanceEvaluation.format(Arrays.stream(fsHistogram).sum() / numRuns));
            row.add(PerformanceEvaluation.format(averageClusterNums.get(testName) / numRuns));
            rows.add(row);
        }

        List<String> headersList = Arrays.asList("Method", "Fitness", "#Features", "#Clusters");
        if (csv) {
            printCSV(headersList, rows);
            StringBuilder sb = new StringBuilder("");
            sb.append("method, fitness, stdDev\n");
            fitnessMeans.keySet().forEach(key -> {
                sb.append(String.format("%s, %s, %s\n", key, PerformanceEvaluation.format(fitnessMeans.get(key)), PerformanceEvaluation.format(fitnessStdDevs.get(key))));
            });
            LOG.println(sb);
        } else {
            Board board = new Board(Main.BOARD_WIDTH);
            List<Integer> colWidthsList = Arrays.asList(PerformanceEvaluation.columnWidths(methods).get(0), 10, 10, 10);
            LOG.println((board.setInitialBlock(new Table(board, Main.BOARD_WIDTH, headersList, rows, colWidthsList).tableToBlocks()).build()).getPreview());
        }
    }

    private void printForSigTests(List<String> measures, Map<String, Map<String, Double>> vals) {
        StringBuilder csv = new StringBuilder();
        for (int i = 0; i < measures.size(); i++) {
            String measure = measures.get(i);
            csv.append(measure);
            if (i < measures.size() - 1) {
                csv.append(", ");
            }
        }
        csv.append("\n");
        for (String method : vals.keySet()) {
            csv.append(method).append(", ");
            Map<String, Double> theseMeans = vals.get(method);
            for (int i = 0; i < measures.size(); i++) {
                String measure = measures.get(i);
                csv.append(PerformanceEvaluation.format(theseMeans.get(measure)));
                if (i < measures.size() - 1) {
                    csv.append(", ");
                }

            }
            csv.append("\n");
        }
        csv.append("\n");
        LOG.println(csv.toString());
    }

    private void printCSV(List<String> headersList, List<List<String>> rows) {
        StringBuilder csv = new StringBuilder("");
        csvThisRowUp(headersList, csv);
        for (List<String> row : rows) {
            csvThisRowUp(row, csv);
        }
        LOG.println(csv.toString());

    }

    private void csvThisRowUp(List<String> row, StringBuilder csv) {
        for (int i = 0; i < row.size(); i++) {
            String s = row.get(i);
            csv.append(s);
            if (i != row.size() - 1) {
                csv.append(" & ");
            }
        }
        csv.append("\\\\\n");

    }
}
