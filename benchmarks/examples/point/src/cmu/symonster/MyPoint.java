package cmu.symonster;

public class MyPoint {

	private int x;
	private int y;
	private double pitch;
	private double roll;
	private double yaw;
	private String name;
	
	public void setName(String name){
		this.name = name;
	}
	
	public int getX(){
		return x;
	}
	
	public int getY(){
		return y;
	} 
	
	public double getPitch(){
		return pitch;
	}
	
	public double getRoll(){
		return roll;
	}
	
	public double getYaw(){
		return yaw;
	}
	
	public String getName(){
		return name;
	}
	
	public MyPoint(String name){
		this.name = name;
	}
	
	public MyPoint(int x, int y){
		this.x = x;
		this.y = y;
		pitch = 0.0;
		roll = 0.0;
		yaw = 0.0;
		name = "";
	}
	
	public MyPoint(double pitch, double roll, double yaw){
		x = 0;
		y = 0;
		name = "";
		this.pitch = pitch;
		this.roll = roll;
		this.yaw = yaw;
			
	}
	
	public MyPoint(int x, int y, double pitch, double roll, double yaw, String name){
		this.x = x;
		this.y = y;
		this.pitch = pitch;
		this.roll = roll;
		this.yaw = yaw;
		this.name = name;
	}
}
