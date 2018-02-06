package edu.cmu.reachability;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import java.util.HashMap;
import java.util.Set;

public interface Encoding {
		
	// Maps the pair <transition in the petri-net, timestamp> to variable
	HashMap<Pair<Transition, Integer>,Variable> transition2variable = new HashMap<>();
	
	// Maps the triple <place in the petri-net, timestamp, value> to variale
	HashMap<Triple<Place, Integer, Integer>,Variable> place2variable = new HashMap<>();
	
	SATSolver solver = new SATSolver();
	
	public void setState(Set<Pair<Place, Integer>> state, int timestep);
	
	public void createConstraints();
	
	public void createVariables();
}
