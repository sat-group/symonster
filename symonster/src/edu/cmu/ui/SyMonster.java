package edu.cmu.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import edu.cmu.compilation.Test;
import edu.cmu.reachability.Encoding;
import edu.cmu.reachability.SequentialEncoding;
import edu.cmu.reachability.Variable;
import edu.cmu.tests.PointPetriNet;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;

public class SyMonster {
	
	private static Set<Pair<Place, Integer>> setInitialState(PetriNet pnet){

		// Initial state
		Place placeMyPoint = pnet.getPlace("MyPoint");
		Place placePoint = pnet.getPlace("Point");
		Place placeVoid = pnet.getPlace("void");
		Place placeInt = pnet.getPlace("int");
		
		HashSet<Pair<Place,Integer>> initial = new HashSet<>();
		initial.add(new ImmutablePair<Place,Integer>(placeMyPoint, 1));
		initial.add(new ImmutablePair<Place,Integer>(placeVoid, 1));
		initial.add(new ImmutablePair<Place,Integer>(placePoint, 0));
		initial.add(new ImmutablePair<Place,Integer>(placeInt, 0));
		
		return initial;
		
	}
	
	private static Set<Pair<Place, Integer>> setGoalState(PetriNet pnet){

		// Final state
		Place placeMyPoint = pnet.getPlace("MyPoint");
		Place placePoint = pnet.getPlace("Point");
		Place placeInt = pnet.getPlace("int");

		// if you do not want any restrictions on a place then do not add it to the list
		HashSet<Pair<Place,Integer>> initial = new HashSet<>();
		initial.add(new ImmutablePair<Place,Integer>(placeMyPoint, 0));
		initial.add(new ImmutablePair<Place,Integer>(placePoint, 1));
		initial.add(new ImmutablePair<Place,Integer>(placeInt, 0));
		
		return initial;
		
	}
	
	public static void main(String[] args){
		
		Test test = new Test();
		
		// 1. Read input from the user
		// TODO: read from .json file instead of using predefined values
		
		// 2. Parse library
		// TODO: use the code to parse the library here
		
		// 3. Build petri-net
		// TODO: use the code for building the petri-net here
		
		PointPetriNet example = new PointPetriNet();
		example.buildPointPetriNet();
		
		int loc = 1;
		int paths = 0;
		int programs = 0;
		boolean solution = false;
		
		while (!solution) {
			System.out.println("loc = " + loc);
			// create a formula that has the same semantics as the petri-net
			Encoding encoding = new SequentialEncoding(example.getPetriNet(), loc);
			
			// set initial state and final state
			encoding.setState(setInitialState(example.getPetriNet()), 0);
			encoding.setState(setGoalState(example.getPetriNet()), loc);

			// 4. Perform reachability analysis
			
			// for each loc find all possible programs
			List<Variable> result = Encoding.solver.findPath();
			while(!result.isEmpty() || solution){
				paths++;
				String path = "Path #" + paths + " =\n";
				List<String> apis  = new ArrayList<String>();
				for (Variable s : result) {
					apis.add(s.getName());
					path += s.toString() + "\n";
				}
				System.out.println(path);
				
				// 5. Convert a path to a program
				// TODO: write this code
				// NOTE: one path may correspond to multiple programs and we may need a loop here!
				programs++;
				
				// 6. Run the test cases
				// TODO: write this code; if all test cases pass then we can terminate
				if (test.runTest(apis)) {
					solution = true;
					System.out.println("Programs explored = " + programs);
					break;
				}
				
				// the current path did not result in a program that passes all test cases
				// find the next path
				result = Encoding.solver.findPath();
			}
			
			// we did not find a program of length = loc
			loc++;
		}
	}

}
