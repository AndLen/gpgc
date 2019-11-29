package other;

import data.Instance;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static other.DatasetUtils.*;

/**
 * Takes a file in my "format" and converts it to a CSV with normalised feature values -- suitable for analysis by R/Weka/etc. Maybe shouldn't ve normalised?
 */
public class FileConverter {
    private static final boolean NORMALISE = false;

    public static void main(String[] args) throws IOException {
        Path path = Paths.get(args[0]);
        traverse(path);
    }

    static void traverse(Path path) throws IOException {
        System.out.println(path);
        if (Files.isDirectory(path)) {
            for (Path dataPath : Files.newDirectoryStream(path)) {
                traverse(dataPath);
            }
        } else {
            if (!path.toString().toLowerCase().endsWith(".csv")) readFile(path);
        }
    }

    static void readFile(Path path) throws IOException {

        List<String> lines = Files.readAllLines(path);
        String[] header = lines.get(0).split(",");
        if (header.length != 4) {
            System.err.println("Invalid file: " + path);
            return;
        }
        String classLabelPosition = header[0];
        int numInitialFeatures = Integer.parseInt(header[1]);


        int numClusters = Integer.parseInt(header[2]);
        String splitString = ",";
        if (header[3].equals("space")) splitString = "'\\s+";
        else if (header[3].equals("tab")) splitString = "\t";

        //Remove bad features
        List<Instance> instances = getInstances(lines, classLabelPosition, numInitialFeatures, splitString);
        if (NORMALISE) {
            instances = normaliseInstances(instances);
        } else {
            instances = scaleInstances(instances);
        }
        int numFeatures = instances.get(0).numFeatures();

        List<String> outputLines = new ArrayList<>();
        StringBuilder sbHeader = new StringBuilder("class");
        for (int i = 0; i < numFeatures; i++) {
            sbHeader.append(", f").append(i);
        }
        outputLines.add(sbHeader.toString());
        for (Instance instance : instances) {
            StringBuilder sbInstance = new StringBuilder(instance.getClassLabel());
            for (int i = 0; i < numFeatures; i++) {
                sbInstance.append(", ").append(instance.getFeatureValue(i));
            }
            outputLines.add(sbInstance.toString());
        }
        String fileName = path.getFileName().toString();
        Files.write(Paths.get(path.getParent().toString(), fileName.replace(fileName.substring(fileName.lastIndexOf(".")), NORMALISE ? "-norm.csv" : ".csv")), outputLines);
    }
}
