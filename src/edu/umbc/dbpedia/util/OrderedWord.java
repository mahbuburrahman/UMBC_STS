package edu.umbc.dbpedia.util;

import java.util.Vector;

public class OrderedWord {
	
	public String tagged_word;
	public String origin;
	public int orderNo;
	public Vector<MappingEntry> potentialMappings;

	public OrderedWord(String par_word, String par_origin, int par_order) {
		// TODO Auto-generated constructor stub
		tagged_word = par_word;
		origin = par_origin;
		orderNo = par_order;
		potentialMappings = new Vector<MappingEntry>();
	}

	public String toString(){
		return tagged_word + "(" + orderNo + ")" + origin;
	}
	
	public int getMappingNo(){
		
		if (potentialMappings.size() > 0)
			return potentialMappings.lastElement().no;
		else
			return -1;
	}
	
	public double getMappingValue(){
		
		if (potentialMappings.size() > 0)
			return potentialMappings.lastElement().value;
		else
			return 0;
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}


}
