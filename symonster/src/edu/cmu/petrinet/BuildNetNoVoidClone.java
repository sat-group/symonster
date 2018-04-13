package edu.cmu.petrinet;

import edu.cmu.parser.MethodSignature;
import soot.Type;

import uniol.apt.adt.exception.NoSuchEdgeException;
import uniol.apt.adt.exception.NoSuchNodeException;
import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.HashMap;
import java.util.Map;

/*
   An reimplementation of BuildNet, but eliminates all clone edges by creating
   a copy for each method that returns its own arguments.
 */
public class BuildNetNoVoidClone {
    static public PetriNet petrinet;
    //map from transition name to a method signature
    static public Map<String, MethodSignature> dict;

    static private Map<String, List<String>> superDict;
    static private Map<String, List<String>> subDict;

    public BuildNetNoVoidClone() {
        petrinet = new PetriNet("net");
        dict = new HashMap<String, MethodSignature>();
        superDict = new HashMap<>();
        subDict = new HashMap<>();
    }

    private static void handlePolymorphism() {
        for(String subClass : superDict.keySet()) {
            addPlace(subClass);
            for (String superClass : superDict.get(subClass)) {
                addPlace(superClass);
                String methodName = subClass + "IsPolymorphicTo" + superClass;
                petrinet.createTransition(methodName);
                petrinet.createFlow(subClass, methodName, 1);
                petrinet.createFlow(methodName, superClass, 1);
            }
        }
    }


    private static void generatePolymophism(Transition t,
                                            int count,
                                            List<Place> inputs,
                                            Stack<Place> polyInputs) {
        if(inputs.size() == count) {
            boolean skip = true;
            for(int i = 0; i < inputs.size(); i++) {
                if(!inputs.get(i).equals(polyInputs.get(i))) {
                    skip = false;
                }
            }
            if(skip) {
                return;
            }

            String newTransitionName = t.getId() + "Poly:(";
            for(Place p : polyInputs) {
                newTransitionName += p.getId() + " ";
            }
            newTransitionName += ")";

            if(petrinet.containsTransition(newTransitionName)) {
                return;
            }
            Transition newTransition  = petrinet.createTransition(newTransitionName);
            for(Place p : polyInputs) {
                addFlow(p.getId(), newTransitionName, 1);
            }

            for(Flow f : t.getPostsetEdges()) {
                Place p = f.getPlace();
                int w = f.getWeight();
                petrinet.createFlow(newTransition, p, w);
            }
            dict.put(newTransitionName, dict.get(t.getId()));

        } else {
            Place p = inputs.get(count);
            List<String> subClasses = subDict.get(p.getId());
            if(subClasses == null) { // No possible polymophism
                polyInputs.push(p);
                generatePolymophism(t, count+1, inputs, polyInputs);
                polyInputs.pop();
                return;
            } else {
                for(String subclass : subClasses) {
                    addPlace(subclass);
                    Place polyClass = petrinet.getPlace(subclass);
                    polyInputs.push(polyClass);
                    generatePolymophism(t, count+1, inputs, polyInputs);
                    polyInputs.pop();
                }
                return;
            }
        }
    }

    private static void handlePolymorphismAlt() {
        // Handles polymorphism by creating copies for each method that
        // has superclass as input type
        for(Transition t : petrinet.getTransitions()) {
            List<Place> inputs = new ArrayList<>();
            Set<Flow> inEdges = t.getPresetEdges();
            for(Flow f : inEdges) {
                for(int i = 0; i < f.getWeight(); i++) {
                    inputs.add(f.getPlace());
                }
            }
            Stack<Place> polyInputs = new Stack<>();
            generatePolymophism(t, 0, inputs, polyInputs);
        }
    }

    private static void generateCopies(Transition t, List<Place> inputs, List<Place> outputs) {
        if(inputs.size() == 0) {
            if(outputs.size() == 0) {
                return;
            }
            String newTransitionName = t.getId()+ "(";
            for(Place p : outputs) {
                newTransitionName += p.getId() + " ";
            }
            newTransitionName += ")";
            if(petrinet.containsTransition(newTransitionName)) {
                return;
            }
            Transition newTransition  = petrinet.createTransition(newTransitionName);
            for(Flow f : t.getPresetEdges()) {
                Place p = f.getPlace();
                int w = f.getWeight();
                petrinet.createFlow(p, newTransition, w);
            }
            for(Flow f : t.getPostsetEdges()) {
                Place p = f.getPlace();
                int w = f.getWeight();
                petrinet.createFlow(newTransition, p, w);
            }
            for(Place p : outputs) {
                addFlow(newTransitionName, p.getId(), 1);
            }
            dict.put(newTransitionName, dict.get(t.getId()));
        } else {
            Place first = inputs.remove(0);
            generateCopies(t, inputs, outputs);
            outputs.add(first);
            generateCopies(t, inputs, outputs);
            inputs.add(first);
            outputs.remove(first);
        }
    }

    private static void createCopies(Transition t) {
        System.out.println(t);
        Set<Flow> inputEdges = petrinet.getPresetEdges(t);
        List<Place> inputs = new ArrayList<>();
        for(Flow e : inputEdges) {
            for(int i = 0 ; i < e.getWeight(); i++) {
                inputs.add(e.getPlace());
            }
        }
        List<Place> outputs = new ArrayList<>();
        MethodSignature sig = dict.get(t.getId());
        /*
        if(sig.getRetType().toString() == "void") {
           Place p = inputs.remove(0);
           outputs.add(p);
        }
        */
        generateCopies(t, inputs, outputs);
    }

    private static void getPolymorphismInformation(
            Map<String, Set<String>> superClassMap,
            Map<String, Set<String>> subClassMap) {
        for(String s : superClassMap.keySet()) {
            Set<String> superClasses = superClassMap.get(s);
            if(superClasses.size() != 0) {
                List<String> superClassList = new ArrayList<>(superClasses);
                superDict.put(s, superClassList);
            }
        }
        for(String s : subClassMap.keySet()) {
            Set<String> subClasses = subClassMap.get(s);
            if(subClasses.size() != 0) {
                List<String> subClassList = new ArrayList<>(subClasses);
                subDict.put(s, subClassList);
            }
        }
    }


    private static void setMaxTokens(List<String> inputs) {
        //Set max tokens for each place
        for (Place p : petrinet.getPlaces()) {
            int count = 0;
            for (Transition t : petrinet.getTransitions()) {
                try {
                    Flow f = petrinet.getFlow(p.getId(), t.getId());
                    count = Math.max(count, f.getWeight() + 1);
                } catch (NoSuchEdgeException e) {}
            }
            if(count != 0) {
                p.setMaxToken(count);
            } else {
                p.setMaxToken(1);
            }
        }
        // Update the maxtoken for inputs
        HashMap<Place, Integer> count = new HashMap<Place, Integer>();
        for (String input : inputs) {
            Place p;
            p = petrinet.getPlace(input);
            if (count.containsKey(p)) {
                count.put(p, count.get(p) + 1);
            } else {
                count.put(p, 1);
            }
        }
        for(Place p : count.keySet()) {
            if(count.get(p) > p.getMaxToken()) {
                p.setMaxToken(count.get(p));
            }
        }
    }

    private static void addPlace(String placeID) {
        try {
            petrinet.getPlace(placeID);
        } catch (NoSuchNodeException e) {
            petrinet.createPlace(placeID);
        }
    }


    private static void addFlow(String ID1, String ID2, int weight) {
        Flow f;
        try {
            f = petrinet.getFlow(ID1, ID2);
            f.setWeight(f.getWeight() + weight);
        } catch (NoSuchEdgeException e) {
            petrinet.createFlow(ID1, ID2, weight);
        }
    }

    private static void addTransition(MethodSignature methodSig) {
        String methodname = methodSig.getName();
        boolean isStatic = methodSig.getIsStatic();
        boolean isConstructor = methodSig.getIsConstructor();
        String className = methodSig.getHostClass().getName();
        String transitionName;
        List<Type> args = methodSig.getArgTypes();

        if(isConstructor) {
            transitionName = methodname + "(Constructor)" +  "(";
            for(Type t : args) {
                transitionName += t.toString() + " ";
            }
            transitionName += ")";
            transitionName += "Ret(" + methodSig.getRetType() + ")";
            petrinet.createTransition(transitionName);
        }
        else if (isStatic) {
            transitionName =  "(static)" + className + "." + methodname + "(";
            for(Type t : args) {
                transitionName += t.toString() + " ";
            }
            transitionName += ")";
            transitionName += "Ret(" + methodSig.getRetType() + ")";
            petrinet.createTransition(transitionName);
        } else { //The method is not static, so it has an extra argument
            transitionName = className + "." + methodname + "(";
            transitionName += className + " ";
            for(Type t : args) {
                transitionName += t.toString() + " ";
            }
            transitionName += ")";
            transitionName += "Ret(" + methodSig.getRetType() + ")";
            petrinet.createTransition(transitionName);

            addPlace(className);
            addFlow(className, transitionName, 1);
        }
        dict.put(transitionName, methodSig); //add signature into map

        for (Type t : args) {
            addPlace(t.toString());
            addFlow(t.toString(), transitionName, 1);
        }

        //add place for the return type
        Type retType = methodSig.getRetType();
        if(retType.toString() != "void") {
            addPlace(retType.toString());
            addFlow(transitionName, retType.toString(), 1);
        } else {
            // TODO Can I do this
            addPlace(className);
            addFlow(transitionName, className, 1);
        }
    }

    public static PetriNet build(List<MethodSignature> result,
                                 Map<String, Set<String>> superClassMap,
                                 Map<String, Set<String>> subClassMap,
                                 List<String> inputs)  throws java.io.IOException{
        getPolymorphismInformation(superClassMap, subClassMap);

        //iterate through each method
        for (MethodSignature k : result) {
            addTransition(k);
        }

        handlePolymorphismAlt();
        for(Transition t : petrinet.getTransitions()) {
            createCopies(t);
        }

        setMaxTokens(inputs);

        Visualization.translate(petrinet);

        // print all transitions
        /*
        for(Transition t : petrinet.getTransitions()) {
            System.out.println(t.getId());
            System.out.println("in:");
            for(Flow in : t.getPresetEdges()) {
                System.out.println(in.getPlace());
            }
            System.out.println("out:");
            for(Flow out: t.getPostsetEdges()) {
                System.out.println(out.getPlace());
            }
            System.out.println();
        }
        */
        System.out.println("Done");
        return petrinet;
    }

}

