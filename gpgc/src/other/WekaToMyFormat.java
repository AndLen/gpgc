package other;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Takes a file in my "format" and converts it to a CSV with normalised feature values -- suitable for analysis by R/Weka/etc. Maybe shouldn't ve normalised?
 */
public class WekaToMyFormat {

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
            if (path.toString().toLowerCase().endsWith(".arff")) readFile(path);
        }
    }

    static void readFile(Path path) throws IOException {

        List<String> lines = Files.readAllLines(path);
        List<String> finalLines = new ArrayList<>();
        Set<String> classLabels = new HashSet<>();
        for (String line : lines) {
            if (Character.isDigit(line.charAt(0))) {
                if (!line.endsWith("-1")) {
                    //Noise line.
                    finalLines.add(line);
                    classLabels.add(line.substring(line.lastIndexOf(',') + 1));

                }
            }
        }
        int numFeatures = (int) finalLines.get(0).chars().filter(ch -> ch == ',').count();

        finalLines.add(0, "classLast," + numFeatures + "," + classLabels.size() + "," + "comma");

        String fileName = path.getFileName().toString();
        Files.write(Paths.get(path.getParent().toString(), fileName.replace(fileName.substring(fileName.lastIndexOf(".")), ".ssAndrew")), finalLines);
    }
}
