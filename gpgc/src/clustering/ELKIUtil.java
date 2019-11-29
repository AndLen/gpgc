package clustering;

import data.Instance;
import de.lmu.ifi.dbs.elki.algorithm.clustering.optics.OPTICSHeap;
import de.lmu.ifi.dbs.elki.algorithm.clustering.optics.OPTICSXi;
import de.lmu.ifi.dbs.elki.data.Clustering;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.data.model.AbstractModel;
import de.lmu.ifi.dbs.elki.data.model.OPTICSModel;
import de.lmu.ifi.dbs.elki.data.model.SubspaceModel;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.StaticArrayDatabase;
import de.lmu.ifi.dbs.elki.database.ids.DBIDIter;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.datasource.ArrayAdapterDatabaseConnection;
import de.lmu.ifi.dbs.elki.datasource.DatabaseConnection;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.EuclideanDistanceFunction;
import other.DatasetUtils;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;


public class ELKIUtil {
    public static final int MIN_POINTS = 10;
    //public static final double XI = 0.1;

//    public static List<Cluster> doOPTICSSearch(List<Instance> instances, int k) {
//        double lowerBound = 0, upperBound = 1;
//
//        int times = 100;
//        double lastXI = -1;
//        List<Cluster> clusters = null;
//        for (int i = 0; i < times; i++) {
//            double thisXI = (upperBound - lowerBound) / 2 + lowerBound;
//            clusters = doOPTICS(instances, thisXI);
//            int thisK = clusters.size();
//            System.out.println(thisK + " " + thisXI);
//            if (thisK == k) {
//                return clusters;
//            } else {
//                if (thisK > k) lowerBound = thisXI;
//                else upperBound = thisXI;
//            }
//        }
//        return clusters;
//    }

    public static List<SubspaceCentroidCluster> doOpenSubspace(List<Instance> instances, String algorithmName) {
        //  return DoOpenSubspace.doOpenSubspace(instances,k);
        try {
            Path tempOutFile = Files.createTempFile(null, ".toSubspace");
            Path tempInFile = Files.createTempFile(null, ".fromSubspace");
            ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(tempOutFile));
            oos.writeObject(algorithmName);
            oos.writeObject(instances);
            oos.close();
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "java", "-jar", "/home/lensenandr/IdeaProjects/phd/out/artifacts/OSInterface_jar/OSInterface.jar",
                    tempOutFile.toString(), tempInFile.toString());
            //    processBuilder.redirectErrorStream(true);
            processBuilder.inheritIO();
            System.out.println(processBuilder.environment());
            Process start = processBuilder.start();
            start.waitFor();
            ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(tempInFile));
            List<SubspaceCentroidCluster> subspaceCentroidClusters = (List<SubspaceCentroidCluster>) ois.readObject();
            ois.close();
            Files.deleteIfExists(tempOutFile);
            Files.deleteIfExists(tempInFile);
            return subspaceCentroidClusters;
        } catch (IOException | ClassNotFoundException | InterruptedException e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }


    private static Instances toWekaFormat(List<Instance> instances, Map<weka.core.Instance, Instance> mapBack) {
        Set<String> classStrings = new HashSet<>();
        instances.forEach(i -> classStrings.add(i.getClassLabel()));
        FastVector att = new FastVector();
        for (int i = 0; i < instances.get(0).numFeatures(); i++) {
            att.addElement(new Attribute("att" + i));
        }

        FastVector fvNominalVal = new FastVector(classStrings.size());
        for (String s : classStrings) {
            fvNominalVal.addElement(s);
        }
        Attribute ca = new Attribute("classAtt", fvNominalVal);
        att.addElement(ca);

        Instances wData = new Instances("weka_convert", att, instances.size());
        wData.setClass(ca);

        for (Instance i : instances) {

            weka.core.Instance instance = instanceToWeka(i, wData);
            wData.add(instance);
            weka.core.Instance copiedInstance = wData.lastInstance();
            //For some reason weka copies the instanceCl....
            //    System.out.println(instanceCl + " " + copiedInstance);
            mapBack.put(copiedInstance, i);
        }

        return wData;
    }


    private static weka.core.Instance instanceToWeka(Instance inst, Instances wData) {
        double[] values = new double[inst.numFeatures() + 1];
        // System.arraycopy(i.values().t.toArray(), 0, values, 0, classSet ?
        // values.length - 1 : values.length);
        for (int i = 0; i < values.length - 1; i++) {
            values[i] = inst.getFeatureValue(i);
        }
        // if (classSet)
        // values[values.length - 1] = inst.classValue();

        weka.core.Instance wI = new weka.core.DenseInstance(1, values);
        wI.setDataset(wData);
        wI.setClassValue(inst.getClassLabel());

        return wI;
    }

    private static List<SubspaceCentroidCluster> getSubspaceClusters(List<Instance> instances, Database db, Clustering<SubspaceModel> clustering) {
        Relation<String> labelIDs = db.getRelation(TypeUtil.STRING);

        List<SubspaceCentroidCluster> finalClusters = new ArrayList<>();

        for (de.lmu.ifi.dbs.elki.data.Cluster<SubspaceModel> clu : clustering.getAllClusters()) {
            List<Integer> pointIndices = new ArrayList<>();
            List<Instance> points = new ArrayList<>();
            for (DBIDIter it = clu.getIDs().iter(); it.valid(); it.advance()) {
                // To get the vector use:
                String id = labelIDs.get(it);
                int index = Integer.parseInt(id);
                pointIndices.add(index);
            }
            Collections.sort(pointIndices);
            for (Integer index : pointIndices) {
                points.add(instances.get(index));
            }
            if (points.size() > 0) {
                //  System.err.println(clu.getModel().getSubspace().dimensonsToString());
                //For some reason ELKI stores dimensions as bitsets so this is easiest way to seperate...
                String[] dimensions = clu.getModel().getSubspace().dimensonsToString(", ").split(", ");
                //   System.err.println(Arrays.toString(dimensions));
                List<Integer> dimensionsInt = Arrays.stream(dimensions).map(s -> Integer.parseInt(s.replace("[", "").replace("]", "")) - 1).collect(Collectors.toList());
                //     System.err.println(dimensionsInt);
                boolean[] featureSubset = DatasetUtils.featureSubset(dimensionsInt, instances.get(0).numFeatures());
                SubspaceCentroidCluster newCluster = new SubspaceCentroidCluster(DatasetUtils.getSampleMean(points), featureSubset);
                newCluster.addAllInstances(points);
                finalClusters.add(newCluster);
            }
        }

        return finalClusters;
    }

    public static List<Cluster> doOPTICS(List<Instance> instances, double XI) {
        OPTICSHeap<NumberVector> opticsHeap = new OPTICSHeap<>(EuclideanDistanceFunction.STATIC, Double.POSITIVE_INFINITY, MIN_POINTS);

        OPTICSXi opticsXi = new OPTICSXi(opticsHeap, XI);
        Database db = getDatabase(instances);


        //    ClusterOrder run = opticsHeap.run(db);

        Clustering<OPTICSModel> clustering = opticsXi.run(db);


        return getClusters(instances, db, clustering);
    }

    static List<Cluster> getClusters(List<Instance> instances, Database db, Clustering<? extends AbstractModel> clustering) {
        Relation<String> labelIDs = db.getRelation(TypeUtil.STRING);

        List<Cluster> finalClusters = new ArrayList<>();

        for (de.lmu.ifi.dbs.elki.data.Cluster<? extends AbstractModel> clu : clustering.getAllClusters()) {
            List<Integer> pointIndices = new ArrayList<>();
            List<Instance> points = new ArrayList<>();
            for (DBIDIter it = clu.getIDs().iter(); it.valid(); it.advance()) {
                // To get the vector use:
                String id = labelIDs.get(it);
                int index = Integer.parseInt(id);
                pointIndices.add(index);
            }
            Collections.sort(pointIndices);
            for (Integer index : pointIndices) {
                points.add(instances.get(index));
            }
            if (points.size() > 0) {
                Cluster newCluster = new MedoidCluster(points.get(0));
                newCluster.addAllInstances(points);
                finalClusters.add(newCluster);
            }
        }

        return finalClusters;
    }

    static Database getDatabase(List<Instance> instances) {
        // Adapter to load data from an existing array.
        double[][] data = new double[instances.size()][];
        String[] labels = new String[instances.size()];
        for (int i = 0; i < instances.size(); i++) {
            Instance instance = instances.get(i);
            data[i] = instance.featureValues;
            labels[i] = "" + i;
        }


        DatabaseConnection dbc = new ArrayAdapterDatabaseConnection(data, labels);
// Create a database (which may contain multiple relations!)
        Database db = new StaticArrayDatabase(dbc, null);
// Load the data into the database (do NOT forget to initialize...)
        db.initialize();
        return db;
    }


}
