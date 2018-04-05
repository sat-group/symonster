package edu.cmu.reachability;

public class Variable implements Comparable<Variable>{
	
	enum Type { FLOWPLACE, PLACE, TRANSITION; }
	
	private int id;
	private String name;
	private int timestep;
	private int value;
	private int flows;
	private Type type;
	
	public Variable(int id, String name, Type type){
		this.id = id;
		this.name = name;
		this.timestep = 0;
		this.value = 0;
		this.type = type; 
	}
	
	public Variable(int id, String name, Type type, int timestep){
		this.id = id;
		this.name = name;
		this.timestep = timestep;
		this.value = 0;
		this.type = type; 		
	}
	
	public Variable(int id, String name, Type type, int timestep, int value){
		this.id = id;
		this.name = name;
		this.timestep = timestep;
		this.value = value;
		this.type = type; 		
	}
	
	public Variable(int id, String name, Type type, int timestep, int value, int flows){
		this.id = id;
		this.name = name;
		this.timestep = timestep;
		this.value = value;
		this.type = type; 	
		this.flows = flows;
	}

	
	public int getId(){
		return id;
	}
	
	public String getName(){
		return name;
	}
	
	public int getTimestep(){
		return timestep;
	}
	
	public int getValue(){
		return value;
	}
	
	public Type getType(){
		return type;
	}
	
	@Override
	public String toString(){
		String res = "";
		if (type == Type.TRANSITION)
			res += "[" + name + "]" + "(id=" + id + ",Type=" + type + ",timestep=" + timestep + ")";
		else if (type == Type.PLACE)
			res += "[" + name + "]" + "(id=" + id + ",Type=" + type + ",timestep=" + timestep + ",value=" + value + ")";
		else if (type == Type.FLOWPLACE)
			res += "[" + name + "]" + "(id=" + id + ",Type=" + type + ",id=" + value + ",flows=" + flows + ",timestep=" + timestep + ")";
		
		return res;
	}
	
	@Override
	public int compareTo(Variable var) {
		return this.timestep - var.timestep;
	}

}
