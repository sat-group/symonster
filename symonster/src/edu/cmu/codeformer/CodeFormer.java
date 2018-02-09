package edu.cmu.codeformer;

import edu.cmu.parser.MethodSignature;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;
import soot.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Given a sequence of method calls, this class will produce a string containing the corresponding Java code.
 */
public class CodeFormer {
    private final List<MethodSignature> sigs;
    private int slotNumber = 0;
    private int retNumber = 0;
    private final VarTable slotTypes = new VarTable();
    private final VarTable returnedValTypes = new VarTable();
    private boolean unsat = false;
    private final List<String> inputTypes;
    private final String retType;
    private final List<String> varNames;
    private final String methodName;
    ISolver solver = SolverFactory.newDefault();

    /**
     *
     * The initial setup for the class.
     * @param sigs requires a sequence of signatures in the expected order.
     * @param varNames
     * @param methodName
     */
    public CodeFormer(List<MethodSignature> sigs, List<String> inputTypes, String retType, List<String> varNames, String methodName) {
        this.sigs = sigs;
        this.inputTypes = inputTypes;
        this.retType = retType;
        this.varNames = varNames;
        this.methodName = methodName;
        solver.setTimeout(1000000);
        //Setup
        //Add method input
        for (String input : inputTypes){
            returnedValTypes.addEntry(input,retNumber);
            retNumber += 1;
        }

        //Add slots and variables to the signatures table
        for (MethodSignature sig : sigs){
            if (!sig.getRetType().toString().equals("void")){
                returnedValTypes.addEntry(sig.getRetType().toString(),retNumber);
                retNumber += 1;
            }
            if (sig.getIsConstructor()){

            }
            else if (!sig.getIsStatic()){
                slotTypes.addEntry(sig.getHostClass().getType().toString(),slotNumber);
                slotNumber += 1;
            }
            for (Type type : sig.getArgTypes()){
                slotTypes.addEntry(type.toString(),slotNumber);
                slotNumber += 1;
            }

        }
        //Add method return value
        if (retType != null)
        {
            slotTypes.addEntry(retType,slotNumber);
            slotNumber += 1;
        }

        //Setup constrains
        addSingleVariableConstrains();
        addAtLeastOneSlot();
    }

    /**
     * Each call to solve will produce one extra solution.
     * @return one solution to the programming (Java code)
     * @throws TimeoutException Iff there is no solution available
     */
    public String solve() throws TimeoutException {
        //Solve
        int[] satResult;
        try {
            if (solver.isSatisfiable()){
                satResult = solver.model();
            }
            else{
                unsat = true;
                throw new TimeoutException();
            }
        } catch (TimeoutException e) {
            unsat = true;
            throw new TimeoutException();
        }

        //A list only with filtered positive elements in the result.
        List<Integer> satList = new ArrayList<>();

        //Block this version, and filter the result with only positive ones.
        VecInt block = new VecInt();
        for (Integer id : satResult){
            block.push(-id);
            if (id > 0) satList.add(id);
        }
        try {
            solver.addClause(block);
        } catch (ContradictionException e) {
            unsat = true;
        }

        //formCode
        return formCode(satList);

    }

    /**
     *
     * @return true iff the problem is no longer solvable.
     */
    public boolean isUnsat() {
        return unsat;
    }


    //Each slot only has variable
    private void addSingleVariableConstrains(){
        for (int slotValue = 0; slotValue < slotNumber ; slotValue += 1) {
            IVecInt vec = new VecInt();
            for (int returnedValue : returnedValTypes.getEntries(slotTypes.getType(slotValue))) {
                vec.push(calculateID(returnedValue,slotValue));
            }
            try {
                solver.addExactly(vec,1);
            } catch (ContradictionException e) {
                unsat = true;
            }
        }
    }

    //Each slot only has one variable
    private void addAtLeastOneSlot(){
        //TODO Constrain by the order
        for (int returnedValue = 0; returnedValue < retNumber ; returnedValue += 1) {
            IVecInt vec = new VecInt();
            for (int slotValue : slotTypes.getEntries(returnedValTypes.getType(returnedValue))) {
                vec.push(calculateID(returnedValue,slotValue));
            }
            try {
                solver.addAtLeast(vec,1);
            } catch (ContradictionException e) {
                unsat = true;
            }
        }
    }

    private String formCode(List<Integer> satResult){
        //FormCode
        StringBuilder builder = new StringBuilder();
        int varCount = 0;
        int slotCount = 0;

        //Add method signature
        builder.append("public ");
        if (retType != null){
            builder.append(retType.toString());
            builder.append(" ");
        }
        else{
            builder.append("void ");
        }
        builder.append(methodName);
        builder.append("(");
        for (int i = 0 ; i < inputTypes.size() ; i++){
            builder.append(inputTypes.get(i));
            builder.append(" ");
            builder.append(varNames.get(varCount));
            varCount += 1;
            if (i != inputTypes.size() - 1) builder.append(", ");
        }
        builder.append("){\n");

        for (MethodSignature sig : sigs){
            if (!sig.getRetType().toString().equals("void")){
                builder.append(sig.getRetType().toString());
                builder.append(" ");
                builder.append(convVarName(varCount));
                varCount += 1;
                builder.append(" = ");
            }

            if (sig.getIsConstructor()){
                builder.append(" new ");
            }
            else if (sig.getIsStatic()){
                builder.append(sig.getHostClass());
                builder.append(".");
            }

            else{
                int id = satResult.get(slotCount);
                slotCount ++;
                int returnedValue = calculateReturnedValue(id);
                int slotValue = calculateSlotValue(id);
                assert (slotValue == slotCount);
                builder.append(convVarName(returnedValue));
                builder.append(".");
            }

            builder.append(sig.getName());
            builder.append("(");
            for (int i = 0; i < sig.getArgTypes().size() ; i++){
                int id = satResult.get(slotCount);
                slotCount ++;
                int returnedValue = calculateReturnedValue(id);
                int slotValue = calculateSlotValue(id);
                assert (slotValue == slotCount);

                builder.append(convVarName(returnedValue));
                if (i != sig.getArgTypes().size() - 1){
                    builder.append(",");
                }
            }
            builder.append(");\n");
        }
        if (retType != null ){
            builder.append("return ");

            int id = satResult.get(slotCount);
            slotCount ++;
            int returnedValue = calculateReturnedValue(id);
            int slotValue = calculateSlotValue(id);
            assert (slotValue == slotCount);
            builder.append(convVarName(returnedValue));
            builder.append(";\n");
        }
        builder.append("}");
        return builder.toString();
    }


    private int calculateID(int returnedValue,int slotValue){
        return returnedValue + retNumber * slotValue + 1;
    }
    private int calculateReturnedValue(int id){
        return (id-1)%retNumber;
    }
    private int calculateSlotValue(int id){
        return (id-1)/retNumber;
    }

    private String convVarName(int val){
        if (val < varNames.size()) return varNames.get(val);
        else return "var_"+(val - varNames.size());
    }

}
