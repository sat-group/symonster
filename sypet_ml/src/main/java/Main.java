import knn.KNN;

import java.util.*;

public class Main {

    private static KNN knn;

    /**
     * Sample main function
     * @param args no use at all
     */
    public static void main(String[] args) {
        // Libraries that we want to read from
        ArrayList<String> libs = new ArrayList<>();
        libs.add("../sypet_ml/lib/rt.jar");

        // Packages from those libraries that we are interested on
        // If this list is empty then we consider all packages!
        ArrayList<String> packages = new ArrayList<>();
        packages.add("java.awt.geom");

        JarParserLib.init(libs, packages);

        // add training data (var dependent)
        /*
        libs.add("train.jar");
        JarParser.parseJar(libs, false);
        Map<String, Map<String, Set<String>>> data = JarParser.getMethodToVarAppearancesMap();
        Map<String, Set<String>> set = new HashMap<>();
        for(Map<String, Set<String>> s :  data.values()){
            for(Set<String> t : s.values()){
                knn.addTrainVector(t);
            }
        }*/
    }

    public static void onParseLibComplete(){
        // add labels from library
        knn = new KNN(JarParserLib.getLabelSet());

        // add training data (var independent)
        List<String> libs = new ArrayList<>();
        libs.add("geometry.jar");
        JarParser.parseJar(libs);
        Map<String, Set<String>> data = JarParser.getMethodToAppearancesMap();
        for(Set<String> set : data.values()){
            knn.addTrainVector(set);
        }
    }

}
