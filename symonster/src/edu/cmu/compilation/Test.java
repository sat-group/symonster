package edu.cmu.compilation;

import edu.cmu.utils.TestUtils;

import java.io.IOException;

/**
 * Write code given the tests and classes.
 */
public class Test {
    private static String classOutputFolder = "build";

    private static final String CLASSNAME = "Target";

    /**
     * run test class based on the synthesized code and test code.
     * @param code synthesized code
     * @param testCode test code with name "test"
     * @return whether test pasted
     */
    public static boolean runTest(String code,String testCode) throws IOException {
        //Create file;
        String classCode = writeCode(code,testCode);
        boolean runResult = TestUtils.runTest(classCode);
        //if (!runResult) file.delete();
        return runResult;
    }

    private static String writeCode(String code,String testCode) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("public class "+CLASSNAME  +" {\n");
        builder.append(code);
        builder.append(testCode);
        builder.append("}\n");
        return builder.toString();
    }

}
