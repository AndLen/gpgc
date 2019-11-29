package tests;

/**
 * Created by lensenandr on 21/04/16.
 */

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;

import java.util.Arrays;

public class SingleJUnitTestRunner {
    public static String PARAMS[] = new String[]{};

    public static void main(String... args) throws ClassNotFoundException {
        System.out.println(Arrays.toString(args));
        String[] classAndMethod = args[0].split("#");
        System.out.println(Arrays.toString(classAndMethod));
        Request request = Request.method(Class.forName(classAndMethod[0]),
                classAndMethod[1]);
        if (args.length > 1) {
            PARAMS = Arrays.copyOfRange(args, 1, args.length);
        }
        System.out.println(Arrays.toString(PARAMS));
        Result result = new JUnitCore().run(request);
        if (result.getFailureCount() > 0) {
            System.out.println(result.getFailures().get(0).getTrace());
        }
        //System.exit(result.wasSuccessful() ? 0 : 1);
    }
}