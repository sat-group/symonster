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
			solver.addExactly(constraint, 1);
		}
	}

	private void postConditionsTransitions() {
		// loop for each time step t
		for (int t = 0; t < loc; t++) {
			// loop for each transition
			for (Transition tr : pnet.getTransitions()) {

				for (Flow f : tr.getPostsetEdges()) {
					Place p = f.getPlace();

					Pair<Transition, Integer> transition = new ImmutablePair<Transition, Integer>(tr, t);
					Variable fireTr = transition2variable.get(transition);

					for (int w = 0; w < p.getMaxToken(); w++) {
						Triple<Place, Integer, Integer> placeBefore = new ImmutableTriple<Place, Integer, Integer>(p, t, w);
						Triple<Place, Integer, Integer> placeAfter = new ImmutableTriple<Place, Integer, Integer>(p, t + 1, w + 1);
						VecInt state = new VecInt();

						Variable previousState = place2variable.get(placeBefore);
						state.push(fireTr.getId());
						state.push(previousState.getId());

						Variable nextState = place2variable.get(placeAfter);
						// if f is fired then the number of tokens is increased in the target place
						solver.addPostConditions(state, nextState.getId());
					}
				}
				
				for (Flow f: tr.getPresetEdges()){
					Place p = f.getPlace();
					
					Pair<Transition, Integer> transition = new ImmutablePair<Transition, Integer>(tr, t);
					Variable fireTr = transition2variable.get(transition);
					
					int weight = f.getWeight();

					for (int w = weight; w <= p.getMaxToken(); w++) {
						Triple<Place, Integer, Integer> placeBefore = new ImmutableTriple<Place, Integer, Integer>(p, t, w);
						Triple<Place, Integer, Integer> placeAfter = new ImmutableTriple<Place, Integer, Integer>(p, t + 1, w - weight);
						VecInt state = new VecInt();

						Variable previousState = place2variable.get(placeBefore);
						state.push(fireTr.getId());
						state.push(previousState.getId());

						Variable nextState = place2variable.get(placeAfter);
						// if f is fired then the number of tokens is decreased in the source place
						solver.addPostConditions(state, nextState.getId());
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
				
				solver.addPreconditions(fireTr.getId(), preconditions);

				for (Flow f : tr.getPostsetEdges()) {
					VecInt pre = new VecInt();
					Place p = f.getPlace();
					Triple<Place, Integer, Integer> triple = new ImmutableTriple<Place, Integer, Integer>(p, t, p.getMaxToken());
					Variable v = place2variable.get(triple);
					pre.push(-v.getId());
					// if is fired then we are not at maximum resources in the target
					solver.addPreconditions(fireTr.getId(), pre);
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
				solver.addExactly(amo, 1);
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
					solver.addSameTokens(transitionsConstr, place2variable.get(current).getId(), place2variable.get(next).getId());
				}
			}
		}
	}

	private void dummyConstraints() {

		VecInt constraint = new VecInt();
		for (int v = 1; v <= nbVariables; v++) {
			constraint.push(v);
		}
		solver.addAtLeast(constraint, 0);

	}
	
	@Override
	public void setPetriNet(PetriNet pnet) {
		this.pnet = pnet;
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

		// FIXME: quick hack to guarantee that all variables are used
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

		// loop for each place and set those that do not appear as false
		for (Place p : pnet.getPlaces()) {
			if ((p.getId().equals("void") && timestep == loc) || visited.contains(p)) {
				// ignore void for final target or if it is in the state
				continue;
			}

			// if it is not in the state set its value to 0
			Triple<Place, Integer, Integer> place = new ImmutableTriple<Place, Integer, Integer>(p, timestep, 0);
			int v = place2variable.get(place).getId();
			solver.setTrue(v);
		}
	}

}
