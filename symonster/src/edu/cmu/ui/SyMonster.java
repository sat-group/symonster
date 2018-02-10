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
import edu.cmu.utils.TimerUtils;
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
	
	private static Set<Pair<Place, Integer>> setInitialState(PetriNet pnet, List<String> inputs){
		// Initial state
		HashSet<Pair<Place,Integer>> initial = new HashSet<>();
		for (String input : inputs) {
		    Place p = pnet.getPlace(input);
			initial.add(new ImmutablePair<Place, Integer>(p, 1));
		}
		Set<Place> ps = pnet.getPlaces();
		for (Place p : ps) {
			boolean isInput = false;
			for (String input : inputs) {
				if (p.getId().equals(input)) {
				    isInput = true;
				}
			}
			if(!isInput) {
				initial.add(new ImmutablePair<Place, Integer>(p, 0));
			}
		}
		// Initial state
		/*
		Place placeMyPoint = pnet.getPlace("cmu.symonster.MyPoint");
		Place placePoint = pnet.getPlace("cmu.symonster.Point");
		Place placeVoid = pnet.getPlace("void");
		Place placeInt = pnet.getPlace("int");

		HashSet<Pair<Place,Integer>> initial = new HashSet<>();
		initial.add(new ImmutablePair<Place,Integer>(placeMyPoint, 1));
		initial.add(new ImmutablePair<Place,Integer>(placeVoid, 1));
		initial.add(new ImmutablePair<Place,Integer>(placePoint, 1));
		initial.add(new ImmutablePair<Place,Integer>(placeInt, 0));
		*/

		return initial;
	}
	
	private static Set<Pair<Place, Integer>> setGoalState(PetriNet pnet, String retType){

		// Final state
		HashSet<Pair<Place,Integer>> initial = new HashSet<>();
		Set<Place> pl = pnet.getPlaces();
		for(Place p : pl){
		    if(p.getId().equals("void")) {
		        continue;
			} else if (p.getId().equals(retType)) {
		        initial.add(new ImmutablePair<Place, Integer>(p, 1));
			} else {
		    	initial.add(new ImmutablePair<Place, Integer>(p, 0));
			}
		}
		/*
		// Final state
		Place placeMyPoint = pnet.getPlace("cmu.symonster.MyPoint");
		Place placePoint = pnet.getPlace("cmu.symonster.Point");
		Place placeInt = pnet.getPlace("int");

		// if you do not want any restrictions on a place then do not add it to the list
		HashSet<Pair<Place,Integer>> initial = new HashSet<>();
		initial.add(new ImmutablePair<Place,Integer>(placeMyPoint, 0));
		initial.add(new ImmutablePair<Place,Integer>(placePoint, 1));
		initial.add(new ImmutablePair<Place,Integer>(placeInt, 0));
		*/

		return initial;
	}
	
	public static void main(String[] args) throws IOException {
		
		Test test = new Test();
		
		// 1. Read input from the user
		// TODO: read from .json file instead of using predefined values
        // TODO: fake
        List<String> varNames = new ArrayList<>();
        varNames.add("p");
        String methodName = "conv";
        List<String> libs = new ArrayList<>();
        libs.add("lib/point.jar");
		List<String> inputs = new ArrayList<>();
        inputs.add("cmu.symonster.MyPoint");
		String retType = "cmu.symonster.Point";
		String testCode = "    public boolean pass(){\n" +
                "        if (conv(new cmu.symonster.MyPoint(20,30)).getX()== 20 && conv(new cmu.symonster.MyPoint(20,30)).getY()==30)\n" +
                "            return true;\n" +
                "        else return false;\n" +
                "    }";
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

        TimerUtils.startTimer("total");

		while (!solution) {
			// create a formula that has the same semantics as the petri-net
			Encoding encoding = new SequentialEncoding(net, loc);
			
			// set initial state and final state
			encoding.setState(setInitialState(net, inputs),  0);
			encoding.setState(setGoalState(net, retType),  loc);

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

                // 5. Convert a path to a program
				// NOTE: one path may correspond to multiple programs and we may need a loop here!

                boolean sat = true;
				CodeFormer former = new CodeFormer(signatures,inputs,retType, varNames, methodName);
                while (sat){
                    //TODO Replace the null pointers with inputs/output types
                    String code;
                    try {
                        code = former.solve();
                    } catch (TimeoutException e) {
                        sat = false;
                        break;
                    }
                    sat = !former.isUnsat();
                    programs++;
                    if (programs % 10000 == 0)
                    {
                        System.out.println("programs: "+programs);
                        System.out.println(signatures);
                        System.out.println("n signatures: "+ signatures.size());
                        System.out.println(code);
                        System.out.println();
                    }


                    // 6. Run the test cases
                    // TODO: write this code; if all test cases pass then we can terminate
                    if (test.runTest(code,testCode)) {
                        solution = true;
                        System.out.println("Programs explored = " + programs);
                        System.out.println("code:");
                        System.out.println(code);
                        System.out.println("total time: "+TimerUtils.getCumulativeTime("total"));
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
