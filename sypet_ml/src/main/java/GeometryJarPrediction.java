import parser.JarParser;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**\
 * Prints prediction for geometry jar using kNN
 */
public class GeometryJarPrediction {

    public static void main(String[] args) throws IOException {

        // Use Analyzer to generate kNN
        Analyzer.init();

        // Mock test on geometry.jar
        JarParser.parseJar(DataSource.generateTest(), DataSource.targetPackages());

        // Use kNN to predict and generate report
        Map<String, LinkedHashSet<String>> data = JarParser.getMethodToAppearancesMap();

        // Generate test reports
        List<List<Analyzer.TestReport>> testReports = Analyzer.getTestReports(data.values(), true, true);
        for(List<Analyzer.TestReport> reports : testReports){
            System.out.println("============start============");
            for(Analyzer.TestReport report : reports){
                System.out.println(report);
            }
            System.out.println("=============end=============");
        }
    }
}
