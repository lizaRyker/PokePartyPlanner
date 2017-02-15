import java.util.Comparator;

public class PokeComparator implements Comparator<Pokemon>{
    //string for which stat to be sorted by
	private String stat;
	
	//constructor
	public PokeComparator(String statToUse){
    	this.stat = statToUse;
    }
	
	//compare that stat of the two Pokemon
	//when used in sorting, results in Pokemon with the highest stats being sorted to the front of the list
	@Override
    public int compare(Pokemon p1, Pokemon p2) {
        return p2.getStat(stat) - p1.getStat(stat);
    }
}
