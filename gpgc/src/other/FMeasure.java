package other;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by lensenandr on 19/09/16.
 */
public class FMeasure {
    public static Pattern pattern = Pattern.compile("Performance measures:.*F-Measure \\^: (.*?)");

    public static void main(String args[]) throws IOException {
        System.out.println("numTrees, r1,r2,r3,r4,r5,r6,r7,r8,r9,r10,r11,12,r13,r14,r15,r16,r17,r18,r19,r20,r21,r22,r23,r24,r25,r26,r27,r28,r29,r30");
        String dir = args[0];
        explore(Paths.get(dir));


    }

    static void explore(Path dir) throws IOException {
        Files.newDirectoryStream(dir).forEach(p -> {
            if(Files.isDirectory(p)){
                try {
                    explore(p);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                String s = p.toString();
                if(s.contains(".out") && s.contains("100d10c") && s.contains("Conn")){
                   // System.out.println(p);

                    List<String> strings = null;
                    try {
                        strings = Files.readAllLines(p);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Path parent = p.getParent();
                    String method = parent.getName(parent.getNameCount() - 2).toString();
                    StringBuilder sb = new StringBuilder(method).append(", ");
                    for (int i = 0; i < strings.size(); i++) {
                        String string = strings.get(i);
                        Matcher matcher = pattern.matcher(string);
                        if (matcher.matches()) {
                            String group = matcher.group(1);
                            //         System.out.println(group);
                            sb.append(group);
                        }

                    }
                    sb.delete(sb.length() - 2, sb.length());
                    System.out.println(sb.toString());
                }
            }
        });
    }

}
