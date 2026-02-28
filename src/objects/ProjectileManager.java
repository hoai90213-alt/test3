package objects;

import java.nio.*;
import java.util.*;

import org.lwjgl.*;
import org.lwjgl.opengl.*;

import utils.Color;
import utils.MathUtils;
import utils.Vector2f;

public class ProjectileManager {
	public HashSet<Projectile> projectiles = new HashSet<Projectile>();
	
	private int vertexVBOID;
	private int colorVBOID;
	private FloatBuffer vertexData;
	private FloatBuffer colorData;
	
	public void init(){
		vertexVBOID = GL15.glGenBuffers();
		colorVBOID = GL15.glGenBuffers();
	}
	
	public void update(float delta){
		vertexData = BufferUtils.createFloatBuffer(projectiles.size() * Projectile.vertexCount * 2);
		colorData = BufferUtils.createFloatBuffer(projectiles.size() * Projectile.vertexCount * 3);
		
		Iterator<Projectile> it = projectiles.iterator();
		ArrayList<Projectile> removeArray = new ArrayList<Projectile>();
		
		while(it.hasNext()){
			Projectile p = it.next();
			
			vertexData.put(p.getVertexArray());
			colorData.put(p.getColorArray());
			
			if(!p.update(delta))
				removeArray.add(p);
		}
		
		projectiles.removeAll(removeArray);
		
		vertexData.flip();
		colorData.flip();
		
		draw();
	}
	
	private void draw(){
		GL20.glEnableVertexAttribArray(0);
		GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexVBOID);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexData, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 0, 0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, colorVBOID);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, colorData, GL15.GL_STATIC_DRAW);
		GL11.glColorPointer(3, GL11.GL_FLOAT, 0, 0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, projectiles.size() * Projectile.vertexCount);
		
		GL20.glDisableVertexAttribArray(0);
		GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
	}
	
	public void destroy(){
		GL15.glDeleteBuffers(vertexVBOID);
		GL15.glDeleteBuffers(colorVBOID);
	}
	
	public void spawn360(Vector2f pos, int count, int team){
		for(int i = 0; i < count; i++){
			Projectile p = new Projectile(pos, 10f, 100f, team, new Color(1f, 1f, 0f));
			p.setInitial(MathUtils.rotate(new Vector2f(5f, 0f), i * 360f / count));
//			p.addChange(3f, 100f, MathUtils.rotate(new Vector2f(1f, 0f), i * 360f / count));
//			p.addChange(6f, 100f, MathUtils.rotate(new Vector2f(-1f, 0f), i * 360f / count));
//			p.addChange(9f, -100f, MathUtils.rotate(new Vector2f(1f, 0f), i * 360f / count));
			
			projectiles.add(p);
		}
	}
	
	public void spawnOneWithSpread(Vector2f pos, Vector2f direction, float randomRot, int team){
		Projectile p = new Projectile(pos, 5f, 100f, team, new Color(1f, 1f, 0f));
		p.setInitial(MathUtils.rotate(MathUtils.scale(direction, 20f), new Random().nextInt((int)(randomRot * 2f + 1f)) - randomRot));
		projectiles.add(p);
	}
}
