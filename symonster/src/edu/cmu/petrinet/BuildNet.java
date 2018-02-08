package edu.cmu.petrinet;

import edu.cmu.parser.JarParser;
import edu.cmu.parser.MethodSignature;
import org.jboss.util.Null;
import soot.Type;

import uniol.apt.adt.exception.NoSuchEdgeException;
import uniol.apt.adt.exception.NoSuchNodeException;
import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;

public class BuildNet {
    static public PetriNet petrinet = new PetriNet("net");
    //map from transition name to a method signature
    static public Map<String, MethodSignature> dict = new HashMap<String, MethodSignature>();

    public static void main(String[] args) {
        build();
        //output all transitions in petrinet
        Set<Transition> pl = petrinet.getTransitions();
        for (Transition p : pl) {
            try {
                System.out.println((dict.get(p.getId())).toString());
            } catch (NullPointerException e) {
                System.out.println(p.toString());
            }
        }
    }

    public static PetriNet build () {
        List<String> libs = new ArrayList<>();
        libs.add("../benchmarks/examples/point/point.jar");
        List<MethodSignature> result = JarParser.parseJar(libs);

        //create void type
        petrinet.createPlace("void");
        petrinet.createTransition("voidClone");
        petrinet.createFlow("void", "voidClone", 1);
        petrinet.createFlow("voidClone", "void", 2);
        for (MethodSignature k : result) {
            //add transition
            String methodname = k.getName();
            boolean isStatic = k.getIsStatic();
            String className = k.getHostClass().getName();
            String transitionName;
            if (isStatic) {
                transitionName = className + "-static-" + methodname;
            } else {
                transitionName = className + "-nonStatic-" + methodname;
            }
            petrinet.createTransition(transitionName);
            dict.put(transitionName, k); //add signature into map

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
                    Flow originalFlow = petrinet.getFlow(t.toString(), transitionName);
                    originalFlow.setWeight(originalFlow.getWeight() + 1);
                } catch (NoSuchEdgeException e) {
                    petrinet.createFlow(t.toString(), transitionName, 1);
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
            petrinet.createFlow(transitionName, retType.toString(), 1);
        }




        /*
        Since we currently can not parse type MyPoint and point, method MyPoint() and Point()
        I will hardcode these nodes into the graph for testing purpose :)
         */
        petrinet.createPlace("MyPoint");
        petrinet.createTransition("MyPointClone");
        petrinet.createFlow("MyPoint", "MyPointClone", 1);
        petrinet.createFlow("MyPointClone", "MyPoint", 2);

        petrinet.createPlace("Point");
        petrinet.createTransition("PointClone");
        petrinet.createFlow("Point", "PointClone", 1);
        petrinet.createFlow("PointClone", "Point", 2);

        petrinet.createTransition("MyPoint()");
        petrinet.createTransition("Point()");

        //flows for MyPoint
        petrinet.createFlow("MyPoint", "cmu.symonster.MyPoint-nonStatic-getX", 1);
        petrinet.createFlow("MyPoint", "cmu.symonster.MyPoint-nonStatic-getY", 1);
        petrinet.createFlow("MyPoint()", "MyPoint", 1);

        //flows for MyPoint()
        petrinet.createFlow("int", "MyPoint()", 2);

        //flows for Point
        petrinet.createFlow("Point", "cmu.symonster.Point-nonStatic-setX", 1);
        petrinet.createFlow("Point", "cmu.symonster.Point-nonStatic-setY", 1);
        petrinet.createFlow("Point()", "Point", 1);

        //flows for Point()
        petrinet.createFlow("void", "Point()");


        //set max tokens for each of the types
        petrinet.getPlace("MyPoint").setMaxToken(2);
        petrinet.getPlace("Point").setMaxToken(2);
        petrinet.getPlace("int").setMaxToken(3);
        petrinet.getPlace("void").setMaxToken(2);
        return petrinet;
    }
}
