package stat;

import stat.parser.LibraryJarParser;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Prediction for custom jar
 */
public class KNNLibPredictor {

    private static LinkedHashSet<String> types = new LinkedHashSet<>();

    /**
     * Output prediction for jar file using given csv
     * @param args first argument is input file(absolute path), second argument is output file(under resources by def)
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        // jar to predict, name of result csv, package of concern, name pretrained data csv, name of output analysis csv, filepath of lib if not rt
        // comment this out if you want to input from command line
        args = new String[]{"", "result_geom_ind_lib", "java.awt.geom", "data_geom_ind_g", "analysis_geom_ind", "1"};

        // Use Analyzer to generate kNN
        if(args.length > 6) {
            Analyzer.init(new String[]{args[2], args[3], args[4], args[6]});
        }else{
            Analyzer.init(new String[]{args[2], args[3], args[4]});
        }

        // Find all availble types
        for(String s : LibraryJarParser.getLabelSet()){
            String t = s.split(" ")[1];
            types.add(t);
        }

        // Generate test reports
        List<List<Analyzer.TestReport>> testReports = Analyzer.getTestReports(Collections.singletonList(LibraryJarParser.getLabelSet()), Analyzer.getModel(), Integer.parseInt(args[5]), false);
        PrintWriter pw = new PrintWriter(new File("src/resources/"+args[1]+".csv"));
        pw.write("previous line,current type,prediction\n");
        for(List<Analyzer.TestReport> reports : testReports){
            for(Analyzer.TestReport report : reports){
                writeAllTypes(report, pw);
            }
        }

        pw.close();
    }

    private static void writeAllTypes(Analyzer.TestReport report, PrintWriter pw){
        LinkedHashMap<String, Double> predictedMethods = report.getPredictedMethods();
        for(String type : types){
            StringBuilder builder = new StringBuilder();
            int k = 0;
            for(String s : predictedMethods.keySet()){
                if(s.split(" ")[1].equals(type) && k<10){
                    builder.append(s + "=" + predictedMethods.get(s) + "|");
                    k++;
                }
            }
            pw.write(report.testDataString()+","+type+","+builder.toString());
            pw.write("\n");
        }
    }
}
