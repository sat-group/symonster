package knn;

import java.util.*;

public class KNN {
    private String[] labels;
    private Map<String, Integer> labelMap;
    private List<int[]> values;
    private int labelSize;

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
        }
    }


    public void addTrainVector(List<String> occurredLabels){
        int[] vector = new int[labelSize];
        for(String s : occurredLabels){
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

    public void predict(List<String> appearances){

    }
}
