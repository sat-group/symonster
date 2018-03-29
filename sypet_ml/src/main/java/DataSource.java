import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Sources for all datas
 */
public class DataSource {
    // Generates a list of training jar file String
    public static ArrayList<String> generateTrain() {
        // read all files from folder
        ArrayList<String> libs = new ArrayList<>();
        File libFolder = new File("lib/corpus/");
        for (final File fileEntry : libFolder.listFiles()) {
            System.out.println(fileEntry.getName());
            libs.add("lib/corpus/" + fileEntry.getName());
        }
        return libs;
    }

    // Generates a list of library jar file String
    public static ArrayList<String> generateLib() {
        ArrayList<String> libs = new ArrayList<>();
        libs.add("../sypet_ml/lib/rt.jar");

        return libs;
    }

    // Generates a list of test jar file String
    public static ArrayList<String> generateTest() {
        ArrayList<String> libs = new ArrayList<>();
        libs.add("../sypet_ml/lib/geometry.jar");
        return libs;
    }

    // Generates a list of target packages
    public static List<String> targetPackages(){
        return Collections.singletonList("java.awt.geom");
    }
}
