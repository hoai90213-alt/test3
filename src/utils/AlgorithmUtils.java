package utils;

import java.util.*;

import game.*;

public class AlgorithmUtils {
	private static float[] dX = {0, 1, 0, -1};
	private static float[] dY = {1, 0, -1, 0};
	
	public static ArrayList<Vector2f> shortestPath(Vector2f start, Vector2f end, float searchRange){
		float totalDist = start.distTo(end);
		while(searchRange >= 10f && totalDist < (searchRange / 4f) * 3f)
			searchRange = (searchRange / 4f) * 3f;
		
		HashMap<Vector2f, Vector2f> prev = new HashMap<Vector2f, Vector2f>();
		PriorityQueue<Vector2f> queue = new PriorityQueue<Vector2f>(new Comparator<Vector2f>(){
			@Override
			public int compare(Vector2f v1, Vector2f v2){
				return (int)(v1.distTo(end) - v2.distTo(end));
			}
		});
		
		queue.add(start);
		
		prev.put(start, null);
		
		float bestDistSoFar = Float.MAX_VALUE;
		Vector2f best = null;
		
		while(!queue.isEmpty()){
			Vector2f v = queue.poll();
			
			Vector2f next;
			for(int i = 0; i < 4; i++){
				next = new Vector2f(v.x + dX[i], v.y + dY[i]);
				
				if(end.equals(next)){
					prev.put(next, v);
					Vector2f curr = next;
					ArrayList<Vector2f> result = new ArrayList<Vector2f>();
					
					while(curr != null){
						//GameMain.getInstance().debug.add(curr);
						result.add(curr);
						curr = prev.get(curr);
					}
					
					return result;
				}
				
				float dist = end.distTo(next);
				
				if(!prev.containsKey(next) &&
						GameMain.getInstance().boxManager.getBoxAt(next) == null &&
						dist <= searchRange){
					if(dist < bestDistSoFar){
						best = next;
						bestDistSoFar = dist;
					}
					prev.put(next, v);
					queue.offer(next);
				}
			}
		}
		
		ArrayList<Vector2f> result = new ArrayList<Vector2f>();
		Vector2f curr = best;
		while(curr != null){
			//GameMain.getInstance().debug.add(curr);
			result.add(curr);
			curr = prev.get(curr);
		}
		
		return result;
	}
}
