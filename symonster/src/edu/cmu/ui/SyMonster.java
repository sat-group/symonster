package edu.cmu.ui;
import edu.cmu.codeformer.CodeFormer;
import edu.cmu.compilation.Test;
import edu.cmu.parser.JarParser;
import edu.cmu.parser.MethodSignature;
import edu.cmu.petrinet.BuildNet;
import edu.cmu.reachability.Encoding;
import edu.cmu.reachability.SequentialEncoding;
import edu.cmu.reachability.Variable;
import edu.cmu.tests.PointPetriNet;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.sat4j.specs.TimeoutException;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
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
	
	public static void main(String[] args) throws IOException {
		
		Test test = new Test();
		
		// 1. Read input from the user
		// TODO: read from .json file instead of using predefined values
        // TODO: fake
        List<String> varNames = new ArrayList<>();
        varNames.add("point1");
        varNames.add("point2");
        String methodName = "conv";
        List<String> libs = new ArrayList<>();
        libs.add("../benchmarks/examples/point/point.jar");

		// 2. Parse library
		// TODO: use the code to parse the library here
        List<MethodSignature> sigs = JarParser.parseJar(libs);

        // 3. build a petrinet and signatureMap of library
		BuildNet b = new BuildNet();
		PetriNet net = b.build(sigs);
		Map<String, MethodSignature> signatureMap = b.dict;

		//example petrinet
		PointPetriNet example = new PointPetriNet();
		example.buildPointPetriNet();
		PetriNet pointNet = example.getPetriNet();

		int loc = 1;
		int paths = 0;
		int programs = 0;
		boolean solution = false;
		
		while (!solution) {
			//System.out.println("loc = " + loc);
			// create a formula that has the same semantics as the petri-net
			Encoding encoding = new SequentialEncoding(net, loc);
			
			// set initial state and final state
			encoding.setState(setInitialState(net), 0);
			encoding.setState(setGoalState(net), loc);

			// 4. Perform reachability analysis
			
			// for each loc find all possible programs
			List<Variable> result = Encoding.solver.findPath();
            while(!result.isEmpty() && !solution){
				paths++;
				String path = "Path #" + paths + " =\n";
				List<String> apis  = new ArrayList<String>();
				//A list of method signatures
				List<MethodSignature> signatures = new ArrayList<>();
				for (Variable s : result) {
					apis.add(s.getName());
					path += s.toString() + "\n";
					MethodSignature sig = signatureMap.get(s.getName());
					if(sig != null) {
						signatures.add(signatureMap.get(s.getName()));
					}
				}
				//System.out.println(path);
				//System.out.println("Signatures:");
				for (MethodSignature sig : signatures) {
					//System.out.println(sig);
				}
                //System.out.println(signatures);
                // 5. Convert a path to a program
				// NOTE: one path may correspond to multiple programs and we may need a loop here!

                boolean sat = true;
                while (sat){
                    System.out.println(signatures);
                    //TODO Replace the null pointers with inputs/output types
                    List<String> inputs = new ArrayList<>();
                    inputs.add("cmu.symonster.MyPoint");
                    inputs.add("cmu.symonster.Point");
                    CodeFormer former = new CodeFormer(signatures,inputs,"cmu.symonster.Point", varNames, methodName);
                    String code;
                    try {
                        code = former.solve();
                    } catch (TimeoutException e) {
                        sat = false;
                        break;
                    }
                    sat = !former.isUnsat();
                    programs++;
                    // 6. Run the test cases
                    // TODO: write this code; if all test cases pass then we can terminate
                    if (test.runTest(code)) {
                        solution = true;
                        System.out.println("Programs explored = " + programs);
                        break;
                    }
                }
				// the current path did not result in a program that passes all test cases
				// find the next path
				result = Encoding.solver.findPath();
			}
			
			// we did not find a program of length = loc
			loc++;
		}

	}

    private static void readFromJson() {

    }

}
