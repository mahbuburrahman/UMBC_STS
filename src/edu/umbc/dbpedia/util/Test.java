package edu.umbc.dbpedia.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import java.io.BufferedReader;
import java.io.FileReader;


import com.mdimension.jchronic.Chronic;
import com.mdimension.jchronic.Options;
import com.mdimension.jchronic.utils.Time;

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeGraphNode;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;

public class Test {

    public static String dataPath;
    public static String dataPath1;
    
	public Test() {
		// TODO Auto-generated constructor stub
		
        /* Read Path for model and data files */
        try{
            //InputStream input = new FileInputStream("../../../../../config.properties");
            BufferedReader br = new BufferedReader(new FileReader("../../../../../config.txt"));
            dataPath = br.readLine();
            dataPath1 = br.readLine();
            //System.out.println("Yes"+dataPath);
        }
        catch (Exception e) {
            //System.out.println("No");
        }
        /* End */
        
        
		/*
		boolean isTime = false;
		
		if (Character.isDigit(wordInlowercase.charAt(0))){
			
			int i=0;
			
			for (i=0; i<wordInlowercase.length(); i++){
				if (Character.isLetter(wordInlowercase.charAt(i)) || wordInlowercase.charAt(i) == ':'){
					break;
				}
			}
			
			if (i <= 2 && i <= wordInlowercase.length() - 2){
				
				while (i < wordInlowercase.length()){
					
					if (Character.isDigit(wordInlowercase.charAt(i))){
						isTime = true;
						break;
					}
					
					i++;
				}
			}
		}
		*/
	}

	public static String lemma_adj(String word, String posTag) {
		
		if (posTag.equals("JJR")){
			
			if (word.endsWith("ier"))
				return word.substring(0, word.length() - 3) + "y";
			else if (word.endsWith("er")){
				
				if (word.charAt(word.length() - 3) == word.charAt(word.length() - 4))
					return word.substring(0, word.length() - 3);
				else
					return word.substring(0, word.length() - 2);
			}else
				return word;
			
		}else if (posTag.equals("JJS")){
			
			if (word.endsWith("iest"))
				return word.substring(0, word.length() - 4) + "y";
			else if (word.endsWith("est")){
				
				if (word.charAt(word.length() - 4) == word.charAt(word.length() - 5))
					return word.substring(0, word.length() - 4);
				else
					return word.substring(0, word.length() - 3);
			}else
				return word;
			
			
		}else
			return word;
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("I.B.M.".replace(".", ""));
		String input = "15hr30.20s";
		System.out.println(input.replaceAll("[a-zA-Z.][a-zA-Z.]*", ":"));
		System.out.println(input.replaceAll("[^0-9][^0-9]*", ":"));
		System.out.println(input);
		
		String time1 = "00:30 ";
		String time2 = "12:30 AM";
		
		Options options = new Options();
		
	    options.setNow(Time.construct(2013, 3, 11, 0, 0, 0, 0));
	    //options.setCompatibilityMode(true);

		
		
		long n = 3 * 1000000000;
		
		System.out.println(Chronic.parse(time1, options));
		System.out.println(Chronic.parse(time2, options));
		System.out.println(Chronic.parse(time1, options).equals(Chronic.parse(time2, options)));
		
		
		/*
		System.out.println(Character.isLetter(','));
		
		System.out.println(Test.lemma_adj("bigger", "JJR"));
		System.out.println(Test.lemma_adj("higher", "JJR"));
		System.out.println(Test.lemma_adj("more", "JJR"));
		
		
		System.out.println(Test.lemma_adj("biggest", "JJS"));
		System.out.println(Test.lemma_adj("highest", "JJS"));
		System.out.println(Test.lemma_adj("happiest", "JJS"));
		
		String word1 = "other_JJ";
		String word2 = "other_NN";
		if (word1.substring(0, word1.lastIndexOf("_")).equals(word2.substring(0, word2.lastIndexOf("_"))))
			System.out.println(word1.substring(0, word1.lastIndexOf("_")));
		
		String name1 = "European Union";
		String name2 = "abc union";
		if (name1.substring(name1.lastIndexOf(" ") + 1, name1.length()).equalsIgnoreCase(name2.substring(name2.lastIndexOf(" ") + 1, name2.length()))){
			System.out.println(name1.substring(name1.lastIndexOf(" ") + 1, name1.length()));
			System.out.println(name2.substring(name2.lastIndexOf(" ") + 1, name2.length()));
		}

		if (word1.length() == word2.length()){
			
			int matches = 0;
			
			for (int i = 0; i < word1.length(); i++ ){
				
				if (word1.charAt(i) == word2.charAt(i))
					matches ++;
			}
			
			System.out.println((double) matches / word1.length());
			
		}

		String lemma1 = "aBda*desgsa1%02ds3a";
		String lemma2 = "aBdade-sgsa102ds3#a";
		
		String cleanForm1 = lemma1.replaceAll("[^A-Za-z0-9][^A-Za-z0-9]*", "");
		String cleanForm2 = lemma2.replaceAll("[^A-Za-z0-9][^A-Za-z0-9]*", "");
		
		if (cleanForm1.length() >= 5 && lemma1.length() - cleanForm1.length() < 3 && lemma2.length() - cleanForm2.length() < 3){
			if (cleanForm1.equals(cleanForm2))
				System.out.println(cleanForm1);
		}
		*/
		
		//System.out.println(Double.parseDouble("1,001"));

		/*
        System.out.println("Loading parser ...");
        LexicalizedParser lp = LexicalizedParser.loadModel(dataPath+"/model/stanford/lexparser/englishPCFG.ser.gz", "-maxLength", "200");
        Tree parse = lp.apply("I like eating fruit today.");
        
		TreebankLanguagePack tlp = new PennTreebankLanguagePack();
 	    GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
 	    GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
 	    Collection<TypedDependency> tdl = gs.typedDependenciesCollapsed();


 	   for( Iterator<TypedDependency> iter = tdl.iterator(); iter.hasNext(); ) {
			TypedDependency var = iter.next();
 
			TreeGraphNode dep = var.dep();
			TreeGraphNode gov = var.gov();
 
			// All useful information for a node in the tree
			String reln = var.reln().getShortName();
			int depIdx = var.dep().index();
			int govIdx = var.gov().index();
			
			System.out.println(dep.labels());
			
			Object relnArr[] = {reln, govIdx, depIdx};
			//relnList.add(relnArr);
 
			//Token depTok = new Token(depIdx, dep.label().tag(), dep.label().value());
			//Token govTok = new Token(govIdx, gov.label().tag(), gov.label().value());
			
			//wordMap.put(depIdx, depTok);
			//wordMap.put(govIdx, govTok);	
		}
        
		//lp.apply(compound1).pennPrint();
		 
		 */
	}

}
