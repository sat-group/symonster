package edu.cmu.tests;

import edu.cmu.codeformer.CodeFormer;
import edu.cmu.parser.JarParser;
import edu.cmu.parser.MethodSignature;
import junit.textui.TestRunner;
import org.sat4j.specs.TimeoutException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CodeFormerTest {

    public static void main(String[] args){
        List<String> libs = new ArrayList<>();
        libs.add("../benchmarks/examples/point/point.jar");
        List<MethodSignature> sigs = JarParser.parseJar(libs);
        System.out.println(sigs);
        List<MethodSignature> sequence = new ArrayList<>();
        sequence.add(sigs.get(2));
        sequence.add(sigs.get(1));
        sequence.add(sigs.get(0));
        sequence.add(sigs.get(5));
        sequence.add(sigs.get(4));
        sequence.add(sigs.get(3));
        List<String> inputTypes = new ArrayList<>();
        inputTypes.add("int");
        inputTypes.add("int");
        String returnType = "cmu.symonster.MyPoint";
        List<String> varNames = new ArrayList<>();
        varNames.add("x");
        varNames.add("y");
        String methodName = "method";
        CodeFormer former = new CodeFormer(sequence,inputTypes,returnType, varNames, methodName,new HashMap<>());
        TestRunner runner = new TestRunner();
        while (!former.isUnsat()){
            try {
                String result = former.solve();
                System.out.println(result);
            } catch (TimeoutException e) {
                break;
            }
        }
    }
}
