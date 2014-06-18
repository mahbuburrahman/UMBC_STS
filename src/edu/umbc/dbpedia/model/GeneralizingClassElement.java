package edu.umbc.dbpedia.model;

public class GeneralizingClassElement implements Comparable<GeneralizingClassElement>{
	
	public String term;
	public double sim;
	public int level;


	public GeneralizingClassElement() {
		// TODO Auto-generated constructor stub
		term = null;
		sim = 0;
		level = -1;
	}

	public GeneralizingClassElement(String par_word, double par_value) {
		// TODO Auto-generated constructor stub
		term = par_word;
		sim = par_value;
		level = -1;
	}

	public String toString(){
		if (level < 0)
			return term + " " + String.format("%1.2f", sim);
		else
			return term + "@" + level + " " + String.format("%1.2f", sim);
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public int compareTo(GeneralizingClassElement o) {
		// TODO Auto-generated method stub
		return (int) Math.signum((this.sim - o.sim));
	}

}
