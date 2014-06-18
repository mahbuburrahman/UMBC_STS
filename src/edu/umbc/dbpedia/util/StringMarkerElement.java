package edu.umbc.dbpedia.util;

public class StringMarkerElement {
	
	public String word;
	public boolean valid;

	public StringMarkerElement(String par_word, boolean par_valid) {
		// TODO Auto-generated constructor stub
		word = par_word;
		valid = par_valid;
	}

	public String toString(){
		return word + "(" + valid + ")";
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}


}
