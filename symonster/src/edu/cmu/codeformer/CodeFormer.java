package edu.cmu.codeformer;

import edu.cmu.parser.JarParser;
import edu.cmu.parser.MethodSignature;
import org.junit.jupiter.api.Test;
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
    List<String> inputTypes;
    String retType;
    ISolver solver = SolverFactory.newDefault();

    /**
     *
     * The initial setup for the class.
     * @param sigs requires a sequence of signatures in the expected order.
     */
    public CodeFormer(List<MethodSignature> sigs, List<String> inputTypes,String retType) {
        this.sigs = sigs;
        this.inputTypes = inputTypes;
        this.retType = retType;
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
            if (id > 0){
                block.push(-id);
                satList.add(id);
            }
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
            //System.out.println("start");
            //System.out.println(slotTypes.getType(slotValue));
            //System.out.println(returnedValTypes.getEntries(slotTypes.getType(slotValue)));
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
        builder.append("method(");
        for (int i = 0 ; i < inputTypes.size() ; i++){
            builder.append(inputTypes.get(i));
            builder.append(" var_"+varCount);
            varCount += 1;
            if (i != inputTypes.size() - 1) builder.append(", ");
        }
        builder.append("){\n");

        for (MethodSignature sig : sigs){
            if (!sig.getRetType().toString().equals("void")){
                builder.append(sig.getRetType().toString());
                builder.append(" ");
                builder.append("var_");
                builder.append(varCount);
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
                int returnedValue = clculateReturnedValue(id);
                int slotValue = calculateSlotValue(id);
                assert (slotValue == slotCount);
                builder.append("var_");
                builder.append(returnedValue);
                builder.append(".");
            }

            builder.append(sig.getName());
            builder.append("(");
            for (int i = 0; i < sig.getArgTypes().size() ; i++){
                int id = satResult.get(slotCount);
                slotCount ++;
                int returnedValue = clculateReturnedValue(id);
                int slotValue = calculateSlotValue(id);
                assert (slotValue == slotCount);

                builder.append("var_");
                builder.append(returnedValue);
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
            int returnedValue = clculateReturnedValue(id);
            int slotValue = calculateSlotValue(id);
            assert (slotValue == slotCount);
            builder.append("var_");
            builder.append(returnedValue);
            builder.append(";\n");
        }
        builder.append("}");
        return builder.toString();
    }


    private int calculateID(int returnedValue,int slotValue){
        return returnedValue + retNumber * slotValue + 1;
    }
    private int clculateReturnedValue(int id){
        return (id-1)%retNumber;
    }
    private int calculateSlotValue(int id){
        return (id-1)/retNumber;
    }

}
