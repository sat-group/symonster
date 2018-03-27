package edu.cmu.parser;

import soot.SootClass;
import soot.SootMethod;
import soot.Type;

import java.lang.reflect.Method;
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
    private final SootMethod method;

    protected MethodSignature(String name, Type retType, List<Type> argTypes, boolean isStatic, SootClass hostClass, boolean isConstructor, SootMethod method){
        this.retType = retType;
        this.argTypes = argTypes;
        this.isStatic = isStatic;
        this.hostClass = hostClass;
        this.isConstructor = isConstructor;
        if (isConstructor) {
            this.name = hostClass.getName();
        }else{
            this.name = name;
        }
        this.method = method;
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
    public SootMethod getMethod() {
        return method;
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
        return hostClass + ":"+ name;
    }

    @Override
    public boolean equals(Object o){
        if (!(o instanceof MethodSignature)) return false;
        MethodSignature sig = (MethodSignature)o;
        return sig.name.equals(name) && sig.hostClass.equals(hostClass) && sig.retType.equals(retType) &&
                sig.isStatic == isStatic && sig.argTypes.equals(argTypes) && sig.isConstructor == isConstructor;
    }

    @Override
    public int hashCode(){
        return method.hashCode();
    }
}
