package edu.cmu.equivprogram;

import edu.cmu.parser.MethodSignature;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;

public class DependencyMap {
    private final Map<MethodSignature,Set<MethodSignature>> map = new HashMap<>();
    public void addDep(MethodSignature m1, MethodSignature m2){
        if (map.containsKey(m1)) map.get(m1).add(m2);
        else{
            Set<MethodSignature> set = new HashSet<>();
            set.add(m2);
            map.put(m1,set);
        }
    }

    List<List<MethodSignature>> findAllTopSort(){
        return null;
    }

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        for (MethodSignature key : map.keySet()){
            builder.append(key + "" + map.get(key) + "\n");
        }
        return builder.toString();
    }
}
