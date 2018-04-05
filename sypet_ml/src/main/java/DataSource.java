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
        File libFolder = new File("../../corpus/");
        for (final File fileEntry : libFolder.listFiles()) {
            System.out.println(fileEntry.getName());
            libs.add("../../corpus/" + fileEntry.getName());
        }
        return libs;
    }

    // Generates a list of library jar file String
    public static ArrayList<String> generateLib(String custom) {
        ArrayList<String> libs = new ArrayList<>();
        libs.add("../sypet_ml/lib/rt.jar");
        if(!custom.equals("")) {
            libs.add(custom);
        }

        return libs;
    }

    // Generates a list of test jar file String
    public static ArrayList<String> generateTest() {
        ArrayList<String> libs = new ArrayList<>();
        libs.add("../sypet_ml/lib/geometry.jar");
        return libs;
    }

    // Generates a list of target packages
    public static List<String> javaAWTGeomPackage(){
        return Collections.singletonList("java.awt.geom");
    }

    public static List<String> javaxSwingTextPackage() { return Collections.singletonList("javax.swing.text");}

    public static List<String> javaxXMLPackage() { return Collections.singletonList("javax.xml");}

    public static List<String> generateCustomLib(String lib){
        return Collections.singletonList(lib);
    }

    // Generates a list of training jar file String
    public static ArrayList<String> generateTrainCustom(String folder) {
        // read all files from folder
        ArrayList<String> libs = new ArrayList<>();
        File libFolder = new File(folder);
        for (final File fileEntry : libFolder.listFiles()) {
            System.out.println(fileEntry.getName());
            libs.add(folder + fileEntry.getName());
        }
        return libs;
    }
}
