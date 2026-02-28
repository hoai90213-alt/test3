package utils;

import java.util.*;

import org.lwjgl.opengl.*;

import game.GameMain;

public class DebugDraw {
	private static final float removeDelay = 0.1f;
	
	private float removeCounter = 0f;
	private LinkedList<Vector2f> list = new LinkedList<Vector2f>();
	
	public void add(Vector2f pos){
		if(!list.contains(pos))
			list.addLast(new Vector2f(pos));
	}
	
	public void update(float delta){
		if(list.isEmpty()){
			removeCounter = 0f;
		}else{
			removeCounter += delta;
			if(removeCounter >= removeDelay){
				list.removeFirst();
				removeCounter = 0f;
			}
			draw();
		}
	}
	
	public void draw(){
		GL11.glColor3f(1f, 1f, 0f);
		GL11.glBegin(GL11.GL_QUADS);
		
		for(Vector2f v : list){
			float x = -0.5f * GameMain.getInstance().boxWidth / 4f + v.x * GameMain.getInstance().boxWidth - GameMain.getInstance().player.box.drawPos.x * GameMain.getInstance().boxWidth; //left
			float y = -0.5f * GameMain.getInstance().boxHeight / 4f + v.y * GameMain.getInstance().boxHeight - GameMain.getInstance().player.box.drawPos.y * GameMain.getInstance().boxHeight; //down
			
			GL11.glVertex2f(x, y + GameMain.getInstance().boxHeight / 4f);
			GL11.glVertex2f(x, y);
			GL11.glVertex2f(x + GameMain.getInstance().boxWidth / 4f, y);
			GL11.glVertex2f(x + GameMain.getInstance().boxWidth / 4f, y + GameMain.getInstance().boxHeight / 4f);
		}
		
		GL11.glEnd();
		GL11.glColor3f(1f, 1f, 1f);
	}
}
