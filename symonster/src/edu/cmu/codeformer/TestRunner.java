package edu.cmu.codeformer;

import edu.cmu.parser.JarParser;
import edu.cmu.parser.MethodSignature;
import edu.cmu.utils.ReflectionUtils;
import org.sat4j.specs.TimeoutException;
import soot.SootClass;
import soot.Type;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
public class TestRunner {
    public static void main(String[] args) throws IOException {
        List<String> libs = new ArrayList<>();
        libs.add("../benchmarks/examples/point/point.jar");
        List<MethodSignature> sigs = JarParser.parseJar(libs);
        List<MethodSignature> sequence = new ArrayList<>();
        sequence.add(sigs.get(0));
        sequence.add(sigs.get(1));
        sequence.add(sigs.get(2));
        sequence.add(sigs.get(3));

        List<String> inputTypes = new ArrayList<>();
        inputTypes.add("cmu.symonster.MyPoint");
        inputTypes.add("cmu.symonster.Point");
        String returnType = "cmu.symonster.Point";

        CodeFormer former = new CodeFormer(sequence,inputTypes,returnType);
        TestRunner runner = new TestRunner();
        while (!former.isUnsat()){
            try {
                String result = former.solve();
                runner.runTest(result);
            } catch (TimeoutException e) {
                break;
            }
        }

    }
    private static final String TESTFILE = "src/edu/cmu/testfiles/Target1.java";
    private static final String CLASSNAME = "Target1";

    public void runTest(String code) throws IOException {
        writeCode(code);
        boolean runResult = ReflectionUtils.runTest(CLASSNAME);
    }

    private void writeCode(String code) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(TESTFILE));
        writer.write("package edu.cmu.testfiles;\n");
        writer.write("public class "+CLASSNAME+ " implements edu.cmu.testfiles.TargetInterface {\n");
        writer.write(code);
        writer.write("}\n");
        writer.close();
    }
}
