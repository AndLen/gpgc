package other;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by lensenandr on 19/09/16.
 */
public class TtestReverse {
    //The things we're comparing stat tests to
    public static final String[] BASELINES //= new String[]{"KMeans", "OPTICS", "PSO", "NaiveGraph", "MCL", "MOCK"};
            //       = new String[]{"NaiveGraph-2NN", "NaiveGraph-3NN", "KMeans", "OPTICS"};
            = new String[]{"GP"};

    private static final String[] UP = new String[]{"+", "\\uparrow"};
    private static final String[] DOWN = new String[]{"-", "\\downarrow"};
    private static final String[] VALID_MEASURE_PREFIXES = new String[]{"Conn.", "Spar.", "Sep.", "FM", "ARI"};
    private static final boolean TEST_FITNESS = true;
    private static final boolean COMMENT_OUT_FM = true;
    private static final boolean PRINT_DOLLARS = false;
    public static Pattern pattern = Pattern.compile("(.*?):");
    private static boolean[] maximise = new boolean[]{true, false, true, true, true};

    public static void main(String args[]) throws IOException {
        String fileName = args[0];
        List<String> strings = Files.readAllLines(Paths.get(fileName));
        String[] names = null;
        Map<String, Result> results = new TreeMap<>();
        Result result = null;
        for (int i = 0; i < strings.size(); i++) {
            String string = strings.get(i);
            if (string.trim().equals("Method & Fitness & #Features & #Clusters\\\\")) {
                i++;
                names = strings.get(i).split("&");

                result = new Result(names[0]);
                results.put(names[0], result);
                result.addDetails(strings.get(i));
            } else if (string.trim().equals("method, fitness, stdDev")) {
                i++;
                result.addFitness(strings.get(i).split(","));
            } else {
                Matcher matcher = pattern.matcher(string);
                if (matcher.matches()) {
                    String group = matcher.group(1);
                    //         System.out.println(group);
                    switch (group) {
                        case "Means":
                            i++;
                            names = strings.get(i).split(",");
                            i++;
                            result.addMeans(strings.get(i));
                            break;
                        case "Std Devs":
                            i += 2;
                            result.addStdDevs(strings.get(i));
                            break;
                        case "Latex":
                            break;
                    }
                }
            }
        }
        results.put(result.name, result);
        //      System.out.println(results);
        Map<String, Result> baselines = new TreeMap<>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                if (o1.startsWith("OPTICS--") && o2.startsWith("OPTICS--")) {
                    return Double.compare(Double.parseDouble(o1.substring(o1.indexOf("0"))), Double.parseDouble(o2.substring(o2.indexOf("0"))));
                } else {
                    return o1.compareTo(o2);
                }
            }
        });


        Map<String, Result> methods = new TreeMap<>();
        results.forEach((name, val) -> {
            boolean baseline = false;
            for (int i = 0; i < BASELINES.length; i++) {
                if (name.startsWith(BASELINES[i])) {
                    baseline = true;
                    baselines.put(name, val);
                    break;
                }
            }
            if (!baseline) methods.put(name, val);
        });


        int ariIndex = -1;
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            if (name.trim().equalsIgnoreCase("ARI")) {
                ariIndex = i;
            }
        }

        //find the best OPTICS only.
        String bestOptics = "";
        double bestFM = -1;
        for (String s : baselines.keySet()) {
            if (s.startsWith("OPTICS--")) {

                Result optics = baselines.get(s);
                //First index is the method name
                double ari = optics.means[ariIndex - 1];//optics.means.length - 1];
                if (ari > bestFM) {
                    bestFM = ari;
                    bestOptics = s;
                }
                //         optics.means[]
            }
        }
        if (bestFM > -1) {
            for (Iterator<String> iterator = baselines.keySet().iterator(); iterator.hasNext(); ) {
                String s = iterator.next();
                if (s.startsWith("OPTICS--")) {
                    if (!s.equals(bestOptics)) {
                        iterator.remove();
                    }
                }
            }
        }


        for (Map.Entry<String, Result> entry : methods.entrySet()) {
            Result method = entry.getValue();
            String s = entry.getKey();
            System.out.printf("%s & ", s);

            if (TEST_FITNESS) {
                System.out.printf("%.2f & ", method.fitness);

            }
            System.out.printf("%.2f", method.clusters);


            //       System.out.printf("%s & %.2f", s, method.clusters);

//            System.out.printf("%s & %.2f & %.2f", s, method.features, method.clusters);

            double[] means = method.means;
            for (int j = 0; j < means.length; j++) {
                double mean = means[j];
                if (validName(names[j])) {

                    if (COMMENT_OUT_FM && j == means.length - 1) {
                        System.out.print("\\\\%");
                    }
                    System.out.print(PRINT_DOLLARS ? " & $" : " &" + mean + (PRINT_DOLLARS ? "$" : ""));
                }
            }
            System.out.println("\\\\");
        }

        final String[] finalNames = names;
        baselines.forEach((s, thisBaseline) -> {
            double[] means = thisBaseline.means;
            System.out.printf("%s", s);


            //System.out.printf("%s & %.2f & %.2f", s, thisBaseline.features, thisBaseline.clusters);

            //t-test fitness
            if (TEST_FITNESS) {
                System.out.print(" & ");

                System.out.print(PRINT_DOLLARS ? "$" : "" + thisBaseline.fitness);
                int j = 0;
                for (Map.Entry<String, Result> entry : methods.entrySet()) {
                    Result methodResult = entry.getValue();
                    try {
                        ttest(methodResult.fitness, thisBaseline.fitness, methodResult.fitnessStdDev, thisBaseline.fitnessStdDev, UP[j], DOWN[j], false);
                    } catch (IOException e) {
                        System.exit(1);
                    }

                    j++;
                }
                if (PRINT_DOLLARS) System.out.print("$");
            }
            System.out.printf("& %.2f", thisBaseline.clusters);

            for (int i = 0; i < means.length; i++) {
                if (validName(finalNames[i])) {

                    if (COMMENT_OUT_FM && i == means.length - 1) {
                        System.out.print("\\\\%");
                    }
                    System.out.print(" & ");

                    double baselineMean = means[i];
                    double baselineStdDev = thisBaseline.stdDevs[i];
                    System.out.print(PRINT_DOLLARS ? "$" : "" + baselineMean);

                    int j = 0;
                    for (Map.Entry<String, Result> entry : methods.entrySet()) {
                        Result methodResult = entry.getValue();
                        try {
                            ttest(methodResult.means[i], baselineMean, methodResult.stdDevs[i], baselineStdDev, UP[j], DOWN[j], !maximise[i]);
                        } catch (IOException e) {
                            System.exit(1);
                        }

                        j++;
                    }
                    if (PRINT_DOLLARS) System.out.print("$");

                }
            }
            System.out.println("\\\\");

        });


    }

    private static void ttest(double mean, double baselineMean, double stdDev, double baselineStdDev, String better, String worse, boolean maximise) throws IOException {
        String format = String.format("/usr/pkg/bin/Rscript /home/lensenandr/masters/ttest.R %f %f %f %f 30 30", baselineMean, mean, baselineStdDev, stdDev);
        //System.out.println(format);
        InputStream stream = Runtime.getRuntime().exec(format).getInputStream();
        String output = new BufferedReader(new InputStreamReader(stream))
                .lines().collect(Collectors.joining("\n"));
        //System.out.println(output);
        String[] tests = output.split(",");

        if (tests[0].trim().equalsIgnoreCase("true")) {
            if (tests[1].trim().equalsIgnoreCase("true") == maximise) {
                System.out.print("\\textsuperscript{$" + better + "$}");
            } else {
                System.out.print("\\textsuperscript{$" + worse + "$}");
            }
        }
    }

    private static boolean validName(String name) {
        //System.out.println(name);
        name = name.trim();
        for (String prefix : VALID_MEASURE_PREFIXES) {
            if (name.startsWith(prefix)) return true;
        }
        return false;

    }

    public static class Result {

        private final String name;
        private double[] means;
        private double[] stdDevs;
        private double features;
        private double clusters;
        private double fitness;
        private double fitnessStdDev;

        public Result(String name) {

            this.name = name;
        }

        public void addMeans(String means) {
            this.means = getVals(means);
        }

        double[] getVals(String means) {
            String[] split = means.split(",");
            double[] vals = new double[split.length - 1];
            //First val is the name
            for (int i = 1; i < split.length; i++) {
                String s = split[i].trim();
                vals[i - 1] = Double.valueOf(s);
            }
            return vals;
        }

        public void addStdDevs(String stdDevs) {
            this.stdDevs = getVals(stdDevs);
        }

        @Override
        public String toString() {
            return "Result{" +
                    "name='" + name + '\'' +
                    ", means=" + Arrays.toString(means) +
                    ", stdDevs=" + Arrays.toString(stdDevs) +
                    '}';
        }

        public void addDetails(String details) {
            String[] split = details.split("&");
            this.features = Double.valueOf(split[2].trim());
            this.clusters = Double.valueOf(split[3].trim().replace("\\", ""));

        }

        public void addFitness(String[] split) {
            this.fitness = Double.valueOf(split[1].trim());
            this.fitnessStdDev = Double.valueOf(split[2].trim());

        }
    }
}
