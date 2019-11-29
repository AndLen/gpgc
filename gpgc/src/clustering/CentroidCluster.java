package clustering;


import data.Instance;
import other.Main;
import other.Util;

import java.util.List;

/**
 * Created by lensenandr on 9/03/16.
 */
public class CentroidCluster extends Cluster {

    protected final Instance centroid;

    public CentroidCluster(Instance centroid) {
        super();
        this.centroid = centroid;
    }

    public static List<CentroidCluster> clusterify(List<Instance> instances, boolean[] featureSubset, List<Instance> prototypes, Util.DistanceMeasure distanceMeasure) {
        if(Main.CONFIG.getBoolean("kdTreeNearestNeighbour")){
            throw new IllegalArgumentException();
            //return kdTreeNearestPrototypeCluster(instancesCl,featureSubset,prototypes);
        }else {
            return nearestPrototypeCluster(instances, featureSubset, prototypes,distanceMeasure);
        }
    }

    public Instance getPrototype() {
        return centroid;
    }

    @Override
    public String getPrototypeToString(List<Instance> allInstances) {
        return centroid.toString();
    }


    @Override
    public double findDistance(Instance instance, boolean[] featureSubset, Util.DistanceMeasure distanceMeasure) {
        return centroid.distanceTo(instance, featureSubset,distanceMeasure);
    }

}
