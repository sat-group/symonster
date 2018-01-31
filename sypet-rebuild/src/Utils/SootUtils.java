package Utils;

import soot.Main;
import soot.Body;
import soot.Unit;
import soot.SootMethod;
import soot.SootField;
import soot.SootClass;
import soot.tagkit.Host;
import soot.NormalUnitPrinter;

import java.util.ArrayList;
import java.util.List;

import java.security.Permission;

/** Various useful utilities
 */
public class SootUtils {
    /** Converts a Unit from a given Body to a String */
    public static String toString(Unit unit, Body body) {
        NormalUnitPrinter printer = new NormalUnitPrinter(body);
        unit.toString(printer);
        return printer.output().toString();
    }

    /** Builds up the appropriate arguments for invoking analysisToRun
     * on classToAnalyze.  Mostly involves setting up a few command-line
     * options and the classpath.
     */
    public static String[] getSootArgs(List<String> libs) {
        String separator = System.getProperty("file.separator");
        String pathSeparator = System.getProperty("path.separator");
        String rtJarPath = "lib" + separator + "rt.jar";
        rtJarPath += pathSeparator + "lib" + separator + "jce.jar";
        String sootClasspath = rtJarPath + pathSeparator + "build";
        List<String> argList = new ArrayList<>();
        argList.add("cp");
        argList.add(sootClasspath);
        argList.add("-keep-line-number");
        argList.add("-f");
        argList.add("-J");
        for (String lib : libs){
            argList.add("-process-dir");
            argList.add(lib);
        }
        String [] fixedargs = { "-cp", sootClasspath, "-keep-line-number", "-f", "J"};
        String[] args = new String[argList.size()];
        for (int i = 0; i < 5; i++){
            args[i] = fixedargs[i];
        }
        for (int i = 5 ; i < argList.size(); i++){
            args[i] = argList.get(i).toString();
        }
        System.out.println(argList);
        return args;
    }

    /** runs Soot with the arguments given,
     * ensuring that Soot does not call System.exit() if we are invoked
     * from JUnit */
    public static void runSoot(String[] args) {
        try {
            forbidSystemExitCall();
            Main.main(args);
            SootUtils.enableSystemExitCall();
        } catch (SootUtils.ExitTrappedException e) {
            // swallow the exception if Soot tried to exit directly; we'll exit soon anyway
        }
    }

    private static class ExitTrappedException extends SecurityException { }

    /** forbids System.exit() calls in Soot */
    // code courtesy of http://stackoverflow.com/questions/5401281/preventing-system-exit-from-api
    private static void forbidSystemExitCall() {
        final SecurityManager securityManager = new SecurityManager() {
            public void checkPermission( Permission permission ) {
                if( permission.getName().startsWith("exitVM") ) {
                    throw new ExitTrappedException() ;
                }
            }
        };
        System.setSecurityManager( securityManager ) ;
    }

    private static void enableSystemExitCall() {
        System.setSecurityManager( null ) ;
    }
}