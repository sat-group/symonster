package stat.parser;

import java.util.*;
import java.util.stream.Collectors;

import soot.CompilationDeathException;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.SourceLocator;
import soot.options.Options;

/**
 * Parser for library jar files used as labels
 */
public class LibraryJarParser {

    private static LinkedHashSet<String> labelSet = new LinkedHashSet<>(); // Make order deterministic

    public static LinkedHashSet<SootMethod> parse(String lib, List<String> packages, boolean visible) {
        LinkedHashSet<SootMethod> methods = new LinkedHashSet<>();

        for (String cl : SourceLocator.v().getClassesUnder(lib)) {
            SootClass clazz = Scene.v().getSootClass(cl);
            LinkedList<SootMethod> methodsCopy = new LinkedList<SootMethod>(clazz.getMethods());
            for (SootMethod method : methodsCopy) {
                // Only considers method that start with the package that we are interested on
                boolean skip = !packages.isEmpty();
                for (String packageName : packages) {
                    if (cl.startsWith(packageName)) {
                        skip = false;
                        break;
                    }
                }

                // Only consider methods that are either public or static
                // This should be used for the libraries but not for the training set
                if (visible && !method.isPublic() && !method.isStatic()) {
                    skip = true;
                }

                if (skip)
                    continue;

                methods.add(method);
                System.out.println("lib: "+method);
            }
        }

        return methods;
    }

    protected static void initSoot(ArrayList<String> libs, List<String> packages) {

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

        options.append(" -cp --quiet" + cp.toString());

        if (!Options.v().parse(options.toString().split(" ")))
            throw new CompilationDeathException(CompilationDeathException.COMPILATION_ABORTED,
                    "Option parse error");

        Scene.v().loadBasicClasses();
        Scene.v().loadNecessaryClasses();
    }

    public static void init(ArrayList<String> libs, List<String> packages) {
        labelSet.clear();
        initSoot(libs, packages);

        for (String lib : libs) {
            LinkedHashSet<SootMethod> methods = parse(lib, packages, true);
            for(SootMethod method : methods){
                labelSet.add(method.getSignature());
            }
            System.out.println("#methods = " + labelSet.size());
        }
    }

    public static LinkedHashSet<String> getLabelSet() {
        return labelSet;
    }
}
