package characters;

import objects.Box;
import utils.Vector2f;

public abstract class AI extends Character{
	public Box box;
	protected float updateTime = 0.3f;
	protected Vector2f oldPos;
	protected float searchRange = 30f;
	
	protected float processCounter;
	protected boolean canProcess;
	
	protected float smoothMoveCounter = 0f;
	protected boolean canSmoothMove = false;
	
	public abstract void update(float delta);
}
