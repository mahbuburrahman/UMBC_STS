package edu.umbc.dbpedia.model;

public class SimilarPropertyElement implements Comparable<SimilarPropertyElement>{
	
	public String term;
	public String from;
	public double sim;
	public boolean isInverse;
	public double condiProb;

	public SimilarPropertyElement(String par_word, String par_from, double par_value, boolean inverse, double condi) {
		// TODO Auto-generated constructor stub
		term = par_word;
		from = par_from;
		sim = par_value;
		isInverse = inverse;
		condiProb = condi;
	}

	public String toString(){
		if (!isInverse)
			return term + " " + String.format("%1.2f", sim);
		else
			return term + " " + String.format("%1.2f", sim) + " (with inversed direction)";
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public int compareTo(SimilarPropertyElement o) {
		// TODO Auto-generated method stub
		return (int) Math.signum((this.sim - o.sim));
	}

}
