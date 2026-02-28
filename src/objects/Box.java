package objects;

import java.util.*;

import game.GameMain;
import utils.*;

public class Box {
	public static final int vertexCount = 12;
	private Color color1;
	private Color color2;
	
	public Vector2f pos;
	public Vector2f drawPos;
	public float verticalOffset = 0f;
	
	public Box(Vector2f pos, Color c1, Color c2){ //grid location
		this.pos = new Vector2f(pos);
		drawPos = new Vector2f(pos);
		this.color1 = c1;
		this.color2 = c2;
	}
	
	public float[] getColorArray(){
		float colorScale = drawPos.distTo(GameMain.getInstance().player.box.drawPos) / 10f;
		if(colorScale < 1f)
			colorScale = 1f;
		
		return new float[]{
			color1.r / colorScale, color1.g / colorScale, color1.b / colorScale,
			color1.r / colorScale, color1.g / colorScale, color1.b / colorScale,
			color1.r / colorScale, color1.g / colorScale, color1.b / colorScale,
			color1.r / colorScale, color1.g / colorScale, color1.b / colorScale,
			color1.r / colorScale, color1.g / colorScale, color1.b / colorScale,
			color1.r / colorScale, color1.g / colorScale, color1.b / colorScale,
			
			color2.r / colorScale, color2.g / colorScale, color2.b / colorScale,
			color2.r / colorScale, color2.g / colorScale, color2.b / colorScale,
			color2.r / colorScale, color2.g / colorScale, color2.b / colorScale,
			color2.r / colorScale, color2.g / colorScale, color2.b / colorScale,
			color2.r / colorScale, color2.g / colorScale, color2.b / colorScale,
			color2.r / colorScale, color2.g / colorScale, color2.b / colorScale,
		};
	}
	
	public float[] getVertexArray(){
		float bX = drawPos.x * GameMain.getInstance().boxWidth + GameMain.getInstance().boxOffset.x - GameMain.getInstance().player.box.drawPos.x * GameMain.getInstance().boxWidth + GameMain.getInstance().player.camOffset.x; //left
		float bY = (verticalOffset + drawPos.y) * GameMain.getInstance().boxHeight + GameMain.getInstance().boxOffset.y - GameMain.getInstance().player.box.drawPos.y * GameMain.getInstance().boxHeight + GameMain.getInstance().player.camOffset.y; //down
		
		return new float[]{
			bX, bY + GameMain.getInstance().boxHeight * 1.5f,
			bX, bY + GameMain.getInstance().boxHeight * 0.5f,
			bX + GameMain.getInstance().boxWidth, bY + GameMain.getInstance().boxHeight * 1.5f,
			bX, bY + GameMain.getInstance().boxHeight * 0.5f,
			bX + GameMain.getInstance().boxWidth, bY + GameMain.getInstance().boxHeight * 0.5f,
			bX + GameMain.getInstance().boxWidth, bY + GameMain.getInstance().boxHeight * 1.5f,
	
			bX, bY + GameMain.getInstance().boxHeight * 0.5f,
			bX, bY,
			bX + GameMain.getInstance().boxWidth, bY + GameMain.getInstance().boxHeight * 0.5f,
			bX, bY,
			bX + GameMain.getInstance().boxWidth, bY,
			bX + GameMain.getInstance().boxWidth, bY + GameMain.getInstance().boxHeight * 0.5f
		};
	}
	
	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + ((color1 == null) ? 0 : color1.hashCode());
		result = prime * result + ((color2 == null) ? 0 : color2.hashCode());
		result = prime * result + ((drawPos == null) ? 0 : drawPos.hashCode());
		result = prime * result + ((pos == null) ? 0 : pos.hashCode());
		result = prime * result + Float.floatToIntBits(verticalOffset);
		return result;
	}
	
	@Override
	public boolean equals(Object obj){
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		Box other = (Box)obj;
		if(color1 == null){
			if (other.color1 != null)
				return false;
		}else if(!color1.equals(other.color1))
			return false;
		if(color2 == null){
			if (other.color2 != null)
				return false;
		}else if(!color2.equals(other.color2))
			return false;
		if(drawPos == null){
			if(other.drawPos != null)
				return false;
		}else if(!drawPos.equals(other.drawPos))
			return false;
		if(pos == null){
			if(other.pos != null)
				return false;
		}else if(!pos.equals(other.pos))
			return false;
		if(Float.floatToIntBits(verticalOffset) != Float.floatToIntBits(other.verticalOffset))
			return false;
		return true;
	}
}
