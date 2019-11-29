package gp.locum.advanced;

import data.Instance;

/**
 * Created by lensenandr on 10/02/17.
 */
public class CosineDistance extends DistanceTerminal {
    @Override
    public String toString() {
        return "cosineDist";
    }

    @Override
    public double getDistance(Instance instance1, Instance instance2) {
        double[] vectorA = instance1.featureValues;
        double[] vectorB = instance2.featureValues;

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
