package cmu.symonster;

public class Rectangle extends Shape {
	
	private MyPoint p1;
	private MyPoint p2;
	private MyPoint p3;
	private MyPoint p4;
	String name;
	
	public Rectangle(MyPoint p1, MyPoint p2, MyPoint p3, MyPoint p4){
		coordinates.add(p1);
		coordinates.add(p2);
		coordinates.add(p3);
		coordinates.add(p4);
		
		this.p1 = p1;
		this.p2 = p2;
		this.p3 = p3;
		this.p4 = p4;
		
		name = "";
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName(){
		return name;
	}
	
}
