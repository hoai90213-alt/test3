package utils;

public class MathUtils {
	public static Vector2f add(Vector2f v1, Vector2f v2){
		return new Vector2f(v1.x + v2.x, v1.y + v2.y);
	}
	
	public static Vector2f subtract(Vector2f v1, Vector2f v2){
		return new Vector2f(v1.x - v2.x, v1.y - v2.y);
	}
	
	public static Vector2f scale(Vector2f v, float s){
		return new Vector2f(v.x * s, v.y * s);
	}
	
	public static float lerp(float f1, float f2, float percentage){ //percentage is from 0 to 1
		return f1 + ((f2 - f1) * percentage);
	}
	
	public static Vector2f lerp(Vector2f v1, Vector2f v2, float percentage){ //percentage is from 0 to 1
		return new Vector2f(lerp(v1.x, v2.x, percentage), lerp(v1.y, v2.y, percentage));
	}
	
	public static Vector2f rotate(Vector2f v, Vector2f center, float degrees){
		if(degrees >= 360f)
			degrees %= 360f;
		if(degrees == 0f)
			return v;
		
		return new Vector2f(
			(float)(center.x + (v.x - center.x) * Math.cos(Math.toRadians(degrees)) - (v.y - center.y) * Math.sin(Math.toRadians(degrees))),
			(float)(center.y + (v.x - center.x) * Math.sin(Math.toRadians(degrees)) + (v.y - center.y) * Math.cos(Math.toRadians(degrees)))
		);
	}
	
	public static Vector2f rotate(Vector2f v, float degrees){
		if(degrees >= 360f)
			degrees %= 360f;
		if(degrees == 0f)
			return v;
		
		return new Vector2f(
			(float)(v.x * Math.cos(Math.toRadians(degrees)) - v.y * Math.sin(Math.toRadians(degrees))),
			(float)(v.x * Math.sin(Math.toRadians(degrees)) + v.y * Math.cos(Math.toRadians(degrees)))
		);
	}
}
