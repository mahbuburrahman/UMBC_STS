package edu.umbc.dbpedia.model;

import java.io.Serializable;
import java.util.TreeMap;

public class RelationDimensionElement implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public int totalFrequency;
	public TreeMap<String, Integer> relationFrequencies;
	
	
	public RelationDimensionElement() {
		// TODO Auto-generated constructor stub
		totalFrequency = 0;
		relationFrequencies = new TreeMap<String, Integer>();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
