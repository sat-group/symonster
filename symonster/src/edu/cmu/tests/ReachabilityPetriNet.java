package edu.cmu.tests;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import edu.cmu.reachability.Encoding;
import edu.cmu.reachability.SequentialEncoding;
import edu.cmu.reachability.Variable;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;

public class ReachabilityPetriNet {
	
	private static Set<Pair<Place, Integer>> setInitialState(PetriNet pnet){

		// Initial state
		Place placeMyPoint = pnet.getPlace("MyPoint");
		Place placeVoid = pnet.getPlace("void");
		
		HashSet<Pair<Place,Integer>> initial = new HashSet<>();
		initial.add(new ImmutablePair<Place,Integer>(placeMyPoint, 1));
		initial.add(new ImmutablePair<Place,Integer>(placeVoid, 1));
		
		return initial;
		
	}
	
	private static Set<Pair<Place, Integer>> setGoalState(PetriNet pnet){

		// Final state
		Place placeMyPoint = pnet.getPlace("Point");
		
		HashSet<Pair<Place,Integer>> initial = new HashSet<>();
		initial.add(new ImmutablePair<Place,Integer>(placeMyPoint, 1));
		
		return initial;
		
	}
	
	public static void main(String[] args){
		
		PointPetriNet example = new PointPetriNet();
		example.buildPointPetriNet();
		
		int loc = 4;
		SequentialEncoding encoding = new SequentialEncoding(example.getPetriNet(), loc);
		encoding.createVariables();
		encoding.createConstraints();
		
		encoding.setState(setInitialState(example.getPetriNet()), 0);
		encoding.setState(setGoalState(example.getPetriNet()), loc);
		
		
		List<Variable> result = Encoding.solver.findPath();
		int itn = 0;
		while (!result.isEmpty()){
			itn++;
			String res = "Solution #" + itn + " =\n";
			for (Variable s : result){
				res += s.toString() + "\n";
			}
			System.out.println(res);
			result = Encoding.solver.findPath();
		}
		System.out.println("Solutions found = " + itn);
		
	}
	
}
