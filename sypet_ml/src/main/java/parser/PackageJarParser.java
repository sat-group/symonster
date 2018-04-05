package parser;

import soot.*;
import soot.jimple.*;

import java.util.*;

/**
 * Parses bundles of jar files and stores them in various map
 */
public class PackageJarParser extends BodyTransformer {
    public static final String ANALYSIS_NAME = "jap.analysis";
    public static Set<String> packages = new HashSet<>();

    /**
     * Parse a list of given jar files, and stores the result in methodDict and methodVarDict
     * @param libs physical addresses of libraries. e.g. "lib/hamcrest-core-1.3.jar"
     */
    public static void parseJar(List<String> libs) {
        packages.clear();
        String[] args = parser.SootUtils.getSootArgs(libs);
        G.reset();
        PackManager.v().getPack("jap").add(new Transform(ANALYSIS_NAME, new PackageJarParser()));
        SootUtils.runSoot(args);
    }

    public static Set<String> getPackages(){
        return packages;
    }

    @Override
    protected void internalTransform(Body body, String s, Map<String, String> map) {
        // main process that extracts information about every method
        synchronized (this) {

            for (Unit unit : body.getUnits()) {
                Stmt stmt = (Stmt) unit;

                if(stmt instanceof AssignStmt){
                    // if is assignment statement
                    AssignStmt x = (AssignStmt) stmt;
                    Value assignVar = x.getLeftOp();
                    Value assignExpr = x.getRightOp();
                    List<ValueBox> expr = assignExpr.getUseBoxes();

                    List<Value> callees;
                    if(assignExpr instanceof InvokeExpr){
                        // if there is a function call, separate callers from callees
                        InvokeExpr invokeExp = ((InvokeExpr) assignExpr);
                        packages.add(invokeExp.getMethod().getDeclaringClass().getPackageName());

                    }
                    //System.out.println("assign to: "+ assignVar +" with " + "caller: "+calls+" callees: "+callees);
                } else if(stmt instanceof InvokeStmt){
                    // if is purely a functional invocation
                    InvokeStmt invokeStmt = (InvokeStmt) stmt;
                    packages.add(invokeStmt.getInvokeExpr().getMethod().getDeclaringClass().getPackageName());
                }
            }

            // store in dict
            String methodName = body.getMethod().toString();
            packages.add(body.getMethod().getDeclaringClass().getPackageName());
        }
    }
}
