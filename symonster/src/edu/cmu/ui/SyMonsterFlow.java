package edu.cmu.ui;
import edu.cmu.parser.*;
import edu.cmu.petrinet.BuildNet;
import edu.cmu.reachability.*;
import edu.cmu.utils.TimerUtils;
import uniol.apt.adt.pn.PetriNet;

import java.io.IOException;
import java.util.*;

public class SyMonsterFlow {
	public static void main(String[] args) throws IOException {
        // 0. Read config
        SymonsterConfig jsonConfig = JsonParser.parseJsonConfig("config/config.json");
        Set<String> acceptableSuperClasses = new HashSet<>();
        acceptableSuperClasses.addAll(jsonConfig.acceptableSuperClasses);

        // 1. Read input from the user
        SyMonsterInput jsonInput;
        if (args.length == 0) {
            System.out.println("Please use the program args next time.");
            jsonInput = JsonParser.parseJsonInput("benchmarks/flow/example.json");
        }
        else{
            jsonInput = JsonParser.parseJsonInput(args[0]);
        }

        List<String> libs = jsonInput.libs;
		List<String> inputs = jsonInput.srcTypes;
        String retType = jsonInput.tgtType;
        
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
        BuildNet  b = new BuildNet();
        PetriNet net = b.build(sigs, superclassMap, subclassMap, inputs);

        int loc = 1;
		int paths = 0;
		boolean solution = false;

        TimerUtils.startTimer("total");

		while (!solution && loc < 4) {
			// create a formula that has the same semantics as the petri-net
			Encoding encoding = new FlowEncoding(net, loc);                     // Set encoding
			
			// set initial state and final state
			encoding.setState(EncodingUtil.setInitialState(net, inputs),  0);
			encoding.setState(EncodingUtil.setGoalState(net, retType),  loc);

			// 4. Perform reachability analysis
			
			// for each loc find all possible programs
			List<Variable> result = Encoding.solver.findPath(loc);
            
            while(!result.isEmpty() && !solution){
				paths++;
				String path = "Path #" + paths + " =\n";
				for (Variable s : result) {
					path += s.toString() + "\n";
				}
				System.out.println(path);
				

				// the current path did not result in a program that passes all test cases
				// find the next path
				result = Encoding.solver.findPath(loc);
			}
			
			// we did not find a program of length = loc
			loc++;
		}

	}

}
