package clustering;

import data.Instance;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import other.DatasetUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Based on https://github.com/apache/commons-math/blob/master/src/main/java/org/apache/commons/math4/ml/clustering/DBSCANClusterer.java
 * Created by lensenandr on 9/03/16.
 */
public class DBSCANClustering {
    //From WEKA: http://weka.sourceforge.net/doc.stable/weka/clusterers/DBSCAN.html
    public static final double EPSILON = 0.1;
    public static final int MIN_POINTS = 5;

    public static List<CentroidCluster> doDBSCAN(List<Instance> instances) {

        DBSCANClusterer<Instance> dbscanClusterer = new DBSCANClusterer<>(EPSILON, MIN_POINTS);
        List<org.apache.commons.math3.ml.clustering.Cluster<Instance>> clusters = dbscanClusterer.cluster(instances);
        List<CentroidCluster> finalClusters = new ArrayList<>();
        Set<Instance> clustedInstances = new HashSet<>();
        for (Cluster<Instance> cluster : clusters) {
            List<Instance> points = cluster.getPoints();
            clustedInstances.addAll(points);
            CentroidCluster newCluster = new CentroidCluster(DatasetUtils.getSampleMean(points));
            newCluster.addAllInstances(points);
            finalClusters.add(newCluster);
        }

        for (Instance instance : instances) {
            if (!clustedInstances.contains(instance)) {
                CentroidCluster cluster = new CentroidCluster(instance);
                cluster.addInstance(instance);
                finalClusters.add(cluster);
            }
        }

        return finalClusters;
    }

}
