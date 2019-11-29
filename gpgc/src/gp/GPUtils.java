package gp;

import clusterFitness.ClusterFitnessFunction;
import clustering.Cluster;
import data.Instance;
import ec.EvolutionState;
import ec.Evolve;
import ec.Individual;
import ec.gp.GPIndividual;
import ec.simple.SimpleFitness;
import ec.simple.SimpleStatistics;
import ec.util.Parameter;
import ec.util.ParameterDatabase;
import other.Main;
import other.TestResult;
import other.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by lensenandr on 5/07/16.
 */
public class GPUtils {
    public static List<Instance> instances;
    public static ClusterFitnessFunction fitnessFunction;
    public static int numClusters;
    public static int numFeatures;
    public static List<Double> fitnesses;

    public static TestResult runGP(int numFeatures, int numClusters, List<Instance> instances, ClusterFitnessFunction fitnessFunction, String paramName) {
        GPUtils.instances = instances;
        GPUtils.fitnessFunction = fitnessFunction;
        GPUtils.numClusters = numClusters;
        GPUtils.numFeatures = numFeatures;
        fitnesses = new ArrayList<>();

        EvolutionState state = getEvolutionState(numClusters, paramName);
        state.run(EvolutionState.C_STARTED_FRESH);
        Individual bestOfRun = ((SimpleStatistics) state.statistics).best_of_run[0];

        List<? extends Cluster> clusters = ((ClusteringProblem) state.evaluator.p_problem).getClusters(state, (GPIndividual) bestOfRun, 0, new Random(GPUtils.getSeed(bestOfRun)));
        SimpleFitness fitness = (SimpleFitness) bestOfRun.fitness;
        fitnesses.add(fitness.fitness());
        Evolve.cleanup(state);
        return new TestResult(clusters, fitness.fitness(), fitnesses);

    }

    public static EvolutionState getEvolutionState(int numClusters, String paramName) {
        ParameterDatabase parameters = Evolve.loadParameterDatabase(new String[]{"-file", paramName});

        boolean sameIndexCrossover = Main.CONFIG.getBoolean("sameIndexCrossover");
        if (sameIndexCrossover) {
            parameters.set(new Parameter("pop.subpop.0.species.pipe.source.0"), "gp.SameIndexCrossoverPipeline");
            Util.LOG.println("Same Index Crossover (SIC)");
        }
        boolean allIndexCrossover = Main.CONFIG.getBoolean("allIndexCrossover");
        if (allIndexCrossover) {
            if (sameIndexCrossover) {
                System.err.println("Both same and all index crossover!!! Defaulting to all!!!");
            }
            parameters.set(new Parameter("pop.subpop.0.species.pipe.source.0"), "gp.AllIndexCrossoverPipeline");
            Util.LOG.println("All Index Crossover (AIC)");

        }
        if (!sameIndexCrossover && !allIndexCrossover) Util.LOG.println("Random Index Crossover (RIC)");

        int numtrees = Main.CONFIG.getInt("numtrees");
        if (numtrees == -1) numtrees = (int) Math.ceil(Util.log(2, numClusters));
        Util.LOG.printf("%d trees\n", numtrees);
        parameters.set(new Parameter("pop.subpop.0.species.ind.numtrees"), Integer.toString(numtrees));
        for (int i = 0; i < numtrees; i++) {
            parameters.set(new Parameter("pop.subpop.0.species.ind.tree." + i), "ec.gp.GPTree");
            parameters.set(new Parameter("pop.subpop.0.species.ind.tree." + i + ".tc"), "tc0");
        }

        if (Main.CONFIG.containsKey("numsubpops")) {
            int numsubpops = Main.CONFIG.getInt("numsubpops");
            Util.LOG.printf("%d subpops\n", numsubpops);
            parameters.set(new Parameter("pop.subpops"), "" + numsubpops);

        }


        if (Main.CONFIG.containsKey("treeDepth")) {
            int treeDepth = Main.CONFIG.getInt("treeDepth");
            Util.LOG.printf("Tree depth: %d\n", treeDepth);

            parameters.set(new Parameter("gp.koza.xover.maxdepth"), "" + treeDepth);
            //         parameters.set(new Parameter("gp.koza.xover.maxdepth"),""+treeDepth);

            //this is important as otherwise xover can make it too big...
            parameters.set(new Parameter("gp.koza.xover.maxsize"), "" + treeDepth);

            parameters.set(new Parameter("gp.koza.grow.max-depth"), "" + treeDepth);
            parameters.set(new Parameter("gp.koza.full.max-depth"), "" + treeDepth);
            parameters.set(new Parameter("gp.koza.half.max-depth"), "" + treeDepth);

        }
        int processors = Runtime.getRuntime().availableProcessors();
        int threads = Math.max(processors / 2, 1);
        Util.LOG.printf("%d processors, using %d threads\n", processors, threads);
        parameters.set(new Parameter("evalthreads"), "" + threads);
        parameters.set(new Parameter("stat.file"), Util.LOG.ECJ_OUT + Main.RUN);
        int seed = ThreadLocalRandom.current().nextInt();
        for (int i = 0; i < threads; i++) {
            parameters.set(new Parameter("seed." + i), Integer.toString(seed));
            seed++;
        }


        return Evolve.initialize(parameters, 0);
    }

    public static int getSeed(Individual individual) {
        return Main.CONFIG.containsKey("clusterSeed") ? Main.CONFIG.getInt("clusterSeed") : individual.hashCode();
    }
}
