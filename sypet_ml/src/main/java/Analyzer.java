import knn.KNN;
import parser.LibraryJarParser;

import java.io.*;
import java.util.*;

import static java.lang.Boolean.parseBoolean;

/**
 * Reads from data csvs and provides analysis csvs & testing results with kNN in STDOUT.
 */
public class Analyzer {
    private static List<TrainData> goodDataList = new ArrayList<>();
    private static List<BadData> badDataList = new ArrayList<>();
    private static List<int[]> vectors = new ArrayList<>();
    private static KNN knn;

    /**
     * Main function that reads from data csvs and provides analysis csvs & testing results in STDOUT.
     * @throws IOException
     */
    public static void init() throws IOException {
        read();
        write();

        // Get labels from library
        LibraryJarParser.init(DataSource.generateLib(), DataSource.targetPackages());

        // initialize kNN
        knn = new KNN(LibraryJarParser.getLabelSet(),vectors);
    }

    public static List<List<TestReport>> getTestReports(Collection<LinkedHashSet<String>> testData,
                                                        boolean accumulative, boolean strict){
        List<List<TestReport>> testReportsList = new ArrayList<>();

        for(LinkedHashSet<String> program : testData){
            List<TestReport> reports = generateReport(program, knn, accumulative, strict);
            testReportsList.add(reports);
        }

        return testReportsList;
    }

    /**
     * Write analytical results to filepath
     * @throws FileNotFoundException file not found
     */
    private static void write() throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(new File("src/resources/analysis.txt"));
        pw.write("bad data: "+badDataList.size()+"\n");
        for(BadData badData : badDataList){
            pw.write(badData.toString()+"\n");
        }

        pw.write("good data: "+goodDataList.size()+"\n");
        for(TrainData goodData : goodDataList){
            pw.write(goodData.toString()+"\n");
        }
        pw.close();
    }

    /**
     * Read csv data from filepath
     * @throws IOException file not found
     */
    private static void read() throws IOException {
        String line = "";
        String cvsSplitBy = ",";

        BufferedReader br = new BufferedReader(new FileReader("src/resources/data.csv"));
        while ((line = br.readLine()) != null) {
            // check if we have reached final
            if(line.startsWith("final result:")){
                // first line of vector
                String s = line.split(":")[1];
                vectors.add(stringToVector(s));
            } else if(line.startsWith("name")){
                // do nothing
            }else if(line.startsWith("<j")){

            }else if(line.startsWith("<")){
                vectors.add(stringToVector(line));
            }else {
                String[] dataStr = line.split(cvsSplitBy);
                String name = dataStr[0].split("/")[4];
                if(dataStr[1].equals("true")) {
                    if(dataStr[2].equals("0")){
                        BadData badData = new BadData(name, "No methods present.");
                        badDataList.add(badData);
                    }else {
                        TrainData data = new TrainData(name, parseBoolean(dataStr[1]), Integer.parseInt(dataStr[2]), Float.parseFloat(dataStr[3]));
                        goodDataList.add(data);
                    }
                }else{
                    BadData badData = new BadData(name, "Soot parse error.");
                    badDataList.add(badData);
                }
            }

        }
    }

    /**
     * Generates a report from a given set of program lines using kNN
     * @param program set of program methods
     * @param knn trained kNN
     * @return report containing predicted results for the program
     */
    private static List<TestReport> generateReport(LinkedHashSet<String> program, KNN knn, boolean accumulative, boolean strict){
        Set<String> testData = new HashSet<>();
        List<TestReport> reports = new ArrayList<>();
        for(String method : program){
            LinkedHashMap<String, Float> predictedResults = knn.predict(testData);
            TestReport report = new TestReport(method, predictedResults, testData, strict);
            if(!method.equals("<java.awt.geom.Area: void <init>(java.awt.Shape)>")) {
                if(accumulative) {
                    testData.add(method);
                }else{
                    testData = new HashSet<>();
                    testData.add(method);
                }
            }
            reports.add(report);
        }
        return reports;
    }

    private static int[] stringToVector(String s){
        s = s.substring(1,s.length()-1);
        String[] strings = s.split(",");
        int[] vec = new int[strings.length];
        for(int i=0; i<strings.length; i++){
            vec[i] = Integer.parseInt(strings[i].replaceAll("\\s+",""));
        }
        return vec;
    }

    // Represents training data
    private static class TrainData{
        String name;
        boolean parsed;
        int rows;
        float average;

        TrainData(String name, boolean parsed, int rows, float average){
            this.name = name;
            this.parsed = parsed;
            this.rows = rows;
            this.average = average;
        }

        @Override
        public String toString(){
            return "NAME: "+name+", ROWS: "+rows+", AVERAGE: "+average;
        }
    }

    // This is a bad data that has problems!
    private static class BadData{
        String name;
        String problem;

        BadData(String name, String problem){
            this.name = name;
            this.problem = problem;
        }

        @Override
        public String toString(){
            return "NAME: "+name+", ERROR: "+problem;
        }
    }

    /**
     * Contains ranking given by prediction from a set of test data
     */
    static class TestReport {
        private Set<String> testData;
        private String originalMethod;
        private String type; // expected prediction type
        private StringBuilder predictionStringBuilder;
        private int matched = -1;
        private boolean strict;

        /**
         * Generates a train report
         * @param originalMethod expected method
         * @param predictedMethods sorted predicted methods
         * @param testData give test data
         * @param strict whether toString should restrict matching and type
         */
        TestReport(String originalMethod, LinkedHashMap<String, Float> predictedMethods, Set<String> testData, boolean strict){
            this.originalMethod = originalMethod;
            if(strict) {
                String[] splitted = originalMethod.split(" ");
                this.type = splitted[0] + " " + splitted[1];
            }
            this.testData = new HashSet<>(testData);

            this.predictionStringBuilder = new StringBuilder();
            this.strict = strict;
            int k = 0;
            for(String method : predictedMethods.keySet()) {
                if (k < 10) {
                    if(strict) {
                        if (method.contains(type)) {
                            if (method.equals(originalMethod)) {
                                matched = k;
                            }
                            predictionStringBuilder.append(method + "=" + predictedMethods.get(method)+"|");
                            k++;
                        }
                    }else{
                        predictionStringBuilder.append(method + "=" + predictedMethods.get(method) + "|");
                        k++;
                    }
                }
            }
        }

        /**
         * Returns String representation of test data
         * @return String representation of test data
         */
        public String testDataString(){
            return testData.toString();
        }

        /**
         * Returns String representation of prediction result
         * @return String represnetation of prediction result
         */
        public String predictionString(){
            return predictionStringBuilder.toString();
        }

        @Override
        public String toString(){

            if(!strict){
                return "TEST: "+testData+"\n"+
                        "\n PREDICTION: "+predictionStringBuilder+"\n";
            }
            return "MATCH: "+matched+", ORIGINAL: "+originalMethod+
                    "\n TEST: "+testData+"\n"+
                    "\n PREDICTION: "+predictionStringBuilder+"\n";
        }
    }
}
