package other;

import data.Instance;
import data.UnlabelledInstance;

import java.util.*;
import java.util.stream.Collectors;

import static other.Util.*;

/**
 * Created by lensenandr on 22/03/16.
 */
public class DatasetUtils {
    public static final int NUM_NNs = 3;
    public static DissimilarityMap DEFAULT_MAP;
    public static PointOneNormDissimilarityMap POINT_ONE_NORM_DISSIM_MAP;
    public static EucledianDissimilarityMap EUCLIDEAN_DISSIM_MAP;
    public static ManhattanDissimilarityMap MANHATTAN_DISSIM_MAP;
    public static HashMap<Instance, List<Instance>> NEAREST_NEIGHBOURS;
    public static boolean[] ALL_FEATURES;
    public static int FEATURE_MIN;
    public static int FEATURE_MAX;
    public static double FEATURE_RANGE;
    public static double FEATURE_RANGE2;
    //Note that multiple places use the NN map -- so if fix, needs to fix to highest requirement.
    private static boolean FIXED_NUM_NNs = false;

    public static List<Instance> getInstances(List<String> lines, String classLabelFirst, int numFeatures, String splitChar) {
        int numInstances = lines.size() - 1;
        boolean hasClassLabel = !classLabelFirst.equalsIgnoreCase("noClass");
        boolean oneOffset = classLabelFirst.equalsIgnoreCase("classFirst");
        List<Instance> instances = new ArrayList<>(numInstances);
        double[][] input = new double[numInstances][numFeatures];
        String[] labels = new String[numInstances];
        for (int lineNum = 1; lineNum < lines.size(); lineNum++) {
            String line = lines.get(lineNum).trim();
            String[] split = line.split(splitChar);


            if (split.length == (hasClassLabel ? numFeatures + 1 : numFeatures)) {
                for (int i = 0; i < numFeatures; i++) {
                    double featureValue = Util.toDouble(split[oneOffset ? i + 1 : i]);
                    input[lineNum - 1][i] = featureValue;
                }
                if (hasClassLabel)
                    labels[lineNum - 1] = split[oneOffset ? 0 : numFeatures];
            }
        }
        boolean[] validFeatures = new boolean[numFeatures];
        int numValidFeatures = 0;
        for (int i = 0; i < numFeatures; i++) {
            double min = Double.MAX_VALUE;
            double max = MOST_NEGATIVE_VAL;
            for (int j = 0; j < numInstances; j++) {
                double val = input[j][i];
                if (val < min) min = val;
                if (val > max) max = val;
            }
            if (min != max) {
                validFeatures[i] = true;
                numValidFeatures++;
            }
        }
        for (int i = 0; i < numInstances; i++) {
            double[] featureVals = new double[numValidFeatures];
            int index = 0;
            for (int j = 0; j < validFeatures.length; j++) {
                if (validFeatures[j]) {
                    featureVals[index] = input[i][j];
                    index++;
                }

            }
            if (index != numValidFeatures) {
                throw new IllegalStateException();
            }
            if (labels[i] != null) {
                instances.add(new Instance(featureVals, labels[i], i));
            } else {
                throw new IllegalArgumentException();
                // instances.add(new UnlabelledInstance(featureVals));
            }
        }
        return instances;

    }

    public static List<Instance> scaleInstances(List<Instance> instances) {
        int numFeatures = instances.get(0).numFeatures();
        double[] minFeatureVals = new double[numFeatures];
        Arrays.fill(minFeatureVals, Double.MAX_VALUE);
        double[] maxFeatureVals = new double[numFeatures];
        Arrays.fill(maxFeatureVals, MOST_NEGATIVE_VAL);

        for (Instance instance : instances) {
            for (int i = 0; i < numFeatures; i++) {
                double featureValue = instance.getFeatureValue(i);
                if (featureValue < minFeatureVals[i]) {
                    minFeatureVals[i] = featureValue;
                }
                if (featureValue > maxFeatureVals[i]) {
                    maxFeatureVals[i] = featureValue;
                }
            }
        }
        //    Util.LOG.println2("Min vals: " + Arrays.toString(minFeatureVals));
        //  Util.LOG.println2("Max vals: " + Arrays.toString(maxFeatureVals));

        return instances.stream().map(instance -> instance.scaledCopy(minFeatureVals, maxFeatureVals)).collect(Collectors.toList());
    }

    public static void scaleArray(double[] vals) {
        int numVals = vals.length;
        double minFeatureVal = Double.MAX_VALUE;
        double maxFeatureVal = MOST_NEGATIVE_VAL;


        for (int i = 0; i < numVals; i++) {
            double val = vals[i];
            if (val < minFeatureVal) {
                minFeatureVal = val;
            }
            if (val > maxFeatureVal) {
                maxFeatureVal = val;
            }
        }
        for (int i = 0; i < numVals; i++) {
            vals[i] = scale(vals[i], minFeatureVal, maxFeatureVal);

        }
    }

    public static List<Instance> normaliseInstances(List<Instance> instances) {
        //Standard score. (value - mean)/stdDev
        int numInstances = instances.size();
        int numFeatures = instances.get(0).numFeatures();
        double[] featureMeans = getFeatureMeans(instances, numInstances, numFeatures);

        double[] featureStdDevs = getFeatureStandardDeviations(instances, numInstances, numFeatures, featureMeans);

        return instances.stream().map(instance -> instance.normalisedCopy(featureMeans, featureStdDevs)).collect(Collectors.toList());

    }


    private static double[] getFeatureStandardDeviations(List<Instance> instances, int numInstances, int numFeatures, double[] featureMeans) {
        double[] featureStdDevs = new double[numFeatures];

        for (Instance instance : instances) {
            for (int i = 0; i < numFeatures; i++) {
                double diff = instance.getFeatureValue(i) - featureMeans[i];
                featureStdDevs[i] += (diff * diff);
            }

        }
        for (int i = 0; i < featureStdDevs.length; i++) {
            featureStdDevs[i] = Math.sqrt(featureStdDevs[i] / numInstances);

        }
        return featureStdDevs;
    }

    private static double[] getFeatureMeans(List<Instance> instances, int numInstances, int numFeatures) {
        double[] featureMeans = new double[numFeatures];

        for (Instance instance : instances) {
            for (int i = 0; i < numFeatures; i++) {
                double featureValue = instance.getFeatureValue(i);
                featureMeans[i] += featureValue;
            }

        }
        for (int i = 0; i < featureMeans.length; i++) {
            featureMeans[i] /= numInstances;

        }
        return featureMeans;
    }


    public static boolean[] featuresToUseForFitness(boolean[] featureSubset) {
        return Main.CONFIG.getBoolean("featureSubsetForFitness") ? featureSubset : ALL_FEATURES;
    }

    public static boolean[] featuresToUseForEvaluation(boolean[] featureSubset) {
        return Main.CONFIG.getBoolean("featureSubsetForEvaluation") ? featureSubset : ALL_FEATURES;

    }

    public static int numFeaturesUsed(boolean[] featureSubset) {
        int numFeaturesUsed = 0;
        for (boolean b : featureSubset) {
            if (b) numFeaturesUsed++;

        }
        return numFeaturesUsed;
    }

    public static int[] featuresUsed(boolean[] featureSubset) {
        int numFeaturesUsed = numFeaturesUsed(featureSubset);
        int[] featuresUsed = new int[numFeaturesUsed];
        int index = 0;
        for (int i = 0; i < featureSubset.length; i++) {
            if (featureSubset[i]) featuresUsed[index++] = i;

        }
        return featuresUsed;
    }


    public static boolean[] featureSubset(List<Integer> featuresUsed, int numFeatures) {
        boolean[] featureSubset = new boolean[numFeatures];
        for (int l : featuresUsed) {
            featureSubset[l] = true;
        }
        return featureSubset;

    }

    public static <T extends Collection<Instance>> UnlabelledInstance getSampleMean(T instances) {

        int numFeatures = instances.iterator().next().numFeatures();

        double[] centroid = new double[numFeatures];
        int numInstances = instances.size();

        for (Instance instance : instances) {
            for (int i = 0; i < numFeatures; i++) {
                centroid[i] += instance.getFeatureValue(i);
            }
        }
        for (int i = 0; i < numFeatures; i++) {
            centroid[i] /= numInstances;
        }
        return new UnlabelledInstance(centroid);
    }

    /**
     * Bad design. Pls fix.
     *
     * @param instances
     */
    public static void initialise(List<Instance> instances, boolean doNNs) {
        int numFeatures = instances.get(0).numFeatures();
        ALL_FEATURES = new boolean[numFeatures];
        Arrays.fill(ALL_FEATURES, true);
        EUCLIDEAN_DISSIM_MAP = new EucledianDissimilarityMap(instances);
        // MANHATTAN_DISSIM_MAP = new ManhattanDissimilarityMap(instances);
        // POINT_ONE_NORM_DISSIM_MAP = new PointOneNormDissimilarityMap(instances);


        //DissimilarityMap dissimilarityMap;

        String dissimMapToUse = Main.CONFIG.getProperty("dissimMapToUse");
        switch (dissimMapToUse) {
            case "euclidean":
                DEFAULT_MAP = EUCLIDEAN_DISSIM_MAP;
                break;
            case "manhattan":
                DEFAULT_MAP = MANHATTAN_DISSIM_MAP;
                break;
            case "fractional0.1":
                DEFAULT_MAP = POINT_ONE_NORM_DISSIM_MAP;
                break;
            default:
                throw new IllegalArgumentException(dissimMapToUse);
        }

        if (doNNs) {


            NEAREST_NEIGHBOURS = new HashMap<>();
            for (int i = 0; i < instances.size(); i++) {
                Instance instance = instances.get(i);
                List<Instance> nearestNeighbours = new ArrayList<>();
                // if (FIXED_NUM_NNs) {
                PriorityQueue<Instance> nnQueue = new PriorityQueue<>(new Comparator<Instance>() {
                    @Override
                    public int compare(Instance i1, Instance i2) {
                        return Double.compare(DEFAULT_MAP.getDissim(instance, i1), DEFAULT_MAP.getDissim(instance, i2));
                        //     return Double.compare(instance.distanceTo(i1, ALL_FEATURES, EUCLIDEAN_DISTANCE), instance.distanceTo(i2, ALL_FEATURES, EUCLIDEAN_DISTANCE));
                    }

                });

                for (int j = 0; j < instances.size(); j++) {
                    if (i != j) {
                        nnQueue.add(instances.get(j));
                    }
                }
                if (FIXED_NUM_NNs) {
                    for (int k = 0; k < NUM_NNs; k++) {
                        Instance neighbour = nnQueue.poll();
                        //    if (instance.meanDifference(neighbour, ALL_FEATURES) < GraphMedoidParticle.MAX_DIST) {
                        nearestNeighbours.add(neighbour);
                        //  } else {
                        //    break;
                        // }
                    }
                } else {
                    int queueSize = nnQueue.size();
                    for (int j = 0; j < queueSize; j++) {
                        Instance neighbour = nnQueue.poll();
                        //    if (instance.meanDifference(neighbour, ALL_FEATURES) < GraphMedoidParticle.MAX_DIST) {
                        nearestNeighbours.add(neighbour);
                        //      }
                    }
                }
//                } else {
//                    for (int j = 0; j < instances.size(); j++) {
//                        if (i != j) {
//                    //        if (EUCLIDEAN_DISSIM_MAP.dissims[i][j] < averageDissim/2) {
//                               nearestNeighbours.add(instances.get(j));
//                            }
//                  //      }
//                    }
//
//                }
                NEAREST_NEIGHBOURS.put(instance, nearestNeighbours);
            }
        }

    }

    public static int[] featuresUsed(double[] features) {
        boolean[] featuresSelected = new boolean[features.length];
        for (int i = 0; i < features.length; i++) {
            if (features[i] != 0) featuresSelected[i] = true;

        }
        return featuresUsed(featuresSelected);
    }

    public static class UsedFeatures {
        private int numFeaturesUsed;
        private StringBuilder usedFeatures;

        public UsedFeatures(boolean... featureSubset) {
            numFeaturesUsed = 0;
            usedFeatures = new StringBuilder("");
            for (int i = 0; i < featureSubset.length; i++) {
                boolean b = featureSubset[i];
                if (b) {
                    numFeaturesUsed++;
                    usedFeatures.append("f").append(i).append(", ");
                }

            }
        }

        public int numFeaturesUsed() {
            return numFeaturesUsed;
        }

        public StringBuilder usedFeatures() {
            return usedFeatures;
        }

    }

    public static class ManhattanDissimilarityMap implements DissimilarityMap {
        private final double[][] dissims;
        private final double medianDissim;

        private double averageDissim;

        public ManhattanDissimilarityMap(List<? extends Instance> instances) {
            int n = instances.size();
            dissims = new double[n][n];
            List<Double> dissimsList = new ArrayList<>();

            for (int i = 0; i < n; i++) {
                Instance instance = instances.get(i);
                //By definition.
                dissims[instance.instanceID][instance.instanceID] = 0d;
                for (int j = i + 1; j < n; j++) {
                    Instance otherInstance = instances.get(j);
                    double dist = instance.distanceTo(otherInstance, ALL_FEATURES, MANHATTAN_DISTANCE);
                    dissims[instance.instanceID][otherInstance.instanceID] = dist;
                    dissims[otherInstance.instanceID][instance.instanceID] = dist;
                    dissimsList.add(dist);

                }
            }

            Collections.sort(dissimsList);
            medianDissim = dissimsList.get(dissimsList.size() / 2);
            averageDissim = dissimsList.stream().mapToDouble(d -> d).average().getAsDouble();
        }

        public double getDissim(Instance i1, Instance i2) {
            return dissims[i1.instanceID][i2.instanceID];
        }


        @Override
        public double averageDissim() {
            return averageDissim;
        }
    }

    public static class EucledianDissimilarityMap implements DissimilarityMap {
        public final double[][] dissims;
        //   private final Map<Instance, Integer> indices;
        private final double medianDissim;
        private final double averageDissim;

        public EucledianDissimilarityMap(List<? extends Instance> instances) {
            int n = instances.size();
            System.out.println(n);
            dissims = new double[n][n];
            List<Double> dissimsList = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                Instance instance = instances.get(i);
                //By definition.
                dissims[instance.instanceID][instance.instanceID] = 0d;
                for (int j = i + 1; j < n; j++) {
                    Instance otherInstance = instances.get(j);
                    double dist = instance.distanceTo(otherInstance, ALL_FEATURES, EUCLIDEAN_DISTANCE);
                    dissims[instance.instanceID][otherInstance.instanceID] = dist;
                    dissims[otherInstance.instanceID][instance.instanceID] = dist;
                    dissimsList.add(dist);
                }
            }
            Collections.sort(dissimsList);
            medianDissim = dissimsList.get(dissimsList.size() / 2);
            averageDissim = dissimsList.stream().mapToDouble(d -> d).average().getAsDouble();
        }

        public double getDissim(Instance i1, Instance i2) {
            return dissims[i1.instanceID][i2.instanceID];
        }

        @Override
        public double averageDissim() {
            return averageDissim;
        }

        public double medianDissim() {
            return medianDissim;
        }
    }

    public static class PointOneNormDissimilarityMap implements DissimilarityMap {
        public final double[][] dissims;
        //   private final Map<Instance, Integer> indices;
        private final double averageDissim;

        public PointOneNormDissimilarityMap(List<? extends Instance> instances) {
            int n = instances.size();
            dissims = new double[n][n];
            List<Double> dissimsList = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                Instance instance = instances.get(i);
                //By definition.
                dissims[instance.instanceID][instance.instanceID] = 0d;
                for (int j = i + 1; j < n; j++) {
                    Instance otherInstance = instances.get(j);
                    double dist = instance.distanceTo(otherInstance, ALL_FEATURES, POINT_ONE_NORM);
                    dissims[instance.instanceID][otherInstance.instanceID] = dist;
                    dissims[otherInstance.instanceID][instance.instanceID] = dist;
                    dissimsList.add(dist);
                }
            }
            Collections.sort(dissimsList);
            averageDissim = dissimsList.stream().mapToDouble(d -> d).average().getAsDouble();
        }

        public double getDissim(Instance i1, Instance i2) {
            return dissims[i1.instanceID][i2.instanceID];
        }

        @Override
        public double averageDissim() {
            return averageDissim;
        }
    }
}
