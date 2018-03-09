import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Boolean.parseBoolean;

public class CSVDataParser {
    static List<TrainData> goodDataList = new ArrayList<>();
    static List<BadData> badDataList = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        read();
        write();
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
                break;
            } else if(line.startsWith("name")){
                // do nothing
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
