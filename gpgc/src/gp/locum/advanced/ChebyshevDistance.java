package gp.locum.advanced;

import data.Instance;

/**
 * Created by lensenandr on 10/02/17.
 */
public class ChebyshevDistance extends DistanceTerminal {
    @Override
    public String toString() {
        return "chebyshevDist";
    }

    @Override
    public double getDistance(Instance instance1, Instance instance2) {
        double maxDistance = 0;
        for (int i = 0; i < instance1.numFeatures(); i++) {
            double diff = Math.abs(instance1.getFeatureValue(i) - instance2.getFeatureValue(i));
            maxDistance = Math.max(maxDistance, diff);
        }
        return maxDistance;
    }
}
