package edu.cmu.parser;

import soot.SootClass;
import soot.Type;

import java.util.List;

/**
 * Data structure that describes a method signature, including
 * 1. Method name
 * 2. Return type
 * 3. The list of argument types in order.
 * 4. Whether the method is static.
 * 5. The host class of the method.
 */
public class MethodSignature {
    private final String name;
    private final Type retType;
    private final List<Type> argTypes;
    private final boolean isStatic;
    private final SootClass hostClass;
    private final boolean isConstructor;
    protected MethodSignature(String name, Type retType, List<Type> argTypes, boolean isStatic, SootClass hostClass, boolean isConstructor){
        this.name = name;
        this.retType = retType;
        this.argTypes = argTypes;
        this.isStatic = isStatic;
        this.hostClass = hostClass;
        this.isConstructor = isConstructor;
    }

    public String getName() {
        return name;
    }

    public Type getRetType() {
        return retType;
    }

    public List<Type> getArgTypes() {
        return argTypes;
    }

    public boolean getIsStatic(){
        return isStatic;
    }
    public boolean getIsConstructor(){
        return isConstructor;
    }
    public SootClass getHostClass() {
        return hostClass;
    }

    @Override
    public String toString(){
        String result =  retType + " " + name + "(";
        if (isStatic) result = "static "+result;
        result = hostClass + ": " + result;
        int i = 0;
        for (Type t : argTypes){
            if (i != argTypes.size()-1) result += t + ", ";
            else result += t;
            i += 1;
        }
        result += ")";
        return result;
    }
}
