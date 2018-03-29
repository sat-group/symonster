import parser.JarParser;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class TrainErrorCalculator {

    public static void main(String[] args) throws IOException {
        // Initialize trained kNN
        Analyzer.init();

        // Get train data jars
        List<String> trainData = DataSource.generateTrain();

        // Count errors
        int total = 0;
        int error_0 = 0;
        int error_10 = 0;
        Map<String, LinkedHashSet<String>> data;
        for(String jar : trainData){
            JarParser.parseJar(Collections.singletonList(jar), DataSource.targetPackages());
            data = JarParser.getMethodToAppearancesMap();
            if(data.size() != 0) {
                List<List<Analyzer.TestReport>> testReports = Analyzer.getTestReports(data.values(), true, true);
                for (List<Analyzer.TestReport> reports : testReports) {
                    for (Analyzer.TestReport report : reports) {
                        if (report.matched() == 0) {

                        } else if (report.matched() > 0) {
                            error_0++;
                        } else {
                            error_0++;
                            error_10++;
                        }
                        total++;
                    }
                }
            }
        }

        PrintWriter pw = new PrintWriter(new File("src/resources/metrics.txt"));
        pw.write("error perfect match: "+error_0+"\n");
        pw.write("error range 10 match: "+error_10+"\n");
        pw.write("error rate perfect: "+(float)error_0/(float)total+"\n");
        pw.write("error rate 10: "+(float)error_10/(float)total+"\n");
        pw.close();
    }
}
