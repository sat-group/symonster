import parser.JarParser;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Prediction for custom jar
 */
public class GenericPrediction {

    /**
     * Output prediction for jar file using given csv
     * @param args first argument is input file(absolute path), second argument is output file(under resources by def)
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        // Use Analyzer to generate kNN
        Analyzer.init();

        // Mock test on geometry.jar
        ArrayList<String> libs = new ArrayList<>();
        libs.add(args[0]);
        JarParser.parseJar(libs, DataSource.targetPackages());

        // Use kNN to predict and generate report1
        Map<String, LinkedHashSet<String>> data = JarParser.getMethodToAppearancesMap();

        // Generate test reports
        List<List<Analyzer.TestReport>> testReports = Analyzer.getTestReports(data.values(), true, true);
        PrintWriter pw = new PrintWriter(new File("src/resources/"+args[1]));
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
