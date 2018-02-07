import soot.*;
import soot.jimple.*;

import java.util.*;

public class JarParser extends BodyTransformer {
    public static final String ANALYSIS_NAME = "jap.analysis";
    private static Map<String, Set<String>> methodDict = new HashMap<>();
    private static Map<String, Map<String, Integer>> methodVarDict = new HashMap<>();

    /**
     * Sample main function
     * @param args no use at all
     */
    public static void main(String[] args) {
        List<String> libs = new ArrayList<>();
        libs.add("lib/geometry.jar");

        parseJar(libs);
    }

    /**
     * Parse a list of given jar files, and stores the result in methodDict and methodVarDict
     * @param libs physical addresses of libraries. e.g. "lib/hamcrest-core-1.3.jar"
     */
    public static void parseJar(List<String> libs) {

        String[] args = SootUtils.getSootArgs(libs);
        PackManager.v().getPack("jap").add(new Transform(ANALYSIS_NAME, new JarParser()));
        SootUtils.runSoot(args);
    }

    @Override
    protected void internalTransform(Body body, String s, Map<String, String> map) {
        // main process that extracts information about every method
        synchronized (this) {
            System.out.println("-----------------");
            System.out.println("main: " + body.getMethod());

            // keep track of all the methods in the body
            Set<String> methodSet = new HashSet<>();

            // keep track of all the variables appeared in the methods
            Map<String, Integer> hashMap = new HashMap<>();

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
                        methodSet.add(method);
                        callees = invokeExp.getArgs();
                        calls.removeAll(callees);
                        if(hashMap.containsKey(assignVar.toString())){
                            hashMap.put(assignVar.toString(), hashMap.get(assignVar.toString())+1);
                        }else{
                            hashMap.put(assignVar.toString(), 1);
                        }
                    }else{
                        callees = new ArrayList<>(calls);
                        calls = null;
                    }
                    System.out.println("assign to: "+ assignVar +" with " + "caller: "+calls+" callees: "+callees);
                } else if(stmt instanceof InvokeStmt){
                    // if is purely a functional invocation
                    InvokeStmt invokeStmt = (InvokeStmt) stmt;
                    methodSet.add(invokeStmt.getInvokeExpr().getMethod().toString());
                }
            }

            // store in dict
            methodDict.put(body.getMethod().toString(), methodSet);
            methodVarDict.put(body.getMethod().toString(), hashMap);
            System.out.println(hashMap);
            System.out.println("-----------------");
        }
    }
}
