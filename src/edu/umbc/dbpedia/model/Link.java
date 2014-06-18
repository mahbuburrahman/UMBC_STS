package edu.umbc.dbpedia.model;

import java.util.ArrayList;

public class Link {
	
	public Node subject;
	public String predicate;
	public Node object;

	public ArrayList<ChoiceElement> choices;
	public double temp_fitness;
	public int temp_index;
	public boolean temp_hasBeenComputed;
	public boolean temp_reverse;
	
	public ArrayList<SimilarPropertyElement> temp_property_cluster;
	
	public Link(Node s, String p, Node o) {
		// TODO Auto-generated constructor stub
		subject = s;
		predicate = p;
		object = o;
		temp_hasBeenComputed = false;
		temp_fitness = 0;
		choices = new ArrayList<ChoiceElement>();
		temp_index = -1;
		temp_reverse = false;
		//temp_property_cluster = new ArrayList<SimilarPropertyElement>();
	}

	public void reset(){
		//temp_hasBeenComputed = false;
		temp_fitness = 0;
		temp_index = -1;
		temp_reverse = false;
	}
	
	/*
    public void setAssociation(double value){
    	temp_association = value;
    	temp_hasBeenComputed = true;
    }
    */

	
	public String toString(){
				
		String result;
		
		result = subject + ":" + predicate + ":" + object;
		
		return result; 
	}
	
	
	public String print(){
		
		String result;
		
		result = subject + ":" + predicate + ":" + object;
		
		if (temp_property_cluster == null){
		
			result += " has a candidate list including ";
			
			if (choices.size() > 0){
				
				//result += " => ";
				
				for (int i = 0; i < choices.size(); i++){
					result += i + ":" + choices.get(i) + " ";
				}
			}
			
			if (temp_index != -1){
				
				/*
				result += "<";
	
				if (temp_reverse)
					result += "!";
				
				result += temp_index + ">";
				*/
				if (!temp_reverse)
					result += "(the selected choice is " + choices.get(temp_index) + ")";
				else
					result += "(the selected choice is " + choices.get(temp_index) + ", with inversed direction)";
			}else
				result += "(no reasonable choice exists in the candidate list)";
			
		
			
		}else{
			
			result += " => ";
			
			for (int i = 0; i < temp_property_cluster.size(); i++){
				result += i + ":" + temp_property_cluster.get(i) + " ";
			}
			
		}
			
		return result; 
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
