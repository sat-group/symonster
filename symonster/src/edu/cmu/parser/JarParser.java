package edu.cmu.parser;
import edu.cmu.equivprogram.DependencyMap;
import edu.cmu.utils.SootUtils;
import soot.*;
import soot.jimple.FieldRef;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.internal.JInstanceFieldRef;
import soot.jimple.internal.JInvokeStmt;
import soot.util.ArraySet;

import java.lang.reflect.Method;
import java.util.*;

/**

 *
 */
public class JarParser extends BodyTransformer{
    public static final String ANALYSIS_NAME = "jap.analysis";
    private static Map<MethodSignature,Set<SootField>> usedFieldDict = new HashMap<>();
    private static DependencyMap dependencyMap = new DependencyMap();
    private static Map<SootMethod,Body> bodies = new HashMap<>();
    private static Set<MethodSignature> workings = new HashSet<>();
    private static List<String> pkgs;

    /**
     * Parse a list of given jar files, and produce a list of method signatures.
     * @param libs physical addresses of libraries. e.g. "lib/hamcrest-core-1.3.jar"
     * @return the list of method signature contained in the given libraries
     */
    public static List<MethodSignature> parseJar(List<String> libs,List<String> pkgss, List<String> blacklist) {
        pkgs = pkgss;
        PackManager.v().getPack("jap").add(new Transform(ANALYSIS_NAME, new JarParser()));
        SootUtils.runSoot(SootUtils.getSootArgs(libs));
        List<MethodSignature> sigs = new LinkedList<>();
        for (String jarPath : libs){
            List<String> cls = SourceLocator.v().getClassesUnder(jarPath);
            for (String cl : cls) {
                SootClass clazz = Scene.v().getSootClass(cl);
                List<SootMethod> methods = clazz.getMethods();
                for (SootMethod method : methods) {
                    if (method.isPublic()){
                        boolean sat = false;
                        for (String pkg : pkgs){
                            if (clazz.getName().startsWith(pkg)){
                                sat = true;
                                break;
                            }
                        }
                        for (String bl : blacklist){
                            if (method.getName().endsWith(bl)) {
                                sat = false;
                                break;
                            }
                        }
                        if (sat) sigs.add(getMethodSignature(method));
                    }
                }
            }
        }
        return sigs;
    }

    /**
     * A method that provides the super classes of all application classes.
     * @param acceptableSuperClasses the set of classes that can be considered super classes. In order to reduce the
     *                               unnecessary super classes (e.g. Object).
     * @return the map from each SootClass, to its corresponding set of super classes.
     */
    public static Map<String,Set<String>> getSuperClasses(Set<String> acceptableSuperClasses){
        Map<String,Set<String>> result = new HashMap<>();
        for (SootClass cl : Scene.v().getClasses()){
            for (String pkg : pkgs){
                if (cl.getName().startsWith(pkg)){
                    result.put(cl.getName(),getSuperClassesOfClass(acceptableSuperClasses,cl));
                    break;
                }
            }
        }
        return result;
    }

    private static Set<String> getSuperClassesOfClass(Set<String> acceptableSuperClasses, SootClass cl){
        Set<String> res;
        if (cl.hasSuperclass()){
            SootClass sup = cl.getSuperclass();
            res= getSuperClassesOfClass(acceptableSuperClasses,sup);
            if (acceptableSuperClasses.contains(sup.getName())) res.add(cl.getSuperclass().getName());
        }
        else{
            res = new HashSet<>();
        }
        for (SootClass interf:cl.getInterfaces()){
            String name = interf.getName();
            if (acceptableSuperClasses.contains(name)) res.add(name);
            res.addAll(getSuperClassesOfClass(acceptableSuperClasses,interf));
        }
        return res;
    }



    /**
     * Return the resulting dependency map of the signatures, and reset all fields.
     * @return
     */

    public static DependencyMap createDependencyMap(){
        DependencyMap ret = dependencyMap;
        dependencyMap = new DependencyMap();
        analyzeDep();
        List<MethodSignature> keyset = new ArrayList<>(usedFieldDict.keySet());
        //Create dependencies
        for (int i = 0 ; i < keyset.size() ; i++){
            for (int j = i + 1; j < keyset.size() ; j++){
                MethodSignature s1 = keyset.get(i);
                MethodSignature s2 = keyset.get(j);
                boolean intersect = false;
                for (SootField field : usedFieldDict.get(s1)){
                    if (usedFieldDict.get(s2).contains(field)){
                        intersect = true;
                        break;
                    }
                }
                if (intersect){
                    dependencyMap.addDep(s1,s2);
                }
                else{
                    if (s2.getArgTypes().contains(s1.getRetType()) ||
                            s1.getArgTypes().contains(s2.getRetType())){
                        dependencyMap.addDep(s1,s2);
                    }
                }
            }
        }
        return dependencyMap;
    }

    private static MethodSignature getMethodSignature(SootMethod method){
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

    private static void analyzeDep(){
        usedFieldDict = new HashMap<>();
        for (SootMethod method : bodies.keySet()){
            Body body = bodies.get(method);
            if (body != null){
                Set<SootField> result = addProgramMethod(body,getMethodSignature(method));
                usedFieldDict.put(getMethodSignature(body.getMethod()),result);
            }
        }

    }

    @Override
    protected void internalTransform(Body body, String s, Map<String, String> map) {
        bodies.put(body.getMethod(),body);
    }

    private static Set<SootField> addProgramMethod(Body body,MethodSignature methodSignature){
        if (usedFieldDict.keySet().contains(methodSignature))return usedFieldDict.get(methodSignature);
        if (workings.contains(methodSignature)) return new HashSet<>();
        workings.add(methodSignature);
        Set<SootField> result = new HashSet<>();
        for (Unit unit : body.getUnits()){
            if (unit instanceof Stmt){
                Stmt stmt = (Stmt)unit;
                result.addAll(addAllFieldInUnit(stmt));
            }
        }
        workings.remove(methodSignature);
        return result;

    }

    // Return all fields reference in a unit
    private static Set<SootField> addAllFieldInUnit(Unit unit){
        Set<SootField> result = new HashSet<>();
        List<ValueBox> boxes = unit.getUseAndDefBoxes();
        if (unit instanceof JInstanceFieldRef){
            JInstanceFieldRef ref = (JInstanceFieldRef)unit;
            result.add(ref.getField());
        }
        else if (unit instanceof JInvokeStmt){
            JInvokeStmt st = (JInvokeStmt)unit;
            SootMethod met = st.getInvokeExpr().getMethod();
            if (bodies.keySet().contains(met))  result.addAll(addProgramMethod(bodies.get(met),getMethodSignature(met)));
            else result.add(null);

        }
        for (ValueBox b : boxes){
            result.addAll(addAllFieldInValue(b.getValue()));
        }
        return result;
    }

    // Return all fields reference in a value
    private static Set<SootField> addAllFieldInValue(Value value){
        Set<SootField> result = new HashSet<>();
        List<ValueBox> boxes = value.getUseBoxes();
        if (value instanceof JInstanceFieldRef){
            JInstanceFieldRef ref = (JInstanceFieldRef)value;
            result.add(ref.getField());
        }
        else if (value instanceof SootFieldRef){
            SootFieldRef ref = (SootFieldRef)value;
            result.add(ref.resolve());
        }
        else if (value instanceof FieldRef){
            FieldRef ref = (FieldRef)value;
            result.add(ref.getField());
        }
        else if (value instanceof InvokeExpr){
            InvokeExpr expr = (InvokeExpr)value;
            SootMethod met = expr.getMethod();
            if (bodies.keySet().contains(met))  result.addAll(addProgramMethod(bodies.get(met),getMethodSignature(met)));
            else result.add(null);
        }
        for (ValueBox b : boxes){
            result.addAll(addAllFieldInValue(b.getValue()));
        }
        return result;
    }

}
