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
	
	enum ConstraintType { LTE, EQ, GTE; }
	
	// Maps the variable id to transition
	public HashMap<Integer,Variable> id2variable = new HashMap<>();
	
	private int nbVariables = 0;
	
	public SATSolver(){
		solver = SolverFactory.newDefault();
	}
	
	public void reset(){
		solver = SolverFactory.newDefault();
		unsat = false;
		id2variable.clear();
		nbVariables = 0;
	}
	
	public void setNbVariables(int vars){
		nbVariables = vars;
		solver.newVar(nbVariables);
	}
	
	public int getNbVariables(){
		return nbVariables;
	}
	
	public void addConstraint(VecInt constraint, ConstraintType ct, int k){ 
		try {
			switch(ct){
				case LTE:
					solver.addAtMost(constraint, k);
					break;
				case EQ:
					solver.addExactly(constraint, k);
					break;
				case GTE:
					// if k == 1 then it is a clause
					if (k == 1) solver.addClause(constraint);
					else solver.addAtLeast(constraint, k);
					break;
				default:
					assert(false);
			}
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
				}
				catch (ContradictionException e) {
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
