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

/*
   An reimplementation of BuildNet, but eliminates all clone edges by creating
   a copy for each method that returns its own arguments.
 */
public class BuildNetWithoutClone {
    static public PetriNet petrinet = new PetriNet("net");
    //map from transition name to a method signature
    static public Map<String, MethodSignature> dict = new HashMap<String, MethodSignature>();

    static private Map<String, List<String>> superDict = new HashMap<>();
    static private Map<String, List<String>> subDict = new HashMap<>();

    public static void main(String[] args) throws java.io.IOException{
        List<String> libs = new ArrayList<>();
        libs.add("lib/point.jar");
        List<MethodSignature> sigs = JarParser.parseJar(libs);
        build(sigs);

        Set<Place> pl = petrinet.getPlaces();
        Set<Transition> tl = petrinet.getTransitions();
        for (Place p : pl) {
            System.out.println(p.toString());
            System.out.println("max token: " + p.getMaxToken());
            //output all methods that take p as an argument
            for (Transition t : tl) {
                try {
                    System.out.println(petrinet.getFlow(p.getId(), t.getId()));
                } catch (NoSuchEdgeException e) {
                }
            }
        }
        for (Transition t : tl) {
            System.out.println(t.toString());
            for (Place p : pl) {
                try {
                    System.out.println(petrinet.getFlow(t.getId(), p.getId()));
                } catch (NoSuchEdgeException e) {
                }
            }
        }

        Visualization.translate(petrinet);
    }

    private static void handlePolymorphism() {
        // This method handles polymorphism by creating methods that transforms each
        // subclass into its super class
        // TODO polymorphism information is currently hardcoded
        List<String> l1 = new ArrayList<>();
        l1.add("cmu.symonster.Shape");

        superDict.put("cmu.symonster.Rectangle", l1);
        superDict.put("cmu.symonster.Triangle", l1);

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

    private static void generatePolymophism(Transition t,
                                            int count,
                                            List<Place> inputs,
                                            List<Place> trueInputs) {
        if(inputs.size() == count) {
            // skip if true inputs is same as inputs
            boolean skip = true;
            for(int i = 0; i < inputs.size(); i++) {
                if(!inputs.get(i).equals(trueInputs.get(i))) {
                    skip = false;
                }
            }
            if(skip) {
                return;
            }

            String newTransitionName = t.getId() + "poly:(";
            for(Place p : trueInputs) {
                newTransitionName += p.getId() + " ";
            }
            newTransitionName += ")";

            if(petrinet.containsTransition(newTransitionName)) {
                return;
            }
            Transition newTransition  = petrinet.createTransition(newTransitionName);
            // Add inputs
            for(Place p : trueInputs) {
                try {
                    Flow f = petrinet.getFlow(p, newTransition);
                    f.setWeight(f.getWeight() + 1);
                } catch (NoSuchEdgeException e) {
                    petrinet.createFlow(p, newTransition, 1);
                }
            }
            // Add outputs but should changes as well....
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
                trueInputs.add(p);
                generatePolymophism(t, count+1, inputs, trueInputs);
                return;
            } else {
                for(String subclass : subClasses) {
                    Place polyClass = petrinet.getPlace(subclass);
                    trueInputs.add(polyClass);
                    generatePolymophism(t, count+1, inputs, trueInputs);
                    trueInputs.remove(polyClass);
                }
                return;
            }
        }
    }
    private static void handlePolymorphismAlt() {
        // Handles polymorphism by creating copies for each method that
        // has superclass as input type
        List<String> l1 = new ArrayList<>();
        l1.add("cmu.symonster.Rectangle");
        l1.add("cmu.symonster.Triangle");
        subDict.put("cmu.symonster.Shape", l1);
        for(Transition t : petrinet.getTransitions()) {
            List<Place> inputs = new ArrayList<>();
            List<Place> outputs = new ArrayList<>();
            Set<Flow> inEdges = t.getPresetEdges();
            Set<Flow> outEdges = t.getPostsetEdges();
            for(Flow f : inEdges) {
                for(int i = 0; i < f.getWeight(); i++) {
                    inputs.add(f.getPlace());
                }
            }
            for(Flow f : outEdges) {
                for(int i = 0; i < f.getWeight(); i++) {
                    outputs.add(f.getPlace());
                }
            }
            List<Place> trueInputs = new ArrayList<>();
            generatePolymophism(t, 0, inputs, trueInputs);
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
                try {
                    Flow f = petrinet.getFlow(newTransition, p);
                    f.setWeight(f.getWeight() + 1);
                } catch (NoSuchEdgeException e) {
                    petrinet.createFlow(newTransition, p, 1);
                }
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
        Set<Flow> inputEdges = petrinet.getPresetEdges(t);
        List<Place> inputs = new ArrayList<>();
        for(Flow e : inputEdges) {
            for(int i = 0 ; i < e.getWeight(); i++) {
                inputs.add(e.getPlace());
            }
        }
        List<Place> outputs = new ArrayList<>();
        generateCopies(t, inputs, outputs);
    }

    public static PetriNet build(List<MethodSignature> result) {
        //create void type
        petrinet.createPlace("void");

        //iterate through each method
        for (MethodSignature k : result) {
            String methodname = k.getName();
            boolean isStatic = k.getIsStatic();
            boolean isConstructor = k.getIsConstructor();
            String className = k.getHostClass().getName();
            //We create two transitions for each method
            //transitionCopy will have its input as its additional output
            String transitionName;
            List<Type> args = k.getArgTypes();

            //adding transition
            if (isConstructor) {
                transitionName = methodname + "(";
                for(Type t : args) {
                    transitionName += t.toString() + " ";
                }
                transitionName += ")";
                petrinet.createTransition(transitionName);
            } else if (isStatic) {
                transitionName = className + "." + methodname + "(";
                for(Type t : args) {
                    transitionName += t.toString() + " ";
                }
                transitionName += ")";
                petrinet.createTransition(transitionName);
            } else { //The method is not static, so it has an extra argument
                transitionName = className + "." + methodname + "(";
                transitionName += k.getHostClass().toString() + " ";
                for(Type t : args) {
                    transitionName += t.toString() + " ";
                }
                transitionName += ")";
                petrinet.createTransition(transitionName);

                //creating the place for the class instance
                try {
                    petrinet.getPlace(k.getHostClass().toString());
                } catch (NoSuchNodeException e) {
                    petrinet.createPlace(k.getHostClass().toString());
                }

                //creating flow from class instance to transition
                try {
                    Flow f = petrinet.getFlow(k.getHostClass().toString(), transitionName);
                    f.setWeight(f.getWeight() + 1);
                } catch (NoSuchEdgeException e) {
                    petrinet.createFlow(k.getHostClass().toString(), transitionName);
                }

            }
            //add method signatures into map
            dict.put(transitionName, k);

            //If method has no argument and is static , create flow with void
            if(args.size() == 0 && (isStatic || isConstructor)) {
                petrinet.createFlow("void", transitionName);
            }

            for (Type t : args) {
                //add place for each argument
                try {
                    petrinet.getPlace(t.toString());
                } catch (NoSuchNodeException e) {
                    petrinet.createPlace(t.toString());
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
            }

            //add flows for the return type
            petrinet.createFlow(transitionName, retType.toString(), 1);

        }

        handlePolymorphismAlt();
        // TODO check the logic here
        for(Transition t : petrinet.getTransitions()) {
            createCopies(t);
        }
        //handlePolymorphism();

        //Set max tokens for each place
        for (Place p : petrinet.getPlaces()) {
            int count = 0;
            for (Transition t : petrinet.getTransitions()) {
                try {
                    Flow f = petrinet.getFlow(p.getId(), t.getId());
                    count = Math.max(count, f.getWeight());
                } catch (NoSuchEdgeException e) {
                    continue;
                }
            }
            if(count != 0) {
                p.setMaxToken(count);
            } else {
                p.setMaxToken(1);
            }
        }
        return petrinet;
    }
}
