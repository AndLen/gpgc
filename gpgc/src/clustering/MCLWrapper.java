package clustering;

import data.Instance;
import other.DatasetUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static other.Main.CONFIG;

/**
 * Created by lensenandr on 21/02/18.
 */
public class MCLWrapper {
    public static List<CentroidCluster> doMCL(List<Instance> instances) throws InterruptedException, IOException {
        String dataset = CONFIG.getProperty("dataset").replaceAll("/", "");

        Path path = Paths.get(String.format("/home/lensenandr/mcl-14-137/%s.mci", dataset));

        Map<Integer, Instance> idToInstance = new HashMap<>();
        instances.forEach(i -> idToInstance.put(i.instanceID, i));
        Map<Instance, Set<Instance>> edgesBasedOnMaxThreshold = NaiveGraphThreshold.getEdgesBasedOnThreshold(instances,
                NaiveGraphThreshold.getMaxDist(instances));

        int numNNs = 10;
        Set<String> lines = new LinkedHashSet<>(instances.size() * numNNs * 2);
        for (int i = 0; i < instances.size(); i++) {
            Instance one = instances.get(i);
            Set<Instance> nns = edgesBasedOnMaxThreshold.get(one);
            //DatasetUtils.NEAREST_NEIGHBOURS.get(one);
            for (Instance two : nns) {
                //for(int j = 0; j < numNNs; j++){
                //Instance two = nns.get(j);
                double sim = 1 - DatasetUtils.DEFAULT_MAP.getDissim(one, two);
                lines.add(one.instanceID + " " + two.instanceID + " " + sim);
                lines.add(two.instanceID + " " + one.instanceID + " " + sim);
            }

        }
        Files.createDirectories(path.getParent());
        Files.write(path, lines);

        ProcessBuilder pb = new ProcessBuilder("/am/courtenay/home1/lensenandr/local/bin/mcl", String.format("/home/lensenandr/mcl-14-137/%s.mci", dataset), "--abc");
        pb.directory(Paths.get("/home/lensenandr/mcl-14-137/").toFile());
        pb.inheritIO();
        Process prcs = pb.start();
        //  InputStreamReader isr = new InputStreamReader(prcs.getInputStream());
//        BufferedReader br = new BufferedReader(isr);
//        br.lines().forEach(l -> System.out.println());
//        br.close();
        prcs.waitFor();
        List<String> clusterLines = Files.readAllLines(Paths.get(String.format("/home/lensenandr/mcl-14-137/out.%s.mci.I20", dataset)));
        List<CentroidCluster> finalClusters = new ArrayList<>();
        clusterLines.forEach(s -> {
            String[] clusterIDs = s.split("\\t");
            List<Instance> thisCluster = new ArrayList<Instance>();
            for (String id : clusterIDs) {
                thisCluster.add(idToInstance.get(Integer.parseInt(id)));
            }
            CentroidCluster centroidCluster = new CentroidCluster(DatasetUtils.getSampleMean(thisCluster));
            centroidCluster.addAllInstances(thisCluster);
            finalClusters.add(centroidCluster);
        });
        //
//        List<net.sf.javaml.core.Instance> denseInstances = instancesCl.stream().map(DenseLabelledInstance::new).collect(Collectors.toList());
//        DefaultDataset data = new DefaultDataset(denseInstances);
//
//        cz.cvut.fit.krizeji1.markov_cluster.MclAlgorithm mclAlgorithm = new MclAlgorithm();
//
//        MCL mcl = new MCL(new JaccardIndexSimilarity(), 0.001, 1.4, 0, 0.001);
//        Dataset[] clusters = mcl.cluster(data);
//        List<CentroidCluster> finalClusters = new ArrayList<>();
//        for (Dataset cluster : clusters) {
//            List<Instance> thisCluster = cluster.stream().map(instanceCl -> ((DenseLabelledInstance) instanceCl).originalInstance).collect(Collectors.toList());
//            CentroidCluster centroidCluster = new CentroidCluster(DatasetUtils.getSampleMean(thisCluster));
//            centroidCluster.addAllInstances(thisCluster);
//            finalClusters.add(centroidCluster);
//        }
//        return finalClusters;
        return finalClusters;
    }

//    private static class DenseLabelledInstance extends DenseInstance {
//        private final Instance originalInstance;
//
//        private DenseLabelledInstance(Instance instanceCl) {
//            super(instanceCl.featureValues, instanceCl.getClassLabel());
//            originalInstance = instanceCl;
//        }
}

