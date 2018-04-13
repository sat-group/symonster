package stat.ngram;

import stat.common.Entry;
import stat.common.EntryComparator;

import java.util.*;

/**
 * bigram model
 */
public class BiGram {

    private double[][] PTable; // table is n x n where n is number of labels
    private List<Entry>[] sortedEntryTable; // n x n, sorted by descending for every row
    private Map<String, Integer> labelToIndexMap;
    private String[] indexToLabel;
    private Set<Integer> seenLabels;

    public BiGram(LinkedHashSet<String> labelSet, List<LinkedHashSet<String>> trainingSets){
        int size = labelSet.size();
        int[][] n2Table = new int[size][size];
        int[] n1Table = new int[size];
        labelToIndexMap = new HashMap<>();

        int i = 0;
        for(String s : labelSet){
            labelToIndexMap.put(s, i);
            indexToLabel[i] = s;
            i++;
        }

        // add training data
        for(LinkedHashSet<String> set : trainingSets){

            if (set.size() < 2){

                // only one method
                for(String s : set){
                    if(labelToIndexMap.keySet().contains(s)){
                        // is label
                        n1Table[stoi(s)] += 1;
                    }
                }
            } else {

                // has two or more methods
                int k = 0;
                String prev = "";
                for(String s : set){
                    n1Table[stoi(s)]+=1;
                    if (k > 0) {
                        n2Table[stoi(prev)][stoi(s)]+=1;
                    }
                    prev = s;
                    k++;
                }
            }

            // P table, sort
            PTable = new double[size][size];
            for(int y=0; y<size; y++){
                sortedEntryTable[y] = new ArrayList<>();
                for(int x=0; x<size; x++){
                    PTable[y][x] = n2Table[x][y] / n1Table[x];
                    sortedEntryTable[y].set(x, new Entry(indexToLabel[y], PTable[y][x]));
                }
                sortedEntryTable[y].sort(new EntryComparator());
            }
        }
    }

    /**
     * For any string prior to the last line, we remove them from the final prediction
     * result, as according to the assumption of SyPet we don't allow duplicate methods.
     * The new method gets automatically added to seen set.
     * @param method
     * @return
     */
    public LinkedHashMap<String, Double> predict(String method){
        if(stoi(method) < 0) {
            return new LinkedHashMap<>();
        }

        // Generate map result
        LinkedHashMap<String, Double> result = new LinkedHashMap<>();
        for (Entry e : sortedEntryTable[stoi(method)]) {
            if(!seenLabels.contains(stoi(e.label))) {
                result.put(e.label, e.freq);
            }
        }

        return result;
    }

    public void clearSeen(){
        seenLabels.clear();
    }

    // String -> Int
    private int stoi(String s){
        if(! labelToIndexMap.containsKey(s)){
            return -1;
        }
        return labelToIndexMap.get(s);
    }

    public double[][] getPTable(){
        return PTable.clone();
    }
}
