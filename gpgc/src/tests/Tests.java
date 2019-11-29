package tests;

/**
 * Created by lensenandr on 4/04/16.
 */

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import other.Main;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class Tests {
    public static Class<?> main = Main.class;
    private void runTestWithConfig(String configFile) throws IOException {
        String configPath = Paths.get(System.getProperty("user.dir"), "/src/tests/config", configFile).toString();
        runTestWithArgs(configPath);

    }

    private void runTestWithArgs(String... supplied) {
        ArrayList<String> args = new ArrayList<>();
        Collections.addAll(args, supplied);
        args.addAll(getTestConfig());
        args.addAll(Arrays.asList(SingleJUnitTestRunner.PARAMS));
        System.out.println(args);
        try {
            Method main = Tests.main.getMethod("main", String[].class);
            main.invoke(null, (Object) args.toArray(new String[args.size()]));
        } catch (NoSuchMethodException e) {
            throw new Error(e);
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        for (long id : threadMXBean.getAllThreadIds()) {
            long threadCpuTime = threadMXBean.getThreadCpuTime(id);
            System.out.println(threadCpuTime);
        }
    }

    private void runTestWithDataset(String datasetFile) throws IOException {
        String configPath = Paths.get(System.getProperty("user.dir"), "/src/tests/tests.config").toString();
        runTestWithArgs(configPath, datasetFile);

    }

    public List<String> getTestConfig() {
        return new ArrayList<>(Arrays.asList("featureSubsetForFitness=false", "preprocessing=scale"));
    }

    @Test
    public void a_irisTest() throws IOException {
        runTestWithConfig("iris.config");
    }

    @Test
    public void b_wineTest() throws IOException {
        runTestWithConfig("wine.config");
    }


    @Test
    public void c_moveLibrasTest() throws IOException {
        runTestWithConfig("movement_libras.config");
    }


    @Test
    public void d_dermatologyTest() throws IOException {
        runTestWithConfig("dermatology.config");
    }

    @Test
    public void e_breastCancerTest() throws IOException {
        runTestWithConfig("breast-cancer-wisconsin.config");
    }


    @Test
    public void f_imageSegmentationTest() throws IOException {
        runTestWithConfig("image-segmentation.config");
    }

    @Test
    public void nhs_spiralTest() throws IOException {
        runTestWithConfig("spiral.config");
    }

    @Test
    public void nhs_aggregationTest() throws IOException {
        runTestWithConfig("aggregation.config");
    }

    @Test
    public void nhs_compoundTest() throws IOException {
        runTestWithConfig("compound.config");
    }

    @Test
    public void nhs_d31Test() throws IOException {
        runTestWithConfig("d31.config");
    }

    @Test
    public void nhs_flameTest() throws IOException {
        runTestWithConfig("flame.config");
    }

    @Test
    public void nhs_jainTest() throws IOException {
        runTestWithConfig("jain.config");
    }

    @Test
    public void nhs_pathbasedTest() throws IOException {
        runTestWithConfig("pathbased.config");
    }

    @Test
    public void nhs_r15Test() throws IOException {
        runTestWithConfig("r15.config");
    }

    @Test
    public void z_10d10ctest() throws IOException {
        runTestWithConfig("10d10c.config");
    }

    @Test
    public void z_10d20ctest() throws IOException {
        runTestWithConfig("10d20c.config");
    }

    @Test
    public void z_10d40ctest() throws IOException {
        runTestWithConfig("10d40c.config");
    }

    @Test
    public void z_50d10ctest() throws IOException {
        runTestWithConfig("ellipsoid.50d10c.config");
    }

    @Test
    public void z_50d20ctest() throws IOException {
        runTestWithConfig("ellipsoid.50d20c.config");
    }

    @Test
    public void z_50d40ctest() throws IOException {
        runTestWithConfig("ellipsoid.50d40c.config");
    }

    @Test
    public void z_100d10ctest() throws IOException {
        runTestWithConfig("ellipsoid.100d10c.config");
    }

    @Test
    public void z_100d20ctest() throws IOException {
        runTestWithConfig("ellipsoid.100d20c.config");
    }

    @Test
    public void z_100d40ctest() throws IOException {
        runTestWithConfig("ellipsoid.100d40c.config");
    }

    @Test
    public void z2_10d10ctest() throws IOException {
        runTestWithConfig("10d10cE.config");
    }

    @Test
    public void z2_10d20ctest() throws IOException {
        runTestWithConfig("10d20cE.config");
    }

    @Test
    public void z2_10d40ctest() throws IOException {
        runTestWithConfig("10d40cE.config");
    }

    @Test
    public void z2_10d100ctest() throws IOException {
        runTestWithConfig("10d100cE.config");
    }

    @Test
    public void z2_10d1000ctest() throws IOException {
        runTestWithConfig("10d1000cE.config");
    }

    @Test
    public void z2_1000d10ctest() throws IOException {
        runTestWithConfig("1000d10c.config");
    }

    @Test
    public void z2_1000d20ctest() throws IOException {
        runTestWithConfig("1000d20c.config");
    }

    @Test
    public void z2_1000d40ctest() throws IOException {
        runTestWithConfig("1000d40c.config");
    }

    @Test
    public void z2_1000d100ctest() throws IOException {
        runTestWithConfig("1000d100c.config");
    }

    @Test
    public void z3_1000d10cGaussiantest() throws IOException {
        runTestWithConfig("1000d10cGaussian.config");
    }


    @Test
    public void z3_1000d100cGaussiantest() throws IOException {
        runTestWithConfig("1000d100cGaussian.config");
    }

    @Test
    public void sparse_50d10ctest() throws IOException {
        runTestWithConfig("ellipsoid.50d10cSparse.config");
    }


    @Test
    public void sparse_100d10ctest() throws IOException {
        runTestWithConfig("ellipsoid.100d10cSparse.config");
    }


    @Test
    public void sparse_1000d10ctest() throws IOException {
        runTestWithConfig("1000d10cSparse.config");
    }

    @Test
    public void sparse_1000d20ctest() throws IOException {
        runTestWithConfig("1000d20cSparse.config");
    }

    @Test
    public void sparse_1000d40ctest() throws IOException {
        runTestWithConfig("1000d40cSparse.config");
    }

    @Test
    public void sparse_1000d100ctest() throws IOException {
        runTestWithConfig("1000d100cSparse.config");
    }

    @Test
    public void subspace_one() throws IOException {
        runTestWithDataset("dataset=subspace/oneNoise.subspace");
    }

    @Test
    public void subspace_big() throws IOException {
        runTestWithDataset("dataset=subspace/big.subspace");
    }

    @Test
    public void os5D_test() throws IOException {
        runTestWithDataset("dataset=openSubspace/synth_dimscale/D05.ssAndrew");
    }

    @Test
    public void os10D_test() throws IOException {
        runTestWithDataset("dataset=openSubspace/synth_dimscale/D10.ssAndrew");
    }

    @Test
    public void os15D_test() throws IOException {
        runTestWithDataset("dataset=openSubspace/synth_dimscale/D15.ssAndrew");
    }

    @Test
    public void os20D_test() throws IOException {
        runTestWithDataset("dataset=openSubspace/synth_dimscale/D20.ssAndrew");
    }

    @Test
    public void os25D_test() throws IOException {
        runTestWithDataset("dataset=openSubspace/synth_dimscale/D25.ssAndrew");
    }

    @Test
    public void os50D_test() throws IOException {
        runTestWithDataset("dataset=openSubspace/synth_dimscale/D50.ssAndrew");
    }

    @Test
    public void os75D_test() throws IOException {
        runTestWithDataset("dataset=openSubspace/synth_dimscale/D75.ssAndrew");
    }

    @Test
    public void os1500S_test() throws IOException {
        runTestWithDataset("dataset=openSubspace/synth_dbsizescale/S1500.ssAndrew");
    }
    @Test
    public void os2500S_test() throws IOException {
        runTestWithDataset("dataset=openSubspace/synth_dbsizescale/S2500.ssAndrew");
    }

    @Test
    public void os3500S_test() throws IOException {
        runTestWithDataset("dataset=openSubspace/synth_dbsizescale/S3500.ssAndrew");
    }

    @Test
    public void os4500S_test() throws IOException {
        runTestWithDataset("dataset=openSubspace/synth_dbsizescale/S4500.ssAndrew");
    }

    @Test
    public void os5500S_test() throws IOException {
        runTestWithDataset("dataset=openSubspace/synth_dbsizescale/S5500.ssAndrew");
    }
//
//    @Test
//    public void opensubspace_test() throws IOException {
//        runTestWithDataset("dataset=openSubspace/synth_dbsizescale/S1500.ssAndrew");
//    }
//
//    @Test
//    public void opensubspace5D_test() throws IOException {
//        runTestWithDataset("dataset=openSubspace/synth_dimscale/D05.ssAndrew");
//    }

    @Test
    public void featureGroupOne_test() throws IOException {
        runTestWithDataset("dataset=featureGroup/one.fg");
    }

    @Test
    public void featureGroupTOX_test() throws IOException {
        runTestWithDataset("dataset=featureGroup/TOX_171.fg");
    }


    @Test
    public void bioinformaticTest() throws IOException {
        runTestWithDataset("dataset=bioinformaticYeung/4rep_low_noise/syn_sine_2_mult1.andrew.csv");
    }

    @Test
    public void in10d10cTest() throws IOException {
        runTestWithDataset("dataset=featureGroup/10d10c.0.fg");
    }
//
//    @Test
//    public void letterRecognitionTest() throws IOException{
//        runTestWithConfig("letter-recognition.config");
//    }
//

    //
    @Test
    public void mfatTest() throws IOException {
        runTestWithConfig("mfat.config");
    }

    @Test
    public void vehicleTest() throws IOException {
        runTestWithDataset("dataset=vehicle.data");
    }
    //
//    @Test
//    public void arceneTest() throws IOException {
//        Main.main(new String[]{"../tests/arcene_train.config"});
//    }

//    @Test
//    public void taishoTest() throws IOException {
//        runTestWithConfig("taisho.config");
//    }

}
