import knn.KNN;

import java.util.ArrayList;
import java.util.List;

public class Main {

    /**
     * Sample main function
     * @param args no use at all
     */
    public static void main(String[] args) {
        List<String> libs = new ArrayList<>();
        libs.add("lib/geometry.jar");

        JarParser.parseJar(libs);
        KNN knn = new KNN(JarParser.getParsedMethods().keySet());

        libs = new ArrayList<>();
        libs.add("lib/rt.jar");
        JarParser.parseJar(libs);
    }

}
