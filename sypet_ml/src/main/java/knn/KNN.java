package knn;

import java.util.*;

/**
 * k-Nearest Neighbors, finds nearest neighbor through frequency
 */
public class KNN {
    private String[] labels;
    private Map<String, Integer> labelMap;
    private List<int[]> values;
    private int labelSize;
    private float[] freqTable;
    private List<Integer> sortedFreqIndexTable;
    boolean isSorted = false;

    public KNN(Set<String> labels) {
        if (labels.size() <= 0) {
            throw new IllegalArgumentException();
        } else {
            this.labelSize = labels.size();
            this.labels = new String[labelSize];
            int j = 0;
            for (String s : labels) {
                this.labels[j] = s;
                j++;
            }
            this.labelMap = new HashMap<>();
            for (int i = 0; i < this.labels.length; i++) {
                labelMap.put(this.labels[i], i);
            }
            this.values = new ArrayList<>();
            this.freqTable = new float[labelSize];
            Arrays.fill(freqTable, -1);
            for (String label : labelMap.keySet()) {
                System.out.println(label);
            }
        }
    }


    public void addTrainVector(Set<String> appearances) {
        int[] vector = new int[labelSize];
        boolean hasOne = false;
        for (String s : appearances) {
            if (labelMap.containsKey(s)) {
                int i = labelMap.get(s);
                vector[i] = 1;
                hasOne = true;
            }
        }
        if(hasOne) {
            values.add(vector);
            // needs update
            isSorted = false;
        }
    }

    public void showTrainSetDense() {
        for (int[] vec : values) {
            StringBuilder vecString = new StringBuilder();
            vecString.append("<");
            for (int i = 0; i < vec.length; i++) {
                vecString.append(vec[i] + ", ");
            }
            vecString.deleteCharAt(vecString.length() - 1);
            vecString.append(">");
            System.out.println(vecString.toString());
        }
    }

    public void showTrainSetSparse() {
        for (int[] vec : values) {
            StringBuilder vecString = new StringBuilder();
            vecString.append("[");
            for (int i = 0; i < vec.length; i++) {
                if (vec[i] == 1) {
                    vecString.append(labels[i].split(":")[1].split(">")[0] + "=1,");
                }
            }
            if(vecString.length()>1)
                vecString.deleteCharAt(vecString.length() - 1);
            vecString.append("]");
            System.out.println(vecString.toString());
        }
    }

    public int getLabelsCount() {
        return labelSize;
    }

    public int getTrainSetSize() {
        return values.size();
    }

    public LinkedHashMap<String, Float> predict(Set<String> appearances) {
        int size = appearances.size();
        if(isSorted){
            // Pre-sorted, just sieve out those that are not in appearnces
            List<Integer> candidates = new ArrayList<>(sortedFreqIndexTable);
            System.out.println("tag3:"+candidates.size());
            for (int i = 0; i < labelSize; i++) {
                if (appearances.contains(labels[i])) {
                    candidates.remove((Integer)i);
                }
            }
            LinkedHashMap<String, Float> result = new LinkedHashMap<>();
            for (int c : candidates) {
                result.put(labels[c], freqTable[c]);
            }
            return result;
        }
        // else
        List<Integer> candidates = new ArrayList<>(labelSize - appearances.size());
        for (int i = 0; i < labelSize; i++) {
            if (!appearances.contains(labels[i])) {
                candidates.add(i);
                if (freqTable[i] == -1) {
                    freqTable[i] = average(i);
                }
            }
        }
        candidates.sort(new FreqComparator());
        LinkedHashMap<String, Float> result = new LinkedHashMap<>();
        for (int c : candidates) {
            result.put(labels[c], freqTable[c]);
        }
        return result;
    }

    private class FreqComparator implements Comparator<Integer> {

        @Override
        public int compare(Integer o1, Integer o2) {
            // descending order
            return Float.compare(freqTable[o2],freqTable[o1]);
        }
    }

    private float average(int col) {
        int[] colVector = new int[getTrainSetSize()];
        int i = 0;
        for (int[] row : values) {
            colVector[i] = row[col];
            i++;
        }
        return (float) Arrays.stream(colVector).sum() / (float) labelSize;
    }

    public float getFreq(String label){
        int i = labelMap.get(label);
        if(freqTable[i]==-1){
            freqTable[i] = average(i);
        }
        return freqTable[i];
    }

    public void preSort(){
        sortedFreqIndexTable = new ArrayList<>();
        for (int i = 0; i < labelSize; i++) {
            if (freqTable[i] == -1) {
                freqTable[i] = average(i);
            }
            sortedFreqIndexTable.add(i);
        }
        System.out.println("tag:"+sortedFreqIndexTable.size());
        System.out.println("tag2:"+freqTable.length);

        sortedFreqIndexTable.sort(new FreqComparator());
        isSorted = true;
    }
}
