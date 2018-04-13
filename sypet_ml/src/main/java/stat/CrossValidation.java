package stat;

import stat.common.DataSource;
import stat.knn.KNN;
import stat.parser.JarParser;
import stat.parser.LibraryJarParser;

import java.io.*;
import java.util.*;

/**
 * Does knn cross validation based on k-fold rule
 * 4 fold to start with
 * input:
 * package of concern, corpus location, output csv file name, analysis txt name, paramter-k, library jar location(rt if not specified)
 * e.g.:
 * args = new String[]{"org.jsoup", "lib/org.jsoup/", "jsoup", "analysis_jsoup", 1, "../sypet_ml/lib/jsoup-1.8.3.jar"};
 * args = new String[]{"org.jsoup", "lib/org.jsoup/", "jsoup", "analysis_jsoup", 1};
 * output: src/resources/cv_args[1].txt
 */
public class CrossValidation {

    private final static int FOLD = 4;
    private static String targetPackage;
    private static String corpusPath;
    private static String outputName;
    private static String libraryJarPath = "";
    private static String analysisName;
    private static int parameterK = 1;
    private static List<Set<String>> jarsLists = new ArrayList<>();

    public static void main(String[] args) throws IOException {

        // package of concern, corpus location, output csv file name, analysis txt name, paramter-k, library jar location(rt if not specified)
        //args = new String[]{"org.jsoup", "lib/org.jsoup/", "jsoup", "analysis_jsoup", 1, "../sypet_ml/lib/jsoup-1.8.3.jar"};
        //args = new String[]{"org.jsoup", "lib/org.jsoup/", "jsoup", "analysis_jsoup", 1};

        targetPackage = args[0];
        corpusPath = args[1];
        outputName = args[2];
        analysisName = args[3];
        parameterK = Integer.parseInt(args[4]);

        if(args.length > 5) {
            libraryJarPath = args[5];
        }else{
            libraryJarPath = "";
        }

        // read all files from folder
        File libFolder = new File(corpusPath);
        int total = libFolder.listFiles().length;
        jarsLists.clear();

        for(int i=0; i<FOLD; i++){
            jarsLists.add(new HashSet<>());
        }

        // need to filter out files that do not work, can only hard code now
        String line = "";
        String cvsSplitBy = ",";

        BufferedReader br = new BufferedReader(new FileReader("src/resources/"+ analysisName +".txt"));
        Set<String> badJars = new HashSet<>();
        while ((line = br.readLine()) != null) {
            // check if we have reached final
            if (line.startsWith("NAME")) {
                String[] arr = line.split(cvsSplitBy);
                String method = arr[0].split(" ")[1];
                badJars.add(method);
            }
            if(line.startsWith("good")){
                break;
            }
        }

        int k = 0;
        for (final File fileEntry : libFolder.listFiles()) {
            if(badJars.contains(fileEntry.getName())){

            }else {
                jarsLists.get(k % FOLD).add(corpusPath + fileEntry.getName());
                k++;
            }
        }
        System.out.println("total: "+k);
        for(int i=0; i<FOLD; i++){
            System.out.println("split size: "+jarsLists.get(i).size());
        }

        // train + validate
        LibraryJarParser.init(DataSource.generateLib(libraryJarPath), DataSource.generateCustomLib(targetPackage));
        for (int i=0; i<FOLD; i++){
            Set<String> validateSet = jarsLists.get(i);
            Set<String> trainSet = new HashSet<>();
            for(int j=0; j<FOLD; j++){
                if(j!=i){
                    trainSet.addAll(jarsLists.get(j));
                }
            }

            crossValidate(trainSet, validateSet, i);
        }

    }

    private static void crossValidate(Set<String> trainSet, Set<String> validateSet, int order) throws FileNotFoundException {

        // training phase
        KNN knn = new KNN(LibraryJarParser.getLabelSet());

        for (String s : trainSet) {
            JarParser.parseJar(Collections.singletonList(s), DataSource.generateCustomLib(targetPackage));
            if (JarParser.getMethodToAppearancesMap().size() != 0) {

                // Train knn
                trainVarIndependent(knn);
            }
        }

        // validation phase
        int among_10 = 0;
        int among_5 = 0;
        int total = 0;
        for (String s : validateSet){
            JarParser.parseJar(Collections.singletonList(s), DataSource.generateCustomLib(targetPackage));

            // Use kNN to predict and generate report
            Map<String, LinkedHashSet<String>> data = JarParser.getMethodToAppearancesMap();
            List<List<Analyzer.TestReport>> testReports = Analyzer.getTestReports(data.values(), knn, parameterK, true);

            for(List<Analyzer.TestReport> reports : testReports){
                for(Analyzer.TestReport report : reports){
                    if(report.getMatched() >= 0){
                        among_10++;
                        if(report.getMatched() <= 4){
                            among_5++;
                        }
                    }
                    total++;
                }
            }
        }

        // output
        PrintWriter pw = new PrintWriter(new File("src/resources/cv_k"+parameterK+"_ind/"+outputName+"_"+order+".csv"));
        pw.write("top 10 rate: "+(float)among_10/(float) total+"\n");
        pw.write("top 5 rate: "+(float)among_5/(float) total+"\n");
        pw.write("total: " + total+"\n");
        pw.write("top 10: "+ among_10+"\n");
        pw.write("top 5: "+ among_5+"\n");
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


}
