import parser.PackageJarParser;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class JarFilter {

    public static void main(String[] args) throws FileNotFoundException {
        parseInfo(args);
    }

    public static void parseInfo(String[] args) throws FileNotFoundException {

        // Get all libs
        List<String> jars = new ArrayList<>();
        File libFolder = new File(args[0]);
        for (final File fileEntry : libFolder.listFiles()) {
            jars.add(args[0]+fileEntry.getName());
        }

        // Parse
        Map<String, Integer> occurrences = new HashMap<>();
        Set<String> unParsable = new HashSet<>();
        for (String jar : jars) {
            PackageJarParser.parseJar(Collections.singletonList(jar));
            if(PackageJarParser.getPackages().size() > 0) {
                for (String packageName : PackageJarParser.getPackages()) {
                    if (occurrences.containsKey(packageName)) {
                        occurrences.put(packageName, occurrences.get(packageName) + 1);
                    } else {
                        occurrences.put(packageName, 1);
                    }
                }
            }else{
                unParsable.add(jar);
            }
        }

        // Output
        PrintWriter pw = new PrintWriter(new File("src/resources/package_info_"+args[1]+".csv"));

        // Header
        pw.write("name,count\n");
        Map<String, Integer> sortedOccurrences = sortByValue(occurrences);
        for(String s : sortedOccurrences.keySet()){
            pw.write(s+","+sortedOccurrences.get(s)+"\n");
        }
        pw.close();

        pw = new PrintWriter(new File("src/resources/unparsable_packages.csv"));
        pw.write("name,parsable\n");
        for(String s : unParsable){
            pw.write(s+"\n");
        }
        pw.close();
    }

    private static Map<String, Integer> sortByValue(Map<String, Integer> unsortMap) {

        // 1. Convert Map to List of Map
        List<Map.Entry<String, Integer>> list =
                new LinkedList<Map.Entry<String, Integer>>(unsortMap.entrySet());

        // 2. Sort list with Collections.sort(), provide a custom Comparator
        //    Try switch the o1 o2 position for a different order
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2) {
                return -(o1.getValue()).compareTo(o2.getValue());
            }
        });

        // 3. Loop the sorted list and put it into a new insertion order Map LinkedHashMap
        Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }
}
