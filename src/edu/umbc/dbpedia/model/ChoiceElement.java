package edu.umbc.dbpedia.model;

public class ChoiceElement implements Comparable<ChoiceElement>{
	
	public String term;
	public double sim;
	public String from;

	public ChoiceElement(String par_word, double par_value) {
		// TODO Auto-generated constructor stub
		term = par_word;
		sim = par_value;
		from = "";
	}

	public String toString(){
		if (from.equals(""))
			return term + " " + String.format("%1.2f", sim);
		else
			return term + from + " " + String.format("%1.2f", sim);
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public int compareTo(ChoiceElement o) {
		// TODO Auto-generated method stub
		return (int) Math.signum((o.sim - this.sim));
	}
	
	public boolean equals(ChoiceElement o){
		return o.term.equals(term);
	}

}
