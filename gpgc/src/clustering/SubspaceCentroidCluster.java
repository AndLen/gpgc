package clustering;

import data.Instance;
import other.DatasetUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Created by lensenandr on 2/05/17.
 */
public class SubspaceCentroidCluster extends CentroidCluster {
    public final boolean[] featureSubset;

    public SubspaceCentroidCluster(Instance centroid, boolean[] featureSubset) {
        super(centroid);
        this.featureSubset = featureSubset;
    }

    @Override
    public String getPrototypeToString(List<Instance> allInstances) {
        //Sorted
        int[] featuresUsed = DatasetUtils.featuresUsed(featureSubset);
        String features = Arrays.toString(featuresUsed);
        double[] centroidUsed = new double[featuresUsed.length];
        int index = 0;
        for (int i : featuresUsed) {
            centroidUsed[index++] = centroid.getFeatureValue(i);
        }

        return "Features: " + features + " " + Arrays.toString(centroidUsed);
    }

}
