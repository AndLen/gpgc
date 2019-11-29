package data;

import java.util.List;

/**
 * Created by lensenandr on 7/04/16.
 */
public class UnlabelledInstance extends Instance {
    public UnlabelledInstance(double[] featureValues) {
        super(featureValues, null, -1);
    }

    public static UnlabelledInstance fromList(List<Double> featureValues) {
        double[] features = new double[featureValues.size()];
        for (int i = 0; i < featureValues.size(); i++) {
            features[i] = featureValues.get(i);
        }
        return new UnlabelledInstance(features);
    }


    public String getClassLabel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object o) {
        throw new UnsupportedOperationException();

    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException();
    }

}
