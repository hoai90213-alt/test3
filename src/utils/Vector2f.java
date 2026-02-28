package utils;

public class Vector2f {
	public float x, y;
	
	public Vector2f(float x, float y){
		this.x = x;
		this.y = y;
	}
	
	public Vector2f(Vector2f vector){
		this.x = vector.x;
		this.y = vector.y;
	}
	
	public Vector2f normalize(){
		float magnitude = magnitude();
		x /= magnitude;
		y /= magnitude;
		return this;
	}
	
	public float magnitude(){
		return (float)Math.sqrt(x * x + y * y);
	}
	
	public float distTo(Vector2f other){
		return Math.abs(x - other.x) + Math.abs(y - other.y);
	}
	
	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(x);
		result = prime * result + Float.floatToIntBits(y);
		return result;
	}
	
	@Override
	public boolean equals(Object obj){
		Vector2f other = (Vector2f)obj;
		return this.x == other.x && this.y == other.y;
	}
}
