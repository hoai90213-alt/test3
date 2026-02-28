package game;

import java.util.*;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import characters.*;
import objects.*;
import utils.*;

public class GameMain {
	public static final float boxSize = 30f; //in pixels
	public static final float updateTime = 0.3f; //in seconds
	public static final float smoothMoveEndDelay = 0.1f; //in seconds
	public static final float moveJumpHeight = 0.5f;
	
	private static GameMain gameMain;
	private boolean shouldClose = false;
	
	public float boxWidth;
	public float boxHeight;
	public Vector2f boxOffset;
	public BoxManager boxManager;
	
	public ProjectileManager projectileManager;
	
	public Player player;
	
	public ArrayList<AI> aiArray = new ArrayList<AI>();
	
	//public DebugDraw debug = new DebugDraw();
	
	public static GameMain getInstance(){
		return gameMain;
	}
	
	public void close(){
		shouldClose = true;
	}
	
	private void run(){
		long window = initGL();
		float lastTime = (float)GLFW.glfwGetTime();
		
		init();
		
		while(!GLFW.glfwWindowShouldClose(window) && !shouldClose){
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
			
			float currTime = (float)GLFW.glfwGetTime();
			
			update(currTime - lastTime, window);
			
			lastTime = currTime;
			
			GLFW.glfwPollEvents();
	        GLFW.glfwSwapBuffers(window);
		}
		
		destroy();
		
		GLFW.glfwDestroyWindow(window);
		GLFW.glfwTerminate();
	}
	
	private void update(float delta, long window){
		projectileManager.update(delta);
		
		boxManager.update();
		
		player.update(delta, window);
		
		for(int i = 0; i < aiArray.size(); i++){
			aiArray.get(i).update(delta);
		}
		
		//debug.update(delta);
	}
	
	private void init(){
		player = new Player(new Vector2f(0f, 0f));
		
//		aiArray.add(new MeleeAI(new Vector2f(-11f, -11f)));
//		aiArray.add(new MeleeAI(new Vector2f(-12f, -12f)));
//		aiArray.add(new MeleeAI(new Vector2f(-11f, -12f)));
//		aiArray.add(new MeleeAI(new Vector2f(-12f, -11f)));
//		aiArray.add(new MeleeAI(new Vector2f(-11f, -13f)));
//		aiArray.add(new MeleeAI(new Vector2f(-12f, -13f)));
		
		boxManager = new BoxManager();
		
		for(int i = -50; i < 50; i += 3){
			for(int j = -50; j < 50; j += 3){
				boxManager.createBox(new Vector2f(i, j));
			}
		}
		
		boxManager.boxes.add(player.box);
		
		for(int i = 0; i < aiArray.size(); i++){
			boxManager.boxes.add(aiArray.get(i).box);
		}
		
		boxManager.init();
		
		projectileManager = new ProjectileManager();
		projectileManager.init();
	}
	
	private void destroy(){
		boxManager.destroy();
		projectileManager.destroy();
	}
	
	private long initGL(){
		long window;
		
		GLFW.glfwInit();
		
		GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, 4);
		GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_ANY_PROFILE);
		GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GL11.GL_FALSE);
		
		window = GLFW.glfwCreateWindow(2880, 1620, "AI Game", 0, 0);
		
		GLFW.glfwMakeContextCurrent(window);
		GLFW.glfwSwapInterval(1);
		GLFW.glfwShowWindow(window);
		GL.createCapabilities();
		GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glCullFace(GL11.GL_BACK);
		
		boxWidth = boxSize / 1920f;
		boxHeight = boxSize / 1080f;
		boxOffset = new Vector2f(-0.5f * boxWidth, -0.5f * boxHeight);
		
		return window;
	}
	
	public static void main(String[] args){
		gameMain = new GameMain();
		gameMain.run();
	}
}
