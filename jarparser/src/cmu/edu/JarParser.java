package cmu.edu;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import soot.CompilationDeathException;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.SourceLocator;
import soot.options.Options;

public class JarParser {
	
	public static Set<SootMethod> parse(String lib, ArrayList<String> pkg, boolean visible){
		Set<SootMethod> methods = new HashSet<>();
		
		for (String cl : SourceLocator.v().getClassesUnder(lib)) {
			SootClass clazz = Scene.v().getSootClass(cl);
		
			LinkedList<SootMethod> methodsCopy = new LinkedList<SootMethod>(clazz.getMethods());
			for (SootMethod method : methodsCopy) {
				
				// Only considers method that start with the package that we are interested on
				boolean skip = !pkg.isEmpty();
				for (String pName : pkg) {
					if (cl.startsWith(pName)) {
						skip = false;
						break;
					}
				}
				
				// Only consider methods that are either public or static
				// This should be used for the libraries but not for the training set
				if (visible && !method.isPublic() && !method.isStatic()){
					skip = true;
				}
				
				if (skip)
					continue;
				
				methods.add(method);
			}
		}
		
		return methods;
	}
	
	public static void initSoot(ArrayList<String> libs, ArrayList<String> packages){
		
		StringBuilder options = new StringBuilder();
		options.append("-prepend-classpath");
		options.append(" -full-resolver");
		options.append(" -allow-phantom-refs");
		StringBuilder cp = new StringBuilder();
		
		for (String lib : libs) {
			cp.append(lib);
			cp.append(":");
			options.append(" -process-dir " + lib);
		}

		options.append(" -cp " + cp.toString());

		if (!Options.v().parse(options.toString().split(" ")))
			throw new CompilationDeathException(CompilationDeathException.COMPILATION_ABORTED,
					"Option parse error");

		Scene.v().loadBasicClasses();
		Scene.v().loadNecessaryClasses();
	}
	
	public static void main(String[] args) {
		
		// Libraries that we want to read from
		ArrayList<String> libs = new ArrayList<>();
		libs.add("../sypet_ml/lib/rt.jar");
		
		// Packages from those libraries that we are interested on
		// If this list is empty then we consider all packages!
		ArrayList<String> packages = new ArrayList<>();
		packages.add("java.awt.geom");
		
		initSoot(libs, packages);

		for (String lib : libs){
			Set<SootMethod> methods = parse(lib, packages, true);
			System.out.println("#methods = " + methods.size());
			for (SootMethod method : methods){
				System.out.println(method.getSignature());
			}
		}		
	}
}
