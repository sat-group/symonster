package edu.cmu.parser;

import java.util.ArrayList;
import java.util.List;

public class SyMonsterInput {
    public List<String> libs;
    public List<String> inputTypes;
    public List<String> inputVarNames;
    public String returnType;
    public String methodName;
    public String testCode;

    @Override
    public String toString(){
        return "libs: " + libs + "\ninputTypes: " + inputTypes + "\ninputVarNames: " + inputVarNames + "\nreturnType: " + returnType + "\nmethodName: " + methodName + "\ntestCode: " + testCode;
    }

}
