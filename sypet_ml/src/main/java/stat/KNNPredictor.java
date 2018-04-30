package stat;

import stat.common.DataSource;
import stat.parser.JarParser;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Prediction for custom jar
 */
public class KNNPredictor {

    /**
     * Output prediction for jar file using given csv
     * @param args first argument is input file(absolute path), second argument is output file(under resources by def)
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        // jar to predict, name of result csv, package of concern, name pretrained data csv, name of output analysis csv, filepath of lib if not rt
        // comment this out if you want to input from command line
        args = new String[]{"lib/corpus/geom_only/geometry.jar", "result_geom", "java.awt.geom", "data_geom_g", "analysis_geom", "1"};

        // Use Analyzer to generate kNN
        if(args.length > 6) {
            Analyzer.init(new String[]{args[2], args[3], args[4], args[6]});
        }else{
            Analyzer.init(new String[]{args[2], args[3], args[4]});
        }

        // Mock test on geometry.jar
        List<String> libs = new ArrayList<>();
        libs.add(args[0]);
        JarParser.parseJar(libs, DataSource.generateCustomLib(args[2]));

        // Use kNN to predict and generate report1
        Map<String, LinkedHashSet<String>> data = JarParser.getMethodToAppearancesMap();

        // Generate test reports
        List<List<Analyzer.TestReport>> testReports = Analyzer.getTestReports(data.values(), Analyzer.getModel(), Integer.parseInt(args[5]), true);
        PrintWriter pw = new PrintWriter(new File("src/resources/"+args[1]+".csv"));
        pw.write("match,method,prediction,original\n");
        int total = 0;
        int correct = 0;
        for(List<Analyzer.TestReport> reports : testReports){
            for(Analyzer.TestReport report : reports){
                pw.write(report.getMatched()+","+report.getType()+","+report.testDataString()+","+report.predictionString()+","+report.getOriginalMethod());
                if(report.getMatched() >= 0){
                    correct++;
                }
                total++;
                pw.write("\n");
            }
        }
        pw.write("correct: "+(float)correct/(float) total+"\n");
        pw.write("total: " + total+"\n");
        pw.write("correct: "+correct);
        pw.close();
    }
}
