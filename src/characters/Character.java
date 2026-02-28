package characters;

public class Character {
	public float health;
	public float maxHealth = 100f;
	
	public void removeHealth(float health){
		this.health -= health;
	}
	
	public void resetHealth(){
		health = maxHealth;
	}
}
