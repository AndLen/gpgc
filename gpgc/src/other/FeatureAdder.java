package other;

import data.Instance;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.IntStream;

import static other.FeatureAdder.NOISE_TYPE.RANDOM;

/**
 * Created by lensenandr on 28/08/17.
 */
public class FeatureAdder {
    public static double FEATURE_REDUNDANCY_RATIO = 10;
    public static double FEATURE_NOISE_RATIO = 2;
    public static double REDUNDANT_FEATURE_NOISE_CAP = 0.1;
    public static double FEATURE_MULTIPLIER_MIN = 1;
    public static double FEATURE_MULTIPLIER_MAX = 5;
    public static NOISE_TYPE NOISE_TYPE = RANDOM;

    /**
     * This is written to fail easily in the case that lengths etc have been screwed up.
     */
    public static void main(String args[]) throws IOException {
        Path sourcePath = Paths.get(System.getProperty("user.dir"), "/datasets", args[0]);
        List<String> lines = Files.readAllLines(sourcePath);
        String[] header = lines.get(0).split(",");
        List<Instance> processedInstances = Main.SetupSystem.getRawInstances(lines, header);
        int numClusters = Integer.parseInt(header[2]);
        int numSourceFeatures = Integer.parseInt(header[1]);
        int numInstances = processedInstances.size();

        int numRedundantFeatures = (int) (numSourceFeatures * FEATURE_REDUNDANCY_RATIO);
        System.out.printf("Generating %d redundant features.\n", numRedundantFeatures);
        double[][] redundantFeatures = getRedundantFeatures(processedInstances, numSourceFeatures, numInstances, numRedundantFeatures);

        int numNoisyFeatures = (int) (numSourceFeatures * FEATURE_NOISE_RATIO);
        System.out.printf("Generating %d noisy features.\n", numNoisyFeatures);

        double[][] noisyFeatures = generateNoisyFeatures(numInstances, numNoisyFeatures);

        int newTotalFeatures = numSourceFeatures + numRedundantFeatures + numNoisyFeatures;

        System.out.printf("Now have %d features in total. %d original, %d redundant, %d noisy.\n", newTotalFeatures, numSourceFeatures, numRedundantFeatures, numNoisyFeatures);


        List<Instance> newInstances = new ArrayList<>(numInstances);
        for (int i = 0; i < processedInstances.size(); i++) {
            Instance instance = processedInstances.get(i);
            double[] combinedFeatures = new double[newTotalFeatures];
            System.arraycopy(instance.featureValues, 0, combinedFeatures, 0, numSourceFeatures);
            System.arraycopy(redundantFeatures[i], 0, combinedFeatures, numSourceFeatures, numRedundantFeatures);
            System.arraycopy(noisyFeatures[i], 0, combinedFeatures, numSourceFeatures + numRedundantFeatures, numNoisyFeatures);
            newInstances.add(new Instance(combinedFeatures, instance.getClassLabel(), instance.instanceID));
        }

        //Rescale it all, since noise may put it below 0!!
        System.out.println("Rescaling features to [0,1].");
        List<Instance> scaledInstances = DatasetUtils.scaleInstances(newInstances);

        List<String> linesToWrite = new ArrayList<>(numInstances + 1);
        linesToWrite.add(String.format("classLast,%d,%d,comma", newTotalFeatures, numClusters));
        System.out.println("Writing to file.");

        for (Instance instance : scaledInstances) {
            StringBuilder sb = new StringBuilder();

            for (double fV : instance.featureValues) {
                sb.append(fV).append(", ");
            }
            sb.append(instance.getClassLabel());
            String s = sb.toString();

            // System.out.println(s);
            linesToWrite.add(s);
        }
        writeToFile(sourcePath, linesToWrite);

    }

    static void writeToFile(Path sourcePath, List<String> linesToWrite) {
        String fileName = sourcePath.getFileName().toString();

        String dir = sourcePath.getParent().toString();

        Path destPath = Paths.get(System.getProperty("user.dir"), "/datasets/featureGroup/", fileName.replace(fileName.substring(fileName.lastIndexOf(".")), ".fg"));

        if (destPath != null) {
            try {

                Files.write(destPath, linesToWrite);
                System.out.printf("Written to %s.\n", destPath.toString());

            } catch (IOException e1) {
                throw new Error(e1);
            }
        }
    }

    static double[][] getRedundantFeatures(List<Instance> processedInstances, int numSourceFeatures, int numInstances, int numRedundantFeatures) {
        double[][] redundantFeatures = new double[numInstances][numRedundantFeatures];

        double MIN_NOISE = 1 - REDUNDANT_FEATURE_NOISE_CAP;
        double MAX_NOISE = 1 + REDUNDANT_FEATURE_NOISE_CAP;


        int[] sourceFeatures = new int[numRedundantFeatures];
        double[] featureMultipliers = new double[numRedundantFeatures];
        Map<Integer, List<Integer>> sourceFeatureUsages = new TreeMap<>();
        IntStream.range(0, numSourceFeatures).forEach(i -> sourceFeatureUsages.put(i, new LinkedList<>()));
        for (int i = 0; i < numRedundantFeatures; i++) {
            //Uniformly choose from source features
            sourceFeatures[i] = Util.randomInt(numSourceFeatures);
            sourceFeatureUsages.get(sourceFeatures[i]).add(numSourceFeatures + i);
            //Choose our multiplier for this feature
            featureMultipliers[i] = Util.randomInRange(FEATURE_MULTIPLIER_MIN, FEATURE_MULTIPLIER_MAX);

        }
        System.out.printf("Source features: %s\n", Arrays.toString(sourceFeatures));
        System.out.printf("Source feature WEIGHTS: %s\n", Arrays.toString(featureMultipliers));
        sourceFeatureUsages.forEach((k, v) -> System.out.printf("Feature %d has redundant features: %s\n", k, v));


        //For each instance
        for (int i = 0; i < numInstances; i++) {
            double[] thisInstanceValues = processedInstances.get(i).featureValues;
            //For each redundant feature
            for (int j = 0; j < numRedundantFeatures; j++) {
                //Scale the source feature
                double scaledFeature = thisInstanceValues[sourceFeatures[j]] * featureMultipliers[j];
                //And add some noise
                double noisyFeature = scaledFeature * Util.randomInRange(MIN_NOISE, MAX_NOISE);
                redundantFeatures[i][j] = noisyFeature;
            }


        }
        return redundantFeatures;
    }

    static double[][] generateNoisyFeatures(int numInstances, int numNoisyFeatures) {
        double[][] noisyFeatures = new double[numInstances][numNoisyFeatures];
        //For each instance
        for (int i = 0; i < numInstances; i++) {
            //For each redundant feature
            for (int j = 0; j < numNoisyFeatures; j++) {
                //For now
                double noisyFeature = NOISE_TYPE.generateNoise(0, 1);
                noisyFeatures[i][j] = noisyFeature;
            }


        }
        return noisyFeatures;
    }

    public enum NOISE_TYPE {

        RANDOM {
            @Override
            public double generateNoise(double min, double max) {
                return Util.randomInRange(min, max);
            }
        };

        public abstract double generateNoise(double min, double max);

    }
}
