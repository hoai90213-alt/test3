package characters;

import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;

import game.GameMain;
import objects.Box;
import utils.*;

public class Player extends Character{
	public Box box;
	public Vector2f oldPos;
	public Vector2f camOffset = new Vector2f(0, 0);
	
	private boolean canProcessKeyInput = true;
	private float processKeyCounter = 0f;
	private float keyWTime, keyATime, keySTime, keyDTime;
	
	private float smoothMoveCounter = 0f;
	private boolean canSmoothMove = false;
	
	private boolean canProcessShootInput = true;
	private float processShootCounter = 0f;
	private static final float shootRate = 0.1f;
	
	public Player(Vector2f pos){
		box = new Box(pos, new Color(0f, 0.7f, 0f), new Color(0f, 0.5f, 0f));
		oldPos = new Vector2f(box.pos);
	}
	
	public void update(float delta, long window){
		processKeyCounter += delta;
		if(processKeyCounter >= GameMain.updateTime){
			canProcessKeyInput = true;
		}
		
		processShootCounter += delta;
		if(processShootCounter >= shootRate){
			canProcessShootInput = true;
		}
		
		DoubleBuffer mouseXBuffer = BufferUtils.createDoubleBuffer(1);
		DoubleBuffer mouseYBuffer = BufferUtils.createDoubleBuffer(1);
		GLFW.glfwGetCursorPos(window, mouseXBuffer, mouseYBuffer);
		
		IntBuffer widthBuffer = BufferUtils.createIntBuffer(1);
		IntBuffer heightBuffer = BufferUtils.createIntBuffer(1);
		GLFW.glfwGetWindowSize(window, widthBuffer, heightBuffer);
		
		camOffset.x = (float)(mouseXBuffer.get(0) - widthBuffer.get(0) / 2) / -widthBuffer.get(0) / 10f;
		camOffset.y = (float)(mouseYBuffer.get(0) - heightBuffer.get(0) / 2) / heightBuffer.get(0) / 10f;
		
		if(canProcessShootInput){
			processShootCounter = 0f;
			canProcessShootInput = false;
			
			boolean mouseLeft = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
			
			if(mouseLeft){
				Vector2f direction = new Vector2f((float)(mouseXBuffer.get(0) - (widthBuffer.get(0) / 2)), (float)(-mouseYBuffer.get(0) + (heightBuffer.get(0) / 2))).normalize();
				
				GameMain.getInstance().projectileManager.spawnOneWithSpread(box.drawPos, direction, 5f, 0);
			}else{
				canProcessShootInput = true;
			}
		}
		
		if(canProcessKeyInput){
			processKeyCounter = 0f;
			canProcessKeyInput = false;
			
			boolean keyW = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS;
			boolean keyA = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS;
			boolean keyS = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS;
			boolean keyD = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS;
			
			if((keyW || keyA || keyS || keyD) && (!keyW || !keyS) && (!keyA || !keyD)){
				Vector2f direction = new Vector2f(0, 0);
				
				if(keyW)
					keyWTime += delta;
				else
					keyWTime = 0f;
				if(keyA)
					keyATime += delta;
				else
					keyATime = 0f;
				if(keyS)
					keySTime += delta;
				else
					keySTime = 0f;
				if(keyD)
					keyDTime += delta;
				else
					keyDTime = 0f;
				
				Float[] floats = {
					keyWTime == 0 ? Float.MAX_VALUE : keyWTime,
					keyATime == 0 ? Float.MAX_VALUE : keyATime,
					keySTime == 0 ? Float.MAX_VALUE : keySTime,
					keyDTime == 0 ? Float.MAX_VALUE : keyDTime
				};
				
				Arrays.sort(floats);
				
				for(int i = 0; i < 4; i++){
					if(floats[i] == keyWTime){
						if(GameMain.getInstance().boxManager.getBoxAt(MathUtils.add(new Vector2f(0f, 1f), box.pos)) == null){
							direction.y += 1;
							break;
						}
					}
					if(floats[i] == keyATime){
						if(GameMain.getInstance().boxManager.getBoxAt(MathUtils.add(new Vector2f(-1f, 0f), box.pos)) == null){
							direction.x -= 1;
							break;
						}
					}
					if(floats[i] == keySTime){
						if(GameMain.getInstance().boxManager.getBoxAt(MathUtils.add(new Vector2f(0f, -1f), box.pos)) == null){
							direction.y -= 1;
							break;
						}
					}
					if(floats[i] == keyDTime){
						if(GameMain.getInstance().boxManager.getBoxAt(MathUtils.add(new Vector2f(1f, 0f), box.pos)) == null){
							direction.x += 1;
							break;
						}
					}
				}
				
				if(direction.x != 0f || direction.y != 0f){
					oldPos = new Vector2f(box.pos);
					box.drawPos = new Vector2f(box.pos);
					box.pos = MathUtils.add(direction, box.pos);
					
					canSmoothMove = true;
					smoothMoveCounter = 0f;
					box.verticalOffset = 0f;
				}
			}else{
				canProcessKeyInput = true;
			}
		}
		
		if(canSmoothMove){
			smoothMoveCounter += delta;
			box.drawPos = MathUtils.lerp(oldPos, box.pos, smoothMoveCounter / (GameMain.updateTime - GameMain.smoothMoveEndDelay));
			box.verticalOffset = GameMain.moveJumpHeight * (float)Math.sin(Math.toRadians(smoothMoveCounter / (GameMain.updateTime - GameMain.smoothMoveEndDelay) * 180f));
			
			if(smoothMoveCounter >= GameMain.updateTime - GameMain.smoothMoveEndDelay){
				box.drawPos = new Vector2f(box.pos);
				box.verticalOffset = 0f;
				canSmoothMove = false;
			}
		}
	}
}
