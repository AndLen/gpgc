package clustering;

import data.Instance;
import other.DatasetUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static other.Main.CONFIG;

/**
 * Created by lensenandr on 21/02/18.
 */
public class MOCKWrapper {
    public static List<CentroidCluster> doMOCK(List<Instance> instances) throws InterruptedException, IOException {
        String dataset = CONFIG.getProperty("dataset").replaceAll("/", "");

        Path tempOutFile = Files.createTempFile(dataset, ".toMOCK");
        Path tempInFile = Files.createTempFile(dataset, ".fromMOCK");

        try {
            List<Instance> sortedInstances = new ArrayList<>(instances);
            //Just to make sure
            Collections.sort(sortedInstances, (o1, o2) -> Integer.compare(o1.instanceID, o2.instanceID));
            List<String> lines = new ArrayList<>();
            sortedInstances.forEach(i -> {
                StringBuilder sb = new StringBuilder();
                double[] featureValues = i.featureValues;
                for (int j = 0; j < featureValues.length; j++) {
                    double featureValue = featureValues[j];
                    sb.append(featureValue);
                    if (j != featureValues.length - 1) sb.append(" ");
                }
                //No class label here.
                //sb.append(i.getClassLabel());

                lines.add(sb.toString());
            });

            Files.write(tempOutFile, lines);

            ProcessBuilder pb = new ProcessBuilder("Rscript", "/home/lensenandr/phd/journals/gpgcECJ/doMock.R", tempOutFile.toString(), tempInFile.toString(), "" + tempOutFile.toString().hashCode());
            pb.inheritIO();
            Process prcs = pb.start();
            int exit = prcs.waitFor();
            List<String> strings = Files.readAllLines(tempInFile);
            if (exit != 0) {
                throw new IllegalStateException(String.valueOf(exit));
            }

            if (strings.size() != 1) {
                throw new IllegalStateException(strings.toString());
            }
            String[] clusterAssignments = strings.get(0).split(" ");
            Map<String, List<Instance>> instanceAssignments = new HashMap<>();
            for (int i = 0; i < clusterAssignments.length; i++) {
                String clusterAssignment = clusterAssignments[i];
                List<Instance> thisCluster = instanceAssignments.getOrDefault(clusterAssignment, new ArrayList<>());
                thisCluster.add(sortedInstances.get(i));
                instanceAssignments.put(clusterAssignment, thisCluster);
            }
            List<CentroidCluster> finalClusters = new ArrayList<>();

            for (List<Instance> thisCluster : instanceAssignments.values()) {
                CentroidCluster centroidCluster = new CentroidCluster(DatasetUtils.getSampleMean(thisCluster));
                centroidCluster.addAllInstances(thisCluster);
                finalClusters.add(centroidCluster);
            }
            return finalClusters;
        } finally {
            Files.deleteIfExists(tempOutFile);
            Files.deleteIfExists(tempInFile);
        }


    }

}

