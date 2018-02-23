package edu.cmu.parser;
import edu.cmu.equivprogram.DependencyMap;
import edu.cmu.utils.SootUtils;
import soot.*;
import soot.jimple.Stmt;
import soot.jimple.internal.JInstanceFieldRef;

import java.util.*;

/**

 *
 */
public class JarParser extends BodyTransformer{
    public static final String ANALYSIS_NAME = "jap.analysis";
    private static Map<MethodSignature,Set<SootField>> usedFieldDict = new HashMap<>();
    private static DependencyMap dependencyMap = new DependencyMap();
    /**
     * Sample main function
     * @param args no use at all
     */
    public static void main(String[] args) {
        List<String> libs = new ArrayList<>();
        libs.add("lib/point.jar");
        //libs.add("../benchmarks/examples/geometry/geometry.jar");
        //libs.add("lib/hamcrest-core-1.3.jar");
        //libs.add("lib/junit-4.11.jar");
        List<MethodSignature> sigs = parseJar(libs);
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
                        sigs.add(getMethodSignature(method));
                    }
                }
            }
        }

        for (MethodSignature sig1 : usedFieldDict.keySet()){
            for (MethodSignature sig2 : usedFieldDict.keySet()){
                if (!sig1.equals(sig2)){
                    Set<SootField> set1 = usedFieldDict.get(sig1);
                    Set<SootField> set2 = usedFieldDict.get(sig2);
                    for (SootField field : set1){
                        if (set2.contains(field)){
                            dependencyMap.addDep(sig1,sig2);
                            break;
                        }
                    }
                }
            }
        }
        return sigs;
    }

    /**
     * Return the resulting dependency map of the signatures, and reset all fields.
     * @return
     */

    public static DependencyMap getDependencyMap(){
        DependencyMap ret = dependencyMap;
        dependencyMap = new DependencyMap();
        usedFieldDict = new HashMap<>();
        return dependencyMap;
    }

    static private MethodSignature getMethodSignature(SootMethod method){
        SootClass clazz = method.getDeclaringClass();
        if (method.getName().equals("<init>")){
            MethodSignature sig = new MethodSignature(method.getName(),clazz.getType(),
                    method.getParameterTypes(),method.isStatic(),clazz,true,method);
            return sig;
        }
        else{
            MethodSignature sig = new MethodSignature(method.getName(),method.getReturnType(),
                    method.getParameterTypes(),method.isStatic(),clazz,false,method);
            return sig;
        }
    }

    @Override
    protected void internalTransform(Body body, String s, Map<String, String> map) {
        Set<SootField> result = addProgramMethod(body,s);
        SootMethod method = body.getMethod();
        if (method != null){
            usedFieldDict.put(getMethodSignature(body.getMethod()),result);
        }
    }

    private static Set<SootField> addProgramMethod(Body body,String name){
        if (usedFieldDict.keySet().contains(name))return new HashSet<>();
        Set<SootField> result = new HashSet<>();
        for (Unit unit : body.getUnits()){
            if (unit instanceof Stmt){
                Stmt stmt = (Stmt)unit;
                result.addAll(addAllFieldInUnit(stmt));
            }
        }
        System.out.println(result.size());
        return result;

    }

    static private Set<SootField> addAllFieldInUnit(Unit unit){
        Set<SootField> result = new HashSet<>();
        List<ValueBox> boxes = unit.getUseAndDefBoxes();
        if (unit instanceof JInstanceFieldRef){
            System.out.println("Field!");
            System.out.println(unit);
            JInstanceFieldRef ref = (JInstanceFieldRef)unit;
            result.add(ref.getField());
        }
        for (ValueBox b : boxes){
            result.addAll(addAllFieldInValue(b.getValue()));
        }
        return result;
    }
    static private Set<SootField> addAllFieldInValue(Value value){
        Set<SootField> result = new HashSet<>();
        List<ValueBox> boxes = value.getUseBoxes();
        if (value instanceof JInstanceFieldRef){
            System.out.println("Field!");
            JInstanceFieldRef ref = (JInstanceFieldRef)value;
            result.add(ref.getField());
        }
        for (ValueBox b : boxes){
            result.addAll(addAllFieldInValue(b.getValue()));
        }
        return result;
    }

}
