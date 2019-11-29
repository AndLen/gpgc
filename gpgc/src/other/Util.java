package other;

import clustering.Cluster;
import data.Instance;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static other.Main.CONFIG;

/**
 * Created by lensenandr on 2/03/16.
 */
public class Util {
    //   public static final Random RANDOM = new Random();
    public static final String DIVIDER = "==============================\n";
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(Math.max(Runtime.getRuntime().availableProcessors() / 2, 1));
    public static LoggerStream LOG;
    public static Map<String, Object> RUN_PARAMS;
    public static double MOST_NEGATIVE_VAL = -Double.MAX_VALUE;

    public static DistanceMeasure POINT_ONE_NORM = ((i1, i2, featureSubset) -> {
        double runningSum = 0;
        int numFeatures = i1.numFeatures();
        for (int i = 0; i < numFeatures; i++) {
            if (featureSubset[i]) {
                //feature is selected
                double featureDistance = i1.getFeatureValue(i) - i2.getFeatureValue(i);
                runningSum += Math.pow((Math.abs(featureDistance) / DatasetUtils.FEATURE_RANGE), 0.1);
            }

        }
        return Math.pow(runningSum, 10.0);
    });
    public static DistanceMeasure POINT_FIVE_NORM = ((i1, i2, featureSubset) -> {
        double runningSum = 0;
        int numFeatures = i1.numFeatures();
        for (int i = 0; i < numFeatures; i++) {
            if (featureSubset[i]) {
                //feature is selected
                double featureDistance = i1.getFeatureValue(i) - i2.getFeatureValue(i);
                runningSum += Math.pow((Math.abs(featureDistance) / DatasetUtils.FEATURE_RANGE), 0.5);
            }

        }
        return runningSum * runningSum;
    });
    public static DistanceMeasure EUCLIDEAN_DISTANCE = ((i1, i2, featureSubset) -> {
        double runningSum = 0;
        int numFeatures = i1.numFeatures();
        for (int i = 0; i < numFeatures; i++) {
            if (featureSubset[i]) {
                //feature is selected
                double featureDistance = i1.getFeatureValue(i) - i2.getFeatureValue(i);
                runningSum += ((featureDistance * featureDistance) / DatasetUtils.FEATURE_RANGE2);
            }

        }
        double sqrt = Math.sqrt(runningSum);
        //  LOG.println(sqrt);
        return sqrt;
    });

    public static DistanceMeasure MANHATTAN_DISTANCE = ((i1, i2, featureSubset) -> {
        double runningSum = 0;
        int numFeatures = i1.numFeatures();
        for (int i = 0; i < numFeatures; i++) {
            if (featureSubset[i]) {
                //feature is selected
                double featureDistance = i1.featureValues[i] - i2.featureValues[i];
                runningSum += (Math.abs(featureDistance) / DatasetUtils.FEATURE_RANGE);
            }

        }
        return runningSum;
    });

    public static double randomDouble() {
        return ThreadLocalRandom.current().nextDouble();
    }

    public static double randomInRange(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
        // return min + (RANDOM.nextDouble() * (max - min));
    }

    public static <T> Future<T> submitJob(Callable<T> job) {
        return threadPool.submit(job);
    }

    public static double sigmoid(double x) {
        return (1.0 / (1.0 + Math.pow(Math.E, -x)));
    }

    public static void shutdownThreads() {
        threadPool.shutdown();
    }

    public static double scale(double rawValue, double minFeatureVal, double maxFeatureVal) {
        //return rawValue;
        return (rawValue - minFeatureVal) / (maxFeatureVal - minFeatureVal);
    }

    public static double toDouble(String s) {
        return Double.parseDouble(s);
    }

    public static String printUserFriendly(List<? extends Cluster> partition, double fitness, List<Instance> allInstances) {
        StringBuilder sb = new StringBuilder();
//        BigDecimal bd = new BigDecimal(fitness);
//        bd = bd.round(new MathContext(3));
        String formatted = PerformanceEvaluation.format(fitness);

        sb.append(String.format("Partition with %d clusters and %s fitness:\n", partition.size(), formatted));
        for (int i = 0; i < partition.size(); i++) {
            Cluster cluster = partition.get(i);
            List<Instance> instancesInCluster = cluster.getInstancesInCluster();
            sb.append(String.format("Cluster %d: %d instances. Prototype: %s, Instance Indices: %s\n", (i + 1), instancesInCluster.size(), cluster.getPrototypeToString(allInstances), printIndices(instancesInCluster, allInstances)));

            //sb.append(String.format("Cluster %d: %d instances. Prototype: %s\n", (i + 1), instancesInCluster.size(), cluster.getPrototype().toString()));
            //   LOG.printf("Cluster %d: %d instances\nMedoid: %s\nInstances: %s\n", (i + 1), instancesInCluster.size(), cluster.getMedoid().toString(), instancesInCluster.toString());
        }
        sb.append(DIVIDER + "\n");
        return sb.toString();
    }

    private static String printIndices(List<Instance> instancesInCluster, List<Instance> allInstances) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (int i = 0; i < instancesInCluster.size(); i++) {
            Instance instance = instancesInCluster.get(i);
            if (allInstances.contains(instance)) {
                int indexOf = allInstances.indexOf(instance);

                if (!first) {
                    sb.append(", ").append(indexOf);
                } else {
                    sb.append(indexOf);
                    first = false;
                }
            }
        }
        return sb.append("]").toString();
    }

    public static <T> String formatList(Iterable<T> iterable, FormatItem<T> formatItem) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (T item : iterable) {
            if (!first) {
                sb.append(", ");
            } else {
                first = false;
            }
            sb.append(formatItem.formatItem(item));
        }
        return sb.append("]").toString();

    }

    public static void removeEmptyClusters(List<? extends Cluster> clusters) {
        for (Iterator<? extends Cluster> iterator = clusters.iterator(); iterator.hasNext(); ) {
            Cluster cluster = iterator.next();
            if (cluster.size() == 0) {
                iterator.remove();
            }
        }
    }

    public static double log(double base, double value) {
        return Math.log(value) / Math.log(base);
    }

    public static void resetRunParams() {
        RUN_PARAMS = new HashMap<>();

    }

    public static void initLogging() {
        String datasetName = CONFIG.getProperty("dataset");
        LOG = new LoggerStream(CONFIG.getProperty("logPrefix", "") + datasetName);
    }

    public static int randomInt(int max) {
        return ThreadLocalRandom.current().nextInt(max);
    }

    public static DistanceMeasure getDistanceMeasureForClustering() {
        String distanceMeasureForClustering = CONFIG.getProperty("distanceMeasureForClustering");
        switch (distanceMeasureForClustering) {
            case "euclidean":
                return EUCLIDEAN_DISTANCE;
            case "manhattan":
                return MANHATTAN_DISTANCE;
            default:
                throw new IllegalArgumentException(distanceMeasureForClustering);
        }
    }

    public static double getMean(Collection<Double> doubles) {
        BigDecimal sum = BigDecimal.ZERO;
        for (Double val : doubles) {
            sum = sum.add(new BigDecimal(val));
        }
        return sum.divide(new BigDecimal(doubles.size()), BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static double getStandardDeviation(Collection<Double> vals, double mean) {
        double stdDev = 0;
        for (Double val : vals) {
            double diff = val - mean;
            stdDev += (diff * diff);
        }
        return Math.sqrt(stdDev / vals.size());
    }

    public static Map<String, Double> computeFinalAverages(Map<String, List<Double>> evaluationResults) {
        Map<String, BigDecimal> averageResults = new LinkedHashMap<>();

        for (String evalMethod : evaluationResults.keySet()) {
            BigDecimal sum = averageResults.getOrDefault(evalMethod, BigDecimal.ZERO);
            List<Double> results = evaluationResults.get(evalMethod);
            for (Double result : results) {

                sum = sum.add(new BigDecimal(result));
            }
            averageResults.put(evalMethod, sum);
        }

        //wow
        int numRuns = evaluationResults.values().iterator().next().size();
        Map<String, Double> results = new LinkedHashMap<>();
        for (String evalMethod : evaluationResults.keySet()) {
            results.put(evalMethod, averageResults.get(evalMethod).divide(new BigDecimal(numRuns), BigDecimal.ROUND_HALF_UP).doubleValue());
        }
        return results;
    }

    public static int getMaxArg(double[] array) {
        double max = Util.MOST_NEGATIVE_VAL;
        int maxIndex = -1;
        for (int i = 0; i < array.length; i++) {
            if (max < array[i]) {
                max = array[i];
                maxIndex = i;
            }

        }
        return maxIndex;
    }

    public static void main(String args[]) {
        String string = "\t\\multitree{10d10c}{\n" +
                "\t\tGPGC  & 19.17 & 36.37 & $61.19$ & $0.157$ & $0.06052$ & $0.7368$\\\\\n" +
                "\t\tAIC  & $21.35^{+}$& 24.00 & $71.72^{+}$ & $0.1659^{-}$ & $0.05921$ & $0.8143^{+}$\\\\\n" +
                "\t\tRIC  & $20.68^{+}$& 23.07 & $73.66^{+}$ & $0.1682^{-}$ & $0.05806$ & $0.799^{+}$\\\\\n" +
                "\t\tSIC  & $21.13^{+}$& 24.70 & $72.88^{+}$ & $0.1644^{-}$ & $0.05743$ & $0.8058^{+}$\\\\\n" +
                "\t}\n" +
                "\t\\multitree{10d20c}{\n" +
                "\t\tGPGC  & 43.69 & 27.77 & $73.34$ & $0.1504$ & $0.09833$ & $0.6827$\\\\\n" +
                "\t\tAIC  & $49.11^{+}$& 22.47 & $77.42^{+}$ & $0.1542^{-}$ & $0.1059^{+}$ & $0.6716$\\\\\n" +
                "\t\tRIC  & $49.51^{+}$& 21.93 & $77.65^{+}$ & $0.1543$ & $0.106^{+}$ & $0.6762$\\\\\n" +
                "\t\tSIC  & $50.28^{+}$& 21.80 & $78.34^{+}$ & $0.1551^{-}$ & $0.1062^{+}$ & $0.6966$\\\\\n" +
                "\t}\n" +
                "\t\\multitree{10d40c}{\n" +
                "\t\tGPGC  & 36.57 & 55.57 & $70.88$ & $0.1361$ & $0.07874$ & $0.5802$\\\\\n" +
                "\t\tAIC  & $33.42^{-}$& 49.20 & $76.62^{+}$ & $0.1385$ & $0.07241^{-}$ & $0.5218^{-}$\\\\\n" +
                "\t\tRIC  & $31.92^{-}$& 53.17 & $75.11^{+}$ & $0.1375$ & $0.07038^{-}$ & $0.4869^{-}$\\\\\n" +
                "\t\tSIC  & $31.95^{-}$& 51.10 & $75.93^{+}$ & $0.1399^{-}$ & $0.07056^{-}$ & $0.5393$\\\\\n" +
                "\t}\n" +
                "\t\\multitree{10d100c}{\n" +
                "\t\tGPGC  & 31.80 & 109.50 & $73.96$ & $0.1312$ & $0.06585$ & $0.4243$\\\\\n" +
                "\t\tAIC  & $32.4$& 106.40 & $75.56$ & $0.1306$ & $0.06583$ & $0.4209$\\\\\n" +
                "\t\tRIC  & $30.84$& 134.90 & $72.57$ & $0.1268^{+}$ & $0.0639$ & $0.4424$\\\\\n" +
                "\t\tSIC  & $31.78$& 113.40 & $74.1$ & $0.1297$ & $0.06571$ & $0.4441$\\\\\n" +
                "\t}\n" +
                "\t\\multitree{50d10c}{\n" +
                "\t\tGPGC  & 31.66 & 12.57 & $57.83$ & $0.4453$ & $0.2756$ & $0.9623$\\\\\n" +
                "\t\tAIC  & $42.49^{+}$& 10.00 & $59.52^{+}$ & $0.472^{-}$ & $0.3407^{+}$ & $0.9865^{+}$\\\\\n" +
                "\t\tRIC  & $41.21^{+}$& 10.03 & $60.13^{+}$ & $0.4653^{-}$ & $0.3283^{+}$ & $0.977$\\\\\n" +
                "\t\tSIC  & $40.21^{+}$& 10.40 & $59.85^{+}$ & $0.4592$ & $0.3195^{+}$ & $0.9691$\\\\\n" +
                "\t}\n" +
                "\t\\multitree{50d20c}{\n" +
                "\t\tGPGC  & 30.36 & 25.23 & $50.65$ & $0.3498$ & $0.2344$ & $0.8101$\\\\\n" +
                "\t\tAIC  & $36.78^{+}$& 21.07 & $51.82^{+}$ & $0.3585$ & $0.2732^{+}$ & $0.8364$\\\\\n" +
                "\t\tRIC  & $34.82^{+}$& 21.50 & $51.33$ & $0.3619^{-}$ & $0.2677^{+}$ & $0.8404$\\\\\n" +
                "\t\tSIC  & $34.06^{+}$& 22.10 & $51.31$ & $0.3578$ & $0.2605^{+}$ & $0.8466$\\\\\n" +
                "\t}\n" +
                "\t\\multitree{50d40c}{\n" +
                "\t\tGPGC  & 29.58 & 49.80 & $54.58$ & $0.3044$ & $0.1961$ & $0.7262$\\\\\n" +
                "\t\tAIC  & $34.36^{+}$& 45.23 & $56.09^{+}$ & $0.308$ & $0.2201^{+}$ & $0.8098^{+}$\\\\\n" +
                "\t\tRIC  & $32.13^{+}$& 46.17 & $55.76^{+}$ & $0.3057$ & $0.2089^{+}$ & $0.7215$\\\\\n" +
                "\t\tSIC  & $32.27^{+}$& 46.33 & $55.75^{+}$ & $0.3071$ & $0.2078^{+}$ & $0.7758$\\\\\n" +
                "\t}\n" +
                "\t\\multitree{100d10c}{\n" +
                "\t\tGPGC  & 39.40 & 10.43 & $47.85$ & $0.6111$ & $0.5448$ & $0.9932$\\\\\n" +
                "\t\tAIC  & $44.41^{+}$& 9.80 & $48.09$ & $0.6212^{-}$ & $0.5802^{+}$ & $0.9976$\\\\\n" +
                "\t\tRIC  & $44.88^{+}$& 9.67 & $48.16$ & $0.6218^{-}$ & $0.584^{+}$ & $0.9965$\\\\\n" +
                "\t\tSIC  & $42.87^{+}$& 10.03 & $48.13$ & $0.6171$ & $0.5687$ & $0.9958$\\\\\n" +
                "\t}\n" +
                "\t\\multitree{100d20c}{\n" +
                "\t\tGPGC  & 28.18 & 22.20 & $38.39$ & $0.5273$ & $0.4485$ & $0.8797$\\\\\n" +
                "\t\tAIC  & $31.99^{+}$& 20.60 & $38.22$ & $0.5353$ & $0.4935^{+}$ & $0.915^{+}$\\\\\n" +
                "\t\tRIC  & $31.31^{+}$& 20.67 & $38.63$ & $0.5302$ & $0.4813^{+}$ & $0.9026$\\\\\n" +
                "\t\tSIC  & $31.59^{+}$& 20.50 & $38.3$ & $0.5376$ & $0.4915^{+}$ & $0.9184^{+}$\\\\\n" +
                "\t}\n" +
                "\t\\multitree{100d40c}{\n" +
                "\t\tGPGC  & 21.60 & 50.20 & $39.7$ & $0.4355$ & $0.282$ & $0.7243$\\\\\n" +
                "\t\tAIC  & $25.02^{+}$& 45.93 & $40.64^{+}$ & $0.4399$ & $0.3155^{+}$ & $0.7712$\\\\\n" +
                "\t\tRIC  & $24.5^{+}$& 47.57 & $40.68^{+}$ & $0.4376$ & $0.3112^{+}$ & $0.7769$\\\\\n" +
                "\t\tSIC  & $23.64^{+}$& 49.20 & $39.9$ & $0.4376$ & $0.3043^{+}$ & $0.7922^{+}$\\\\\n" +
                "\t}\n" +
                "\t\n" +
                "\t\\multitree{1000d10c}{\n" +
                "\t\tGPGC  & 11.60 & 10.10 & $15.01$ & $2.132$ & $1.704$ & $0.9801$\\\\\n" +
                "\t\tAIC  & $12.6^{+}$& 9.73 & $14.92$ & $2.126$ & $1.784^{+}$ & $0.9871$\\\\\n" +
                "\t\tRIC  & $12.55^{+}$& 9.67 & $15.04$ & $2.122$ & $1.776^{+}$ & $0.9838$\\\\\n" +
                "\t\tSIC  & $12.48^{+}$& 9.60 & $15.18$ & $2.115$ & $1.754$ & $0.9781$\\\\\n" +
                "\t}\n" +
                "\t\\multitree{1000d20c}{\n" +
                "\t\tGPGC  & 9.22 & 23.13 & $11.99$ & $1.539$ & $1.325$ & $0.8411$\\\\\n" +
                "\t\tAIC  & $11.48^{+}$& 19.63 & $12.43^{+}$ & $1.575^{-}$ & $1.511^{+}$ & $0.8133$\\\\\n" +
                "\t\tRIC  & $11.37^{+}$& 19.53 & $12.22$ & $1.58^{-}$ & $1.533^{+}$ & $0.7936$\\\\\n" +
                "\t\tSIC  & $10.96^{+}$& 19.40 & $12.26$ & $1.589^{-}$ & $1.498^{+}$ & $0.8139$\\\\\n" +
                "\t}\n" +
                "\t\\multitree{1000d40c}{\n" +
                "\t\tGPGC  & 8.48 & 47.50 & $13.95$ & $1.376$ & $1.006$ & $0.7964$\\\\\n" +
                "\t\tAIC  & $10.14^{+}$& 42.50 & $14.15$ & $1.387$ & $1.13^{+}$ & $0.8037$\\\\\n" +
                "\t\tRIC  & $10.01^{+}$& 42.57 & $14.17$ & $1.384$ & $1.126^{+}$ & $0.8318$\\\\\n" +
                "\t\tSIC  & $9.664^{+}$& 44.07 & $14.17$ & $1.382$ & $1.086^{+}$ & $0.8276$\\\\\n" +
                "\t}\n" +
                "\t\\multitree{1000d100c}{\n" +
                "\t\tGPGC & 7.91 & 132.50 & $15.79$ & $1.172$ & $0.7614$ & $0.8387$\\\\\n" +
                "\t\tAIC  & $9.903^{+}$& 117.20 & $15.96$ & $1.189^{-}$ & $0.9013^{+}$ & $0.9155^{+}$\\\\\n" +
                "\t\tRIC  & $9.056^{+}$& 119.90 & $15.96^{+}$ & $1.186^{-}$ & $0.8389^{+}$ & $0.863$\\\\\n" +
                "\t\tSIC  & $8.558$& 124.70 & $15.96$ & $1.172$ & $0.7966$ & $0.8525$\\\\\n" +
                "\t}";
        Matcher matcher = Pattern.compile("(\\d+\\.\\d+)").matcher(string);
        StringBuilder sb = new StringBuilder();
        int prevEnd = 0;
        while (matcher.find()) {
            sb.append(string.substring(prevEnd, matcher.start()));
            prevEnd = matcher.end();
            String group = matcher.group();
            BigDecimal bigDecimal = new BigDecimal(Double.parseDouble(group));
            String format = String.format("%.3G", bigDecimal);
            //   System.out.println(format);
            sb.append(format);
        }
        sb.append(string.substring(prevEnd, string.length()));
        System.out.println(sb.toString());
    }

    @FunctionalInterface
    public interface FormatItem<T> {
        String formatItem(T item);
    }

    @FunctionalInterface
    public interface DistanceMeasure {
        double distance(Instance instance1, Instance instance2, boolean[] featureSubset);
    }

    public static class LoggerStream {
        public final String ECJ_OUT;
        public final Path DIRECTORY;
        private final BufferedWriter FILE_OUT;
        private final PrintStream SYSTEM_OUT = System.out;
        private int count = 0;

        public LoggerStream(String prefix) {
            try {
                String timestamp = CONFIG.getProperty("jobID", String.format("%6d", (int) (Math.random() * 10000000)));
                Path path = Paths.get(System.getProperty("user.home"), "/masters/output/", prefix + "-" + new Date().toString().replaceAll("[\\s:]", "-") + "-" + timestamp + ".out");
                Path ecj = Paths.get(System.getProperty("user.home"), "/masters/output/", prefix + "-" + new Date().toString().replaceAll("[\\s:]", "-") + "-" + timestamp + ".ecj");
                ECJ_OUT = ecj.toString();
                DIRECTORY = path.getParent();
                Files.createDirectories(DIRECTORY);
                FILE_OUT = Files.newBufferedWriter(path);
            } catch (IOException e) {
                throw (new Error(e));
            }
        }

        public LoggerStream(String prefix, Path directory) {
            try {
                Files.createDirectories(directory);
                String timestamp = CONFIG.getProperty("jobID", String.format("%6d", (int) (Math.random() * 10000000)));
                Path path = directory.resolve(prefix + "-" + new Date().toString().replaceAll("[\\s:]", "-") + "-" + timestamp + ".out");
                Path ecj = directory.resolve(prefix + "-" + new Date().toString().replaceAll("[\\s:]", "-") + "-" + timestamp + ".ecj");
                ECJ_OUT = ecj.toString();
                DIRECTORY = path.getParent();
                Files.createDirectories(DIRECTORY);
                FILE_OUT = Files.newBufferedWriter(path);
            } catch (IOException e) {
                throw (new Error(e));
            }
        }

        public <T> void println2(T s) {
            if (CONFIG.getBoolean("verbose")) println(s);
        }

        public void printf2(String format, Object... args) {
            if (CONFIG.getBoolean("verbose")) printf(format, args);
        }

        public boolean verbose() {
            return CONFIG.getBoolean("verbose");
        }

        /**
         * End of run logging
         *
         * @param format
         * @param args
         */
        public void printf(String format, Object... args) {
            //end of run always prints out for now
            String formatted = String.format(format, args);
            print(formatted);
        }

        private void print(String formatted) {
            synchronized (this) {
                SYSTEM_OUT.print(formatted);
                try {
                    FILE_OUT.write(formatted);
                    if (count++ % 10 == 0) {
                        FILE_OUT.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public <T> void println(T s) {
            String str = String.valueOf(s) + "\n";

            print(str);
        }

        public void closeStreams() {
            SYSTEM_OUT.close();
            try {
                FILE_OUT.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void println() {
            print("\n");
        }
    }
}
