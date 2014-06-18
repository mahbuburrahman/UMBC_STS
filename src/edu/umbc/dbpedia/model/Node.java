package edu.umbc.dbpedia.model;

import java.util.ArrayList;
import java.util.HashSet;

public class Node {

	public String id;         // id is a (numeric or literal) value if type is null.
	public String type;
	public boolean hidden;
	
	public HashSet<Link> outgoingLinks;
	public HashSet<Link> incomingLinks;
	
	public int temp_index;
	public ArrayList<ChoiceElement> choices;
	
	public GeneralizingClassElement temp_class;
	
	public Node(String ID, String TYPE){

		if (ID.startsWith("*")){
			id = "?" + ID.substring(1, ID.length());
			hidden = true;
		}else{
			id = ID;
			hidden = false;
		}
		
		type = TYPE;
		outgoingLinks = new HashSet<Link>();
		incomingLinks = new HashSet<Link>();
		choices = new ArrayList<ChoiceElement>();
		temp_index = -1;
		temp_class = new GeneralizingClassElement();
	}

	public String toString(){
		
		String result;
		
		result = id + "/" + type;
		
		return result; 
	}

	
	public String print(){
		
		String result;
		
		result = id + "/" + type;
	
		if (temp_class.term == null){
			
			result += " has a candidate list including ";
			
			if (choices.size() > 0){
				
				//result += " => ";
				
				for (int i = 0; i < choices.size(); i++){
					result += i + ":" + choices.get(i) + " ";
				}
			}

			if (temp_index != -1){
				
				//result += "<" + temp_index + ">";
				result += "(the selected choice is " + choices.get(temp_index) + ")";  
			
			}else{
				result += "(no reasonable choice exists in the candidate list)";
			}
				
		
		}else{
			
			result += " => ";
			result += temp_class.term;
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
