package edu.cmu.compilation;

import edu.cmu.utils.ReflectionUtils;

import javax.tools.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class Test {
    private static String classOutputFolder = "build";
    public boolean runTest(List<String> program){

		// TODO: compile the program and run the test cases
		// Currently we are just checking if the sequence of APIs is the one we expect
		// NOTE: there may be other programs that would pass all test cases, e.g. reordering the APIs
		if (program.get(0).equals("Point<-Point(void)") &&
			program.get(1).equals("MyPoint<-clone(MyPoint)") &&
			program.get(2).equals("int<-getX(MyPoint)") &&
			program.get(3).equals("int<-getY(MyPoint)") &&
			program.get(4).equals("Point<-clone(Point)") &&
			program.get(5).equals("void<-setX(Point,int)") &&
			program.get(6).equals("Point<-clone(Point)") &&
			program.get(7).equals("void<-setY(Point,int)"))
			return true;
		else
			return false;
	}

    private static final String CLASSNAME = "Target";
    public static boolean runTest(String code,String testCode) throws IOException {
        //Create file;
        String classCode = writeCode(code,testCode);
        boolean runResult = ReflectionUtils.runTest(classCode);
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
