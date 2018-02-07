package edu.cmu.petrinet;

import edu.cmu.parser.JarParser;
import edu.cmu.parser.MethodSignature;
import soot.Type;

import uniol.apt.adt.exception.NoSuchEdgeException;
import uniol.apt.adt.exception.NoSuchNodeException;
import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Transition;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BuildNet {
    protected static PetriNet petrinet = new PetriNet("net");

    public static void main(String[] args) {
        build();
        Set<Transition> pl = petrinet.getTransitions();
        for (Transition p : pl) {
            System.out.println(p.getId());
        }
    }

    public static void build () {
        List<String> libs = new ArrayList<>();
        libs.add("../benchmarks/examples/point/point.jar");
        List<MethodSignature> result = JarParser.parseJar(libs);

        for (MethodSignature k : result) {
            //add transition
            String methodname = k.getName();
            boolean isStatic = k.getIsStatic();
            String className = k.getHostClass().getName();
            String transitionName;
            if (isStatic) {
                transitionName = className + " -static-" + methodname;
            } else {
                transitionName = className + " -nonStatic-" + methodname;
            }
            petrinet.createTransition(transitionName);

            List<Type> args = k.getArgTypes();
            for (Type t : args) {
                //add place
                try {
                    petrinet.getPlace(t.toString());
                } catch (NoSuchNodeException e) {
                    petrinet.createPlace(t.toString());
                    //add clone transition
                    petrinet.createTransition(t.toString() + "Clone");
                    petrinet.createFlow(t.toString(), t.toString() + "Clone", 1);
                    petrinet.createFlow(t.toString() + "Clone", t.toString(), 2);
                }

                //add flow
                try {
                    Flow originalFlow = petrinet.getFlow(t.toString(), methodname);
                    originalFlow.setWeight(originalFlow.getWeight() + 1);
                } catch (NoSuchEdgeException e) {
                    petrinet.createFlow(t.toString(), methodname, 1);
                }
            }

            Type retType = k.getRetType();
            //add place
            try {
                petrinet.getPlace(retType.toString());
            } catch (NoSuchNodeException e) {
                petrinet.createPlace(retType.toString());
                //add clone transition
                petrinet.createTransition(retType.toString() + "Clone");
                petrinet.createFlow(retType.toString(), retType.toString() + "Clone", 1);
                petrinet.createFlow(retType.toString() + "Clone", retType.toString(), 2);
            }
            //add flow
            petrinet.createFlow(methodname, retType.toString(), 1);
        }
    }


}
