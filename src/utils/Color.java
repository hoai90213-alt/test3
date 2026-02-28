package utils;

public class Color {
	public float r, g, b, a;
	
	public Color(float r, float g, float b){
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = 1.0f;
	}
	
	public Color(float r, float g, float b, float a){
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}
	
	public Color(Color color){
		this.r = color.r;
		this.g = color.g;
		this.b = color.b;
		this.a = color.a;
	}
	
	public float[] toArray3f(){
		return new float[]{r, g, b};
	}
	
	public float[] toArray4f(){
		return new float[]{r, g, b, a};
	}
}
