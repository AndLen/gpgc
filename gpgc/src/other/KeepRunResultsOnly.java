package other;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lensenandr on 19/03/18.
 */
public class KeepRunResultsOnly {
    public static void main(String[] args) throws IOException {
        Path path = Paths.get(args[0]);
        traverse(path);
    }

    private static void traverse(Path path) throws IOException {
        System.out.println(path);
        if (Files.isDirectory(path)) {
            for (Path dataPath : Files.newDirectoryStream(path)) {
                traverse(dataPath);
            }
        } else {
            if (path.toString().toLowerCase().endsWith(".out"))
                try {
                    printRunResults(path);
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                    //Try the next file anyway
                }
        }
    }

    private static void printRunResults(Path path) throws IOException {
        List<String> out = new ArrayList<>();
        out.add("==============================");
        List<String> lines = Files.readAllLines(path);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.startsWith("RUN")) {
                i += 4;
                int numBreaks = 0;
                while (numBreaks != 2) {
                    line = lines.get(i);
                    if (line.startsWith("==============================")) {
                        numBreaks++;
                    } else {
                        out.add(line);
                    }
                    i++;
                }
                out.add("==============================");
            }
        }
        String outPath = path.toString().replace(".out", ".outRR");
        Files.write(Paths.get(outPath), out);

    }
}
