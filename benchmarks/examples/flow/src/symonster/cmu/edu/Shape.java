package symonster.cmu.edu;

public class Shape {
	
	protected int x;
	protected int y;
	protected int z;
	
	public Shape(int x, int y, int z){
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Shape(){
		x = y = z = 0;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public void setY(int y) {
		this.y = y;
	}
	
	public void setZ(int z) {
		this.z = z;
	}
	
	public void setXYZ (int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public void setXY (int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public int getZ() {
		return z;
	}

}
