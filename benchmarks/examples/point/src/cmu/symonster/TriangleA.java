package cmu.symonster;

public class TriangleA extends Triangle {
	
	private MyPoint p1;
	private MyPoint p2;
	private MyPoint p3;
	
	public TriangleA(MyPoint p1, MyPoint p2, MyPoint p3) {
		super(p1, p2, p3);
		this.p1 = p1;
		this.p2 = p2;
		this.p3 = p3;
	}

}
