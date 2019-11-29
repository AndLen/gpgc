package data;

import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.ml.clustering.Clusterable;
import other.DatasetUtils;
import other.Util;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by lensenandr on 2/03/16.
 */
public class Instance implements Clusterable, Serializable {
    //Allow hashcode and equals while allowing instances with the same feature values...
    public final int instanceID;
    public final double[] featureValues;
    final String classLabel;
    private final int numFeatures;
    public static final long serialVersionUID = 42L;


    public Instance(double[] featureValues, String classLabel, int instanceID) {
        this.featureValues = featureValues;
        this.classLabel = classLabel;
        this.instanceID = instanceID;
        this.numFeatures = featureValues.length;
    }

    public static double distanceBetween(double[] instance1, double[] instance2, boolean[] featureSubset) {
        //Eucledian distance
        double runningSum = 0;
        for (int i = 0; i < featureSubset.length; i++) {
            if (featureSubset[i]) {
                //feature is selected
                double featureDistance = instance1[i] - instance2[i];
                runningSum += (featureDistance * featureDistance);
            }

        }
        return Math.sqrt(runningSum);
    }

    public double getFeatureValue(int index) {
        return featureValues[index];
    }

    public int numFeatures() {
        return numFeatures;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Instance instance = (Instance) o;
        return instanceID==instance.instanceID;

    }

    @Override
    public int hashCode() {
        return instanceID;
    }

    /**
     * @param other
     * @param featureSubset boolean array of whether features selected.
     * @param distanceMeasure
     * @return
     */
    public double distanceTo(Instance other, boolean[] featureSubset, Util.DistanceMeasure distanceMeasure) {
        //Euclidean distance
        return distanceMeasure.distance(this,other,featureSubset);

    }

    public double meanDifference(Instance other, boolean[] featureSubset) {
        double runningSum = 0;
        double numSelectedFeatures = 0;
        for (int i = 0; i < numFeatures; i++) {
            if (featureSubset[i]) {
                //feature is selected
                numSelectedFeatures++;
                double featureDistance = Math.abs(featureValues[i] - other.featureValues[i]);
                runningSum += featureDistance / DatasetUtils.FEATURE_RANGE2;
            }

        }
        return runningSum / numSelectedFeatures;
    }

    @Override
    public String toString() {
        return "Instance{" +
                "featureValues=" + Arrays.toString(featureValues) +
                '}';
    }

    @Override
    public Instance clone() {
        return new Instance(Arrays.copyOf(featureValues, featureValues.length), classLabel, instanceID);
    }

    public Instance scaledCopy(double[] minFeatureVals, double[] maxFeatureVals) {
        double[] scaledFeatures = new double[numFeatures];
        for (int i = 0; i < numFeatures; i++) {
            double featureValue = getFeatureValue(i);
            scaledFeatures[i] = minFeatureVals[i] == maxFeatureVals[i] ? 0 : Util.scale(featureValue, minFeatureVals[i], maxFeatureVals[i]);
        }
        return new Instance(scaledFeatures, classLabel,instanceID);
    }

    public Instance normalisedCopy(double[] featureMeans, double featureStdDevs[]) {
        double[] normalisedFeatures = new double[numFeatures];
        for (int i = 0; i < numFeatures; i++) {
            double featureValue = getFeatureValue(i);
            double newVal = (featureValue - featureMeans[i]) / featureStdDevs[i];
            normalisedFeatures[i] = featureStdDevs[i] == 0 ? 0 : newVal;
        }
        return new Instance(normalisedFeatures, classLabel,instanceID);
    }

    public String getClassLabel() {
        if (classLabel == null) {
            throw new IllegalStateException();
        }
        return classLabel;
    }

    public RealMatrix toRowVector(boolean[] featuresToUse) {
        int numFeatures = DatasetUtils.numFeaturesUsed(featuresToUse);
        double[] data = new double[numFeatures];
        int index = 0;
        for (int i = 0; i < featuresToUse.length; i++) {
            if (featuresToUse[i]) {
                data[index] = getFeatureValue(i);
                index++;
            }

        }
        if (index != numFeatures) {
            throw new IllegalStateException();
        }

        double[][] rawData = BlockRealMatrix.toBlocksLayout(new double[][]{data});
        return new BlockRealMatrix(1, numFeatures, rawData, true);
    }

    @Override
    public double[] getPoint() {
        return featureValues;
    }

}
