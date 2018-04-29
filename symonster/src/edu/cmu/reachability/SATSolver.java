package edu.cmu.reachability;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.sat4j.core.VecInt;
//import org.sat4j.minisat.SolverFactory;
//import org.sat4j.specs.ISolver;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

import edu.cmu.reachability.SATSolver.ConstraintType;
import uniol.apt.adt.pn.Place;

public class SATSolver {
	
	private IPBSolver solver = null;
	private boolean unsat = false;
	private VecInt assumptions;
	
	enum ConstraintType { LTE, EQ, GTE; }
	
	// Maps the variable id to transition
	public HashMap<Integer,Variable> id2variable = new HashMap<>();
	
	private int nbVariables = 0;
	private int loc_variable;
	
	public SATSolver(){
		solver = SolverFactory.newDefault();
		assumptions = new VecInt();
		
	}
	
	public void reset(){
		solver = SolverFactory.newDefault();
		unsat = false;
		id2variable.clear();
		nbVariables = 0;
		assumptions.clear();
	}
	
	public int getNbConstraints(){
		return solver.nConstraints();
	}

	public void initialVarSet() {
		solver.newVar(1000000);
	}
	
	public void setNbVariables(int vars){

		nbVariables = vars;
		solver.newVar(nbVariables);
	}
	
	public void setNbVariables_new(int vars){
		
		 // version for additional variables
		
		//for (int i = vars+1; i <= vars+100; i++)
		//	loc_variables.push(i);
		//nbVariables = vars+100;
		//solver.newVar(nbVariables);
		
		// dummy constraints for the additional variables
		// each variable much appear at least once in the solver
		/*
		for (int i = vars+1; i <= 100; i++) {
			try {
				solver.addAtLeast(new VecInt(new int[] {i}), 1);
			} catch (ContradictionException e) {
				assert(false);
			}
		}
		*/
		
		//nbVariables = vars;
		//solver.newVar(nbVariables);
	
		
		loc_variable = vars+1;
		nbVariables = vars + 1;
		//solver.newVar(nbVariables);
		try {
			solver.addAtLeast(new VecInt(new int[] {loc_variable}), 1);
		} catch (ContradictionException e) {
			assert(false);
		}
		
		
		
	}
	
	public int getNbVariables(){
		return nbVariables;
	}
	
	public void printClause(VecInt constraint) {
		for (int i = 0 ;i < constraint.size(); i++) {
			if (constraint.get(i) < 0) System.out.print("~");
			System.out.print(id2variable.get(Math.abs(constraint.get(i))));
			if (i != constraint.size()-1)
				System.out.print(" OR ");
		}
		System.out.println("");
//		for (int i = 0 ;i < constraint.size(); i++) {
//			System.out.print(constraint.get(i));
//			if (i != constraint.size()-1)
//				System.out.print(" OR ");
//		}
//		System.out.println("");
	}
	
	public void addClause(VecInt constraint) {
		try {
			//printClause(constraint);
			solver.addClause(constraint);
		} catch (ContradictionException e) {
			unsat = false;
		}
	}
	
	public void printConstraint(VecInt constraint, VecInt coeffs, ConstraintType ct, int k) {
		switch(ct){
		case LTE:
			for (int i = 0 ;i < constraint.size(); i++)
				System.out.print(coeffs.get(i) + "*" + id2variable.get(constraint.get(i)) + " ");
			System.out.println("<= " + k);
			break;
		case EQ:
			for (int i = 0 ;i < constraint.size(); i++)
				System.out.print(coeffs.get(i) + "*" + id2variable.get(constraint.get(i)) + " ");
			System.out.println("= " + k);
			break;
		case GTE:
			for (int i = 0 ;i < constraint.size(); i++)
				System.out.print(coeffs.get(i) + "*" + id2variable.get(constraint.get(i)) + " ");
			System.out.println(">= " + k);
			break;
		default:
			assert(false);
	}
	}
	
	public void addConstraint(VecInt constraint, VecInt coeffs, ConstraintType ct, int k){ 
		try {
			//printConstraint(constraint, coeffs, ct, k);
			switch(ct){
				case LTE:
					solver.addAtMost(constraint, coeffs, k);
					break;
				case EQ:
					solver.addExactly(constraint, coeffs, k);
					break;
				case GTE:
					solver.addAtLeast(constraint, coeffs, k);
					break;
				default:
					assert(false);
			}
		} catch (ContradictionException e) {
			unsat = true;
		}
	}
	
	public void printConstraint(VecInt constraint, ConstraintType ct, int k) {
		switch(ct){
		case LTE:
			for (int i = 0 ;i < constraint.size(); i++)
				System.out.print(id2variable.get(constraint.get(i)) + " ");
			System.out.println("<= " + k);
			break;
		case EQ:
			for (int i = 0 ;i < constraint.size(); i++)
				System.out.print(id2variable.get(constraint.get(i)) + " ");
			System.out.println("= " + k);
			break;
		case GTE:
			for (int i = 0 ;i < constraint.size(); i++)
				System.out.print(id2variable.get(constraint.get(i)) + " ");
			System.out.println(">= " + k);
			break;
		default:
			assert(false);
	}
	}
	
	public void addConstraint(VecInt constraint, ConstraintType ct, int k){ 
		try {
			//printConstraint(constraint, ct, k);
			switch(ct){
				case LTE:
					solver.addAtMost(constraint, k);
					break;
				case EQ:
					solver.addExactly(constraint, k);
					break;
				case GTE:
					solver.addAtLeast(constraint, k);
					break;
				default:
					assert(false);
			}
		} catch (ContradictionException e) {
			unsat = true;
		}
	}
	
	public void setAssumption(int v) {
		assumptions.push(v);
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
	
	public void setFState( List<Integer> fstate) {
		//Clear all old assumptions since loc has been updated
		assumptions.clear();
		
		//Add new final state as an assumption
		for (int x: fstate) {
			assumptions.push(x);
		}
	}
	
	public List<Variable> findPath( int loc){
		
		ArrayList<Variable> res = new ArrayList<>();
		// TODO: what happens when loc -> loc+1
		// 1) initial state can be encoded as constraints
		// clear the assumptions: 1) final state, 2) blocking of models
		// set a new final state
		// set the previous state as true (you can use constraints -> setTrue)
		// incrementally increase the encoding to loc+1
		
		
		try {
			// comment the below assert when using assumptions
			//assert(assumptions.isEmpty());
			if(!unsat && solver.isSatisfiable(assumptions)){
				int [] model = solver.model();  
				//assert (model.length == nbVariables);
				VecInt block = new VecInt();
				for (Integer id : id2variable.keySet()){
					if (model[id-1] > 0){
						block.push(-id);
						res.add(id2variable.get(id));
					}
				}

				// block model
				try {
					// ~getX(loc=1) OR ~setX(loc=2) OR ~setY(loc=3)
					// ~getX(loc=1) OR ~setX(loc=2) OR ~setY(loc=3) OR L1
					
					//block.push(loc_variable);
					//assumptions.push(-loc_variable);
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
