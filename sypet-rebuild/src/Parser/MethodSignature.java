package Parser;

import heros.ThreadSafe;
import soot.Type;

import java.util.List;

/**
 * Data structure that describes a method signature, including name, return type, and the list of argument types required.
 */
@ThreadSafe
public class MethodSignature {
    private String name;
    private Type retType;
    private List<Type> argTypes;

    public MethodSignature(String name, Type retType, List<Type> argTypes){
        this.name = name;
        this.retType = retType;
        this.argTypes = argTypes;
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

    @Override
    public String toString(){
        String result =  retType + " " + name + "(";
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
