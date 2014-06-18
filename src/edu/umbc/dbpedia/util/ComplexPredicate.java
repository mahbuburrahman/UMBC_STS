package edu.umbc.dbpedia.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Map.Entry;

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeGraphNode;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import edu.umbc.dbpedia.model.SimilarityArrayModel;
import edu.umbc.similarity.dictionary.StanfordTermTokenizer;

public class ComplexPredicate {

	int adj_vb_discriminator_threshold = 500;
	static double ShortTermWeight = 0.5;
	public static double noun_phrase_sim_threshold = 0.10;
	public String ori_phrase;
	public OrderedWord[] componentWords;
	public boolean isSimpleNounPhrase;
	boolean hasNegation;
    public static String dataPath;
    public static String dataPath1;


	public static final HashSet<String> stopwords = new HashSet<String>(Arrays.asList("of", "at", "on", "in", "out", "from", "to", "the", "has", "have", "is", "was", "are", "were", "be", "been",  
			"let", "do", "did", "does", "a", "am", "among", "an", "by", "can", "cannot", "could", "would", "had", "may", "might", "must", "should", "there", "was", "also", "and", "indeed", "such", "so"));

	/*
	public static final HashSet<String> extended_stopwords = new HashSet<String>(Arrays.asList("point", "one", "exist", "take", "get", "part", "belong", "belongs", "himself", "themselves", "ourselves", 
			"yourself", "myself", "involve", "something", "anything", "include", "happen", "take place", "go on", "able", "physically", "all", "almost", "any", "either", "else", "ever", "every", "its", "just", "least", 
			"most", "or", "own", "some", "will", "today", "now", "top", "right", "more", "agent", "means", "make", "many", "much", "actually", "nothing", "two", "frame", "entity", "object", "instance", "entry", "thing", "unit", 
			"feature", "attribute", "way", "sort", "type", "kind", "line", "category", "class", "piece", "part", "fraction", "portion", "member", "slice", "quite", "rather", "become", "act", "state", "fact",
			"deal", "concern", "word", "noun", "verb", "adjective", "adverb", "describe", "represent", "contain", "denote"));
	*/

	public static final HashSet<String> extended_stopwords = new HashSet<String>(Arrays.asList("point", "one", "exist", "take", "get", "part", "belong", "belongs", "himself", "themselves", "ourselves", 
			"yourself", "myself", "involve", "something", "anything", "include", "happen", "take place", "go on", "able", "physically", "all", "almost", "any", "either", "else", "ever", "every", "its", "just", "least", 
			"most", "or", "own", "some", "will", "today", "now", "top", "right", "more", "means", "make", "many", "much", "actually", "nothing", "entity", "object", "instance", "entry", "thing", "unit", 
			"feature", "attribute", "way", "sort", "type", "kind", "line", "category", "class", "piece", "part", "fraction", "portion", "member", "slice", "quite", "rather", "become", "act", "fact",
			"deal", "concern", "word", "noun", "verb", "adjective", "adverb", "describe", "represent", "contain", "denote"));
	

	public static final HashSet<String> negations = new HashSet<String>(Arrays.asList(	"no", "not", "n't", "neither", "none", "hardly", "never", "seldom", "seldomly", "yet", "nothing", "from"));
	
	public int lengthInAllWords;
	public int lengthInContentWords;
	
	public ComplexPredicate(String phrase, MaxentTagger tagger, Morphology morpha, SimilarityArrayModel model, boolean includeLargeRB, boolean extendedStopwords, boolean supportAcronym) {
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

        
		//String delimitedPharse = LexicalProcess.tokenLocalName(phrase);
		hasNegation =false;
		int largeRB_threshold = 500000; // 600000;
		
		String delimitedPharse = phrase.replace('_', ' ');
		
		delimitedPharse = delimitedPharse.trim();
		
		if (!delimitedPharse.endsWith(".") && !delimitedPharse.endsWith("?") && !delimitedPharse.endsWith("\""))
			delimitedPharse = delimitedPharse + ".";
		
		/*
		int position = delimitedPharse.indexOf(":");
		
		if (position > 0){
			String prefix = delimitedPharse.substring(0, position);
			
			if (prefix.length() <= 20 && prefix.matches("[A-Za-z ]*") && delimitedPharse.length() >= 2 * prefix.length()){
				delimitedPharse = delimitedPharse.substring(position + 1, delimitedPharse.length());
			}
		}
		*/
		
		String taggedPhrase;
		
		// choose the method of POS parser
		String firstWord;
		
		if (delimitedPharse.contains(" "))
			firstWord = delimitedPharse.substring(0, delimitedPharse.indexOf(" "));
		else
			firstWord = delimitedPharse;
		
		boolean isVerbPhrase = false;
		
		if (model.index(firstWord.toLowerCase() + "_VB") > 0 && delimitedPharse.length() < 80)
			isVerbPhrase = true;
		
		if (delimitedPharse.contains(" ") && !isVerbPhrase){
		
			taggedPhrase = tagger.tagString(delimitedPharse).replace('/', '_').trim();
			
		}else{
			
			taggedPhrase = tagger.tagString("which " + delimitedPharse).replace('/', '_').trim();
			
			int start = taggedPhrase.indexOf(' ');
			taggedPhrase = taggedPhrase.substring(start + 1, taggedPhrase.length());
		}
			
		
		StanfordTermTokenizer st = new StanfordTermTokenizer(taggedPhrase, new String[0], morpha, model.vocabulary, model.frequency, supportAcronym);
		
		
		Vector<OrderedWord> components = new Vector<OrderedWord>();
		
		isSimpleNounPhrase = true;
		ori_phrase = phrase;
		OrderedWord orderedWord;
		
		while ((orderedWord = st.getNextValidWord()) != null){
			
			String taggedWord = orderedWord.tagged_word;
			int index = taggedWord.lastIndexOf('_');
			String word = taggedWord.substring(0, index);
			String posTag = taggedWord.substring(index + 1, taggedWord.length());
			
			if (negations.contains(word))
				hasNegation = true;
			
			if (!extendedStopwords){
				if (stopwords.contains(word)){
					isSimpleNounPhrase = false;
					continue;
				}
			
			}else{
				if (stopwords.contains(word)){
					isSimpleNounPhrase = false;
					continue;
				}
				
				if (extended_stopwords.contains(word)){
					isSimpleNounPhrase = false;
					continue;
				}
			}
			
			if (posTag.startsWith("NN")){
				
				if (word.endsWith("sation")){
					
					String AmericanWord = word.replace("sation", "zation");

					if (model.getFrequency(word + "_NN") == 0 && model.getFrequency(AmericanWord + "_NN") > 0){
						word = AmericanWord;
						taggedWord = AmericanWord + "_NN";
					}
				}
				
				orderedWord.tagged_word = taggedWord;
				components.add(orderedWord);
				
			}else if (posTag.startsWith("VB")){
				
				if (word.endsWith("ise") && word.length() >= 5){
					
					String AmericanWord = word.replace("ise", "ize");

					if (model.getFrequency(word + "_VB") == 0 && model.getFrequency(AmericanWord + "_VB") > 0){
						word = AmericanWord;
						taggedWord = AmericanWord + "_VB";
					}
				}
				
				isSimpleNounPhrase = false;
				orderedWord.tagged_word = taggedWord;
				components.add(orderedWord);
				
			}else if (posTag.startsWith("JJ")){
				
				components.add(orderedWord);
			
			}else if (posTag.startsWith("RB")){
				
				if (includeLargeRB){
					if (model.getFrequency(taggedWord) > 0)
						components.add(orderedWord);
				}else if (model.getFrequency(taggedWord) < largeRB_threshold && model.getFrequency(taggedWord) > 0)   // for the webbase_all corpus
					components.add(orderedWord);
			
			}else if (posTag.startsWith("CD")){
				
				components.add(orderedWord);
			
			}else if (posTag.startsWith("TE")){
				
				components.add(orderedWord);
			
			}else if (posTag.startsWith("PRP")){
				
				components.add(orderedWord);
			
			}else if (posTag.startsWith("FW")){
				
				//components.add(orderedWord);
			
			}else if (posTag.equals("OT")){
				
				isSimpleNounPhrase = false;
				//components.add(taggedWord);
			}

		}

		lengthInAllWords = st.lengthInAllWords;
		
		lengthInContentWords = components.size();

		if (lengthInContentWords == 1 && !delimitedPharse.contains(" ")){
			
			orderedWord = components.get(0);
			String taggedWord = orderedWord.tagged_word;
			
			int index = taggedWord.lastIndexOf('_');
			String word = taggedWord.substring(0, index);
			//String posTag = taggedWord.substring(index + 1, taggedWord.length());
			
			if (word.equals(delimitedPharse)){
				
				String noun = word + "_NN";
				String verb = word + "_VB";
				String adj = word + "_JJ";
				
				int nounFrq = model.getFrequency(noun);
				int verbFrq = model.getFrequency(verb) / 2;
				int adjFrq = model.getFrequency(adj) / 20;
				
				if (nounFrq > verbFrq && nounFrq > adjFrq){
					orderedWord.tagged_word = noun;
					components.set(0, orderedWord);
					isSimpleNounPhrase = true;
				}else if (verbFrq > nounFrq && verbFrq > adjFrq){
					orderedWord.tagged_word = verb;
					components.set(0, orderedWord);
					isSimpleNounPhrase = false;
				}
			}
			
		}
		
		componentWords = components.toArray(new OrderedWord[components.size()]);
		
		if (componentWords.length > 0 && !componentWords[componentWords.length - 1].tagged_word.endsWith("_NN"))
			isSimpleNounPhrase = false;

	}

	public static double getSimilarity(ComplexPredicate target, ComplexPredicate compound, SimilarityArrayModel model){
		
		if (target.lengthInContentWords == 0 || compound.lengthInContentWords == 0)
			return Double.NEGATIVE_INFINITY;
		
		double maxSimTarget = 0;
		
		for (int i=0; i<target.lengthInContentWords; i++){
			
			double localMaxSim = -1.0;
			for (int j=0; j<compound.lengthInContentWords; j++){
				
				double localSim = model.getSimilarity(target.componentWords[i].tagged_word, compound.componentWords[j].tagged_word);
			
				if (localSim > localMaxSim)
					localMaxSim = localSim;
			}
			
			maxSimTarget += localMaxSim;
			
		}

		double maxSimCompound = 0;
		
		for (int i=0; i<compound.lengthInContentWords; i++){
			
			double localMaxSim = -1.0;
			for (int j=0; j<target.lengthInContentWords; j++){
				
				double localSim = model.getSimilarity(compound.componentWords[i].tagged_word, target.componentWords[j].tagged_word);
			
				if (localSim > localMaxSim)
					localMaxSim = localSim;
			}
			
			maxSimCompound += localMaxSim;
			
		}

		
		//return normalizedSim;
		if (target.lengthInContentWords >= compound.lengthInContentWords)
			return maxSimTarget / target.lengthInContentWords * (1 - ShortTermWeight) + maxSimCompound / compound.lengthInContentWords * ShortTermWeight;
		else
			return maxSimTarget / target.lengthInContentWords * ShortTermWeight + maxSimCompound / compound.lengthInContentWords * (1 - ShortTermWeight);

	}

	
	public static double getSTS_Similarity(ComplexPredicate target, ComplexPredicate compound, SimilarityArrayModel model, String[] antonym_set){
		
		
		if (target.lengthInContentWords == 0 || compound.lengthInContentWords == 0)
			return 0; // Double.NEGATIVE_INFINITY;
	
		double capacity = 10; //10;
		boolean useCapacity = false;
		double threshold = 0.05;
		double mappingThreshold = 0.5;
		double antonym_ratio = 0.5;
		

		double maxSimTarget = 0;
		double targetTotalWeight = 0;

		for (int i=0; i<target.lengthInContentWords; i++){
			
			double localMaxSim = 0;
			String localMaxMapping = "";
			int localMaxMappingNo = -1;
			
			for (int j=0; j<compound.lengthInContentWords; j++){
				
				double localSim;
				
				if (i > 0 && (target.componentWords[i-1].origin + target.componentWords[i].origin).equalsIgnoreCase(compound.componentWords[j].origin)){
					localSim = 1.01;
				}else if (i < target.lengthInContentWords - 1 && (target.componentWords[i].origin + target.componentWords[i+1].origin).equalsIgnoreCase(compound.componentWords[j].origin)){
					localSim = 1.01;
				}else if (j > 0 && (compound.componentWords[j-1].origin + compound.componentWords[j].origin).equalsIgnoreCase(target.componentWords[i].origin)){
					localSim = 1.01;
				}else
					localSim = model.getSimilarity(target.componentWords[i].tagged_word, compound.componentWords[j].tagged_word);
				
				if (localSim == 0){
					if (target.componentWords[i].origin.endsWith(compound.componentWords[j].origin) && compound.componentWords[j].origin.length() >= 6)
						localSim = 1;
					
					if (compound.componentWords[j].origin.endsWith(target.componentWords[i].origin) && target.componentWords[i].origin.length() >= 6)
						localSim = 1;
				}
			
				if (localSim > localMaxSim){
					localMaxSim = localSim;
					localMaxMapping = compound.componentWords[j].tagged_word;
					localMaxMappingNo = j;
					
					if (localMaxSim > 1)
						localMaxSim = 1;
					
					if (localSim > 1)
						localSim = 1;
				}
				
				if (localSim > mappingThreshold){
					target.componentWords[i].potentialMappings.add(new MappingEntry(j, localSim));
				}
			}
			
			double pos_ratio; 
			
			if (target.componentWords[i].tagged_word.endsWith("_NN"))
				pos_ratio = 1.0;
			else if (target.componentWords[i].tagged_word.endsWith("_VB"))
				pos_ratio = 1.0;
			else if (target.componentWords[i].tagged_word.endsWith("_JJ"))
				pos_ratio = 1.0;
			else if (target.componentWords[i].tagged_word.endsWith("_RB"))
				pos_ratio = 1.0;
			else if (target.componentWords[i].tagged_word.endsWith("_PRP"))
				pos_ratio = 1.0;
			else if (target.componentWords[i].tagged_word.endsWith("_CD"))
				pos_ratio = 1.0;
			else
				pos_ratio = 1.0;

			
			if (useCapacity)
				targetTotalWeight += Math.log(capacity) * pos_ratio;
			else
				targetTotalWeight += pos_ratio;

			//if (localMaxSim > 0 || model.index(target.componentWords[i].tagged_word) >= 0){
			if (localMaxSim > 0 || model.getFrequency(target.componentWords[i].tagged_word) > 0){
				
				
				if (localMaxSim >= threshold){
					
					if (!target.hasNegation && !compound.hasNegation){
						
						int index1 = target.componentWords[i].tagged_word.lastIndexOf('_');
						String word1 = target.componentWords[i].tagged_word.substring(0, index1);

						int index2 = localMaxMapping.lastIndexOf('_');
						String word2 = localMaxMapping.substring(0, index2);
						
						String order1 = word1 + "," + word2;
						String order2 = word2 + "," + word1;
							
						if (Arrays.binarySearch(antonym_set, order1) > 0 || Arrays.binarySearch(antonym_set, order2) > 0){
							// do nothing or penalty?
							if (useCapacity)
								maxSimTarget -= antonym_ratio * Math.log(capacity) * pos_ratio;
							else
								maxSimTarget -= antonym_ratio * pos_ratio;
							
							// System.out.println("Antonym found: " + order1);
							
						}else{
							if (useCapacity)
								maxSimTarget += Math.log(capacity * localMaxSim) * pos_ratio;
							else
								maxSimTarget += localMaxSim * pos_ratio;
						}
					}else{
						if (useCapacity)
							maxSimTarget += Math.log(capacity * localMaxSim) * pos_ratio;
						else
							maxSimTarget += localMaxSim * pos_ratio;
					}
					
				}else{
					
					int frequency = model.getFrequency(target.componentWords[i].tagged_word);
					
					double ic_ratio;
					
					if (Math.log(frequency) - 10 < 1)
						ic_ratio = 3.0;
					else
						ic_ratio = 3.0 / (Math.log(frequency) - 10);
					
					if (ic_ratio > 1.0) ic_ratio = 1.0;
					
					
					if (target.componentWords[i].tagged_word.endsWith("_NN"))
						pos_ratio = 1.0;
					else if (target.componentWords[i].tagged_word.endsWith("_VB"))
						pos_ratio = 1.0;
					else if (target.componentWords[i].tagged_word.endsWith("_JJ"))
						pos_ratio = 0.5;
					else if (target.componentWords[i].tagged_word.endsWith("_RB"))
						pos_ratio = 0.5;
					else if (target.componentWords[i].tagged_word.endsWith("_PRP"))
						pos_ratio = 1.0;
					else if (target.componentWords[i].tagged_word.endsWith("_CD"))
						pos_ratio = 1.0;
					else
						pos_ratio = 0.5;
					
					double penalty;
					
					if (useCapacity)
						penalty =  Math.log(capacity) *  ic_ratio * pos_ratio;
					else
						penalty =  1 *  ic_ratio * pos_ratio;
					
					maxSimTarget -= penalty;
					
					// System.out.println("Penalty for occuring of unrelevant words: " + target.componentWords[i].tagged_word + " <<<< " + target.ori_phrase);
				}
				
				if (localMaxSim > 0){
					
					for (int k = 0; k < target.componentWords[i].potentialMappings.size(); k++){
						
						if (target.componentWords[i].potentialMappings.elementAt(k).no == localMaxMappingNo){
							target.componentWords[i].potentialMappings.remove(k);
							break;
						}
					}
					
					target.componentWords[i].potentialMappings.add(new MappingEntry(localMaxMappingNo, localMaxSim));
				}
				
			//if the word is not in our vocabulary 	
			}else{
				//targetValidLength --;
				
				if (target.componentWords[i].tagged_word.endsWith("_CD")){
					
					if (useCapacity){
						
						if (compound.ori_phrase.contains(" some "))
							maxSimTarget += Math.log(capacity) * pos_ratio;
						else
							maxSimTarget -= Math.log(capacity) * pos_ratio;
					}else{
						
						if (compound.ori_phrase.contains(" some "))
							maxSimTarget += 1 * pos_ratio;
						else
							maxSimTarget -= 1 * pos_ratio;
					}
					
					// System.out.println("Penalty for occuring of unrelevant words: " + target.componentWords[i].tagged_word + " <<<< " + target.ori_phrase);

				}
				
			}
			
		}
		
		

		double maxSimCompound = 0;
		double compoundTotalWeight = 0;
		
		for (int i=0; i<compound.lengthInContentWords; i++){
			
			double localMaxSim = 0;
			String localMaxMapping = "";
			int localMaxMappingNo = -1;
			for (int j=0; j<target.lengthInContentWords; j++){

				double localSim;
				
				if (i > 0 && (compound.componentWords[i-1].origin + compound.componentWords[i].origin).equalsIgnoreCase(target.componentWords[j].origin)){
					localSim = 1.01;
				}else if (i < compound.lengthInContentWords - 1 && (compound.componentWords[i].origin + compound.componentWords[i+1].origin).equalsIgnoreCase(target.componentWords[j].origin)){
					localSim = 1.01;
				}else if (j > 0 && (target.componentWords[j-1].origin + target.componentWords[j].origin).equalsIgnoreCase(compound.componentWords[i].origin)){
					localSim = 1.01;
				}else
					localSim = model.getSimilarity(compound.componentWords[i].tagged_word, target.componentWords[j].tagged_word);
				
				if (localSim == 0){
					if (compound.componentWords[i].origin.endsWith(target.componentWords[j].origin) && target.componentWords[j].origin.length() >= 6)
						localSim = 1;
					
					if (target.componentWords[j].origin.endsWith(compound.componentWords[i].origin) && compound.componentWords[i].origin.length() >= 6)
						localSim = 1;
				}

			
				if (localSim > localMaxSim){
					localMaxSim = localSim;
					localMaxMapping = target.componentWords[j].tagged_word;
					localMaxMappingNo = j;

					if (localMaxSim > 1) 
						localMaxSim = 1;
					
					if (localSim > 1)
						localSim = 1;
				}
				
				if (localSim > mappingThreshold){
					compound.componentWords[i].potentialMappings.add(new MappingEntry(j, localSim));
				}

			}
			
			double pos_ratio;
			
			if (compound.componentWords[i].tagged_word.endsWith("_NN"))
				pos_ratio = 1.0;
			else if (compound.componentWords[i].tagged_word.endsWith("_VB"))
				pos_ratio = 1.0;
			else if (compound.componentWords[i].tagged_word.endsWith("_JJ"))
				pos_ratio = 1.0;
			else if (compound.componentWords[i].tagged_word.endsWith("_RB"))
				pos_ratio = 1.0;
			else if (compound.componentWords[i].tagged_word.endsWith("_PRP"))
				pos_ratio = 1.0;
			else if (compound.componentWords[i].tagged_word.endsWith("_CD"))
				pos_ratio = 1.0;
			else
				pos_ratio = 1.0;

			
			if (useCapacity)
				compoundTotalWeight += Math.log(capacity) * pos_ratio;
			else
				compoundTotalWeight += pos_ratio;
			
			//if (localMaxSim > 0 || model.index(compound.componentWords[i].tagged_word) >= 0){
			if (localMaxSim > 0 || model.getFrequency(compound.componentWords[i].tagged_word) > 0){
				
				if (localMaxSim >= threshold){
					
					if (!target.hasNegation && !compound.hasNegation){
						
						int index1 = compound.componentWords[i].tagged_word.lastIndexOf('_');
						String word1 = compound.componentWords[i].tagged_word.substring(0, index1);

						int index2 = localMaxMapping.lastIndexOf('_');
						String word2 = localMaxMapping.substring(0, index2);
						
						String order1 = word1 + "," + word2;
						String order2 = word2 + "," + word1;
							
						if (Arrays.binarySearch(antonym_set, order1) > 0 || Arrays.binarySearch(antonym_set, order2) > 0){
							// do nothing or penalty?
							if (useCapacity)
								maxSimCompound -= antonym_ratio * Math.log(capacity) * pos_ratio;
							else
								maxSimCompound -= antonym_ratio * pos_ratio;
							
							// System.out.println("Antonym found: " + order1);

						}else{
							
							if (useCapacity)
								maxSimCompound += Math.log(capacity * localMaxSim) * pos_ratio;
							else
								maxSimCompound += localMaxSim * pos_ratio;
						}
						
					}else{
						
						if (useCapacity)
							maxSimCompound += Math.log(capacity * localMaxSim) * pos_ratio;
						else
							maxSimCompound += localMaxSim * pos_ratio;
						
					}

					
				}else{
					
					int frequency = model.getFrequency(compound.componentWords[i].tagged_word);
					
					double ic_ratio;
					
					if (Math.log(frequency) - 10 < 1)
						ic_ratio = 3.0;
					else
						ic_ratio = 3.0 / (Math.log(frequency) - 10);
					
					if (ic_ratio > 1.0) ic_ratio = 1.0;

					if (compound.componentWords[i].tagged_word.endsWith("_NN"))
						pos_ratio = 1.0;
					else if (compound.componentWords[i].tagged_word.endsWith("_VB"))
						pos_ratio = 1.0;
					else if (compound.componentWords[i].tagged_word.endsWith("_JJ"))
						pos_ratio = 0.5;
					else if (compound.componentWords[i].tagged_word.endsWith("_RB"))
						pos_ratio = 0.5;
					else if (compound.componentWords[i].tagged_word.endsWith("_PRP"))
						pos_ratio = 1.0;
					else if (compound.componentWords[i].tagged_word.endsWith("_CD"))
						pos_ratio = 1.0;
					else
						pos_ratio = 0.5;
					
					double penalty;
					
					if (useCapacity)
						penalty =  Math.log(capacity) *  ic_ratio * pos_ratio;
					else
						penalty =  1 *  ic_ratio * pos_ratio;
					
					maxSimCompound -= penalty;
					
					// System.out.println("Penalty for occuring of unrelevant words: " + compound.componentWords[i].tagged_word + " <<<< " + compound.ori_phrase);

				}
			
				if (localMaxSim > 0){

					for (int k = 0; k < compound.componentWords[i].potentialMappings.size(); k++){
						
						if (compound.componentWords[i].potentialMappings.elementAt(k).no == localMaxMappingNo){
							compound.componentWords[i].potentialMappings.remove(k);
							break;
						}
					}
					
					compound.componentWords[i].potentialMappings.add(new MappingEntry(localMaxMappingNo, localMaxSim));

				}

				
			// if the word is not in our vocabulary 	
			}else{ 
				//compoundValidLength --;
				
				if (compound.componentWords[i].tagged_word.endsWith("_CD")){
					
					if (useCapacity){
						
						if (target.ori_phrase.contains(" some "))
							maxSimCompound += Math.log(capacity) * pos_ratio;
						else
							maxSimCompound -= Math.log(capacity) * pos_ratio;
					}else{
						
						if (target.ori_phrase.contains(" some "))
							maxSimCompound += 1 * pos_ratio;
						else
							maxSimCompound -= 1 * pos_ratio;
					}
					
					// System.out.println("Penalty for occuring of unrelevant words: " + compound.componentWords[i].tagged_word + " <<<< " + compound.ori_phrase);

				}
				
				
			}
			
		}

		
		//return normalizedSim;
		if (target.lengthInContentWords >= compound.lengthInContentWords){
			double result = maxSimTarget / (targetTotalWeight) * (1 - ShortTermWeight) + maxSimCompound / (compoundTotalWeight) * ShortTermWeight;
			if (result > 0) return result;
		}else{
			double result = maxSimTarget / (targetTotalWeight) * ShortTermWeight + maxSimCompound / (compoundTotalWeight) * (1 - ShortTermWeight);
			if (result > 0) return result;
		}
		
		return 0;
	}

	

	

	
	/*
	public static double getSTS_Similarity2(ComplexPredicate target, ComplexPredicate compound, SimilarityArrayModel model, String[] antonym_set){
		

		if (target.lengthInContentWords == 0 || compound.lengthInContentWords == 0)
			return Double.NEGATIVE_INFINITY;
		
		long corpus_size = 3000000000L;
		
		double maxSimTarget = 0;
		
		//double log_ratio = 25;
		//double threshold = 0.04;

		// test no. 8
		double capacity = 20;
		double threshold = 0.05;
		int targetTotalWeight = 0;
		int compoundTotalWeight = 0;

		for (int i=0; i<target.lengthInContentWords; i++){
			
			double localMaxSim = 0;
			String localMaxMapping = "";
			for (int j=0; j<compound.lengthInContentWords; j++){
				
				double localSim = model.getSimilarity(target.componentWords[i], compound.componentWords[j]);
			
				if (localSim > localMaxSim){
					localMaxSim = localSim;
					localMaxMapping = compound.componentWords[j];
				}
			}
			
			int frequency = model.getFrequency(target.componentWords[i]);
			
			double ic_ratio = - Math.log( ((double)frequency) / corpus_size);
			
			double pos_ratio;
			
			if (target.componentWords[i].endsWith("_NN"))
				pos_ratio = 1.0;
			else if (target.componentWords[i].endsWith("_VB"))
				pos_ratio = 1.0;
			else if (target.componentWords[i].endsWith("_JJ"))
				pos_ratio = 0.5;
			else if (target.componentWords[i].endsWith("_RB"))
				pos_ratio = 0.5;
			else if (target.componentWords[i].endsWith("_PRP"))
				pos_ratio = 1.0;
			else if (target.componentWords[i].endsWith("_CD"))
				pos_ratio = 1.0;
			else
				pos_ratio = 0.5;

			
			targetTotalWeight += Math.log(capacity) *  ic_ratio * pos_ratio;
			
			
			if (localMaxSim > 0 || model.index(target.componentWords[i]) >= 0){
				
				
				if (localMaxSim >= threshold){
					
					if (!target.hasNegation && !compound.hasNegation){
						
						int index1 = target.componentWords[i].lastIndexOf('_');
						String word1 = target.componentWords[i].substring(0, index1);

						int index2 = localMaxMapping.lastIndexOf('_');
						String word2 = localMaxMapping.substring(0, index2);
						
						String order1 = word1 + "," + word2;
						String order2 = word2 + "," + word1;
							
						if (Arrays.binarySearch(antonym_set, order1) > 0 || Arrays.binarySearch(antonym_set, order2) > 0){
							// do nothing or penalty?
							//maxSimTarget -= Math.log(capacity);
						}else
							maxSimTarget += Math.log(capacity * localMaxSim) * ic_ratio * pos_ratio;
						
						
					}else
						maxSimTarget += Math.log(capacity * localMaxSim) * ic_ratio * pos_ratio;
					
				}else{
					
					double penalty =  Math.log(capacity) *  ic_ratio * pos_ratio;
					
					maxSimTarget -= penalty;
				}
			
			//if the word is not in our vocabulary 	
			}else{
				//targetValidLength --;
				
				if (target.componentWords[i].endsWith("_CD"))
					maxSimTarget -= Math.log(capacity) *  ic_ratio * pos_ratio;
				
			}
		}

		double maxSimCompound = 0;
		
		for (int i=0; i<compound.lengthInContentWords; i++){
			
			double localMaxSim = 0;
			String localMaxMapping = "";
			for (int j=0; j<target.lengthInContentWords; j++){
				
				double localSim = model.getSimilarity(compound.componentWords[i], target.componentWords[j]);
			
				if (localSim > localMaxSim){
					localMaxSim = localSim;
					localMaxMapping = target.componentWords[j];
				}
			}
			
			int frequency = model.getFrequency(compound.componentWords[i]);
			
			double ic_ratio = - Math.log(((double)frequency) / corpus_size);
			

			double pos_ratio;
			
			if (compound.componentWords[i].endsWith("_NN"))
				pos_ratio = 1.0;
			else if (compound.componentWords[i].endsWith("_VB"))
				pos_ratio = 1.0;
			else if (compound.componentWords[i].endsWith("_JJ"))
				pos_ratio = 0.5;
			else if (compound.componentWords[i].endsWith("_RB"))
				pos_ratio = 0.5;
			else if (compound.componentWords[i].endsWith("_PRP"))
				pos_ratio = 1.0;
			else if (compound.componentWords[i].endsWith("_CD"))
				pos_ratio = 1.0;
			else
				pos_ratio = 0.5;

			compoundTotalWeight += Math.log(capacity) *  ic_ratio * pos_ratio;
			
			if (localMaxSim > 0 || model.index(compound.componentWords[i]) >= 0){
				
				if (localMaxSim >= threshold){
					
					if (!target.hasNegation && !compound.hasNegation){
						
						int index1 = compound.componentWords[i].lastIndexOf('_');
						String word1 = compound.componentWords[i].substring(0, index1);

						int index2 = localMaxMapping.lastIndexOf('_');
						String word2 = localMaxMapping.substring(0, index2);
						
						String order1 = word1 + "," + word2;
						String order2 = word2 + "," + word1;
							
						if (Arrays.binarySearch(antonym_set, order1) > 0 || Arrays.binarySearch(antonym_set, order2) > 0){
							// do nothing or penalty?
							//maxSimCompound -= Math.log(capacity);
						}else
							maxSimCompound += Math.log(capacity * localMaxSim) *  ic_ratio * pos_ratio;
						
					}else
						maxSimCompound += Math.log(capacity * localMaxSim) *  ic_ratio * pos_ratio;

					
				}else{
					
					double penalty =  Math.log(capacity) *  ic_ratio * pos_ratio;
					
					maxSimCompound -= penalty;
				}
			
			// if the word is not in our vocabulary 	
			}else{ 
				//compoundValidLength --;
				
				if (compound.componentWords[i].endsWith("_CD"))
					maxSimCompound -= Math.log(capacity) * ic_ratio * pos_ratio;

			}
			
		}

		if (maxSimTarget < 0)
			maxSimTarget = 0;
		
		if (maxSimCompound < 0)
			maxSimCompound = 0;
		
		//return normalizedSim;
		if (target.lengthInContentWords >= compound.lengthInContentWords){
			double result = maxSimTarget / targetTotalWeight * (1 - ShortTermWeight) + maxSimCompound / compoundTotalWeight * ShortTermWeight;
			if (result > 0) return result;
		}else{
			double result = maxSimTarget / targetTotalWeight * ShortTermWeight + maxSimCompound / compoundTotalWeight * (1 - ShortTermWeight);
			if (result > 0) return result;
		}
		
		return 0;
	}
	
	*/

	
	public String toString(){
		String result =
			    ori_phrase + "\n" + 
				"The number of words is " + lengthInAllWords + "\n" +
				"The number of content words is " + lengthInContentWords + "\n" +
				"The list of content words includes: ";
		
		for (OrderedWord orderedWord: componentWords){
			result += orderedWord.tagged_word + ":" + orderedWord.orderNo + " ";
		}

		return result;
	}
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
    	//String modelLocation = dataPath+"/model/stanford/left3words-distsim-wsj-0-18.tagger";
    	//String modelLocation = dataPath+"/model/stanford/20120612/left3words-distsim-wsj-0-18.tagger";
    	//String modelLocation = dataPath+"/model/stanford/pos-tagger/wsj-left3words/wsj-0-18-left3words-distsim.tagger";
    	//String modelLocation = dataPath+"/model/stanford/pos-tagger/english-bidirectional/english-bidirectional-distsim.tagger";
    	String modelLocation = dataPath+"/model/stanford/pos-tagger/wsj-bidirectional/wsj-0-18-bidirectional-distsim.tagger";
    	//String modelLocation = "./edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger";
    	//String modelLocation = dataPath+"/model/stanford/left3words-wsj-0-18.tagger";
    	//String modelLocation = dataPath+"/model/stanford/bidirectional-distsim-wsj-0-18.tagger";
        System.out.println("Reading model from file=" + modelLocation);
        MaxentTagger tagger = new MaxentTagger(modelLocation);
		Morphology morph = new Morphology();

        System.out.println("Reading sim array model ... ");
        SimilarityArrayModel simModel = SimilarityArrayModel.readModel(dataPath+"/model/BigArray/ceilling_cut/webbase2012AllPlusW5_S3");
        //SimilarityArrayModel simModel = SimilarityArrayModel.readModel(dataPath+"/model/BigArray/Gigawords2009AllW5_S3");
        
        System.out.println("Reading antonym set ... ");
		//File antonymFile = new File(dataPath1+"/testbed/STS/data/AntonymsLexicon");
		File antonymFile = new File(dataPath1+"/testbed/STS/data/WNet-antonyms-clean-pos.txt");
		BufferedReader antonymReader = new BufferedReader(new FileReader(antonymFile), 1000000);
		
        DependencyCheck d_check;
		
		//d_check = new DependencyCheck();
        d_check = null;

        String rdline;
		rdline = antonymReader.readLine();
		int sizeOfAntonyms = Integer.valueOf(rdline);
		
		String[] ANTONYM_SET = new String[sizeOfAntonyms];
		
		for (int i = 0; i < sizeOfAntonyms; i++){
			ANTONYM_SET[i] = antonymReader.readLine();
		}

		antonymReader.close();
		Arrays.sort(ANTONYM_SET);

        
    	BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
    	String compound1;
    	String compound2;
    	ComplexPredicate firstCompound;
    	ComplexPredicate secondCompound;

    	while(true){
        	System.out.println("Please input first compound");
        	compound1 = input.readLine();
        	
           	System.out.println("Please input second compound");
        	compound2 = input.readLine();
       	
        	firstCompound = new ComplexPredicate(compound1, tagger, morph, simModel, false, true, false);
        	System.out.println(firstCompound.toString());
        	
         	secondCompound = new ComplexPredicate(compound2, tagger, morph, simModel, false, true, false);
        	System.out.println(secondCompound.toString());

        	System.out.println("The similarity is: " + getSimilarity(firstCompound, secondCompound, simModel));
        	System.out.println("The STS similarity is: " + getSTS_Similarity(firstCompound, secondCompound, simModel, ANTONYM_SET));
        	
        	        	
       		System.out.println("Mapping from first phrase to second phrase:");
    		for (int i=0; i < firstCompound.componentWords.length; i++){
    			
    			if (firstCompound.componentWords[i].potentialMappings.size() > 0){
    				
    				String outline = firstCompound.componentWords[i] + " --> ";
    				
    				for (int k = 0; k < firstCompound.componentWords[i].potentialMappings.size(); k++){
    					
    					outline += secondCompound.componentWords[firstCompound.componentWords[i].potentialMappings.elementAt(k).no] + " " + firstCompound.componentWords[i].potentialMappings.elementAt(k).value + " ";
    				}
    				
    				System.out.println(outline);
    			}
    		}
    		
    		System.out.println();

    		System.out.println("Mapping from second phrase to first phrase:");
    		for (int i=0; i < secondCompound.componentWords.length; i++){
    			
    			if (secondCompound.componentWords[i].potentialMappings.size() > 0){
    				
    				String outline = secondCompound.componentWords[i] + " --> ";
    				
    				for (int k = 0; k < secondCompound.componentWords[i].potentialMappings.size(); k++){
    					
    					outline += firstCompound.componentWords[secondCompound.componentWords[i].potentialMappings.elementAt(k).no] + " " + secondCompound.componentWords[i].potentialMappings.elementAt(k).value + " ";
    				}
    				
    				System.out.println(outline);
    				
     			}
    		}

    		System.out.println();
            
    		
    		if (d_check != null){
    			
		 	    // --------------------------------------------------------------
		 	    // match from first compound to second compound.
		 	    // --------------------------------------------------------------
	    		Collection<TypedDependency> tdl1 = d_check.getDependencyCollection(compound1);
	    		Collection<TypedDependency> tdl2 = d_check.getDependencyCollection(compound2);
	    		
		 	    HashSet<Contradiction> contradictionSet1 = d_check.getContradictions(tdl1, tdl2, firstCompound, secondCompound);
		 	    	
	 	    	System.out.println("The number of contraditions is " + contradictionSet1.size());
	    		System.out.println(contradictionSet1);
	
	    		System.out.println();
	    		
	 	 	    // --------------------------------------------------------------
		 	    // match from second compound to first compound.
		 	    // --------------------------------------------------------------
	    	
	    		//switch tdl1 and tdl2
	 	    	Collection<TypedDependency> tmp_tdl = tdl2;
	 	    	tdl2 = tdl1;
	 	    	tdl1 = tmp_tdl;
	 	    	
	 	    	//switch firstCompound and secondCompound
	 	    	ComplexPredicate tmp_compound = secondCompound;
	 	    	secondCompound = firstCompound;
	 	    	firstCompound = tmp_compound;
	 	    	
	 	    	
		 	    HashSet<Contradiction> contradictionSet2 = d_check.getContradictions(tdl1, tdl2, firstCompound, secondCompound);
	 	    	
	 	    	System.out.println("The number of contraditions is " + contradictionSet2.size());
	    		System.out.println(contradictionSet2);
	
	    		System.out.println();
	    	
    		}
    		
        	
        	/*
        	firstCompound = new ComplexPredicate(compound1, tagger, morph, simModel, true, false, false);
        	System.out.println(firstCompound.toString());
        	
         	secondCompound = new ComplexPredicate(compound2, tagger, morph, simModel, true, false, false);
        	System.out.println(secondCompound.toString());

        	System.out.println("The similarity is: " + getSimilarity(firstCompound, secondCompound, simModel));
        	System.out.println("The STS similarity is: " + getSTS_Similarity(firstCompound, secondCompound, simModel, ANTONYM_SET));
        	
        	System.out.println();
        	*/
        	
        	/*
        	firstCompound = new ComplexPredicate(compound1, tagger, morph, simModel, false, false, true);
        	System.out.println(firstCompound.toString());
        	
         	secondCompound = new ComplexPredicate(compound2, tagger, morph, simModel, false, false, true);
        	System.out.println(secondCompound.toString());

        	System.out.println("The similarity is: " + getSimilarity(firstCompound, secondCompound, simModel));
        	System.out.println("The STS similarity is: " + getSTS_Similarity(firstCompound, secondCompound, simModel, ANTONYM_SET));
        	
       		System.out.println("Mapping from first phrase to second phrase:");
    		for (int i=0; i < firstCompound.componentWords.length; i++){
    			
    			if (firstCompound.componentWords[i].mappingNo > -1)
    				System.out.println(firstCompound.componentWords[i] + " --> " + secondCompound.componentWords[firstCompound.componentWords[i].mappingNo] + " " + firstCompound.componentWords[i].mappingValue);
    			
    		}
    		
    		System.out.println();

    		System.out.println("Mapping from second phrase to first phrase:");
    		for (int i=0; i < secondCompound.componentWords.length; i++){
    			
    			if (secondCompound.componentWords[i].mappingNo > -1)
    				System.out.println(secondCompound.componentWords[i] + " --> " + firstCompound.componentWords[secondCompound.componentWords[i].mappingNo] + " " + secondCompound.componentWords[i].mappingValue);
    			
    		}

        	System.out.println();
        	*/

        	/*
        	firstCompound = new ComplexPredicate(compound1, tagger, morph, simModel, true, true, false);
        	System.out.println(firstCompound.toString());
        	
         	secondCompound = new ComplexPredicate(compound2, tagger, morph, simModel, true, true, false);
        	System.out.println(secondCompound.toString());

        	System.out.println("The similarity is: " + getSimilarity(firstCompound, secondCompound, simModel));
        	System.out.println("The STS similarity is: " + getSTS_Similarity(firstCompound, secondCompound, simModel, ANTONYM_SET));
        	
        	System.out.println();
        	*/

    		System.out.println();
        }
	}

}
