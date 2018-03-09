package edu.cmu.codeformer;

import java.util.*;

public class VarTable {
    final Map<String,List<Integer>> table = new HashMap<>();
    private final Map<Integer, String> lookupTable = new HashMap<>();

    public void addEntry(String type, int var){
        if (!table.containsKey(type)){
            table.put(type,new ArrayList<>());
        }
        table.get(type).add(var);
        lookupTable.put(var,type);
    }

    /**
     * No defensive copy is made here.
     * @param type
     * @return
     */
    public List<Integer> getEntries(String type){
        if (table.containsKey(type)) return table.get(type);
        else return new LinkedList<>();
    }

    public String getType(int val){
        return lookupTable.get(val);
    }
}
