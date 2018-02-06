package edu.cmu.codeformer;

import edu.cmu.utils.ReflectionUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
public class TestRunner {
    public static void main(String[] args) throws IOException {
        TestRunner r = new TestRunner(new ArrayList<>());
        r.runTest("233");
    }
    private static final String TESTFILE = "testDirectory/Test1.java";
    private static final String CLASSNAME = "Test1";

    public TestRunner(List<String> libs){
        ReflectionUtils.loadLibraries(libs);
    }

    public void runTest(String code) throws IOException {
        writeCode(code);
    }

    private void writeCode(String code) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(TESTFILE));
        writer.write("public class "+CLASSNAME+"{");
        writer.write(code);
        writer.write("}");
        writer.close();
    }
}
