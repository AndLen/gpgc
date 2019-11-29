package other;

import clusterFitness.ClusterFitnessFunction;
import clustering.*;
import data.Instance;
import gp.GPUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static clustering.CLICK.doCLICK;
import static clustering.DBSCANClustering.doDBSCAN;
import static clustering.HighlyConnectedSubgraphs.doHCS;
import static clustering.KMeansClustering.doKMeans;
import static clustering.KMeansPlusPlusClustering.doKMeansPlusPlus;
import static clustering.KMeansPlusPlusClustering.kmPPInitialise;
import static clustering.MCLWrapper.doMCL;
import static other.Util.LOG;

/**
 * Created by lensenandr on 6/03/18.
 */
public enum METHOD {
    K_MEANS {
        @Override
        TestResult doTest(int numFeatures, int numClusters, List<Instance> instances, ClusterFitnessFunction fitnessFunction) {
            List<CentroidCluster> clustering = doKMeans(instances, numClusters, Main.CONFIG.getInt("iterations"));
            return getTestResult(instances, fitnessFunction, clustering);
        }

        @Override
        String humanFriendly() {
            return "K-Means";
        }
    }, K_MEANS_PLUS_PLUS {
        @Override
        TestResult doTest(int numFeatures, int numClusters, List<Instance> instances, ClusterFitnessFunction fitnessFunction) {
            List<CentroidCluster> clustering = doKMeansPlusPlus(instances, numClusters, Main.CONFIG.getInt("iterations"), Util.getDistanceMeasureForClustering());
            return getTestResult(instances, fitnessFunction, clustering);
        }

        @Override
        String humanFriendly() {
            return "K-Means++";
        }
    }, DBSCAN {
        @Override
        TestResult doTest(int numFeatures, int numClusters, List<Instance> instances, ClusterFitnessFunction fitnessFunction) {
            List<CentroidCluster> clustering = doDBSCAN(instances);
            return getTestResult(instances, fitnessFunction, clustering);
        }

        @Override
        String humanFriendly() {
            return "DBSCAN";
        }
    }, OPTICS {
        @Override
        TestResult doTest(int numFeatures, int numClusters, List<Instance> instances, ClusterFitnessFunction fitnessFunction) {

            double opticsXI = Main.CONFIG.getDouble("opticsXI");
            LOG.printf("Xi: %.3f\n", opticsXI);
            List<Cluster> clustering = ELKIUtil.doOPTICS(instances, opticsXI);
            double fitness = fitnessFunction.fitness(clustering, instances, DatasetUtils.ALL_FEATURES, DatasetUtils.ALL_FEATURES);
            List<Double> blah = new ArrayList<>();
            blah.add(fitness);
            return new TestResult(clustering, fitness, blah);
        }

        @Override
        String humanFriendly() {
            return "OPTICS";
        }
    }, SIL_PAM {
        @Override
        TestResult doTest(int numFeatures, int numClusters, List<Instance> instances, ClusterFitnessFunction fitnessFunction) {
            LOG.println(new SilhouettePAM().estimatedNumberOfClusters(instances));
            return null;
        }

        @Override
        String humanFriendly() {
            return null;
        }
    },  GP {
        @Override
        TestResult doTest(int numFeatures, int numClusters, List<Instance> instances, ClusterFitnessFunction fitnessFunction) {
            return GPUtils.runGP(numFeatures, numClusters, instances, fitnessFunction, "src/gp/cluster.params");
        }

        @Override
        String humanFriendly() {
            return "GP";
        }
    }, GP_VECTOR {
        @Override
        TestResult doTest(int numFeatures, int numClusters, List<Instance> instances, ClusterFitnessFunction fitnessFunction) {
            return GPUtils.runGP(numFeatures, numClusters, instances, fitnessFunction, "src/gp/clusterVector.params");
        }

        @Override
        String humanFriendly() {
            return "GP Vector";
        }
    }, GP_VECTOR_PADDING {
        @Override
        TestResult doTest(int numFeatures, int numClusters, List<Instance> instances, ClusterFitnessFunction fitnessFunction) {
            return GPUtils.runGP(numFeatures, numClusters, instances, fitnessFunction, "src/gp/clusterVectorPadding.params");
        }

        @Override
        String humanFriendly() {
            return "GP Vector Padding";
        }
    }, GP_LOCUM {
        @Override
        TestResult doTest(int numFeatures, int numClusters, List<Instance> instances, ClusterFitnessFunction fitnessFunction) {
            return GPUtils.runGP(numFeatures, numClusters, instances, fitnessFunction, "src/gp/locum.params");
        }

        @Override
        String humanFriendly() {
            return "GP Locum";
        }
    }, GP_LOCUM_PAIR {
        @Override
        TestResult doTest(int numFeatures, int numClusters, List<Instance> instances, ClusterFitnessFunction fitnessFunction) {
            return GPUtils.runGP(numFeatures, numClusters, instances, fitnessFunction, "src/gp/locumPair.params");
        }

        @Override
        String humanFriendly() {
            return "GP Locum Pair";
        }
    }, GP_LOCUM_ARITH_TERMS {
        @Override
        TestResult doTest(int numFeatures, int numClusters, List<Instance> instances, ClusterFitnessFunction fitnessFunction) {
            return GPUtils.runGP(numFeatures, numClusters, instances, fitnessFunction, "src/gp/locumArithTerms.params");
        }

        @Override
        String humanFriendly() {
            return "GP Locum Arith Terms";
        }
    }, GP_DIST_TERMS {
        @Override
        TestResult doTest(int numFeatures, int numClusters, List<Instance> instances, ClusterFitnessFunction fitnessFunction) {
            return GPUtils.runGP(numFeatures, numClusters, instances, fitnessFunction, "src/gp/gpgcDistTerms.params");
        }

        @Override
        String humanFriendly() {
            return "GPGC Dist Terms";
        }
    }, GPGC_ARITH_V2 {
        @Override
        TestResult doTest(int numFeatures, int numClusters, List<Instance> instances, ClusterFitnessFunction fitnessFunction) {
            return GPUtils.runGP(numFeatures, numClusters, instances, fitnessFunction, "src/gp/gpgcArithV2.params");
        }

        @Override
        String humanFriendly() {
            return "GPGC Arith V2";
        }
    },
    NAIVE_GRAPH_THRESHOLD {
        @Override
        TestResult doTest(int numFeatures, int numClusters, List<Instance> instances, ClusterFitnessFunction fitnessFunction) {
            return getTestResult(instances, fitnessFunction, NaiveGraphThreshold.cluster(instances));
        }

        @Override
        String humanFriendly() {
            return "Naive Graph Threshold";
        }
    },
    NAIVE_GRAPH_NN {
        @Override
        TestResult doTest(int numFeatures, int numClusters, List<Instance> instances, ClusterFitnessFunction fitnessFunction) {
            return getTestResult(instances, fitnessFunction, NaiveGraphNN.cluster(instances));
        }

        @Override
        String humanFriendly() {
            return "Naive Graph 2NN";
        }
    },
     PROCLUS {
        @Override
        TestResult doTest(int numFeatures, int numClusters, List<Instance> instances, ClusterFitnessFunction fitnessFunction) {

            List<SubspaceCentroidCluster> clustering = ELKIUtil.doOpenSubspace(instances, "proclus");
            double fitness = fitnessFunction.fitness(clustering, instances, DatasetUtils.ALL_FEATURES, DatasetUtils.ALL_FEATURES);
            List<Double> blah = new ArrayList<>();
            blah.add(fitness);
            return new TestResult(clustering, fitness, blah);
        }

        @Override
        String humanFriendly() {
            return "PROCLUS";
        }
    }, FIRES {
        @Override
        TestResult doTest(int numFeatures, int numClusters, List<Instance> instances, ClusterFitnessFunction fitnessFunction) {

            List<SubspaceCentroidCluster> clustering = ELKIUtil.doOpenSubspace(instances, "fires");
            double fitness = fitnessFunction.fitness(clustering, instances, DatasetUtils.ALL_FEATURES, DatasetUtils.ALL_FEATURES);
            List<Double> blah = new ArrayList<>();
            blah.add(fitness);
            return new TestResult(clustering, fitness, blah);
        }

        @Override
        String humanFriendly() {
            return "FIRES";
        }
    }, MINECLUS {
        @Override
        TestResult doTest(int numFeatures, int numClusters, List<Instance> instances, ClusterFitnessFunction fitnessFunction) {

            List<SubspaceCentroidCluster> clustering = ELKIUtil.doOpenSubspace(instances, "mineclus");
            double fitness = fitnessFunction.fitness(clustering, instances, DatasetUtils.ALL_FEATURES, DatasetUtils.ALL_FEATURES);
            List<Double> blah = new ArrayList<>();
            blah.add(fitness);
            return new TestResult(clustering, fitness, blah);
        }

        @Override
        String humanFriendly() {
            return "MINECLUS";
        }
    }, DOC {
        @Override
        TestResult doTest(int numFeatures, int numClusters, List<Instance> instances, ClusterFitnessFunction fitnessFunction) {

            List<SubspaceCentroidCluster> clustering = ELKIUtil.doOpenSubspace(instances, "doc");
            double fitness = fitnessFunction.fitness(clustering, instances, DatasetUtils.ALL_FEATURES, DatasetUtils.ALL_FEATURES);
            List<Double> blah = new ArrayList<>();
            blah.add(fitness);
            return new TestResult(clustering, fitness, blah);
        }

        @Override
        String humanFriendly() {
            return "DOC";
        }
    }, INSCY {
        @Override
        TestResult doTest(int numFeatures, int numClusters, List<Instance> instances, ClusterFitnessFunction fitnessFunction) {
            List<SubspaceCentroidCluster> clustering = ELKIUtil.doOpenSubspace(instances, "inscy");
            double fitness = fitnessFunction.fitness(clustering, instances, DatasetUtils.ALL_FEATURES, DatasetUtils.ALL_FEATURES);
            List<Double> blah = new ArrayList<>();
            blah.add(fitness);
            return new TestResult(clustering, fitness, blah);
        }

        @Override
        String humanFriendly() {
            return "INSCY";
        }
    }, SUBCLU {
        @Override
        TestResult doTest(int numFeatures, int numClusters, List<Instance> instances, ClusterFitnessFunction fitnessFunction) {
            List<SubspaceCentroidCluster> clustering = ELKIUtil.doOpenSubspace(instances, "subclu");
            double fitness = fitnessFunction.fitness(clustering, instances, DatasetUtils.ALL_FEATURES, DatasetUtils.ALL_FEATURES);
            List<Double> blah = new ArrayList<>();
            blah.add(fitness);
            return new TestResult(clustering, fitness, blah);
        }

        @Override
        String humanFriendly() {
            return "SUBCLU";
        }
    },
    GPGC_SUBSPACE {
        @Override
        TestResult doTest(int numFeatures, int numClusters, List<Instance> instances, ClusterFitnessFunction fitnessFunction) {
            return GPUtils.runGP(numFeatures, numClusters, instances, fitnessFunction, "src/gp/gpgcSubspace.params");
        }

        @Override
        String humanFriendly() {
            return "GPGC SUBSPACE";
        }
    }, SUBSPACE_COEVOL {
        @Override
        TestResult doTest(int numFeatures, int numClusters, List<Instance> instances, ClusterFitnessFunction fitnessFunction) {
            Main.CONFIG.put("numsubpops", "" + numClusters);
            return GPUtils.runGP(numFeatures, numClusters, instances, fitnessFunction, "src/gp/subspaceCoevol.params");
        }

        @Override
        String humanFriendly() {
            return "SUBSPACE COEVOL";
        }
    },
    GP_SUBSPACE_ST {
        @Override
        TestResult doTest(int numFeatures, int numClusters, List<Instance> instances, ClusterFitnessFunction fitnessFunction) {
            return GPUtils.runGP(numFeatures, numClusters, instances, fitnessFunction, "src/gp/singleTreeSubspace.params");
        }

        @Override
        String humanFriendly() {
            return "GP Subspace ST";
        }
    }, RANDOM_FS_KMEANPLUS {
        @Override
        TestResult doTest(int numFeatures, int numClusters, List<Instance> instances, ClusterFitnessFunction fitnessFunction) {


            double[] features = new double[numFeatures];
            List<Integer> featureIndices = new ArrayList<>(numFeatures);
            for (int i = 0; i < numFeatures; i++) {
                featureIndices.add(i);
            }
            //TODO: random seed?
            Collections.shuffle(featureIndices);
            //TODO: num random selected.
            for (int i = 0; i < 500; i++) {
                features[featureIndices.get(i)] = 1;
            }
            LOG.println(Arrays.toString(DatasetUtils.featuresUsed(features)));
            LOG.println(Arrays.toString(features));
            List<Instance> means = kmPPInitialise(instances, numClusters, features, Util.getDistanceMeasureForClustering());

            List<CentroidCluster> clustering = KMeansClustering.doFGKMeans(instances, 100, means, features, Util.getDistanceMeasureForClustering());
            return getTestResult(instances, fitnessFunction, clustering);

        }

        @Override
        String humanFriendly() {
            return "Random FS KMeans++";
        }
    }, HCS {
        @Override
        TestResult doTest(int numFeatures, int numClusters, List<Instance> instances, ClusterFitnessFunction fitnessFunction) {
            List<CentroidCluster> clustering = null;
            try {
                clustering = doHCS(instances);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
            return getTestResult(instances, fitnessFunction, clustering);
        }

        @Override
        String humanFriendly() {
            return "HCS";
        }
    }, CLICK {
        @Override
        TestResult doTest(int numFeatures, int numClusters, List<Instance> instances, ClusterFitnessFunction fitnessFunction) {
            List<CentroidCluster> clustering = null;
            try {
                clustering = doCLICK(instances);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return getTestResult(instances, fitnessFunction, clustering);
        }

        @Override
        String humanFriendly() {
            return "CLICK";
        }
    }, MCL {
        @Override
        TestResult doTest(int numFeatures, int numClusters, List<Instance> instances, ClusterFitnessFunction fitnessFunction) {
            List<CentroidCluster> clustering = null;
            try {
                clustering = doMCL(instances);
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
            return getTestResult(instances, fitnessFunction, clustering);
        }

        @Override
        String humanFriendly() {
            return "MCL";
        }
    }, MOCK {
        @Override
        TestResult doTest(int numFeatures, int numClusters, List<Instance> instances, ClusterFitnessFunction fitnessFunction) {
            try {
                List<CentroidCluster> clustering = MOCKWrapper.doMOCK(instances);
                return getTestResult(instances, fitnessFunction, clustering);
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
                throw new Error(e);
            }
        }

        @Override
        String humanFriendly() {
            return "MOCK";
        }
    };

    public static TestResult getTestResult(List<Instance> instances, ClusterFitnessFunction fitnessFunction, List<? extends Cluster> clustering) {
        double fitness = fitnessFunction.fitness(clustering, instances, DatasetUtils.ALL_FEATURES, DatasetUtils.ALL_FEATURES);
        List<Double> blah = new ArrayList<>();
        blah.add(fitness);
        return new TestResult(clustering, fitness, blah);
    }

    abstract TestResult doTest(int numFeatures, int numClusters, List<Instance> instances, ClusterFitnessFunction fitnessFunction);

    abstract String humanFriendly();
}
