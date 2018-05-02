package cmu.symonster;

public class TriangleB extends Triangle {
	
	private MyPoint p1;
	private MyPoint p2;
	private MyPoint p3;
	
	String name;
	String type;

	public TriangleB(MyPoint p1, MyPoint p2, MyPoint p3) {
		super(p1, p2, p3);
		this.p1 = p1;
		this.p2 = p2;
		this.p3 = p3;
		
		name = "";
		type = "";
	}
	
	public MyPoint getP1() {
		return p1;
	}
	
	public MyPoint getP2() {
		return p2;
	}
	
	public MyPoint getP3() {
		return p3;
	}
	
	public void setName(String s) {
		this.name = s;
	}
	
	public void setType(String s) {
		this.type = s;
	}

	

}
