package cmu.symonster;

public class MyPoint {

	private int x;
	private int y;
	
	public int getX(){
		return x;
	}
	
	public int getY(){
		return y;
	} 
	
	public MyPoint(int x, int y){
		this.x = x;
		this.y = y;
	}
	
	static int convert(MyPoint sypet_arg0){
		int sypet_var2 = sypet_arg0.getY();
		return sypet_var2;
	}
	
	public static void main(String[] args){
		cmu.symonster.MyPoint mp = new cmu.symonster.MyPoint(1,3);
		System.out.println("x= " + mp.getX());
		System.out.println("convert= " + convert(mp));
	}
}
