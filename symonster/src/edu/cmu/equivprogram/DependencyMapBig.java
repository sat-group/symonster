package edu.cmu.equivprogram;

import com.google.common.collect.Sets;
import edu.cmu.parser.MethodSignature;
import soot.SootField;

import java.util.*;

/**
 * Provide a data structure to store dependent methods: methods that cannot be exchanged in order.
 */
public class DependencyMapBig implements DependencyMap {
    private final Map<MethodSignature,Set<SootField>> map;

    public DependencyMapBig(Map<MethodSignature,Set<SootField>> map){
        this.map = map;
    }

    /**
     * Find all topological sort of a given sequence of method signatures.
     * @param signatures sequence
     * @return the set of all possible sequences
     */
    public List<List<MethodSignature>> findAllTopSorts(List<MethodSignature> signatures){
        if (signatures.size() == 0) return new LinkedList<>();
        List<List<Integer>> inGraph = new LinkedList<>();
        List<List<Integer>> outGraph = new LinkedList<>();
        for (int i = 0; i < signatures.size() ; i++){
            if (map.containsKey(signatures.get(i))) {
                List<Integer> inset = new LinkedList<>();
                List<Integer> outset = new LinkedList<>();
                for (int j = 0; j < i; j++) {
                    if (map.containsKey(signatures.get(j)) && intersect(map.get(signatures.get(j)), map.get(signatures.get(i)))) {
                        inset.add(j);
                    }
                }
                inGraph.add(inset);
                for (int j = i + 1; j < signatures.size(); j++) {
                    if (map.containsKey(signatures.get(j)) && intersect(map.get(signatures.get(j)), map.get(signatures.get(i)))) {
                        outset.add(j);
                    }
                }
                outGraph.add(outset);
            }
        }
        List<List<Integer>> allTopSorts = findAllTopSortHelper(inGraph,outGraph,signatures.size());
        List<List<MethodSignature>> result = new LinkedList<>();
        for (List<Integer> sort : allTopSorts){
            assert (sort.size() == signatures.size());
            List<MethodSignature> smallResult = new LinkedList<>();
            for (int ind : sort){
                smallResult.add(signatures.get(ind));
            }
            result.add(smallResult);
        }
        return result;
    }

    private boolean intersect(Set<?> set1, Set<?> set2){
        Set<?> newset = Sets.intersection(set1, set2);
        return !newset.isEmpty();
    }

    private  List<List<Integer>> findAllTopSortHelper(List<List<Integer>> inGraph, List<List<Integer>> outGraph,int length) {
        List<Integer> starts = new LinkedList<>();
        for (int i = 0; i < inGraph.size() ; i ++){
            if (inGraph.get(i) != null && inGraph.get(i).size() == 0){
                starts.add(i);
            }
        }
        List<List<Integer>> bigResult = new LinkedList<>();
        //Base case
        if (starts.size() == 0) {
            if (length == 0){
                bigResult.add(new LinkedList<>());
            }
            return bigResult;
        }
        for (int start : starts) {
            List<List<Integer>> newInGraph = new LinkedList<>();
            for (List<Integer> inlist : inGraph){
                if (inlist == null){
                    newInGraph.add(null);
                }
                else{
                    List<Integer> newList = new LinkedList<>();
                    for (int i : inlist){
                        if (i != start) newList.add(i);
                    }
                    newInGraph.add(newList);
                }
            }
            newInGraph.set(start,null);

            List<List<Integer>> newOutGraph = new LinkedList<>();
            for (List<Integer> outlist : outGraph){
                if (outlist == null){
                    newOutGraph.add(null);
                }
                else{
                    List<Integer> newList = new LinkedList<>();
                    for (int i : outlist){
                        if (i != start) newList.add(i);
                    }
                    newOutGraph.add(newList);
                }
            }
            newOutGraph.set(start,null);
            List<List<Integer>> small = findAllTopSortHelper(newInGraph,newOutGraph,length - 1);
            for (List<Integer> smallsmall : small){
                smallsmall.add(0,start);
            }
            bigResult.addAll(small);
        }
        return bigResult;
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