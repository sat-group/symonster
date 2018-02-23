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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.io.BufferedWriter;
import java.io.FileWriter;
import edu.cmu.petrinet.BuildNet;

public class Visualization {
    public static void main(String[] args) throws IOException{
        List<String> libs = new ArrayList<>();
        libs.add("lib/point.jar");
        List<MethodSignature> sigs = JarParser.parseJar(libs);
        System.out.println(sigs);
        PetriNet net = BuildNet.build(sigs);
        translate(net);
    }
    public static void translate(PetriNet net) throws java.io.IOException{
        Set<Place> ps = net.getPlaces();
        Set<Transition> ts = net.getTransitions();

        String str = "digraph " + net.getName() + "{\n";
        BufferedWriter writer = new BufferedWriter(new FileWriter(net.getName() + "visualization"));
        writer.write(str);

        for (Place p : ps) {
            for(Transition t : ts) {
                try {
                    Flow f = net.getFlow(p.getId(), t.getId());
                    str = "\""+p.getId()+"\"" + "->" +  "\""+t.getId()+"\""  + "[label=\"" + f.getWeight() + "\"]\n";
                    writer.write(str);
                } catch (NoSuchEdgeException e) {}
            }
        }
        for (Transition t : ts) {
            for (Place p : ps) {
                try {
                    Flow f = net.getFlow(t.getId(), p.getId());
                    str =  "\""+t.getId()+"\"" + "->" + "\""+p.getId()+"\"" + "[label=\"" + f.getWeight() + "\"]\n";
                    writer.write(str);
                } catch (NoSuchEdgeException e) {}
            }
        }
        writer.write("}");
        writer.close();
    }
}
