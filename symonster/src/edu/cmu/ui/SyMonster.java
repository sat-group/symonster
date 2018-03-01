package edu.cmu.ui;
import edu.cmu.codeformer.CodeFormer;
import edu.cmu.compilation.Test;
import edu.cmu.parser.JarParser;
import edu.cmu.parser.MethodSignature;
import edu.cmu.petrinet.BuildNet;
import edu.cmu.petrinet.BuildNetWithoutClone;
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
		// Initiaol state
		HashSet<Pair<Place,Integer>> initial = new HashSet<>();
		HashMap<Place, Integer> count = new HashMap<Place, Integer>();
		// Count the number of inputs
		for (String input : inputs) {
			Place p = pnet.getPlace(input);
			if (count.containsKey(p)) {
				count.put(p, count.get(p) + 1);
			} else {
				count.put(p, 1);
			}
		}
		// Add inputs into initial state
		for(Place key : count.keySet()) {
			initial.add(new ImmutablePair<Place, Integer>(key, count.get(key)));
		}


		//Add non-input places into initial states
		Set<Place> ps = pnet.getPlaces();
		for (Place p : ps) {
			boolean isInput = false;
			for (String input : inputs) {
				if (p.getId().equals(input)) {
					isInput = true;
				}
			}
			if(p.getId().equals("void")) {
				initial.add(new ImmutablePair<Place, Integer>(p, 1));
			}
			else if(!isInput) {
				initial.add(new ImmutablePair<Place, Integer>(p, 0));
			}
		}
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
		return initial;
	}
	
	public static void main(String[] args) throws IOException {
		
		Test test = new Test();
		
		// 1. Read input from the user
		// TODO: read from .json file instead of using predefined values
        // TODO: fake
        List<String> varNames = new ArrayList<>();
        String methodName = "conv";
        List<String> libs = new ArrayList<>();
        libs.add("lib/simplePoint.jar");
		List<String> inputs = new ArrayList<>();
        //inputs.add("int");
        //varNames.add("x");
        //inputs.add("int");
        //varNames.add("y");
        inputs.add("cmu.symonster.MyPoint");
        varNames.add("p");

        String retType = "cmu.symonster.Point";
		String testCode = "    public boolean pass(){\n" +
                "       cmu.symonster.MyPoint p = new cmu.symonster.MyPoint(15,20); " +
                "       return conv(p).getX() == 15 && conv(p).getY() == 20;\n"+
                "    }";
		// 2. Parse library
		// TODO: use the code to parse the library here
        List<MethodSignature> sigs = JarParser.parseJar(libs);
        // 3. build a petrinet and signatureMap of library
        // Currently built without clone edges
		BuildNetWithoutClone b = new BuildNetWithoutClone();
		PetriNet net = b.build(sigs);
		Map<String, MethodSignature> signatureMap = b.dict;

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
			System.out.println(result.size());
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
					if(sig != null) { //check if s is a line of a code
						signatures.add(sig);
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
                    if (programs % 50 == 0)
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
                        TimerUtils.stopTimer("total");
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
