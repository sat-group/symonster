package stat;

import java.io.*;
import java.util.*;

// Just a boring scraper :|
public class Scraper {

    public static void main(String[] args) throws IOException {
        // read all files from folder
        List<String> parsableFiles = new ArrayList<>();
        List<String> unparsableFiles = new ArrayList<>();
        File libFolder = new File("src/resources/jarfilter/");
        for (final File fileEntry : libFolder.listFiles()) {
            String name = fileEntry.getName();
            if(name.startsWith("package")) {
                parsableFiles.add("src/resources/jarfilter/" + fileEntry.getName());
            }else{
                unparsableFiles.add("src/resources/jarfilter/" + fileEntry.getName());
            }
        }

        // rearrange parsable files
        Map<String, Integer> occurrences = new LinkedHashMap<>();
        String line = "";
        BufferedReader br;
        String csvsplitby = ",";
        for(String file : parsableFiles){
            br = new BufferedReader(new FileReader(file));
            while((line=br.readLine())!=null){
                if(line.startsWith("name")){

                }else{
                    String[] s = line.split(csvsplitby);
                    int count = Integer.parseInt(s[1]);
                    if(occurrences.containsKey(s[0])) {
                        occurrences.put(s[0], occurrences.get(s[0])+count);
                    }else{
                        occurrences.put(s[0], count);
                    }
                }
            }
        }
        Map<String, Integer> sortedOccurrences = PackageInfoExtractor.sortByValue(occurrences);

        // rearrange unparsable files
        Set<String> badFiles = new HashSet<>();
        for(String file : unparsableFiles){
            br = new BufferedReader(new FileReader(file));
            while((line=br.readLine())!=null){
                if(line.startsWith("name")){

                }else{
                    String[] s = line.split(csvsplitby);
                    badFiles.add(s[0]);
                }
            }
        }

        // write
        PrintWriter pw = new PrintWriter(new File("src/resources/package_final.csv"));
        pw.write("name,count\n");
        for(Map.Entry<String, Integer> e : sortedOccurrences.entrySet()){
            pw.write(e.getKey()+","+e.getValue()+"\n");
        }
        pw.close();

        pw = new PrintWriter(new File("src/resources/unparsable_final.csv"));
        pw.write("TOTAL: "+badFiles.size()+"\n");
        pw.write("name\n");
        for(String s : badFiles){
            pw.write(s+"\n");
        }
        pw.close();
    }
}
