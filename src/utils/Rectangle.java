package utils;

public class Rectangle {
	public Vector2f pos, size;
	
	public Rectangle(Vector2f pos, Vector2f size){
		this.pos = new Vector2f(pos);
		this.size = new Vector2f(size);
	}
	
	public Rectangle(float x, float y, float width, float height){
		this.pos = new Vector2f(x, y);
		this.size = new Vector2f(width, height);
	}
}
