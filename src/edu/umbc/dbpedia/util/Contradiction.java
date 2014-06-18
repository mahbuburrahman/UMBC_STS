package edu.umbc.dbpedia.util;

public class Contradiction {
	
	String relation;
	double value;

	public Contradiction(String relation, double value) {
		// TODO Auto-generated constructor stub
		this.relation = relation;
		this.value = value;
	}

	public String toString(){
		return relation + ": " + value; 
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
