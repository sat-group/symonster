package knn;

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
            for (String label : labelMap.keySet()) {
                System.out.println(label);
            }
        }
    }

    public KNN(Set<String> labels, List<int[]> preTrained){
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
        this.values = preTrained;
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

    public String getTrainSparseString(){
        StringBuilder vecString = new StringBuilder();
        vecString.append(values.size()).append(",");
        float total = 0;
        for (int[] vec : values) {
            for (int i = 0; i < vec.length; i++) {
                if (vec[i] == 1) {
                    total+=1;
                }
            }
        }
        float average = total/values.size();
        vecString.append(average).append('\n');
        return vecString.toString();
    }

    public String getTrainDenseString(){
        StringBuilder vecString = new StringBuilder();
        for (int[] vec : values) {
            vecString.append("<");
            for (int i = 0; i < vec.length; i++) {
                vecString.append(vec[i] + ", ");
            }
            vecString.deleteCharAt(vecString.length() - 1);
            vecString.append(">\n");
        }
        return vecString.toString();
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
        List<Float> sumVector = new ArrayList<>(labelSize);
        for(int i=0; i<labelSize; i++){
            sumVector.add((float) 0);
        }
        int count = 0;
        List<Integer> indexList = appearances.stream().map(s -> labelMap.get(s)).collect(Collectors.toList());
        for(int[] row : values) {
            boolean containsAll = true;
            for (int i : indexList) {
                if (row[i] != 1) {
                    containsAll = false;
                }
            }
            if (containsAll) {
                for(int i : indexList){
                    sumVector.set(i, sumVector.get(i) - 1);
                }
                for (int i = 0; i < labelSize; i++) {
                    sumVector.set(i, sumVector.get(i)+row[i]);
                }
                count++;
            }
        }
        if(count == 0){
            System.out.println("nothing!");
            return new LinkedHashMap<>();
        }
        List<Entry> entryList = new ArrayList<>(labelSize);

        for(int i=0; i<labelSize; i++){
            sumVector.set(i, sumVector.get(i)/count);
            entryList.add(new Entry(labels[i], sumVector.get(i)));
        }

        entryList.sort(new EntryComparator());

        LinkedHashMap<String, Float> result = new LinkedHashMap<>();
        for(Entry e : entryList){
            result.put(e.label, e.freq);
        }

        return result;
    }

    class Entry{
        String label;
        Float freq;

        Entry(String label, Float freq){
            this.label = label;
            this.freq = freq;
        }
    }

    class EntryComparator implements Comparator {

        @Override
        public int compare(Object o1, Object o2) {
            if(!(o1 instanceof Entry) || !(o2 instanceof Entry)){
                return 0;
            }else{
                Entry e1 = (Entry) o1;
                Entry e2 = (Entry) o2;
                return -e1.freq.compareTo(e2.freq);
            }
        }

        @Override
        public boolean equals(Object obj) {
            return false;
        }
    }

    /*
    public String getSortedFreqString(){
        StringBuilder result = new StringBuilder();
        for(int index : sortedFreqIndexTable){
            result.append(labels[index])
            .append("<").append(freqTable[index]).append(",");
        }
        return result.toString();
    }*/
}
