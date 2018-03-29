import knn.KNN;
import parser.JarParser;
import parser.JarParserLibrary;

import java.io.*;
import java.util.*;

import static java.lang.Boolean.parseBoolean;

/**
 * Reads from data csvs and provides analysis csvs & testing results with kNN in STDOUT.
 */
public class Analyzer {
    static List<TrainData> goodDataList = new ArrayList<>();
    static List<BadData> badDataList = new ArrayList<>();
    static List<int[]> vectors = new ArrayList<>();

    /**
     * Main function that reads from data csvs and provides analysis csvs & testing results in STDOUT.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        read();
        write();

        // Get labels from library
        JarParserLibrary.init(DataSource.generateLib(), DataSource.targetPackages());

        // initialize kNN
        KNN knn = new KNN(JarParserLibrary.getLabelSet(),vectors);

        //PrintWriter pw = new PrintWriter(new File("src/resources/vector_sparse.txt"));
        //pw.write(knn.getTrainSparseString());
        //pw.close();

        // Mock test on geometry.jar
        JarParser.parseJar(DataSource.generateTest(), DataSource.targetPackages());

        // Use kNN to predict and generate report
        Map<String, LinkedHashSet<String>> data = JarParser.getMethodToAppearancesMap();
        for(LinkedHashSet<String> program : data.values()){
            List<TrainReport> reports = generateReport(program, knn);
            System.out.println("============start============");
            for(TrainReport report : reports){
                System.out.println(report);
            }
            System.out.println("=============end=============");
        }
    }

    /**
     * Write analytical results to filepath
     * @throws FileNotFoundException file not found
     */
    public static void write() throws FileNotFoundException {
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
    public static void read() throws IOException {
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
    static List<TrainReport> generateReport(LinkedHashSet<String> program, KNN knn){
        Set<String> set = new HashSet<>();
        List<TrainReport> reports = new ArrayList<>();
        for(String method : program){
            LinkedHashMap<String, Float> map = knn.predict(set);
            TrainReport report = new TrainReport(method,map,set);
            if(!method.equals("<java.awt.geom.Area: void <init>(java.awt.Shape)>")) {
                set.add(method);
            }
            reports.add(report);
        }
        return reports;
    }

    static int[] stringToVector(String s){
        s = s.substring(1,s.length()-1);
        String[] strings = s.split(",");
        int[] vec = new int[strings.length];
        for(int i=0; i<strings.length; i++){
            vec[i] = Integer.parseInt(strings[i].replaceAll("\\s+",""));
        }
        return vec;
    }

    static class TrainData{
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

    static class BadData{
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
    static class TrainReport{
        Set<String> train;
        String originalMethod;
        LinkedHashMap<String, Float> predictedMethods; // sorted predicted entries
        String type; // expected prediction type
        int matched = -1;

        /**
         * Generates a train report
         * @param originalMethod expected method
         * @param predictedMethods sorted predicted methods
         * @param testData give test data
         */
        TrainReport(String originalMethod, LinkedHashMap<String, Float> predictedMethods, Set<String> testData){
            this.originalMethod = originalMethod;
            this.predictedMethods = predictedMethods;
            String[] splitted = originalMethod.split(" ");
            this.type = splitted[0]+" "+splitted[1];
            this.train = new HashSet<>(testData);
        }

        @Override
        public String toString(){
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("===== Top 10 Prediction =====\n");
            int k = 0;
            for(String method : predictedMethods.keySet()) {
                if (k < 10) {
                    if(method.contains(type)) {
                        if(method.equals(originalMethod)){
                            matched = k;
                        }
                        stringBuilder.append(method + " -> " + predictedMethods.get(method) + "\n");
                        k++;
                    }
                }
            }
            return "MATCH: "+matched+", ORIGINAL: "+originalMethod+
                    "\n TRAIN: "+train+"\n"+
                    "\n PREDICTION: "+stringBuilder+"\n";
        }
    }
}
