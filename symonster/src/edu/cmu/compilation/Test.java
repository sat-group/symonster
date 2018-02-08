package edu.cmu.compilation;

import edu.cmu.utils.ReflectionUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Test {
	
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

    private static final String TESTFILE = "src/edu/cmu/testfiles/Target1.java";
    private static final String CLASSNAME = "Target1";

    public boolean runTest(String code) throws IOException {
        writeCode(code);
        boolean runResult = ReflectionUtils.runTest(CLASSNAME);
        return runResult;
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
