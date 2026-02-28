package objects;

import java.nio.*;
import java.util.*;

import org.lwjgl.*;
import org.lwjgl.opengl.*;

import utils.*;

public class BoxManager {
	public ArrayList<Box> boxes = new ArrayList<Box>();
	
	private int vertexVBOID;
	private int colorVBOID;
	private FloatBuffer vertexData;
	private FloatBuffer colorData;
	
	public void init(){
		vertexData = BufferUtils.createFloatBuffer(boxes.size() * Box.vertexCount * 2);
		colorData = BufferUtils.createFloatBuffer(boxes.size() * Box.vertexCount * 3);
		
		vertexVBOID = GL15.glGenBuffers();
		colorVBOID = GL15.glGenBuffers();
	}
	
	private void process(){
		Collections.sort(boxes, new Comparator<Box>(){
			@Override
			public int compare(Box b1, Box b2) {
				if(b2.pos.y == b1.pos.y)
					return (int)(b2.pos.x - b1.pos.x);
				else
					return (int)(b2.pos.y - b1.pos.y);
			}
		});
		
		processVBO();
	}
	
	private void processVBO(){
		vertexData.clear();
		colorData.clear();
		
		for(int i = 0; i < boxes.size(); i++){
			vertexData.put(boxes.get(i).getVertexArray());
			colorData.put(boxes.get(i).getColorArray());
		}
		
		vertexData.flip();
		colorData.flip();
	}
	
	public Box getBoxAt(Vector2f pos){
		for(int i = 0; i < boxes.size(); i++){
			if(boxes.get(i).pos.equals(pos)){
				return boxes.get(i);
			}
		}
		return null;
	}
	
	public Box createBox(Vector2f pos){ //grid location
		Box box = new Box(pos, new Color(0.7f, 0.7f, 0.7f), new Color(0.5f, 0.5f, 0.5f));
		boxes.add(box);
		return box;
	}
	
	public void update(){
		draw();
	}
	
	private void draw(){
		process();
		
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

		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, boxes.size() * Box.vertexCount);
		
		GL20.glDisableVertexAttribArray(0);
		GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
	}
	
	public void destroy(){
		GL15.glDeleteBuffers(vertexVBOID);
		GL15.glDeleteBuffers(colorVBOID);
	}
}
