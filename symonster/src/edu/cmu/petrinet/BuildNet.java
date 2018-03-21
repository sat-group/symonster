package edu.cmu.petrinet;

import edu.cmu.parser.JarParser;
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

public class BuildNet {
    static public PetriNet petrinet = new PetriNet("net");
    // A map from transition name to a method signature
    static public Map<String, MethodSignature> dict = new HashMap<String, MethodSignature>();

    // TODO polymorphism information is currently hardcoded
    // A map that encodes the polymorphism
    static private Map<String, List<String>> superDict = new HashMap<>();

    private static void handlePolymorphism() {
        // This method handles polymorphism by creating methods that transforms each
        // subclass into its super class
        for(String subClass : superDict.keySet()) {
            for (String superClass : superDict.get(subClass)) {
                assert (petrinet.containsNode(subClass));
                assert (petrinet.containsNode(superClass));
                String methodName = subClass + "=" + superClass;
                petrinet.createTransition(methodName);
                petrinet.createFlow(subClass, methodName);
                petrinet.createFlow(methodName, superClass);
            }
        }
    }

    private static void handlePolyorphismAlt(MethodSignature k) {
        // This method handles polymorphism by creating altered version for
        // each existing method that work on polymorphic types
        // TODO polymorphism information is currently hardcoded
        // TODO First check if method is non-static, if so, add an argument

    }

    public static PetriNet build(List<MethodSignature> result,
                                 Map<String, Set<String>> superClassMap,
                                 Map<String, Set<String>> subClassMap) {
        // Create polymorphism dicts
        for(String s : superClassMap.keySet()) {
            Set<String> superClasses = superClassMap.get(s);
            List<String> li = new ArrayList<>(superClasses);
            if(li.size() != 0) {
                superDict.put(s, li);
            }
        }

        //create void type
        petrinet.createPlace("void");
        petrinet.createTransition("voidClone");
        petrinet.createFlow("void", "voidClone", 1);
        petrinet.createFlow("voidClone", "void", 2);

        //iterate through each method
        for (MethodSignature k : result) {
            String methodname = k.getName();
            boolean isStatic = k.getIsStatic();
            boolean isConstructor = k.getIsConstructor();
            String className = k.getHostClass().getName();
            String transitionName;

            List<Type> args = k.getArgTypes();
            //adding transition
            if(isConstructor) {
                transitionName = methodname + "(";
                for(Type t : args) {
                    transitionName += t.toString() + " ";
                }
                transitionName += ")";
                petrinet.createTransition(transitionName);
            }
            else if (isStatic) {
                transitionName =  className + "." + methodname + "(";
                for(Type t : args) {
                    transitionName += t.toString() + " ";
                }
                transitionName += ")";
                petrinet.createTransition(transitionName);
            } else { //The method is not static, so it has an extra argument
                transitionName = className + "." + methodname + "(";
                for(Type t : args) {
                    transitionName += t.toString() + " ";
                }
                transitionName += ")";
                petrinet.createTransition(transitionName);

                //creating the place and flow for class instance
                Flow f;
                try {
                    petrinet.getPlace(k.getHostClass().toString());
                } catch (NoSuchNodeException e) {
                    petrinet.createPlace(k.getHostClass().toString());
                    petrinet.createTransition(k.getHostClass().toString() + "Clone");
                    petrinet.createFlow(k.getHostClass().toString(), k.getHostClass().toString() + "Clone", 1);
                    petrinet.createFlow(k.getHostClass().toString() + "Clone", k.getHostClass().toString(), 2);
                }
                try {
                    f = petrinet.getFlow(k.getHostClass().toString(), transitionName);
                    f.setWeight(f.getWeight() + 1);
                } catch (NoSuchEdgeException e) {
                    petrinet.createFlow(k.getHostClass().toString(), transitionName);
                }
            }
            dict.put(transitionName, k); //add signature into map

            //Add a edge from void if the method has no argument and is static
            if(args.size() == 0 && (isStatic || isConstructor)) {
                petrinet.createFlow("void", transitionName);
            }

            for (Type t : args) {
                //add place for each argument
                try {
                    petrinet.getPlace(t.toString());
                } catch (NoSuchNodeException e) {
                    petrinet.createPlace(t.toString());
                    //add clone transition
                    petrinet.createTransition(t.toString() + "Clone");
                    petrinet.createFlow(t.toString(), t.toString() + "Clone", 1);
                    petrinet.createFlow(t.toString() + "Clone", t.toString(), 2);
                }

                //add flow for each argument
                try {
                    Flow originalFlow = petrinet.getFlow(t.toString(), transitionName);
                    originalFlow.setWeight(originalFlow.getWeight() + 1);
                } catch (NoSuchEdgeException e) {
                    petrinet.createFlow(t.toString(), transitionName, 1);
                }
            }

            //add place for the return type
            Type retType = k.getRetType();
            try {
                petrinet.getPlace(retType.toString());
            } catch (NoSuchNodeException e) {
                petrinet.createPlace(retType.toString());
                //add clone transition
                petrinet.createTransition(retType.toString() + "Clone");
                petrinet.createFlow(retType.toString(), retType.toString() + "Clone", 1);
                petrinet.createFlow(retType.toString() + "Clone", retType.toString(), 2);
            }
            //add flow for the return type
            petrinet.createFlow(transitionName, retType.toString(), 1);
        }


        // Create methods that handle polymorphism
        handlePolymorphism();

        //Set max tokens for each place
        for (Place p : petrinet.getPlaces()) {
            int count = 0;
            for(Transition t : petrinet.getTransitions()) {
                try {
                    Flow f = petrinet.getFlow(p.getId(), t.getId());
                    count = Math.max(count, f.getWeight()+1);
                } catch (NoSuchEdgeException e) {
                    continue;
                }
            }
            p.setMaxToken(count);
        }
        return petrinet;
    }
}