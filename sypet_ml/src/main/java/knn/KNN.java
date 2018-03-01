package knn;

import soot.SootMethod;

import java.util.*;
import java.util.stream.Collectors;

/**
 * k-Nearest Neighbors, finds nearest neighbor through frequency
 */
public class KNN {
    private String[] labels;
    private Map<String, Integer> labelMap;
    private List<int[]> values;
    private int labelSize;
    private float[] freqTable;

    public KNN(Set<String> labels){
        if(labels.size() <= 0){
            throw new IllegalArgumentException();
        }else{
            this.labelSize = labels.size();
            this.labels = new String[labelSize];
            int j = 0;
            for(String s : labels){
                this.labels[j] = s;
                j++;
            }
            this.labelMap = new HashMap<>();
            for(int i=0; i<this.labels.length; i++){
                labelMap.put(this.labels[i], i);
            }
            this.values = new ArrayList<>();
            this.freqTable = new float[labelSize];
            Arrays.fill(freqTable, -1);
            for (String label : labelMap.keySet()){
                System.out.println(label);
            }
        }
    }


    public void addTrainVector(Set<String> appearances){
        int[] vector = new int[labelSize];
        for(String s : appearances){
            if(labelMap.containsKey(s)){
                int i = labelMap.get(s);
                vector[i] = 1;
            }
        }
        values.add(vector);
    }

    public int getLabelsCount() {
        return labelSize;
    }

    public int getTrainSetSize(){
        return values.size();
    }

    public Map<String, Float> predict(Set<String> appearances){
        int size = appearances.size();
        List<Integer> candidates = new ArrayList<>(labelSize - appearances.size());
        for(int i=0; i<labelSize; i++){
            if(!appearances.contains(labels[i])){
                candidates.add(i);
                if(freqTable[i]==-1) {
                    freqTable[i] = average(i);
                }
            }
        }
        candidates.sort(new FreqComparator());
        Map<String, Float> result = new LinkedHashMap<>();
        for(int c : candidates){
            result.put(labels[c], freqTable[c]);
        }
        return result;
    }

    private class FreqComparator implements Comparator<Integer> {

        @Override
        public int compare(Integer o1, Integer o2) {
            // descending order
            return (int) -(freqTable[o1] - freqTable[o2]);
        }
    }

    private float average(int col){
        int[] colVector = new int[getTrainSetSize()];
        int i = 0;
        for(int[] row : values){
            colVector[i] = row[col];
        }
        return (float) Arrays.stream(colVector).sum() / (float) labelSize;
    }
}
