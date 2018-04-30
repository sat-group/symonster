package edu.cmu.reachability;

import java.util.*;

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

public class ParallelEncoding implements Encoding {

    int loc = 1;
    PetriNet pnet = null;
    int nbVariables = 1;
    int nbConstraints = 0;

    public ParallelEncoding(PetriNet pnet, int loc) {
        this.pnet = pnet;
        this.loc = loc;

        // clean the data structures before creating a new encoding
        place2variable.clear();
        transition2variable.clear();
        solver.reset();

        createVariables();
        createConstraints();
    }

    /*
     * Checks if two transitions share some input or output
     */
    private boolean isIndependent(Transition t1, Transition t2) {
        Set<Flow> inFlow1 = t1.getPresetEdges();
        Set<Flow> inFlow2 = t2.getPresetEdges();
        Set<Place> inputs1 = new HashSet<>();
        Set<Place> inputs2 = new HashSet<>();
        for(Flow f : inFlow1) {
            Place p = f.getPlace();
            inputs1.add(p);
        }
        for(Flow f : inFlow2) {
            Place p = f.getPlace();
            inputs2.add(p);
        }

        Set<Flow> outFlow1 = t1.getPostsetEdges();
        Set<Flow> outFlow2 = t2.getPostsetEdges();
        Set<Place> outputs1 = new HashSet<>();
        Set<Place> outputs2 = new HashSet<>();
        for(Flow f : outFlow1) {
            Place p = f.getPlace();
            outputs1.add(p);
        }
        for(Flow f : outFlow2) {
            Place p = f.getPlace();
            outputs2.add(p);
        }

        return (Collections.disjoint(inputs1, inputs2) && Collections.disjoint(outputs1, outputs2));
    }


    /*
     * If two transition share no input or output, they can be fired at same time
     */
    private void parallelConstraints() {
        // loop for each time step t
        for (int t = 0; t < loc; t++) {
            // loop for each transition
            VecInt constraintTotal = new VecInt();
            for (Transition tr1 : pnet.getTransitions()) {
                Pair<Transition, Integer> pair1 = new ImmutablePair<Transition, Integer>(tr1, t);
                Variable var1 = transition2variable.get(pair1);
                constraintTotal.push(var1.getId());

                // loop for each transition
                for(Transition tr2 : pnet.getTransitions()) {
                    if(tr1.getId() == tr2.getId()) {
                        continue;
                    }
                    if(isIndependent(tr1, tr2)) { // same transition...
                        continue;
                    }
                    VecInt constraintDependent = new VecInt();
                    Pair<Transition, Integer> pair2 = new ImmutablePair<>(tr2, t);
                    Variable var2 = transition2variable.get(pair2);
                    constraintDependent.push(var1.getId());
                    constraintDependent.push(var2.getId());
                    solver.addConstraint(constraintDependent, ConstraintType.LTE, 1);
                }
            }
            // add constraints to the solver
            // at least one transition is going to be fired
            solver.addConstraint(constraintTotal, ConstraintType.GTE, 1);
        }
    }

    private void postConditionsTransitions() {
        // loop for each time step t
        for (int t = 0; t < loc; t++) {
            // loop for each transition
            for (Transition tr : pnet.getTransitions()) {

                // collect all places that will have their marking changed
                HashMap<Place, Integer> places_to_be_changed = new HashMap<>();
                for (Flow f : tr.getPostsetEdges()) {
                    Place p = f.getPlace();
                    if (!places_to_be_changed.containsKey(p))
                        places_to_be_changed.put(p, f.getWeight());
                    else
                        places_to_be_changed.put(p, places_to_be_changed.get(p) + f.getWeight());
                }

                for (Flow f : tr.getPresetEdges()) {
                    Place p = f.getPlace();
                    if (!places_to_be_changed.containsKey(p))
                        places_to_be_changed.put(p, -f.getWeight());
                    else
                        places_to_be_changed.put(p, places_to_be_changed.get(p) - f.getWeight());
                }

                Pair<Transition, Integer> transition = new ImmutablePair<Transition, Integer>(tr, t);
                Variable fireTr = transition2variable.get(transition);

                for (Place p : places_to_be_changed.keySet()) {
                    for (int w = 0; w <= p.getMaxToken(); w++) {

                        int diff = places_to_be_changed.get(p);
                        // void always remains the same                                                 ???
                        if (p.getId().equals("void"))
                            diff = 0;

                        if (w + places_to_be_changed.get(p) < 0
                                || w + places_to_be_changed.get(p) > p.getMaxToken())
                            continue;

                        Triple<Place, Integer, Integer> placeBefore = new ImmutableTriple<Place, Integer, Integer>(
                                p, t, w);
                        Triple<Place, Integer, Integer> placeAfter = new ImmutableTriple<Place, Integer, Integer>(p,
                                t + 1, w + diff);

                        Variable previousState = place2variable.get(placeBefore);
                        Variable nextState = place2variable.get(placeAfter);
                        VecInt state = new VecInt(
                                new int[] { -fireTr.getId(), -previousState.getId(), nextState.getId() }); // Why is this true though
                        solver.addConstraint(state, ConstraintType.GTE, 1);
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
                for (VecInt pc : preconditions) {
                    pc.push(-fireTr.getId());
                    solver.addConstraint(pc, ConstraintType.GTE, 1);
                }

                // we cannot fire a transition if we are at max capacity
                // Exception: if the overall difference is zero
                for (Flow f : tr.getPostsetEdges()) {
                    Place p = f.getPlace();
                    int w1 = f.getWeight();
                    Triple<Place, Integer, Integer> triple = new ImmutableTriple<Place, Integer, Integer>(p, t,
                            p.getMaxToken());
                    Variable v = place2variable.get(triple);
                    boolean ok = true;
                    if (p.getId().equals("void"))
                        ok = false;

                    for (Flow o : tr.getPresetEdges()) {
                        Place c = o.getPlace();
                        if (p == c) {
                            int w2 = o.getWeight();
                            // same source as target
                            int diff = w1 - w2;
                            if (diff == 0) {
                                ok = false;
                                break;
                            }
                        }
                    }

                    if (ok) {
                        VecInt clause = new VecInt(new int[] { -v.getId(), -fireTr.getId() });
                        solver.addConstraint(clause, ConstraintType.GTE, 1);
                    }
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
                for (Transition tr : transitions) {
                    Pair<Transition, Integer> pair = new ImmutablePair<Transition, Integer>(tr, t);
                    transitionsConstr.push(transition2variable.get(pair).getId());
                }

                for (int w = 0; w <= p.getMaxToken(); w++) {
                    Triple<Place, Integer, Integer> current = new ImmutableTriple<Place, Integer, Integer>(p, t, w);
                    Triple<Place, Integer, Integer> next = new ImmutableTriple<Place, Integer, Integer>(p, t + 1, w);

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
                    // create a variable with <place in the petri-net,
                    // timestamp, value>
                    Triple<Place, Integer, Integer> triple = new ImmutableTriple<Place, Integer, Integer>(p, t, v);
                    Variable var = new Variable(nbVariables, p.getId(), Type.PLACE, t, v);
                    place2variable.put(triple, var);
                    // solver.id2variable.put(nbVariables, var);
                    // each variable is associated with an id (starts at 1)
                    nbVariables++;
                }
            }
        }

        for (Transition tr : pnet.getTransitions()) {
            for (int t = 0; t < loc; t++) {
                // create a variable with <transition in the petri-net,
                // timestamp>
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
        // These dummy constraints would not be necessary if the above invariant
        // is maintained
        dummyConstraints();

        // Exactly one transition f is fired at each time step t
        parallelConstraints();

        // A place can only have 0, 1, 2, ..., n tokens. Example: if a place has
        // 2 tokens then it cannot have 3 tokens
        tokenRestrictions();

        // Pre-conditions for firing f
        preConditionsTransitions();

        // Post-conditions for firing f
        postConditionsTransitions();

        // if no transitions were fired that used the place p then the marking
        // of p remains the same from times step t to t+1
        noTransitionTokens();

    }

    @Override
    public void setState(Set<Pair<Place, Integer>> state, int timestep) {

        Set<Place> visited = new HashSet<Place>();
        for (Pair<Place, Integer> p : state) {
            Triple<Place, Integer, Integer> place = new ImmutableTriple<Place, Integer, Integer>(p.getLeft(), timestep,
                    p.getRight());
            int v = place2variable.get(place).getId();
            solver.setTrue(v);
            visited.add(p.getLeft());
        }
    }

	@Override
	public List<Integer> getFState(Set<Pair<Place, Integer>> state, int timestep) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateSAT(int loc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void forceTransition(String api) {
		// TODO Auto-generated method stub
		
	}

}
