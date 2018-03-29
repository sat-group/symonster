import knn.KNN;
import parser.JarParser;
import parser.LibraryJarParser;

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
public class TrainedDataCSVGenerator {

    private static KNN completeKnn; // kNN for all jars
    private static KNN dummyKnn; // dummy kNN that changes for every different jar

    /**
     * Sample main function
     *
     * @param args no use at all
     */
    public static void main(String[] args) throws FileNotFoundException {
        // Parse lib
        LibraryJarParser.init(DataSource.generateLib(), DataSource.targetPackages());

        // Initialize
        completeKnn = new KNN(LibraryJarParser.getLabelSet());
        dummyKnn = new KNN(LibraryJarParser.getLabelSet());
        List<String> trainData = DataSource.generateTrain();
        PrintWriter pw = new PrintWriter(new File("src/resources/data.csv"));

        // Header
        pw.write("name,parsed,rows,average\n");

        // For each jar
        for (String s : trainData) {
            JarParser.parseJar(Collections.singletonList(s), DataSource.targetPackages());
            pw.write(s);
            if (JarParser.getMethodToAppearancesMap().size() != 0) {

                // Train dummy kNN
                trainVarIndependent(trainData, dummyKnn);
                String resultString = dummyKnn.getTrainAnalysisInfoString();
                pw.write(",true,");
                pw.write(resultString);

                // Reload dummy
                dummyKnn = new KNN(LibraryJarParser.getLabelSet());

                // Train actual kNN
                trainVarIndependent(trainData, completeKnn);

            } else {

                // No methods, record as not parsable
                pw.write(",false\n");
            }
        }
        pw.write("final result:");
        pw.write(completeKnn.getTrainDenseString());
        pw.close();
    }

    // Adds var dependent traning data from parser.JarParser
    private static void trainVarDependent(List<String> libs, KNN knn) {
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

    // Adds var independent traning data from parser.JarParser
    private static void trainVarIndependent(List<String> libs, KNN knn) {
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
