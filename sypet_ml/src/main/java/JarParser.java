import soot.*;
import soot.jimple.*;

import java.util.*;

/**
 * Parses bundles of jar files for ml purposes
 */
public class JarParser extends BodyTransformer {
    public static final String ANALYSIS_NAME = "jap.analysis";
    private static Map<String, Set<String>> methodToAppearancesMap = new HashMap<>();
    private static Map<String, Map<String, Set<String>>> methodToVarAppearancesMap = new HashMap<>();
    private static Map<String, Map<String, Integer>> methodToVarFreqMap = new HashMap<>();
    private static List<String> pckg;
    private static boolean parseOK;

    /**
     * Parse a list of given jar files, and stores the result in methodDict and methodVarDict
     * @param libs physical addresses of libraries. e.g. "lib/hamcrest-core-1.3.jar"
     */
    public static void parseJar(List<String> libs, List<String> packg) {
        String[] args = SootUtils.getSootArgs(libs);
        pckg = packg;
        G.reset();
        PackManager.v().getPack("jap").add(new Transform(ANALYSIS_NAME, new JarParser()));
        methodToVarAppearancesMap = new HashMap<>();
        methodToVarAppearancesMap = new HashMap<>();
        methodToVarFreqMap = new HashMap<>();
        SootUtils.runSoot(args);
    }

    public static Map<String, Set<String>> getMethodToAppearancesMap(){
        return methodToAppearancesMap;
    }

    public static Map<String, Map<String, Set<String>>> getMethodToVarAppearancesMap(){
        return methodToVarAppearancesMap;
    }

    public static void refresh(){
        methodToAppearancesMap.clear();
        methodToVarAppearancesMap.clear();
        methodToVarFreqMap.clear();
    }

    @Override
    protected void internalTransform(Body body, String s, Map<String, String> map) {
        // main process that extracts information about every method
        synchronized (this) {
            //System.out.println("-----------------");
            //System.out.println("main: " + body.getMethod());

            // keep track of all the methods in the body
            Set<String> methodSet = new HashSet<>();

            // keep track of all the variables appeared in the methods
            Map<String, Integer> hashMap = new HashMap<>();
            Map<String, Set<String>> varMethodMap = new HashMap<>();

            for (Unit unit : body.getUnits()) {
                Stmt stmt = (Stmt) unit;

                if(stmt instanceof AssignStmt){
                    // if is assignment statement
                    AssignStmt x = (AssignStmt) stmt;
                    Value assignVar = x.getLeftOp();
                    Value assignExpr = x.getRightOp();
                    List<ValueBox> expr = assignExpr.getUseBoxes();

                    // stores every variable appeared in a statement
                    List<Value> calls = new ArrayList<>();
                    for(ValueBox b : expr){
                        Value v = b.getValue();
                        calls.add(v);
                        if(hashMap.containsKey(v.toString())){
                            hashMap.put(v.toString(), hashMap.get(v.toString())+1);
                        }else{
                            hashMap.put(v.toString(), 1);
                        }
                    }

                    List<Value> callees;
                    if(assignExpr instanceof InvokeExpr){
                        // if there is a function call, separate callers from callees
                        InvokeExpr invokeExp = ((InvokeExpr) assignExpr);
                        String method = invokeExp.getMethod().toString();
                        boolean fitsPackage = false;
                        for(String pck : pckg){
                            if(method.contains(pck)){
                                methodSet.add(method);
                                fitsPackage = true;
                                break;
                            }
                        }
                        callees = invokeExp.getArgs();
                        calls.removeAll(callees);
                        if(fitsPackage) {
                            if (hashMap.containsKey(assignVar.toString())) {
                                hashMap.put(assignVar.toString(), hashMap.get(assignVar.toString()) + 1);
                            } else {
                                hashMap.put(assignVar.toString(), 1);
                                Set<String> set = new HashSet<>();
                                set.add(invokeExp.getMethod().toString());
                                varMethodMap.put(assignVar.toString(), set);
                            }
                            for (Value var : calls) {
                                if (varMethodMap.containsKey(var.toString())) {
                                    Set<String> set = varMethodMap.get(var.toString());
                                    set.add(invokeExp.getMethod().toString());
                                } else {
                                    Set<String> set = new HashSet<>();
                                    set.add(invokeExp.getMethod().toString());
                                    varMethodMap.put(var.toString(), set);
                                }
                            }
                        }
                    }else{
                        callees = new ArrayList<>(calls);
                        calls = null;
                    }
                    //System.out.println("assign to: "+ assignVar +" with " + "caller: "+calls+" callees: "+callees);
                } else if(stmt instanceof InvokeStmt){
                    // if is purely a functional invocation
                    InvokeStmt invokeStmt = (InvokeStmt) stmt;
                    String method = invokeStmt.getInvokeExpr().getMethod().toString();
                    for(String pck : pckg){
                        if(method.contains(pck)){
                            methodSet.add(method);
                            break;
                        }
                    }
                }
            }

            // store in dict
            String methodName = body.getMethod().toString();
            if(methodSet.size()>0) {
                methodToAppearancesMap.put(methodName, methodSet);
                methodToVarFreqMap.put(methodName, hashMap);
                methodToVarAppearancesMap.put(methodName, varMethodMap);
            }
            //System.out.println(hashMap);
            //System.out.println("-----------------");
        }
    }
}
