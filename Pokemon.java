import java.util.*;

public class Pokemon {
	//Pokemon object: ID, name, type(s), stats, is Legendary?
	//Does not support altering fields--intended for Pokedex use
	private int id;
	private String name;
	private String type1;
	private String type2;
	private int generation;
	private boolean legendary;
	private Map<String, Integer> stats;
	
	//Constructor: expects String of comma separated values
	public Pokemon(String newStats){
		String[] separate = newStats.split(",");
		
		if(separate.length != 13){
			throw new IllegalArgumentException("Unexpected String: Expecting String of \"Id,Name,Type1,Type2,Total,HP,Attack,Defense,SpAttack,SpDef,Speed,Generation,Legendary\"");
		}
		
		String[] mapKeys = {"total","hp","attack","defense","spAttack","spDefense","speed"};
		this.stats = new HashMap<String, Integer>();
		
		this.id = Integer.parseInt(separate[0]);
		this.name = separate[1];
		this.type1 = separate[2];
		this.type2 = separate[3];
		for(int i = 4; i < 11; i++){
			stats.put(mapKeys[i-4],Integer.parseInt(separate[i]));
		}
		stats.put("weighted", 0); //since the weight function has not been run yet, it has no weight
		this.generation = Integer.parseInt(separate[11]);
		this.legendary = Boolean.parseBoolean(separate[12]);
	}
	
	//get methods
	public int getId(){
		return id;
	}
	
	public String getName(){
		return name;
	}
	
	public String getType1(){
		return type1;
	}
	
	//WARNING: may return empty string (not all Pokemon are dual-typed)
	public String getType2(){
		return type2;
	}
	
	public int getStat(String key){
		return stats.get(key);
	}
	
	public int getGen(){
		return generation;
	}
	
	public boolean isLegendary(){
		return legendary;
	}
	
	//calculates the weighted value for this Pokemon using the given weights
	//since "weighted" is already initialized, this will overwrite the current value
	public void calcWeighted(Map<String, Integer> weights){
		int weightedTot = 0;
		for(String key : weights.keySet()){
			weightedTot += stats.get(key) * weights.get(key);
		}
		stats.put("weighted", weightedTot);
	}
	
	//returns string with name and all stats (including weighted)
	public String toString(){
		String output = this.name + " ";
		for(String val : stats.keySet()){
			output += " " + val + " " + stats.get(val);
		}
		return output;
	}
	
	//pretty print
	public void prettyPrint(){
		System.out.println("ID: " + id + " " + name);
		System.out.println(type1 + " " + type2);
		for(String key : stats.keySet()){
			if(!key.equals("weighted")){
				System.out.println(key + ": " + stats.get(key));
			}
		}
	}
}
