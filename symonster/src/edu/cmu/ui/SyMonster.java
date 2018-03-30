package edu.cmu.ui;
import edu.cmu.codeformer.CodeFormer;
import edu.cmu.compilation.Test;
import edu.cmu.equivprogram.DependencyMap;
import edu.cmu.parser.*;
import edu.cmu.petrinet.BuildNetNoVoid;
import edu.cmu.petrinet.BuildNet;
import edu.cmu.reachability.*;
import edu.cmu.utils.TimerUtils;
import org.sat4j.specs.TimeoutException;
import uniol.apt.adt.pn.PetriNet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class SyMonster {
	public static void main(String[] args) throws IOException {
		Test test = new Test();
        // 0. Read config
        SymonsterConfig jsonConfig = JsonParser.parseJsonConfig("config/config.json");
        Set<String> acceptableSuperClasses = new HashSet<>();
        acceptableSuperClasses.addAll(jsonConfig.acceptableSuperClasses);

        // 1. Read input from the user
        SyMonsterInput jsonInput;
        if (args.length == 0) {
            System.out.println("Please use the program args next time.");
            jsonInput = JsonParser.parseJsonInput("benchmarks/joda/18/benchmark18.json");
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
                    subclassMap.put(value,new HashSet<>());
                }
                subclassMap.get(value).add(key);
            }
        }
        System.out.println(1);
        // 3. build a petrinet and signatureMap of library
        // Currently built without clone edges
		BuildNetNoVoid b = new BuildNetNoVoid();                          // Set petrinet
		//BuildNetWithoutClone b = new BuildNetWithoutClone(noVoid);
		PetriNet net = b.build(sigs, superclassMap, subclassMap);
        System.out.println(1.5);
		Map<String, MethodSignature> signatureMap = b.dict;

		int loc = 1;
		int paths = 0;
		int programs = 0;
		boolean solution = false;

        TimerUtils.startTimer("total");

        Set<List<MethodSignature>> repeatSolutions = new HashSet<>();
        //DependencyMap dependencyMap = JarParser.createDependencyMap();
        System.out.println(2);
		while (!solution) {
			// create a formula that has the same semantics as the petri-net
			Encoding encoding = new SequentialEncoding(net, loc);                     // Set encoding
			
			// set initial state and final state
			encoding.setState(EncodingUtil.setInitialState(net, inputs),  0);
			encoding.setState(EncodingUtil.setGoalState(net, retType),  loc);

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
                System.out.println(path);
                if (true){//!repeatSolutions.contains(signatures)){
                    //List<List<MethodSignature>> repeated = dependencyMap.findAllTopSorts(signatures);
                    //repeatSolutions.addAll(repeated);
                    // 5. Convert a path to a program
                    // NOTE: one path may correspond to multiple programs and we may need a loop here!
                    boolean sat = true;
                    CodeFormer former = new CodeFormer(signatures,inputs,retType, varNames, methodName,subclassMap, superclassMap);
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
                        if (true)
                        {
                            System.out.println(path);
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
                            System.out.println("Paths explored = " + paths);
                            System.out.println("code:");
                            System.out.println(code);
                            TimerUtils.stopTimer("total");
                            System.out.println("total time: "+TimerUtils.getCumulativeTime("total"));
                            break;
                        }
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

}
