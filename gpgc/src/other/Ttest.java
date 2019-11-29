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
public class Ttest {
    private static final String[] UP = new String[]{"+", "\\uparrow"};
    private static final String[] DOWN = new String[]{"-", "\\downarrow"};

    public static Pattern pattern = Pattern.compile("(.*?):");
    public static String[] BASELINES = new String[]{"KMeans", "FF2-SSCI-Scale"};

    public static void main(String args[]) throws IOException {
        String fileName = args[0];
        List<String> strings = Files.readAllLines(Paths.get(fileName));
        String[] names = null;
        Map<String, Result> results = new HashMap<>();
        Result result = null;
        for (int i = 0; i < strings.size(); i++) {
            String string = strings.get(i);
            if (string.trim().equals("Method & Fitness & #Features & #Clusters\\\\")) {
                i++;
                names = strings.get(i).split("&");
                result.addDetails(strings.get(i));
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
                        default:
                            if (result != null) {
                                results.put(result.name, result);
                            }
                            result = new Result(group);
                            break;
                    }
                }
            }
        }
        boolean[] maximise = new boolean[]{true, false, true, true};
        results.put(result.name, result);
        //System.out.println(results);
        Result[] baselines = new Result[BASELINES.length];
        for (int i = 0; i < BASELINES.length; i++) {
            baselines[i] = results.get(BASELINES[i]);
        }

        ArrayList<String> list = new ArrayList<>();
        Collections.addAll(list, "SMGD", "SMGDE", "3Stage", "KMeans", "sil4KMeans", "FF2-SSCI-Scale");
        //Collections.sort(list);
        for (String s : list) {
            if (!s.equals("KMeans") && !s.equals("FF2-SSCI-Scale") && results.get(s).means != null) {
                Result thisResult = results.get(s);
                double[] means = thisResult.means;
                System.out.printf("%s & %.2f & %.2f", s, thisResult.features, thisResult.clusters);

                for (int i = 0; i < means.length; i++) {
                    if (validName(names[i])) {
                        System.out.print(" & ");

                        System.out.print("$" + means[i]);
                        for (int j = 0; j < baselines.length; j++) {
                            Result baseline = baselines[j];
                            ttest(means[i], baseline.means[i], thisResult.stdDevs[i], baseline.stdDevs[i], UP[j], DOWN[j], maximise[i]);

                        }
                        System.out.print("$");

                    }
                }
                System.out.println("\\\\");

            }
        }
        for (int i = 0; i < BASELINES.length; i++) {
            String s = BASELINES[i];
            System.out.printf("%s & %.2f & %.2f", s, baselines[i].features, baselines[i].clusters);

            double[] means = baselines[i].means;
            for (int j = 0; j < means.length; j++) {
                double mean = means[j];
                if (validName(names[j])) {

                    System.out.print(" & $" + mean + "$");
                }
            }
            System.out.println("\\\\");

        }


    }

    private static void ttest(double mean, double baselineMean, double stdDev, double baselineStdDev, String better, String worse, boolean maximise) throws IOException {
        String format = String.format("/usr/pkg/bin/Rscript /home/lensenandr/masters/ttest.R %f %f %f %f 30 30", baselineMean, mean, baselineStdDev, stdDev);
        InputStream stream = Runtime.getRuntime().exec(format).getInputStream();
        String output = new BufferedReader(new InputStreamReader(stream))
                .lines().collect(Collectors.joining("\n"));
        String[] tests = output.split(",");

        if (tests[0].trim().equalsIgnoreCase("true")) {
            if (tests[1].trim().equalsIgnoreCase("true") == maximise) {
                System.out.print("^{" + better + "}");
            } else {
                System.out.print("^{" + worse + "}");
            }
        }
    }

    private static boolean validName(String name) {
        //System.out.println(name);
        name = name.trim();
        return name.startsWith("Scat") || name.startsWith("Total Intra") || name.startsWith("Class") || name.startsWith("F-");
    }

    public static class Result {

        private final String name;
        private double[] means;
        private double[] stdDevs;
        private double features;
        private double clusters;

        public Result(String name) {

            this.name = name;
        }

        public void addMeans(String means) {
            this.means = getVals(means);
        }

        double[] getVals(String means) {
            String[] split = means.split(",");
            double[] vals = new double[split.length - 1];
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
    }
}
