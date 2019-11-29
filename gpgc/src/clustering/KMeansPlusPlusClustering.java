package clustering;

import data.Instance;
import other.DatasetUtils;
import other.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Created by lensenandr on 9/03/16.
 */
public class KMeansPlusPlusClustering {
    public static List<CentroidCluster> doKMeansPlusPlus(List<? extends Instance> instances, int numClusters, int numIterations, Util.DistanceMeasure distanceMeasure) {
        double[] featureWeights = new double[DatasetUtils.ALL_FEATURES.length];
        for (int i = 0; i < featureWeights.length; i++) {
            featureWeights[i] = 1d;
        }

        List<Instance> means = kmPPInitialise(instances, numClusters, featureWeights, distanceMeasure);
        List<CentroidCluster> kMeansClustering = KMeansClustering.doKMeans(instances, numIterations, means, distanceMeasure);
        return kMeansClustering;
    }

    /**
     * Stolen from apache commons
     * http://commons.apache.org/proper/commons-math/apidocs/org/apache/commons/math4/ml/clustering/KMeansPlusPlusClusterer.html
     */
    public static List<Instance> kmPPInitialise(List<? extends Instance> instances, int numClusters, double[] featureWeights, Util.DistanceMeasure distanceMeasure) {
        // Convert to list for indexed access. Make it unmodifiable, since removal of items
        // would screw up the logic of this method.
        final List<Instance> pointList = Collections.unmodifiableList(new ArrayList<>(instances));

        // The number of points in the list.
        final int numPoints = pointList.size();

        // Set the corresponding element in this array to indicate when
        // elements of pointList are no longer available.
        final boolean[] taken = new boolean[numPoints];

        // The resulting list of initial centers.
        final List<Instance> resultSet = new ArrayList<>();

        // Choose one center uniformly at random from among the data points.
        final int firstPointIndex = Util.randomInt(numPoints);

        final Instance firstPoint = pointList.get(firstPointIndex);

        resultSet.add(firstPoint);

        // Must mark it as taken
        taken[firstPointIndex] = true;

        // To keep track of the minimum distance squared of elements of
        // pointList to elements of resultSet.
        final double[] minDistSquared = new double[numPoints];

        // Initialize the elements.  Since the only point in resultSet is firstPoint,
        // this is very easy.
        for (int i = 0; i < numPoints; i++) {
            if (i != firstPointIndex) { // That point isn't considered
                double d = firstPoint.distanceTo(pointList.get(i), DatasetUtils.ALL_FEATURES, distanceMeasure);
                //distanceMeasure.distance(firstPoint,pointList.get(i),DatasetUtils.ALL_FEATURES);
                //findDistance(firstPoint, pointList.get(i), featureWeights);
                //double d = DatasetUtils.EUCLIDEAN_DISSIM_MAP.getDissim(firstPoint, pointList.get(i));
                minDistSquared[i] = d * d;
            }
        }

        while (resultSet.size() < numClusters) {

            // Sum up the squared distances for the points in pointList not
            // already taken.
            double distSqSum = 0.0;

            for (int i = 0; i < numPoints; i++) {
                if (!taken[i]) {
                    distSqSum += minDistSquared[i];
                }
            }

            // Add one new data point as a center. Each point x is chosen with
            // probability proportional to D(x)2
            final double r = Util.randomDouble() * distSqSum;

            // The index of the next point to be added to the resultSet.
            int nextPointIndex = -1;

            // Sum through the squared min distances again, stopping when
            // sum >= r.
            double sum = 0.0;
            for (int i = 0; i < numPoints; i++) {
                if (!taken[i]) {
                    sum += minDistSquared[i];
                    if (sum >= r) {
                        nextPointIndex = i;
                        break;
                    }
                }
            }

            // If it's not set to >= 0, the point wasn't found in the previous
            // for loop, probably because distances are extremely small.  Just pick
            // the last available point.
            if (nextPointIndex == -1) {
                for (int i = numPoints - 1; i >= 0; i--) {
                    if (!taken[i]) {
                        nextPointIndex = i;
                        break;
                    }
                }
            }

            // We found one.
            if (nextPointIndex >= 0) {

                final Instance p = pointList.get(nextPointIndex);

                resultSet.add(p);

                // Mark it as taken.
                taken[nextPointIndex] = true;

                if (resultSet.size() < numClusters) {
                    // Now update elements of minDistSquared.  We only have to compute
                    // the distance to the new center to do this.
                    for (int j = 0; j < numPoints; j++) {
                        // Only have to worry about the points still not taken.
                        if (!taken[j]) {
                            double d = p.distanceTo(pointList.get(j), DatasetUtils.ALL_FEATURES, distanceMeasure);
                            //findDistance(p, pointList.get(j), featureWeights);

                            //    double d = DatasetUtils.EUCLIDEAN_DISSIM_MAP.getDissim(p, pointList.get(j));
                            double d2 = d * d;
                            if (d2 < minDistSquared[j]) {
                                minDistSquared[j] = d2;
                            }
                        }
                    }
                }

            } else {
                // None found --
                // Break from the while loop to prevent
                // an infinite loop.
                break;
            }
        }

        return resultSet;
    }

}
