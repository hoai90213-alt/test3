package characters;

import java.util.*;

import game.GameMain;
import objects.Box;
import utils.*;

public class MeleeAI extends AI{
	public MeleeAI(Vector2f pos){
		updateTime = 0.3f;
		searchRange = 30f;
		box = new Box(pos, new Color(0.7f, 0f, 0f), new Color(0.5f, 0f, 0f));
	}
	
	@Override
	public void update(float delta){
		processCounter += delta;
		if(processCounter >= updateTime){
			canProcess = true;
		}
		
		if(canProcess){
			processCounter = 0f;
			canProcess = false;
			
			if(box.pos.distTo(GameMain.getInstance().player.box.pos) <= searchRange && box.pos.distTo(GameMain.getInstance().player.box.pos) > 1){
				ArrayList<Vector2f> path = AlgorithmUtils.shortestPath(box.pos, GameMain.getInstance().player.box.pos, searchRange);
				
				if(path != null && path.size() >= 3){
					oldPos = new Vector2f(box.pos);
					box.drawPos = new Vector2f(box.pos);
					box.pos = path.get(path.size() - 2);
					
					canSmoothMove = true;
					smoothMoveCounter = 0f;
					box.verticalOffset = 0f;
				}
			}
		}
		
		if(canSmoothMove){
			smoothMoveCounter += delta;
			box.drawPos = MathUtils.lerp(oldPos, box.pos, smoothMoveCounter / (updateTime - GameMain.smoothMoveEndDelay));
			box.verticalOffset = GameMain.moveJumpHeight * (float)Math.sin(Math.toRadians(smoothMoveCounter / (GameMain.updateTime - GameMain.smoothMoveEndDelay) * 180f));
			
			if(smoothMoveCounter >= updateTime - GameMain.smoothMoveEndDelay){
				box.drawPos = new Vector2f(box.pos);
				box.verticalOffset = 0f;
				canSmoothMove = false;
			}
		}
	}
}
