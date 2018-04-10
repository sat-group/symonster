import parser.PackageJarParser;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Used for extracting occurrences of packages in the folder. Can use option to split the process
 * if there are way too many jar files.
 */
public class PackageInfoExtractor {

    /**
     * Initialize the package info extractor that reads different packages in the folder and write them to
     * text files (one that stores package occurrences and the other stores jars that are not parsable)
     * @param args cmd lines arguments: location of jar files, output file tags, shatter amount
     */
    public static void main(String[] args) {
        //args = new String[]{"lib/", "jojo", "2"};
        parseInfo(args);
    }

    /**
     * Starts parsing
     * @param args cmd line arguments
     */
    public static void parseInfo(String[] args) {

        // Get all libs
        System.out.println("[initializing]");
        int shatterAmount = Integer.parseInt(args[2]);
        List<List<String>> shatteredJars = new ArrayList<>(shatterAmount);
        for (int i = 0; i < shatterAmount; i++) {
            shatteredJars.add(new ArrayList<>());
        }

        File libFolder = new File(args[0]);
        File[] listFiles = libFolder.listFiles();
        int size = listFiles.length;

        System.out.println("[total size: " + size + "]");
        int jarsPerShatter = size / shatterAmount;

        for (int i = 0; i < size; i++) {
            int index = i / jarsPerShatter;
            if (index < shatterAmount) {
                shatteredJars.get(index).add(args[0] + listFiles[i].getName());
            } else {
                shatteredJars.get(index - 1).add(args[0] + listFiles[i].getName());
            }
        }

        System.out.println("[parsing starts]");

        // Parse
        readWrite(0, shatterAmount, shatteredJars, args[1]);
    }

    private static int[] createRange(int total, int sections) {
        int[] range = new int[sections + 1];
        int end = 0;
        for (int i = 0; i < range.length; i++) {
            range[i] = end;
            end += total / sections;
        }
        range[sections] = total;
        return range;
    }

    // Read from files, extract package informations and store them in files
    private static void readWrite(int shatterStart, int shatterEnd, List<List<String>> shatteredJars, String name) {
        for (int i = shatterStart; i < shatterEnd; i++) {
            List<String> jars = shatteredJars.get(i);
            Map<String, Integer> occurrences = new HashMap<>();
            Set<String> unParsable = new HashSet<>();
            for (String jar : jars) {
                PackageJarParser parser = new PackageJarParser();
                parser.parseJar(Collections.singletonList(jar));
                if (parser.getPackages().size() > 0) {
                    for (String packageName : parser.getPackages()) {
                        if (occurrences.containsKey(packageName)) {
                            occurrences.put(packageName, occurrences.get(packageName) + 1);
                        } else {
                            occurrences.put(packageName, 1);
                        }
                    }
                } else {
                    unParsable.add(jar);
                }
            }

            // Output
            PrintWriter pw = null;
            try {
                pw = new PrintWriter(new File("src/resources/jarfilter/package_info_" + name + "_" + shatteredJars.indexOf(jars) + ".csv"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            // Header
            pw.write("name,count\n");
            Map<String, Integer> sortedOccurrences = sortByValue(occurrences);
            for (String s : sortedOccurrences.keySet()) {
                pw.write(s + "," + sortedOccurrences.get(s) + "\n");
            }
            pw.close();

            try {
                pw = new PrintWriter(new File("src/resources/jarfilter/unparsable_info_" + name + "_" + shatteredJars.indexOf(jars) + ".csv"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            pw.write("name\n");
            for (String s : unParsable) {
                pw.write(s + "\n");
            }
            pw.close();
        }
    }

    // Sorts a map by its values
    public static Map<String, Integer> sortByValue(Map<String, Integer> unsortMap) {
        List<Map.Entry<String, Integer>> list =
                new LinkedList<Map.Entry<String, Integer>>(unsortMap.entrySet());

        Collections.sort(list, (o1, o2) -> -(o1.getValue()).compareTo(o2.getValue()));

        Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }
}
