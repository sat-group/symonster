package edu.cmu.reachability;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.sat4j.core.VecInt;

import edu.cmu.reachability.SATSolver.ConstraintType;
import edu.cmu.reachability.Variable.Type;
import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;

public class SequentialEncoding implements Encoding {

	int loc = 1;
	PetriNet pnet = null;
	int nbVariables = 1;
	int nbConstraints = 0;

	public SequentialEncoding(PetriNet pnet, int loc) {
		this.pnet = pnet;
		this.loc = loc;
		
		// clean the data structures before creating a new encoding
		place2variable.clear();
		transition2variable.clear();
		solver.reset();
		
		createVariables();
		createConstraints();
	}

	// Exactly one transition f is fired at each time step t
	private void sequentialTransitions() {

		// loop for each time step t
		for (int t = 0; t < loc; t++) {
			// loop for each transition
			VecInt constraint = new VecInt();
			for (Transition tr : pnet.getTransitions()) {
				Pair<Transition, Integer> pair = new ImmutablePair<Transition, Integer>(tr, t);
				Variable var = transition2variable.get(pair);
				constraint.push(var.getId());
			}

			// add constraints to the solver
			solver.addConstraint(constraint, ConstraintType.EQ, 1);
		}
	}

	private void postConditionsTransitions() {
		// loop for each time step t
		for (int t = 0; t < loc; t++) {
			// loop for each transition
			for (Transition tr : pnet.getTransitions()) {

				Place target = null;
				int source_weight = 1;
				int target_weight = 1;

				assert (tr.getPostsetEdges().size() == 1);
				for (Flow f : tr.getPostsetEdges()) {
					target = f.getPlace();
					target_weight = f.getWeight();
				}

				boolean sameSourceTarget = false;
				for (Flow f : tr.getPresetEdges()) {
					Place p = f.getPlace();
					if (p.equals(target)) {
						sameSourceTarget = true;
						// check how much do we have at the end
						source_weight = f.getWeight();
					}
				}

				if (sameSourceTarget) {
					// consider the case where the source and target are the same
					int diff_weight = target_weight - source_weight;
					
					for (Flow f : tr.getPostsetEdges()) {
						Place p = f.getPlace();

						Pair<Transition, Integer> transition = new ImmutablePair<Transition, Integer>(tr, t);
						Variable fireTr = transition2variable.get(transition);

						for (int w = 0; w < p.getMaxToken(); w++) {
							
							// there was not enough resources to fire this transition
							if (w+diff_weight < 0)
								continue;
							
							Triple<Place, Integer, Integer> placeBefore = new ImmutableTriple<Place, Integer, Integer>(p, t, w);
							Triple<Place, Integer, Integer> placeAfter = new ImmutableTriple<Place, Integer, Integer>(p, t + 1, w + diff_weight);
							
							// if f is fired then the number of tokens is increased in the target place
							Variable previousState = place2variable.get(placeBefore);
							Variable nextState = place2variable.get(placeAfter);
							VecInt state = new VecInt(new int[]{-fireTr.getId(), -previousState.getId(), nextState.getId()});
							solver.addConstraint(state, ConstraintType.GTE, 1);
						}
					}
					
				} else {

					for (Flow f : tr.getPostsetEdges()) {
						Place p = f.getPlace();

						Pair<Transition, Integer> transition = new ImmutablePair<Transition, Integer>(tr, t);
						Variable fireTr = transition2variable.get(transition);

						for (int w = 0; w < p.getMaxToken(); w++) {
							Triple<Place, Integer, Integer> placeBefore = new ImmutableTriple<Place, Integer, Integer>(p, t, w);
							Triple<Place, Integer, Integer> placeAfter = new ImmutableTriple<Place, Integer, Integer>(p, t + 1, w + 1);

							// if f is fired then the number of tokens is increased in the target place
							Variable previousState = place2variable.get(placeBefore);
							Variable nextState = place2variable.get(placeAfter);
							VecInt state = new VecInt(new int[]{-fireTr.getId(), -previousState.getId(), nextState.getId()});
							solver.addConstraint(state, ConstraintType.GTE, 1);
						}
					}

					for (Flow f : tr.getPresetEdges()) {
						Place p = f.getPlace();

						Pair<Transition, Integer> transition = new ImmutablePair<Transition, Integer>(tr, t);
						Variable fireTr = transition2variable.get(transition);

						int weight = f.getWeight();

						for (int w = weight; w <= p.getMaxToken(); w++) {
							Triple<Place, Integer, Integer> placeBefore = new ImmutableTriple<Place, Integer, Integer>(p, t, w);
							Triple<Place, Integer, Integer> placeAfter = new ImmutableTriple<Place, Integer, Integer>(p, t + 1, w - weight);
							
							// if f is fired then the number of tokens is decreased in the source place
							Variable previousState = place2variable.get(placeBefore);
							Variable nextState = place2variable.get(placeAfter);
							VecInt state = new VecInt(new int[]{-fireTr.getId(), -previousState.getId(), nextState.getId()});
							solver.addConstraint(state, ConstraintType.GTE, 1);
						}
					}
				}
			}
		}
	}

	private void preConditionsTransitions() {
		// loop for each time step t
		for (int t = 0; t < loc; t++) {
			// loop for each transition
			for (Transition tr : pnet.getTransitions()) {
				List<VecInt> preconditions = new ArrayList<VecInt>(); 
				for (Flow f : tr.getPresetEdges()) {
					VecInt pre = new VecInt();
					Place p = f.getPlace();
					int weight = f.getWeight();
					for (int w = weight; w <= p.getMaxToken(); w++) {
						Triple<Place, Integer, Integer> triple = new ImmutableTriple<Place, Integer, Integer>(p, t, w);
						Variable v = place2variable.get(triple);
						pre.push(v.getId());
					}
					preconditions.add(pre);
				}

				Pair<Transition, Integer> pair = new ImmutablePair<Transition, Integer>(tr, t);
				Variable fireTr = transition2variable.get(pair);
				
				// if f is fired then there are enough resources to fire it
				for (VecInt pc : preconditions){
					pc.push(-fireTr.getId());
					solver.addConstraint(pc, ConstraintType.GTE, 1);
				}

				for (Flow f : tr.getPostsetEdges()) {
					Place p = f.getPlace();
					Triple<Place, Integer, Integer> triple = new ImmutableTriple<Place, Integer, Integer>(p, t, p.getMaxToken());
					Variable v = place2variable.get(triple);
					
					// if is fired then we are not at maximum resources in the target
					VecInt clause = new VecInt(new int[]{-v.getId(), -fireTr.getId()});
					solver.addConstraint(clause, ConstraintType.GTE, 1);
				}
			}
		}
	}

	private void tokenRestrictions() {

		// loop for each time step t
		for (int t = 0; t <= loc; t++) {
			// loop for each place
			for (Place p : pnet.getPlaces()) {
				VecInt amo = new VecInt();
				// loop for each number of tokens
				for (int w = 0; w <= p.getMaxToken(); w++) {
					Triple<Place, Integer, Integer> triple = new ImmutableTriple<Place, Integer, Integer>(p, t, w);
					Variable v = place2variable.get(triple);
					amo.push(v.getId());
				}
				// enforce token restrictions
				solver.addConstraint(amo, ConstraintType.EQ, 1);
			}
		}

	}
	
	private void noTransitionTokens() {
		
		// loop for each time step t
		for (int t = 0; t < loc; t++) {
			// loop for each place
			for (Place p : pnet.getPlaces()) {
				Set<Transition> transitions = new HashSet<Transition>();
				transitions.addAll(p.getPostset());
				transitions.addAll(p.getPreset());
				VecInt transitionsConstr = new VecInt();
				for (Transition tr : transitions){
					Pair<Transition, Integer> pair = new ImmutablePair<Transition, Integer>(tr, t);
					transitionsConstr.push(transition2variable.get(pair).getId());
				}
				
				for (int w = 0; w <= p.getMaxToken(); w++){
					Triple<Place, Integer, Integer> current = new ImmutableTriple<Place, Integer, Integer>(p, t, w);
					Triple<Place, Integer, Integer> next = new ImmutableTriple<Place, Integer, Integer>(p, t+1, w);
					
					VecInt clause = new VecInt();
					transitionsConstr.copyTo(clause);
					clause.push(-place2variable.get(current).getId());
					clause.push(place2variable.get(next).getId());
					solver.addConstraint(clause, ConstraintType.GTE, 1);
				}
			}
		}
	}
	
	private void dummyConstraints() {

		for (int v = 1; v <= nbVariables; v++) {
			VecInt constraint = new VecInt();
			constraint.push(v);
			solver.addConstraint(constraint, ConstraintType.LTE, 1);
		}

	}

	@Override
	public void createVariables() {
		assert (pnet != null);

		for (Place p : pnet.getPlaces()) {
			for (int t = 0; t <= loc; t++) {
				for (int v = 0; v <= p.getMaxToken(); v++) {
					// create a variable with <place in the petri-net, timestamp, value>
					Triple<Place, Integer, Integer> triple = new ImmutableTriple<Place, Integer, Integer>(p, t, v);
					Variable var = new Variable(nbVariables, p.getId(), Type.PLACE, t, v);
					place2variable.put(triple, var);
					//solver.id2variable.put(nbVariables, var);
					// each variable is associated with an id (starts at 1)
					nbVariables++;
				}
			}
		}

		for (Transition tr : pnet.getTransitions()) {
			for (int t = 0; t < loc; t++) {
				// create a variable with <transition in the petri-net, timestamp>
				Pair<Transition, Integer> pair = new ImmutablePair<Transition, Integer>(tr, t);
				Variable var = new Variable(nbVariables, tr.getLabel(), Type.TRANSITION, t);
				transition2variable.put(pair, var);
				solver.id2variable.put(nbVariables, var);
				// each variable is associated with an id (starts at 1)
				nbVariables++;
			}
		}

		// set number of variables in the solver
		solver.setNbVariables(nbVariables);
		assert (solver.getNbVariables() > 0);
	}

	@Override
	public void createConstraints() {

		// All variables must be used in some constraint
		// These dummy constraints would not be necessary if the above invariant is maintained
		dummyConstraints();

		// Exactly one transition f is fired at each time step t
		sequentialTransitions();

		// A place can only have 0, 1, 2, ..., n tokens. Example: if a place has 2 tokens then it cannot have 3 tokens
		tokenRestrictions();

		// Pre-conditions for firing f
		preConditionsTransitions();

		// Post-conditions for firing f
		postConditionsTransitions();	
		
		// if no transitions were fired that used the place p then the marking of p remains the same from times step t to t+1
		noTransitionTokens();
		
	}

	@Override
	public void setState(Set<Pair<Place, Integer>> state, int timestep) {

		Set<Place> visited = new HashSet<Place>();
		for (Pair<Place, Integer> p : state) {
			Triple<Place, Integer, Integer> place = new ImmutableTriple<Place, Integer, Integer>(p.getLeft(), timestep, p.getRight());
			int v = place2variable.get(place).getId();
			solver.setTrue(v);
			visited.add(p.getLeft());
		}
	}

}
