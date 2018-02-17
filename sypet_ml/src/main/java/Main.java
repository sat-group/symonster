import knn.KNN;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Main {

    /**
     * Sample main function
     * @param args no use at all
     */
    public static void main(String[] args) {
        List<String> libs = new ArrayList<>();

        // add labels from library
        libs.add("lib/geometry.jar");
        JarParser.parseJar(libs, true);
        KNN knn = new KNN(JarParser.getLabelMap());

        // add trainig data
        /* libs.add("some.jar");
        JarParser.parseJar(libs, false);
        Map<String, Set<String>> data = JarParser.getMethodToAppearancesMap();
        for(Set<String> set : data.values()){
            knn.addTrainVector(set);
        }
        */
    }

}
