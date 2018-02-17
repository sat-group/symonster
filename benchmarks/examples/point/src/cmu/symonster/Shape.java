package cmu.symonster;

import java.util.Vector;

public class Shape {
	
	protected Vector<MyPoint> coordinates;
	
	public Shape(){
		coordinates = new Vector<>();
	};
	
	public Vector<MyPoint> getCoordinates(){
		return coordinates;
	}
	
}
