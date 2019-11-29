package clusterFitness;


import clustering.Cluster;
import data.Instance;

import java.util.*;
import java.util.stream.Collectors;

/**
 * From OpenSubspace, i.e. package weka.clusterquality;
 */
public class F1Measure {


    public double calculateQuality(List<? extends Cluster> clusters, List<Instance> instances) {

        //distinct classes
        List<String> classLabels = instances.stream().map(Instance::getClassLabel).distinct().collect(Collectors.toList());
        int size = classLabels.size();

        Map<String, Set<Instance>> inClasses = new TreeMap<>();
        List<Set<Integer>> outClasses = new ArrayList<>();
        classLabels.forEach(c -> {
            inClasses.put(c, new HashSet<>());
            outClasses.add(new HashSet<>());
        });

        //Objekte den jeweiligen Eingangsklassen zuordnen
        //Assign objects to the respective input classes
        instances.forEach(i -> inClasses.get(i.getClassLabel()).add(i));


        //Die h�ufigkeiten der Klassen in den Ausgabeclustern bestimmen und F_value bzgl. dieser Klasse berechnen.
        //Determine the frequencies of the classes in the output clusters and calculate F_value for this class.
        for (int i = 0; i < clusters.size(); i++) {
            Map<String, Integer> classFrequency = new TreeMap<>();
            classLabels.forEach(c -> classFrequency.put(c, 0));

            List<Integer> thisCluster = new ArrayList<>();
            Cluster cluster = clusters.get(i);

            cluster.getInstancesInCluster().forEach(d -> {
                classFrequency.put(d.getClassLabel(), classFrequency.get(d.getClassLabel()) + 1);
                thisCluster.add(d.instanceID);
            });

            double maxValue = 0;
            int maxIndex = 0;
            for (int k = 0; k < size; k++) {
                // Variante 1
                //if (classFrequency[k]>maxValue) {
                //	maxValue = classFrequency[k];
                //	maxIndex = k;
                //}
                // Variante 2
                int thisFreq = classFrequency.get(classLabels.get(k));
                double val = thisFreq * thisFreq / (double) inClasses.get(classLabels.get(k)).size();
                if (val > maxValue) {
                    maxValue = val;
                    maxIndex = k;
                }
                // Ende Varianten
            }
            outClasses.get(maxIndex).addAll(thisCluster);
        }

        //F-value berechnen pro AusgabeCluster
        // Calculate F-value per output cluster
        double[] m_F1_values = new double[size];
        double[] m_precision = new double[size];
        double[] m_recall = new double[size];
        double m_F1 = 0.0;

        for (int i = 0; i < size; i++) {
            int intersect = 0;
            Set<Instance> thisInClasses = inClasses.get(classLabels.get(i));
            Set<Integer> thisOutClasses = outClasses.get(i);

            for (Instance thisInClass : thisInClasses) {
                if (thisOutClasses.contains(thisInClass.instanceID)) {
                    intersect++;
                }
            }

            m_precision[i] = 0;
            m_recall[i] = 0;
            if ((thisOutClasses.size() == 0) && (thisInClasses.size() == 0)) {
                m_F1_values[i] = 0;
            } else {
                m_F1_values[i] = 2 * (double) intersect / (thisOutClasses.size() + thisInClasses.size());
                if (thisOutClasses.size() != 0) m_precision[i] = (double) intersect / thisOutClasses.size();
                if (thisInClasses.size() != 0) m_recall[i] = (double) intersect / thisInClasses.size();
            }

            m_F1 += m_F1_values[i];

        }
        m_F1 = m_F1 / size;

        return m_F1;

    }


}
