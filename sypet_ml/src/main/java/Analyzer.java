import knn.KNN;

import java.io.*;
import java.util.*;

import static java.lang.Boolean.parseBoolean;

public class Analyzer {
    static List<TrainData> goodDataList = new ArrayList<>();
    static List<BadData> badDataList = new ArrayList<>();
    static List<int[]> vectors = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        read();
        write();
        JarParserLib.init(Main.generateLib(), Main.packages, false);
        KNN knn = new KNN(JarParserLib.getLabelSet(),vectors);
        knn.preSort();

        // mock test on geometry
        List<String> libs = Main.generateTest();
        JarParser.parseJar(libs, Main.packages);
        Map<String, Set<String>> dataT = JarParser.getMethodToAppearancesMap();
        //Set<String> sampleRotate = data.get("<symonster.cmu.edu.Examples: java.awt.geom.Area rotate(java.awt.geom.Area,java.awt.geom.Point2D,double)>");
        Set<String> sampleRotate = new HashSet<>();
        sampleRotate.add("<java.awt.geom.Point2D: double getX()>");
        //sampleRotate.remove("<java.awt.geom.Area: java.awt.geom.Area createTransformedArea(java.awt.geom.AffineTransform)>");
        //System.out.println("prob: "+knn.getFreq(""));
        LinkedHashMap<String, Float> map = knn.predict(sampleRotate);
        System.out.println("===== Top 10 Prediction =====");
        int k = 0;
        for(String method : map.keySet()) {
            if (k < 10) {
                System.out.println(method + " -> " + map.get(method));
                k++;
            }
        }

            System.out.println(knn.getTrainSetSize());
    }

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

    public static void read() throws IOException {
        String line = "";
        String cvsSplitBy = ",";

        BufferedReader br = new BufferedReader(new FileReader("src/resources/data.csv"));
        while ((line = br.readLine()) != null) {
            // check if we have reached final
            if(line.startsWith("final result:")){
                // first line of vector
                String s = line.split(":")[1];
                stringToVector(s);
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
}
