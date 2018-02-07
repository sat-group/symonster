package edu.cmu.parser;
import edu.cmu.utils.SootUtils;
import soot.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**

 *
 */
public class JarParser extends BodyTransformer{
    public static final String ANALYSIS_NAME = "jap.analysis";

    /**
     * Sample main function
     * @param args no use at all
     */
    public static void main(String[] args) {
        List<String> libs = new ArrayList<>();
        libs.add("../benchmarks/examples/point/point.jar");
        //libs.add("../benchmarks/examples/geometry/geometry.jar");
        //libs.add("lib/hamcrest-core-1.3.jar");
        //libs.add("lib/junit-4.11.jar");
        System.out.println(parseJar(libs));
    }

    /**
     * Parse a list of given jar files, and produce a list of method signatures.
     * @param libs physical addresses of libraries. e.g. "lib/hamcrest-core-1.3.jar"
     * @return the list of method signature contained in the given libraries
     */
    public static List<MethodSignature> parseJar(List<String> libs) {
        String[] args = SootUtils.getSootArgs(libs);
        PackManager.v().getPack("jap").add(new Transform(ANALYSIS_NAME, new JarParser()));
        SootUtils.runSoot(args);
        List<MethodSignature> sigs = new LinkedList<>();

        for (String jarPath : libs){
            List<String> cls = SourceLocator.v().getClassesUnder(jarPath);
            for (String cl : cls) {
                SootClass clazz = Scene.v().getSootClass(cl);
                List<SootMethod> methods = clazz.getMethods();
                for (SootMethod method : methods) {
                    if (method.isPublic()){
                        if (method.getName().equals("<init>")){
                            MethodSignature sig = new MethodSignature(clazz.getName(),clazz.getType(),method.getParameterTypes(),method.isStatic(),clazz,true);
                            sigs.add(sig);
                        }
                        else{
                            MethodSignature sig = new MethodSignature(method.getName(),method.getReturnType(),method.getParameterTypes(),method.isStatic(),clazz,false);
                            sigs.add(sig);
                        }
                    }
                }
            }
        }
        return sigs;
    }

    @Override
    protected void internalTransform(Body body, String s, Map<String, String> map) {
    }
}
