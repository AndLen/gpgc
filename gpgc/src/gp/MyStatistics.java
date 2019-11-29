package gp;

import clusterFitness.F1Measure;
import clustering.Cluster;
import ec.EvolutionState;
import ec.Individual;
import ec.Statistics;
import ec.gp.GPIndividual;
import other.PerformanceEvaluation;
import other.Util;

import java.util.List;
import java.util.Random;

/**
 * Created by lensenandr on 8/07/16.
 */
public class MyStatistics extends Statistics {
    public void postEvaluationStatistics(final EvolutionState state) {
        super.postEvaluationStatistics(state);

        // for now we just print the best fitness per subpopulation.
        Individual individual;  // quiets compiler complaints
        individual = state.population.subpops[0].individuals[0];
        for (int y = 1; y < state.population.subpops[0].individuals.length; y++) {
            if (state.population.subpops[0].individuals[y] != null) {
                if (individual == null || state.population.subpops[0].individuals[y].fitness.betterThan(individual.fitness)) {
                    individual = state.population.subpops[0].individuals[y];
                }
            }

        }


//        double fitness = individual.fitness.fitness();
        //      GPUtils.fitnesses.add(fitness);
        List<? extends Cluster> clusters = ((ClusteringProblem) state.evaluator.p_problem).getClusters(state, (GPIndividual) individual, 0, new Random(GPUtils.getSeed(individual)));
        double fMeasure = PerformanceEvaluation.fMeasure(clusters, GPUtils.instances);
        Util.LOG.printf("F-Measure: %.3f K: %d\n", fMeasure, clusters.size());
        double f1Measure = new F1Measure().calculateQuality(clusters, GPUtils.instances);
        Util.LOG.printf("F1M SS: %.3f K: %d\n", f1Measure, clusters.size());

        Util.LOG.printf(Util.printUserFriendly(clusters, individual.fitness.fitness(), GPUtils.instances));
    }
}
