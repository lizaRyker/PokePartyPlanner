import java.io.*;
import java.util.*;

public class KaggleData {
	//List of Pokemon and Pokedex (sorted by name) plus reference map for weaknesses
	private List<Pokemon> pokeList;
	private Map<String, Pokemon> pokedexByName;
	//Weaknesses map is arranged as "Attacker type"=[List of types that are weak against said attacker]
	private Map<String, ArrayList<String>> weaknesses;
	private List<Pokemon> party;
	
	//Constructor: read in a file, populates pokeList, then populates pokedex
	public KaggleData()throws FileNotFoundException{
		Scanner file = new Scanner(new File("Pokemon.csv"));
		
		//Get first line and throw it away:
		file.nextLine();
		
		this.pokeList = new ArrayList<Pokemon>();
		while(file.hasNext()){
			Pokemon toAdd = new Pokemon(file.nextLine());
			pokeList.add(toAdd);
		}
		
		file.close();
		mapPokedex();
		buildWeaknesses();
		party = new ArrayList<Pokemon>();
	}
	
	//Iterates through pokeList and populates pokedex with Pokemon name/Pokemon object key/value pairs
	private void mapPokedex(){
		this.pokedexByName = new TreeMap<String, Pokemon>();
		for(int i = 0; i < pokeList.size(); i++){
			pokedexByName.put(pokeList.get(i).getName(), pokeList.get(i));
		}
	}
	
	//Builds a reference map for what types are weak against what attackers -- used in party building (well, it will be... eventually)
	private void buildWeaknesses() throws FileNotFoundException{
		this.weaknesses = new HashMap<String, ArrayList<String>>();
		Scanner file = new Scanner(new File("typeChart"));
		
		String row = file.nextLine();
		ArrayList<String> types = new ArrayList<String>(Arrays.asList(row.split(",")));
		ArrayList<String> weakTo = new ArrayList<String>();
		
		int y = 0;
		
		while(file.hasNextLine()){
			row = file.nextLine();
			ArrayList<String> toAdd = new ArrayList<String>();
			weakTo = new ArrayList<String>(Arrays.asList(row.split(",")));
			
			for(int i = 1; i < weakTo.size(); i++){
				if(weakTo.get(i).compareTo("2") == 0){
					toAdd.add(types.get(i - 1));
				}
			}
			
			weaknesses.put(types.get(y), toAdd);
			y++;
		}
		
		file.close();
	}
	
	//Uses custom comparator to sort pokeList by a given stat
	//Comparator constructor takes a string of the stat you want to sort by (must match key name in Pokemon object's stats map)
	public void sortBy(String stat){
		Collections.sort(pokeList, new PokeComparator(stat));
	}
	
	//Helper function to sanitize user input
	private int sanitize(int in){
		if(in < 0){
			return 0;
		} else if(in > 10){
			return 10;
		} else return in;
	}
	
	//Function asks user to weight stat priorities, then calls calcWeighted on each Pokemon object in list
	private void weightPriorities(List<Pokemon> toWeight, Scanner input){
		Map<String, Integer> weights = new HashMap<String, Integer>();
		
		System.out.println("Weight from 0-10 how much you value each stat: ");
		System.out.println("HP: ");
		weights.put("hp", sanitize(input.nextInt()));
		System.out.println("Attack: ");
		weights.put("attack", sanitize(input.nextInt()));
		System.out.println("Defense: ");
		weights.put("defense", sanitize(input.nextInt()));
		System.out.println("Special Attack: ");
		weights.put("spAttack", sanitize(input.nextInt()));
		System.out.println("Special Defense: ");
		weights.put("spDefense", sanitize(input.nextInt()));
		System.out.println("Speed: ");
		weights.put("speed", sanitize(input.nextInt()));
		weights.put("total", 0); //Irrelevant stat but necessary for iteration
		
		for(int i = 0; i < toWeight.size(); i++){
			toWeight.get(i).calcWeighted(weights);
		}
	}
	
	//Prints the list of Pokemon names and their stats
	public void printList(){
		for(int i = 0; i < pokeList.size(); i++){
			pokeList.get(i).prettyPrint();
			System.out.println();
		}
	}
	
	//Returns pokedex
	public Map<String, Pokemon> getPokedex(){
		return pokedexByName;
	}
	
	//Prints pokedex
	public void printPokedex(){
		for(String key : pokedexByName.keySet()){
			pokedexByName.get(key).prettyPrint();
			System.out.println();
		}
	}

	//Function to build the ideal party based on user-chosen starter Pokemon and stat priorities
	//This function is a behemoth.
	public void buildMyParty() throws FileNotFoundException{
		Scanner input = new Scanner(System.in);
		//Scanner input = new Scanner(new File("Input")); //used for automation in testing
		
		//Build list of Pokemon left to consider
		List<Pokemon> optionsLeft = new LinkedList<Pokemon>(pokeList);
		
		//build list of weaknesses that need to be covered
		Set<String> toCover = new HashSet<String>(weaknesses.keySet());
		//System.out.println("To cover size at start: " + toCover.size()); //Debugging
		
		System.out.println("Input the name of your starter Pokemon (Case matters): ");
		String key = input.next();
		
		//Keep asking for starter until user inputs a valid Pokemon name
		while(!pokedexByName.containsKey(key)){
			System.out.println("That Pokemon is not in the Pokedex. Enter another one:");
			key = input.next();
		}
		
		//Begin building party by adding starter to party list
		addToParty(pokedexByName.get(key),toCover);
		
		//Remove starter from remaining options list
		optionsLeft.remove(optionsLeft.indexOf(pokedexByName.get(key)));
		
		//Ask user to weight their stats priorities
		weightPriorities(optionsLeft, input);
		
		//Ask user whether or not to include legendaries in their party
		System.out.println("Do you want to include legendaries in your party? (Note: if you say yes you will probably end up with a party of nothing but legendaries");
		key = input.next();
		
		//If "no" to include legendaries, remove legendaries from list
		if(key.equalsIgnoreCase("no")){
			for(Iterator<Pokemon> iterator = optionsLeft.iterator(); iterator.hasNext();){
				Pokemon toRemove = iterator.next();
				if(toRemove.isLegendary()){
					iterator.remove();
				}
			}
		}
		
		//Ask user whether or not to include megas in their party
		System.out.println("Do you want to include megas in your party? (Note: if you say yes you will probably end up with a party of nothing but megas");
		key = input.next();
		
		//If "no" to include megas, remove megas from list
		if(key.equalsIgnoreCase("no")){
			for(Iterator<Pokemon> iterator = optionsLeft.iterator(); iterator.hasNext();){
				Pokemon toRemove = iterator.next();
				if(toRemove.getName().contains("Mega")){
					iterator.remove();
				}
			}
		}
		
		System.out.println("Do you want to limit your party search to a single generation?");
		key = input.next();
		
		if(key.equalsIgnoreCase("yes")){
			System.out.println("Type in the number for the generation you want to limit it to (1-6): ");
			int gen = input.nextInt();
			if(gen < 0){
				gen = 0;
			} else if(gen > 6){
				gen = 6;
			}
			for(Iterator<Pokemon> iterator = optionsLeft.iterator(); iterator.hasNext();){
				Pokemon toRemove = iterator.next();
				if(toRemove.getGen() != gen){
					iterator.remove();
				}
			}
		}
		
		//No more need for user input
		input.close();
		
		//sort options by weight
		Collections.sort(optionsLeft, new PokeComparator("weighted"));
		
		Pokemon pokeCandidate = optionsLeft.get(0);
		
		while(party.size() < 6){
			//boolean for checking type -- don't want repeats
			boolean typeCompare = false;
			//if there are no more weaknesses to cover, just get the highest weighted Pokemon from the list
			if(toCover.size() == 0){
				pokeCandidate = optionsLeft.remove(0);
			} else { //otherwise, weight the types and get the best fit
				pokeCandidate = typeCompare(optionsLeft, weightTypes(toCover));
			}
			//now check the type and make sure you aren't getting six grass Pokemon in your party
			for(int i = 0; i < party.size(); i++){
				//Oh god I created a monster... I probably should have put the Pokemon fields into an array I could iterate over or something...
				typeCompare = pokeCandidate.getType1().equals(party.get(i).getType1()) ||
						pokeCandidate.getType1().equals(party.get(i).getType2()) ||
						pokeCandidate.getType2().equals(party.get(i).getType1()) ||
						pokeCandidate.getType2().equals(party.get(i).getType2());
				
				//if at any point you get a type repeat, break out of the loop cuz you don't need to iterate over the rest of them
				if(typeCompare){
					break;
				}
			}
			
			//add to party if it isn't a type you already have
			if(!typeCompare){
				addToParty(pokeCandidate,toCover);
			}
			//don't need an else because we don't want it to do anything if we have a type repeat - just move on
		}
		
		//pretty-print party
		for(int i = 0; i < party.size(); i++){
			party.get(i).prettyPrint();
			System.out.println();
		}
	}
	
	private void addToParty(Pokemon pokeCandidate, Set<String> toCover){
		party.add(pokeCandidate);
		
		//reference typechart and remove weaknesses covered by this Pokemon from the remaining toCover set
		toCover.removeAll(weaknesses.get(pokeCandidate.getType1()));
		if(pokeCandidate.getType2().length() > 0){
			toCover.removeAll(weaknesses.get(pokeCandidate.getType2()));
		}
	}
	
	private TreeMap<Integer, String> weightTypes(Set<String> toCover){
		TreeMap<Integer, String> results = new TreeMap<Integer,String>();
		
		for(String key : weaknesses.keySet()){
			int count = 0;
			//for each element in the reference value list
			for(int i = 0; i < weaknesses.get(key).size(); i++){
				//check each element of the toCover list
				for(String element : toCover){
					//if they are the same, increment count
					if(element.equals(weaknesses.get(key).get(i))){
						count++;
					}
				}
			}
			//the number of elements in the values list that are also in the toCover list will become the weight for that type
			//add the weight number and the key from the weaknesses reference chart to the map
			results.put(count, key);
		}
		//return map
		return results;
	}
	
	private Pokemon typeCompare(List<Pokemon> options, TreeMap<Integer, String> typeWeights){
		//reference typeWeights map: choose type with highest weight (stored in treemap, already sorted)
		String type = typeWeights.get(typeWeights.lastKey());
		//build set of pokemon of only that type (primary or secondary)
		TreeSet<Pokemon> thisType = new TreeSet<Pokemon>(new PokeComparator("weighted"));
		for(int i = 0; i < options.size(); i++){
			boolean sameType = options.get(i).getType1().equals(type) ||
					options.get(i).getType2().equals(type);
			if(sameType){
				thisType.add(options.get(i));
			}
		}
		
		//take top weighted result from results set and return it after removing it from the remaining options list
		//System.out.println(thisType.size());
		options.remove(options.indexOf(thisType.first()));
		return thisType.first();
	}
}
