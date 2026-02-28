package objects;

import java.util.*;

import game.GameMain;
import utils.*;

public class Projectile {
	public static final int vertexCount = 6;
	
	private Color color;
	
	public Vector2f startPos;
	public Vector2f velocity = new Vector2f(0, 0);
	public float constRot = 0f;
	public float lifeTime;
	public float damage;
	public int team; //0 = player, 1 = AI
	
	private LinkedList<State> states = new LinkedList<State>();
	
	private float currentTime = 0f;
	private Vector2f currentPos;
	private float currentRot = 0f;
	
	public Vector2f actualPos;
	
	public Projectile(Vector2f startPos, float lifeTime, float damage, int team, Color color){
		this.startPos = new Vector2f(startPos);
		this.currentPos = new Vector2f(startPos);
		this.actualPos = new Vector2f(startPos);
		this.lifeTime = lifeTime;
		this.damage = damage;
		this.team = team;
		this.color = color;
	}
	
	public boolean update(float delta){
		currentTime += delta;
		
		if(currentTime >= lifeTime){
			return false;
		}
		
		while(!states.isEmpty() && currentTime > states.getFirst().time){
			State s = states.removeFirst();
			
			if(s.state == 0 || s.state == 1){
				velocity = s.velocity;
				constRot = s.constRot;
			}else if(s.state == 2){
				
			}
		}
		
		currentPos = MathUtils.add(currentPos, MathUtils.scale(velocity, delta));
		currentRot += constRot * delta;
		currentRot %= 360f;
		
		Vector2f newPos = MathUtils.rotate(currentPos, startPos, currentRot);
		
		int collisionResult = handleCollision(new Vector2f((float)Math.floor(newPos.x), (float)Math.floor(newPos.y)));
		
		if(collisionResult == 1 || collisionResult == 2 || collisionResult == 3){
			return false;
		}
		
		actualPos = new Vector2f(newPos);
		
		return true;
	}
	
	private int handleCollision(Vector2f newPos){
		if(GameMain.getInstance().boxManager.getBoxAt(newPos) == null){
			return 0;
		}else{
			if(GameMain.getInstance().player.box.pos.equals(newPos)){
				if(team == 1){
					GameMain.getInstance().player.removeHealth(damage);
					return 2;
				}else{
					return 0;
				}
			}
			for(int i = 0; i < GameMain.getInstance().aiArray.size(); i++){
				if(GameMain.getInstance().aiArray.get(i).box.pos.equals(newPos)){
					if(team == 0){
						GameMain.getInstance().aiArray.get(i).removeHealth(damage);
						return 3;
					}else{
						return 0;
					}
				}
			}
			return 1;
		}
	}
	
	public Projectile setInitial(Vector2f velocity){
		this.velocity = new Vector2f(velocity);
		return this;
	}
	
	public Projectile setInitial(float constRot, Vector2f velocity){
		this.constRot = constRot;
		this.velocity = velocity;
		return this;
	}
	
	public Projectile addChange(float time, Vector2f velocity){
		states.add(new State(time, velocity));
		return this;
	}
	
	public Projectile addChange(float time, float constRot, Vector2f velocity){
		states.add(new State(time, constRot, velocity));
		return this;
	}
	
	public Projectile addChange(float time, int count, float velocity){
		states.add(new State(time, count, velocity));
		return this;
	}
	
	public float[] getColorArray(){
		float colorScale = actualPos.distTo(GameMain.getInstance().player.box.drawPos) / 10f;
		if(colorScale < 1f)
			colorScale = 1f;
		
		return new float[]{
			color.r / colorScale, color.g / colorScale, color.b / colorScale,
			color.r / colorScale, color.g / colorScale, color.b / colorScale,
			color.r / colorScale, color.g / colorScale, color.b / colorScale,
			color.r / colorScale, color.g / colorScale, color.b / colorScale,
			color.r / colorScale, color.g / colorScale, color.b / colorScale,
			color.r / colorScale, color.g / colorScale, color.b / colorScale
		};
	}
	
	public float[] getVertexArray(){
		float bW = GameMain.getInstance().boxWidth / 4f;
		float bH = GameMain.getInstance().boxHeight / 4f;
		float bX = actualPos.x * GameMain.getInstance().boxWidth + (GameMain.getInstance().boxOffset.x / 4f) - GameMain.getInstance().player.box.drawPos.x * GameMain.getInstance().boxWidth + GameMain.getInstance().player.camOffset.x; //left
		float bY = actualPos.y * GameMain.getInstance().boxHeight + (GameMain.getInstance().boxOffset.y / 4f) - GameMain.getInstance().player.box.drawPos.y * GameMain.getInstance().boxHeight + GameMain.getInstance().player.camOffset.y; //down
		
		return new float[]{
			bX, bY + bH,
			bX, bY,
			bX + bW, bY + bH,
			bX, bY,
			bX + bW, bY,
			bX + bW, bY + bH
		};
	}
	
	public static class State{
		public float time;
		public int state; //0 = change to velocity, 1 = change to rotation, 2 = spawn more
		
		public Vector2f velocity;
		
		public float constRot;
		
		public int count;
		public float spawnVelocity;
		
		public State(float time, Vector2f velocity){
			this.velocity = new Vector2f(velocity);
			this.time = time;
			constRot = 0f;
			state = 0;
		}
		
		public State(float time, float constRot, Vector2f velocity){
			this.constRot = constRot;
			this.velocity = new Vector2f(velocity);
			this.time = time;
			state = 1;
		}
		
		public State(float time, int count, float velocity){
			this.count = count;
			this.spawnVelocity = velocity;
			this.time = time;
			state = 2;
		}
	}
}
