package edu.umbc.dbpedia.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LexicalProcess {

	public static String tokenDeep(String localname){
    	//    	token the localname
		//ArrayList tokens = new ArrayList();
		String ret = "";
			
		
	    Pattern pattern;
	    Matcher matcher;

	    //System.out.println(localname);
	    pattern = Pattern.compile ("[A-Z]*[a-z]*");
	    matcher = pattern.matcher(localname);
	    int previousEnd = 0;
	    
	    while (matcher.find()){
	    	
	    	String temptext = matcher.group();
	    	if (temptext.length()==0)
	    		continue;
	    	
	    	if (previousEnd != matcher.start()){
	    		
	    		String tmp = localname.substring(previousEnd, matcher.start()).trim();
	    		
	    		if (tmp.startsWith("-")) tmp = tmp.substring(1);
	    		
	    		ret += tmp + " ";
	    	}
	    	
	    	previousEnd = matcher.end();
	    	
	    	int i;
	    	for (i = temptext.length() -1 ; i >= 0; i--){
	    		if (!Character.isLowerCase(temptext.charAt(i)))
	    			break;
	    	}
	    	if (i > 0 && i != temptext.length() -1)
	    		temptext = temptext.substring(0, i) + " " + temptext.substring(i);
	    	
	    	//tokens.add(temptext);
	    	ret+=temptext+" ";
	       // System.out.println( temptext );
	    }		
	
	    //modified on 12/22/2008 to deal with the ending number. -- Lushan Han
	    if (previousEnd != localname.length()){
	    	String tmp = localname.substring(previousEnd).trim();
	    	if (tmp.startsWith("-")) tmp = tmp.substring(1);
	    	ret += tmp;
	    }
	    
	    return ret.trim();
	}

    
	public static String tokenLocalName(String localname){
    	//    	token the localname
		//ArrayList tokens = new ArrayList();
		String ret = "";
			
		
	    Pattern pattern;
	    Matcher matcher;

	    //System.out.println(localname);
	    pattern = Pattern.compile ("[A-Z.]*[a-z]*[0-9]*");
	    matcher = pattern.matcher(localname);
	    
	    while (matcher.find()){
	    	
	    	String temptext = matcher.group();
	    	if (temptext.length()==0)
	    		continue;
	    	
	    	int i;
	    	for (i = temptext.length() -1 ; i >= 0; i--){
	    		if (!Character.isDigit(temptext.charAt(i)) && !Character.isLowerCase(temptext.charAt(i)))
	    			break;
	    	}
	    	if (i > 0 && i != temptext.length() -1)
	    		temptext = temptext.substring(0, i) + " " + temptext.substring(i);
	    	
	    	//tokens.add(temptext);
	    	if (!isAcronym(temptext))
	    		temptext = temptext.toLowerCase();
	    	
	    	ret += temptext + " ";
	       // System.out.println( temptext );
	    }		
	    
	    return ret.trim();
	}
	
	
	public static boolean isAcronym(String word){
		
		if (word.length() == 0)
			return false;
		
		boolean result = true;
		
		for (int i=0; i < word.length(); i++){
			//if (Character.isLetter(word.charAt(i)) && !Character.isUpperCase(word.charAt(i))){
			if (word.charAt(i) != '.' && !Character.isUpperCase(word.charAt(i))){
				result = false;
				break;
			}
		}

		return result;
	}

	
	public static boolean isNumber(String word){

		if (word.length() == 0)
			return false;
		
		boolean result = true;
		
		for (int i=0; i < word.length(); i++){
			if (word.charAt(i) != '.' && word.charAt(i) != '-' && !Character.isDigit(word.charAt(i))){
				result = false;
				break;
			}
		}

		return result;
	}

	
	public static String getNounForm(String verb){
		
		int index = verb.indexOf("_");
		String word = verb.substring(0, index);
		String pos = verb.substring(index + 1, verb.length());
		
		if (!pos.equals("VB"))
			return verb;
		
		//deal with special changes
		if (word.equals("create"))
			return "creator_NN";

		if (word.equals("locate"))
			return "location_NN";

		if (word.endsWith("e"))
			word = word + "r";
		else
			word = word + "er";
		
		return word + "_NN";
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
				
		System.out.println(LexicalProcess.tokenLocalName("owning2Organisation"));
		System.out.println(LexicalProcess.tokenLocalName("co2Emission"));
		System.out.println(LexicalProcess.tokenLocalName("U.S. President"));
		System.out.println(LexicalProcess.isAcronym("P..RC"));
		System.out.println(LexicalProcess.isAcronym("USA"));
		System.out.println(LexicalProcess.isAcronym("UMBC"));
		System.out.println(LexicalProcess.getNounForm("eat_VB"));
		System.out.println(LexicalProcess.isNumber("43535"));
		System.out.println(LexicalProcess.isNumber("435-35"));
		System.out.println(LexicalProcess.isNumber("43.5-35"));
		System.out.println(LexicalProcess.isNumber(""));
	}

}
