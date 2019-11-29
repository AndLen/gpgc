package other;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Created by lensenandr on 17/03/16.
 */
public class BetterProperties extends Properties {

    public int getInt(String key) {
        return Integer.parseInt(getProperty(key));
    }

    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(getProperty(key));
    }

    public void build(String[] args) throws IOException {
        Path path = Paths.get(args[0]);
        BufferedReader bufferedReader = Files.newBufferedReader(path);
        this.putAll(internalLoad(bufferedReader, path.getParent()));
        for (int i = 1; i < args.length; i++) {
            String[] split = args[i].split("=");
            if (split.length == 2) {
                put(split[0].trim(), split[1].trim());
            }
        }
    }

    private BetterProperties internalLoad(BufferedReader reader, Path directory) throws IOException {
        BetterProperties properties = new BetterProperties();
        properties.load(reader);
        if (properties.containsKey("parent")) {
            //load parent first
            Path parentFile = directory.resolve(properties.getProperty("parent"));
            BetterProperties parentProperties = internalLoad(Files.newBufferedReader(parentFile), parentFile.getParent());
            //Overwrite parent with our child properties
            parentProperties.putAll(properties);
            return parentProperties;
        } else {
            return properties;
        }
    }

    public double getDouble(String key) {
        return Double.parseDouble(getProperty(key));
    }
}
