package stat.ngram;

import stat.StatModel;
import stat.common.Entry;
import stat.common.EntryComparator;

import java.util.*;

/**
 * bigram model
 */
public class BiGram implements StatModel {

    private double[][] PTable; // table is n x n where n is number of labels
    private List<List<Entry>> sortedEntryTable; // n x n, sorted by descending for every row
    private Map<String, Integer> labelToIndexMap;
    private String[] indexToLabel;
    private Set<Integer> seenLabels;

    public BiGram(LinkedHashSet<String> labelSet, List<LinkedHashSet<String>> trainingSets) {
        int size = labelSet.size();
        int[][] n2Table = new int[size][size];
        int[] n1Table = new int[size];
        labelToIndexMap = new HashMap<>();
        indexToLabel = new String[size];
        seenLabels = new HashSet<>();

        int i = 0;
        for (String s : labelSet) {
            labelToIndexMap.put(s, i);
            indexToLabel[i] = s;
            i++;
        }

        // add training data
        for (LinkedHashSet<String> set : trainingSets) {

            if (set.size() < 2) {

                // only one method
                for (String s : set) {
                    if (labelToIndexMap.keySet().contains(s)) {
                        // is label
                        n1Table[stoi(s)] += 1;
                    }
                }
            } else {

                // has two or more methods
                int k = 0;
                String prev = "";
                for (String s : set) {
                    if (labelToIndexMap.keySet().contains(s)) {
                        n1Table[stoi(s)] += 1;
                        System.out.println("contained");
                        if (k > 0) {
                            n2Table[stoi(prev)][stoi(s)] += 1;
                        }
                        prev = s;
                        k++;
                    }
                }
            }
        }

        // P table, sort
        PTable = new double[size][size];
        sortedEntryTable = new ArrayList<>();
        for (int y = 0; y < size; y++) {
            sortedEntryTable.add(new ArrayList<>());
            for (int x = 0; x < size; x++) {
                if (n1Table[x] != 0) {
                    PTable[y][x] = (double) n2Table[x][y] / (double) n1Table[x];
                } else {
                    PTable[y][x] = 0;
                }
                sortedEntryTable.get(y).add(new Entry(indexToLabel[x], PTable[y][x]));
            }
            sortedEntryTable.get(y).sort(new EntryComparator());
        }
    }

    public BiGram(LinkedHashSet<String> labelSet, double[][] PTable) {
        int size = labelSet.size();
        labelToIndexMap = new HashMap<>();
        indexToLabel = new String[size];
        seenLabels = new HashSet<>();

        int i = 0;
        for (String s : labelSet) {
            labelToIndexMap.put(s, i);
            indexToLabel[i] = s;
            i++;
        }

        // P table, sort
        this.PTable = PTable;
        sortedEntryTable = new ArrayList<>();
        for (int y = 0; y < size; y++) {
            sortedEntryTable.add(new ArrayList<>());
            StringBuilder j = new StringBuilder();
            boolean pos = false;
            for (int x = 0; x < size; x++) {
                sortedEntryTable.get(y).add(new Entry(indexToLabel[x], this.PTable[y][x]));
                if (this.PTable[y][x] > 0) {
                    j.append(sortedEntryTable.get(y).get(x).toString() + ",");
                    pos = true;
                }
            }
            if(pos) {
                System.out.println("y: " + indexToLabel[y]);
                System.out.println("x: " + j.toString());
                System.out.println("=========================");
            }
            //sortedEntryTable.get(y).sort(new EntryComparator());
            //System.out.println("after: "+sortedEntryTable.get(y));
        }
    }

    /**
     * For any string prior to the last line, we remove them from the final prediction
     * result, as according to the assumption of SyPet we don't allow duplicate methods.
     * The new method gets automatically added to seen set.
     *
     * @param methods set of size 1
     * @return
     */
    @Override
    public LinkedHashMap<String, Double> predict(Set<String> methods) {
        String method = "";
        for (String s : methods) {
            method = s;
        }
        if (stoi(method) < 0) {
            return new LinkedHashMap<>();
        }

        // Generate map result
        seenLabels.clear();
        //System.out.println("=====================");
        //System.out.println(method);
        //System.out.println("---------------------");
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < sortedEntryTable.size(); i++) {
            Entry e = sortedEntryTable.get(i).get(stoi(method));
            if (!seenLabels.contains(stoi(e.label))) {
                entries.add(new Entry(indexToLabel[i], e.freq));
            }
        }
        //System.out.println("=====================");
        entries.sort(new EntryComparator());


        LinkedHashMap<String, Double> result = new LinkedHashMap<>();
        for (Entry e : entries) {
            result.put(e.label, e.freq);
        }
        return result;
    }

    public void clearSeen() {
        seenLabels.clear();
    }

    // String -> Int
    private int stoi(String s) {
        if (!labelToIndexMap.containsKey(s)) {
            return -1;
        }
        return labelToIndexMap.get(s);
    }

    public double[][] getPTable() {
        return PTable.clone();
    }
}
