package edu.cmu.reachability;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

public class SATSolver {
	
	private ISolver solver = null;
	private boolean unsat = false;
	
	// Maps the variable id to transition
	public HashMap<Integer,Variable> id2variable = new HashMap<>();
	
	private int nbVariables = 0;
	
	public SATSolver(){
		solver = SolverFactory.newDefault();
	}
	
	public void setNbVariables(int vars){
		nbVariables = vars;
		solver.newVar(nbVariables);
	}
	
	public int getNbVariables(){
		return nbVariables;
	}
	
	public void addAtLeast(VecInt constraint, int k){
		try {
			solver.addAtLeast(constraint, k);
		} catch (ContradictionException e) {
			unsat = true;
		}
	}
	
	public void addAtMost(VecInt constraint, int k){
		try {
			solver.addAtMost(constraint, k);
		} catch (ContradictionException e) {
			unsat = true;
		}
	}
	
	public void addExactly(VecInt constraint, int k){
		try {
			solver.addExactly(constraint, k);
		} catch (ContradictionException e) {
			unsat = true;
		}
	}
	
	// v is only true if preconditions holds
	public void addPreconditions(int v, VecInt preconditions){
		try {
			VecInt clause = new VecInt(new int[] {-v});
			preconditions.copyTo(clause);
			solver.addClause(clause);
		} catch (ContradictionException e) {
			unsat = true;
		}		
	}
	
	// v is only true if all preconditions holds
	public void addPreconditions(int v, List<VecInt> preconditions){
		try {
			for (VecInt vc : preconditions){
				VecInt clause = new VecInt(new int[] {-v});
				vc.copyTo(clause);
				solver.addClause(clause);
			}
		} catch (ContradictionException e) {
			unsat = true;
		}		
	}
	
	// if the variables in state hold then they imply that v is true
	public void addPostConditions(VecInt state, int v){
		try{
			VecInt clause = new VecInt();
			for (int i = 0; i < state.size(); i++){
				clause.push(-state.get(i));
			}
			clause.push(v);
			solver.addClause(clause);
		} catch (ContradictionException e) {
			unsat = true;
		}	
	}
	
	public void setTrue(int v){
		try{
			VecInt clause = new VecInt(new int[] {v});
			solver.addClause(clause);
		} catch (ContradictionException e) {
			unsat = true;
		}
	}

	public void setFalse(int v){
		try{
			VecInt clause = new VecInt(new int[] {-v});
			solver.addClause(clause);
		} catch (ContradictionException e) {
			unsat = true;
		}
	}
	
	public void addSameTokens(VecInt transitions, int placePrevious, int placeAfter){
		try{
			VecInt clause = new VecInt();
			for (int i = 0; i < transitions.size(); i++){
				clause.push(transitions.get(i));
			}
			clause.push(-placePrevious);
			clause.push(placeAfter);
			solver.addClause(clause);
		} catch (ContradictionException e) {
			unsat = true;
		}		
	}
	
	public List<Variable> findPath(){
		
		ArrayList<Variable> res = new ArrayList<>();
		
		try {
			if(!unsat && solver.isSatisfiable()){
				int [] model = solver.model();  
				assert (model.length == nbVariables);
				VecInt block = new VecInt();
				for (Integer id : id2variable.keySet()){
					if (model[id-1] > 0){
						block.push(-id);
						res.add(id2variable.get(id));
					}
				}
				
				// block model
				try {
					solver.addClause(block);
				} catch (ContradictionException e) {
					unsat = true;
				}
				
				
			}
		} catch (TimeoutException e) {
			// consider as it did not find a solution
			unsat = true;
		}
		
		// sort transitions by increasing time step
		Collections.sort(res);
		
		return res;
		
	}

}
