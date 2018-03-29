import parser.LibraryJarParser;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Runs kNN on every method for java.awt.geometry class
 */
public class JavaAwtGeometryPrediction {

    public static void main(String[] args) throws IOException {

        // Use Analyzer to generate kNN
        Analyzer.init();

        // Turn label set into linked hash set
        LinkedHashSet<String> labels = new LinkedHashSet<>(LibraryJarParser.getLabelSet());

        // Generate test reports
        List<List<Analyzer.TestReport>> testReports = Analyzer.getTestReports(Collections.singleton(labels), false, false);
        PrintWriter pw = new PrintWriter(new File("src/resources/java_awt_geom_prediction.csv"));
        pw.write("method,prediction\n");
        for(List<Analyzer.TestReport> reports : testReports){
            for(Analyzer.TestReport report : reports){
                pw.write(report.testDataString()+","+report.predictionString());
                pw.write("\n");
            }
        }
        pw.close();
    }
}
