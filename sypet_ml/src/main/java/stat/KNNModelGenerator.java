package stat;

import stat.common.DataSource;
import stat.parser.JarParser;
import stat.parser.LibraryJarParser;
import stat.knn.KNN;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Takes in a library of jars, use Soot to parse their methods and generate kNN row vectors as well as
 * analytical information for each jar.
 * The data is stored in data.csv under resources folder.
 * Format:
 * name,parsed,rows,average
 * METHOD_NAME, true/false, rows, average one for rows
 * ...
 * METHOD_NAME, true/false, rows, average one for rows
 * final result: <0/1,0/1, ... , 0/1>
 * ...
 */
public class KNNModelGenerator {

    private static KNN completeKnn; // kNN for all jars
    private static KNN dummyKnn; // dummy kNN that changes for every different jar

    /**
     * Sample main function
     *
     * @param args no use at all
     */
    public static void main(String[] args) throws FileNotFoundException {

        // package of concern, corpus location, output data csv file name, flag for dependent or not, library jar location(rt if not specified)
        //args = new String[]{"org.jsoup", "lib/javax.xml/", "jsoup", 0,"../sypet_ml/lib/jsoup-1.8.3.jar"};
        //args = new String[]{"org.jsoup", "lib/javax.xml/", 0, "jsoup"};

        int dependence = Integer.parseInt(args[3]);
        // Parse lib
        System.out.println("=== get lib ====");
        if(args.length > 4) {
            LibraryJarParser.init(DataSource.generateLib(args[4]), DataSource.generateCustomLib(args[0]));
        }else{
            LibraryJarParser.init(DataSource.generateLib(""), DataSource.generateCustomLib(args[0]));
        }
        System.out.println("=== get lib done ====");

        // Initialize
        completeKnn = new KNN(LibraryJarParser.getLabelSet());
        dummyKnn = new KNN(LibraryJarParser.getLabelSet());
        List<String> trainData = DataSource.generateTrainCustom(args[1]);
        PrintWriter pw = new PrintWriter(new File("src/resources/data_"+args[2]+".csv"));

        // Header
        pw.write("name,parsed,rows,average\n");

        // For each jar
        for (String s : trainData) {
            JarParser.parseJar(Collections.singletonList(s), DataSource.generateCustomLib(args[0]));
            pw.write(s);
            if (JarParser.getMethodToAppearancesMap().size() != 0) {

                // Train dummy kNN
                trainVarIndependent(dummyKnn);
                String resultString = dummyKnn.getTrainAnalysisInfoString();
                pw.write(",true,");
                pw.write(resultString);

                // Reload dummy
                dummyKnn = new KNN(LibraryJarParser.getLabelSet());

                // Train actual kNN
                if(dependence > 0){
                    trainVarDependent(completeKnn);
                }else {
                    trainVarIndependent(completeKnn);
                }

            } else {

                // No methods, record as not parsable
                pw.write(",false\n");
            }
        }
        pw.write("final result:");
        pw.write(completeKnn.getTrainDenseString());
        pw.close();
    }

    // Adds var dependent traning data from JarParser
    private static void trainVarDependent(KNN knn) {
        Map<String, Map<String, Set<String>>> varData = JarParser.getMethodToVarAppearancesMap();
        Map<String, LinkedHashSet<String>> data = JarParser.getMethodToAppearancesMap();
        for (Map<String, Set<String>> s : varData.values()) {
            for (Set<String> t : s.values()) {
                knn.addTrainVector(t);
            }
        }
        for (Set<String> t : data.values()) {
            knn.addTrainVector(t);
        }
    }

    // Adds var independent traning data from JarParser
    private static void trainVarIndependent(KNN knn) {
        Map<String, LinkedHashSet<String>> data = JarParser.getMethodToAppearancesMap();

        for (Set<String> set : data.values()) {
            knn.addTrainVector(set);
        }
    }

    // Prints train result. Prints dense if dense is set to true; prints sparse if sparse is set to true.
    private static void printTrainResult(boolean dense, boolean sparse) {
        System.out.println("===== Var Dependent Training Set Matrix =====");
        if (dense) {
            System.out.println("===== Dense =====");
            completeKnn.showTrainSetDense();
        }
        if (sparse) {
            System.out.println("===== Sparse =====");
            completeKnn.showTrainSetSparse();
        }
    }



}
