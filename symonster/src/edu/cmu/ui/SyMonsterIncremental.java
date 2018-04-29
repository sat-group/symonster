package edu.cmu.ui;
import edu.cmu.codeformer.CodeFormer;
import edu.cmu.compilation.Test;
import edu.cmu.parser.*;
import edu.cmu.petrinet.BuildNet;
import edu.cmu.petrinet.BuildNetNoVoidClone;
import edu.cmu.reachability.*;
import edu.cmu.utils.TimerUtils;
import uniol.apt.adt.pn.PetriNet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import org.sat4j.specs.TimeoutException;

public class SyMonsterIncremental {
	public static void main(String[] args) throws IOException {
		// 0. Read config
		SymonsterConfig jsonConfig = JsonParser.parseJsonConfig("config/config.json");
		Set<String> acceptableSuperClasses = new HashSet<>();
		acceptableSuperClasses.addAll(jsonConfig.acceptableSuperClasses);

		// 1. Read input from the user
		SyMonsterInput jsonInput;
		if (args.length == 0) {
			System.out.println("Please use the program args next time.");
			//jsonInput = JsonParser.parseJsonInput("untested/tests/1/test1.json");
			jsonInput = JsonParser.parseJsonInput("untested/simplepoint/convert.json");
		}
		else{
			jsonInput = JsonParser.parseJsonInput(args[0]);
		}

		String methodName = jsonInput.methodName;
		List<String> libs = jsonInput.libs;
		List<String> inputs = jsonInput.srcTypes;

		List<String> varNames = jsonInput.paramNames;
		String retType = jsonInput.tgtType;
		File file = new File(jsonInput.testPath);
		BufferedReader br = new BufferedReader(new FileReader(file));
		StringBuilder fileContents = new StringBuilder();
		String line = br.readLine();
		while (line != null) {
			fileContents.append(line);
			line = br.readLine();
		}
		String testCode = fileContents.toString();        
		System.out.println(0);
		// 2. Parse library
		List<MethodSignature> sigs = JarParser.parseJar(libs,jsonInput.packages,jsonConfig.blacklist);
		System.out.println(sigs);
		Map<String,Set<String>> superclassMap = JarParser.getSuperClasses(acceptableSuperClasses);
		Map<String,Set<String>> subclassMap = new HashMap<>();
		for (String key : superclassMap.keySet()){
			for (String value :superclassMap.get(key)){
				if (!subclassMap.containsKey(value)){
					subclassMap.put(value,new HashSet<String>());
				}
				subclassMap.get(value).add(key);
			}
		}


		// 3. build a petrinet and signatureMap of library
		// Currently built without clone edges
		Map<String, MethodSignature> signatureMap;
		boolean copyPoly = true;

		BuildNetNoVoidClone b = new BuildNetNoVoidClone();
		PetriNet net = b.build(sigs, superclassMap, subclassMap, inputs, copyPoly);
		signatureMap = b.dict;

		int loc = 1;
		int paths = 0;
		boolean solution = false;

		boolean incremental = false;
		Encoding encoding = null;

		TimerUtils.startTimer("total");
		if (incremental) {
			encoding = new IncrementalEncoding(net);
			encoding.setState(EncodingUtil.setInitialState(net, inputs), 0);
		}

		int limit = 7;
		while (!solution && loc < limit) {
			// create a formula that has the same semantics as the petri-net
			if (incremental) {
				List<Integer> fstate  = encoding.getFState(EncodingUtil.setGoalState(net, retType), loc);
				// for each loc find all possible programs
				Encoding.solver.setFState(fstate);


			} else {
				encoding = new SequentialEncoding(net, loc);                     // Set encoding
				// set initial state and final state
				encoding.setState(EncodingUtil.setInitialState(net, inputs),  0);
				encoding.setState(EncodingUtil.setGoalState(net, retType),  loc);
			}

			// 4. Perform reachability analysis

			// for each loc find all possible programs
			//if (loc >= 4) {
			//encoding.setState(EncodingUtil.setGoalState(net, retType),  loc);
			List<Variable> result = Encoding.solver.findPath(loc);
            while(!result.isEmpty() && !solution){
                TimerUtils.startTimer("path");
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
                    } else {
                        System.out.println(s.getName());
                    }
                }
                TimerUtils.stopTimer("path");
                    // 5. Convert a path to a program
                    // NOTE: one path may correspond to multiple programs and we may need a loop here!
                    boolean sat = true;
                    
                    for (MethodSignature s : signatures) {
                    	System.out.println("s = " + s);
                    }
                    
                    CodeFormer former = new CodeFormer(signatures,inputs,retType, varNames, methodName,subclassMap, superclassMap);
                    while (sat){
                        TimerUtils.startTimer("code");
                        String code;
                        try {
                            code = former.solve();
                            System.out.println("code = " + code);
                        } catch (TimeoutException e) {
                            sat = false;
                            break;
                        }
                        sat = !former.isUnsat();
                        TimerUtils.stopTimer("code");
                        // 6. Run the test cases
                        // TODO: write this code; if all test cases pass then we can terminate
                        TimerUtils.startTimer("compile");
                        System.out.println("code = "  + code);
                        System.out.println("testCode = "  + testCode);
                        boolean compre = Test.runTest(code,testCode);
                        TimerUtils.stopTimer("compile");
                        if (compre) {
                            solution = true;
                            System.out.println("code = " + code);
                            File compfile = new File("build/Target.class");
                            compfile.delete();
                            break;
                        }
                    }
                    System.exit(0);

				result = Encoding.solver.findPath(loc);
			}
				loc++;
				if (loc >= limit)
					break;
				encoding.updateSAT(loc);
			}
		}

	}
