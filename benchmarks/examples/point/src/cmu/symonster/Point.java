package cmu.symonster;

public class Point {
	
	private int x;
	private int y;
	private double pitch;
	private double roll;
	private double yaw;
	private String name;
	
	public void setName(String name){
		this.name = name;
	}
	
	public String getName(){
		return name;
	}
	
	public void setX(int x){
		this.x = x;
	}
	
	public void setY(int y){
		this.y = y;
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
	
	public void setPitch(double p){
		pitch = p;
	}
	
	public void setRoll(double r){
		roll = r;
	}
	
	public double getRoll(){
		return roll;
	}
	
	public void setYaw(double y){
		yaw = y;
	}
	
	public double getYaw(){
		return yaw;
	}
	
	public Point(){
		x = 0;
		y = 0;
		pitch = 0.0;
		roll = 0.0;
		yaw = 0.0;
		name = "";
	}
}
