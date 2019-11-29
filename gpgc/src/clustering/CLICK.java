package clustering;

import data.Instance;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lensenandr on 21/02/18.
 */
public class CLICK {
    public static List<CentroidCluster> doCLICK(List<Instance> instances) throws IOException {

        Path path = Paths.get("/home/lensenandr/clickLinux/clickInput.orig");
        List<String> lines = new ArrayList<>();
        lines.add(instances.size() + " " + instances.get(0).numFeatures());
        for (Instance instance : instances) {
            StringBuilder sb = new StringBuilder();
            sb.append(instance.instanceID);
            for (double featureValue : instance.featureValues) {
                sb.append("\t").append(featureValue);
            }
            lines.add(sb.toString());

        }
        Files.write(path, lines);

        String paramsFileName = "/home/lensenandr/clickLinux/params.txt";
        File e = new File(paramsFileName);
        FileWriter out = new FileWriter(e);
        out.write("DATA_TYPE\n");
        out.write("FP \n");
        out.write("INPUT_FILES_PREFIX\n/home/lensenandr/clickLinux/clickInput \n");
        out.write("OUTPUT_FILE_PREFIX\n/home/lensenandr/clickLinux/clickOutput \n");
        out.write("SIMILARITY_TYPE\nCORRELATION \n");
        out.flush();
        out.close();


        return null;

    }
}
