package stat;

import stat.common.DataSource;
import stat.ngram.BiGram;
import stat.parser.JarParser;
import stat.parser.LibraryJarParser;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class BiGramPredictor {

    private static String targetPackage;
    private static String predictPath;
    private static String outputName;
    private static String modelName;
    private static String libraryJarPath = "";

    public static void main(String[] args) throws IOException {
        // jar to predict, name of result csv, package of concern, filepath of lib if not rt
        // comment this out if you want to input from command line
        args = new String[]{"lib/corpus/org.joda.time/joda.jar", "result_joda_bigram", "data_bigram_jodatime", "org.joda.time","lib/joda-time-2.8.2.jar"};

        targetPackage = args[3];
        predictPath = args[0];
        outputName = args[1];
        modelName = args[2];

        if(args.length > 4) {
            libraryJarPath = args[4];
        }else{
            libraryJarPath = "";
        }


        BufferedReader br = new BufferedReader(new FileReader("src/resources/" + modelName + ".csv"));
        String line = "";
        String cvsSplitBy = ",";
        double[][] PTable = null;

        int y = 0;
        while ((line = br.readLine()) != null) {
            String[] s = line.split(cvsSplitBy);

            if(PTable == null){
                PTable = new double[s.length][s.length];
            }
            for(int x=0; x<s.length; x++){
                PTable[y][x] = Double.parseDouble(s[x]);
            }
            y++;
        }

        // Form bigram
        LibraryJarParser.init(DataSource.generateLib(libraryJarPath), DataSource.generateCustomLib(targetPackage));
        BiGram biGram = new BiGram(LibraryJarParser.getLabelSet(), PTable);


        List<String> libs = new ArrayList<>();
        libs.add(predictPath);
        JarParser.parseJar(libs, DataSource.generateCustomLib(targetPackage));

        // Use kNN to predict and generate report1
        Map<String, LinkedHashSet<String>> data = JarParser.getMethodToAppearancesMap();

        // Generate test reports
        List<List<Analyzer.TestReport>> testReports = Analyzer.getTestReports(data.values(), biGram, -1, true);
        PrintWriter pw = new PrintWriter(new File("src/resources/"+outputName+".csv"));
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
