package cmu.symonster;

public class Rectangle extends Shape {
	
	public Rectangle(MyPoint p1, MyPoint p2, MyPoint p3, MyPoint p4){
		coordinates.add(p1);
		coordinates.add(p2);
		coordinates.add(p3);
		coordinates.add(p4);
	}
}
