package edu.cmu.petrinet;
<<<<<<< HEAD
import edu.cmu.parser.JarParser;
import edu.cmu.petrinet.Visualization;
=======
<<<<<<< HEAD
=======
import edu.cmu.parser.JarParser;
import edu.cmu.petrinet.Visualization;
>>>>>>> 0aab3bd41aa47af091127a4942197760d0115e3f
>>>>>>> b88dd56f0bfa223bd4d54c79d7b2cb35e1f6e80e
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
import java.util.HashMap;
import java.util.Map;

public class BuildNetNoVoid {
    static public PetriNet petrinet = new PetriNet("net");
    // A map from transition name to a method signature
    static public Map<String, MethodSignature> dict = new HashMap<String, MethodSignature>();

    static private Map<String, List<String>> superDict = new HashMap<>();

    // This method handles polymorphism by creating methods that transforms each
    // subclass into its super class
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
    }


    private static void addPlace(String placeID) {
        try {
            petrinet.getPlace(placeID);
        } catch (NoSuchNodeException e) {
            petrinet.createPlace(placeID);
            petrinet.createTransition(placeID + "Clone");
            petrinet.createFlow(placeID, placeID + "Clone", 1);
            petrinet.createFlow(placeID + "Clone", placeID, 2);
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
            transitionName += methodSig.getRetType();
            petrinet.createTransition(transitionName);
        }
        else if (isStatic) {
            transitionName =  "(static)" + className + "." + methodname + "(";
            for(Type t : args) {
                transitionName += t.toString() + " ";
            }
            transitionName += ")";
            transitionName += methodSig.getRetType();
            petrinet.createTransition(transitionName);
        } else { //The method is not static, so it has an extra argument
            transitionName = className + "." + methodname + "(";
            transitionName += className + " ";
            for(Type t : args) {
                transitionName += t.toString() + " ";
            }
            transitionName += ")";
            transitionName += methodSig.getRetType();
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

    public static PetriNet build(List<MethodSignature> result,
                                 Map<String, Set<String>> superClassMap,
                                 Map<String, Set<String>> subClassMap,
                                 List<String> inputs
                                 ) throws java.io.IOException {

        getPolymorphismInformation(superClassMap, subClassMap);

        for (MethodSignature k : result) {
            addTransition(k);
        }

        handlePolymorphism();

        setMaxTokens(inputs);

        Visualization.translate(petrinet);

        /*
        for(Place p : petrinet.getPlaces()) {
            System.out.println(p.toString() + " " + p.getMaxToken());
        }
        */

        return petrinet;
    }
}