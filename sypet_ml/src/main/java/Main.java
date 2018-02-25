package main.java;
import main.java.knn.KNN;

import java.util.*;

public class Main {

    /**
     * Sample main function
     * @param args no use at all
     */
    public static void main(String[] args) {
        List<String> libs = new ArrayList<>();

        // add labels from library
        libs.add("lib/rt.jar");
        JarParser.parseJar(libs, true);
        KNN knn = new KNN(JarParser.getLabelMap());

        // add training data (var independent)
        /* libs.add("train.jar");
        JarParser.parseJar(libs, false);
        Map<String, Set<String>> data = JarParser.getMethodToAppearancesMap();
        for(Set<String> set : data.values()){
            knn.addTrainVector(set);
        }
        */

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

}
