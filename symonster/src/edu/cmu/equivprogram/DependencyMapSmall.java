package edu.cmu.equivprogram;

import edu.cmu.parser.MethodSignature;

import java.util.*;

/**
 * Provide a data structure to store dependent methods: methods that cannot be exchanged in order.
 */
public class DependencyMapSmall implements DependencyMap {
    private final Map<MethodSignature,Set<MethodSignature>> map = new HashMap<>();

    /**
     * Create a dependency between 2 method signatures.
     * @param m1 method 1
     * @param m2 method 2
     */
    public void addDep(MethodSignature m1, MethodSignature m2){
        if (map.containsKey(m1)) map.get(m1).add(m2);
        else{
            Set<MethodSignature> set = new HashSet<>();
            set.add(m2);
            map.put(m1,set);
        }
        if (map.containsKey(m2)) map.get(m1).add(m1);
        else{
            Set<MethodSignature> set = new HashSet<>();
            set.add(m1);
            map.put(m2,set);
        }
    }

    /**
     * Find all topological sort of a given sequence of method signatures.
     * @param signatures sequence
     * @return the set of all possible sequences
     */
    @Override
	public List<List<MethodSignature>> findAllTopSorts(List<MethodSignature> signatures){
        if (signatures.size() == 0) return new LinkedList<>();
        List<List<Integer>> inGraph = new LinkedList<>();
        List<List<Integer>> outGraph = new LinkedList<>();
        for (int i = 0; i < signatures.size() ; i++){
            List<Integer> inset = new LinkedList<>();
            List<Integer> outset = new LinkedList<>();
            for (int j = 0 ; j < i ; j++){
                if (map.containsKey(signatures.get(j)) && map.get(signatures.get(j)).contains(signatures.get(i))){
                    inset.add(j);
                }
            }
            inGraph.add(inset);
            for (int j = i+1 ; j < signatures.size() ; j++){
                if (map.containsKey(signatures.get(j)) && map.get(signatures.get(j)).contains(signatures.get(i))){
                    outset.add(j);
                }
            }
            outGraph.add(outset);
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
                bigResult.add(new LinkedList<Integer>());
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