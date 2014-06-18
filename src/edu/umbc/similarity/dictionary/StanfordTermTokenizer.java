package edu.umbc.similarity.dictionary;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

import edu.stanford.nlp.process.Morphology;
import edu.umbc.dbpedia.model.LSA_Model;
import edu.umbc.dbpedia.model.SimilarityArrayModel;
import edu.umbc.dbpedia.util.LexicalProcess;
import edu.umbc.dbpedia.util.OrderedWord;


public class StanfordTermTokenizer {

	String paragraph;
	String[] stopWords;
	StringTokenizer st;
	String[] vocabulary;
	int frequency[];
	Morphology morpha;
	boolean lastIsHave;
	String result;
	public int lengthInAllWords;
	int orderNo;

	String nextTaggedWord;
	String nextWord;
	String nextPosTag;
	
	String preTaggedWord;
	String preWord;
	String prePosTag;
	
	boolean identifyEntityName = false;
    
    public static String dataPath;
    public static String dataPath1;

	public static final HashMap<String, String> morpha_corrections = new HashMap<String, String>();
    static {
    	morpha_corrections.put("papers_NN", "paper");
    }

	public static final HashSet<String> CentNumbers = new HashSet<String>(Arrays.asList( "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety"));
	public static final HashSet<String> NineNumbers = new HashSet<String>(Arrays.asList( "one", "two", "three", "four", "five", "six", "seven", "eight", "nine"));


    
	public StanfordTermTokenizer(String paragraph_par, String[] stopwords_par, Morphology morpha_par, String[] vocabulary_par, int[] frequency_par, boolean enableEntityName) {
		// TODO Auto-generated constructor stub
		paragraph = paragraph_par;
		stopWords = stopwords_par;
		vocabulary = vocabulary_par;
		frequency = frequency_par;
		orderNo = 0;
		
		if (stopWords.length > 1)
			Arrays.sort(stopWords);
		
		morpha = morpha_par;
		st = new StringTokenizer(paragraph, " ");
		lastIsHave = false;

		nextTaggedWord = null;
		nextWord = null;
		nextPosTag = null;
		lengthInAllWords = 0;
		
		preTaggedWord = null;
		preWord = null;
		prePosTag = null;

		identifyEntityName = enableEntityName;
	}

	public int index(String term){
		return Arrays.binarySearch(vocabulary, term);
	}
	
	
	public String lemma(String word, String tag){
		
		if (LexicalProcess.isAcronym(word))
			return word;
		
		String correction = morpha_corrections.get(word.toLowerCase() + "_" + tag);
		
		if (correction != null) return correction;
		
		return morpha.lemma(word, tag);
		
	}


	public int getFrequency(String term){
		int index = index(term);
		if (index >= 0)
			return frequency[index];
		else
			return 0;
	}

	public OrderedWord getNextValidWord(){
		
		
		while (st.hasMoreTokens() || nextTaggedWord != null){

			lengthInAllWords ++;
			
			result = null;
			
			String taggedWord;
			
			if (nextTaggedWord != null){
				taggedWord = nextTaggedWord;
				nextTaggedWord = null;
				nextWord = null;
				nextPosTag = null;
			}else{
				taggedWord = st.nextToken();
				orderNo ++;
			}
				
			int index = taggedWord.lastIndexOf('_');
			if (index <= 0){
				lastIsHave = false;
				continue;
			}
			String word = taggedWord.substring(0, index);
			String posTag = taggedWord.substring(index + 1, taggedWord.length());
			
			
			if (word.endsWith(">")){
				lastIsHave = false;
				continue;
			}
			
			String wordInlowercase = word.toLowerCase();
			
			if (CentNumbers.contains(wordInlowercase) && !posTag.equals("CD")){
				posTag = "CD";
				taggedWord = word + "_" + posTag;
			}
			
			if (Character.isUpperCase(word.charAt(0)) || Character.isLowerCase(word.charAt(0)) || Character.isDigit(word.charAt(0)) || word.equals("'s")){
				
				if (stopWords.length == 0 || Arrays.binarySearch(stopWords, wordInlowercase) < 0){
					
					// NN
					if (posTag.startsWith("NN")){
						
						if (identifyEntityName && posTag.startsWith("NNP")){
							
							String nounPhrase = word;
							boolean headNounAtEnd = true;
							
							while (st.hasMoreTokens()){
								nextTaggedWord = st.nextToken();
								orderNo ++;
							
								int nextIndex = nextTaggedWord.lastIndexOf('_');

								if (nextIndex > 0){
									nextWord = nextTaggedWord.substring(0, nextIndex);
									nextPosTag = nextTaggedWord.substring(nextIndex + 1, nextTaggedWord.length());
								}
								
								if (nextPosTag.startsWith("NNP") || nextPosTag.equals("IN")){
									
									//if (nextPosTag.startsWith("NNP"))
									nounPhrase = nounPhrase + " " + nextWord;
									
									if (nextPosTag.equals("IN"))
										headNounAtEnd = false;
									
								}else{
									break;
								}
							}

							/*
							if (!st.hasMoreTokens()){
								nextTaggedWord = null;
								nextWord = null;
								nextPosTag = null;
							}
							*/
							
							if (nounPhrase.contains(" ")){
								
								if (nextTaggedWord != null){
									
									if (headNounAtEnd){
										return new OrderedWord(nounPhrase + "_NN", lastWord(nounPhrase), orderNo - 1);
									}else
										return new OrderedWord(nounPhrase + "_NN", "", orderNo - 1);
										
								}else{
									
									if (headNounAtEnd)
										return new OrderedWord(nounPhrase + "_NN", lastWord(nounPhrase), orderNo);
									else
										return new OrderedWord(nounPhrase + "_NN", "", orderNo);
								}
								
							}else{
								if (nextTaggedWord != null)
									return new OrderedWord(nounPhrase + "_NN", nounPhrase, orderNo - 1); 
								else
									return new OrderedWord(nounPhrase + "_NN", nounPhrase, orderNo); 
							}
							
						}else{  
							
							if (st.hasMoreTokens()){
								nextTaggedWord = st.nextToken();
								orderNo ++;
							
								int nextIndex = nextTaggedWord.lastIndexOf('_');
	
								if (nextIndex > 0){
									nextWord = nextTaggedWord.substring(0, nextIndex);
									nextPosTag = nextTaggedWord.substring(nextIndex + 1, nextTaggedWord.length());
								}
							}
							
							if (nextPosTag != null && nextPosTag.startsWith("NN")){
								
								String twoWordsTerm;
								int found;
								
								if (Character.isUpperCase(word.charAt(0)) && Character.isUpperCase(nextWord.charAt(0))){
									twoWordsTerm = word + " " + nextWord;
									found = index(twoWordsTerm + "_NN");
									
								}else{
									twoWordsTerm = wordInlowercase + " " + lemma(nextWord, "NN");
									found = index(twoWordsTerm + "_NN");
								}
								
								if (found >= 0){
									String returnWord = nextWord;
									
									result = twoWordsTerm + "_NN";
									lastIsHave = false;
									nextTaggedWord = null;
									nextWord = null;
									nextPosTag = null;
									
									return new OrderedWord(result, returnWord, orderNo);
								}
							}
							
							//if (posTag.startsWith("NNP") && index(word + "_NN") >= 0)
							if (posTag.startsWith("NNP")){
								result = word + "_NN";
								
								if (Character.isUpperCase(word.charAt(0)) && word.length() <= 3 && index(result) < 0){
									String wordToChange = word + "." + "_NN";
									
									if (index(wordToChange) > 0)
										result = wordToChange;
								}
								
							}else{
								
								if (preWord != null && preWord.equalsIgnoreCase("at")){
									
									if (wordInlowercase.matches("[1-2][0-9][h:][r]*[0-6][0-9]")){
										String time = wordInlowercase.replaceAll("[^0-9][^0-9]*", ":");
										if (time.endsWith(":"))
											time = time.substring(0, time.length() - 1);
	
										result = time + "_TE";
									}else
										result = lemma(word, "NN") + "_NN";
									
								}else{
									
									result = lemma(word, "NN") + "_NN";

									/*
									if (index(result) < 0 && wordInlowercase.endsWith("ing") && wordInlowercase.length() >= 5){
										String wordToChange = lemma(word, "VB") + "_VB";
										
										if (index(wordToChange) > 0)
											result = wordToChange;
									}
									*/
									
								}
							}	
						}
						
					// VB
					}else if (posTag.startsWith("VB")){
						
						if (st.hasMoreTokens()){
							nextTaggedWord = st.nextToken();
							orderNo ++;
						
							int nextIndex = nextTaggedWord.lastIndexOf('_');

							if (nextIndex > 0){
								nextWord = nextTaggedWord.substring(0, nextIndex);
								nextPosTag = nextTaggedWord.substring(nextIndex + 1, nextTaggedWord.length());
							}
						}
						
						if (nextPosTag != null){
							
							String twoWordsTerm;
							
							twoWordsTerm = lemma(word, "VB") + " " + nextWord.toLowerCase();

							int found = index(twoWordsTerm + "_VB");

							if (found >= 0){
								result = twoWordsTerm + "_VB";
								lastIsHave = false;
								nextTaggedWord = null;
								nextWord = null;
								nextPosTag = null;
								
								return new OrderedWord(result, word, orderNo - 1);
							}
						}

						//result[0] = morpha.stem(word, "VB").word() + "_VB";

						if (posTag.equals("VBN")){
							
							if (lastIsHave){
								result = lemma(word, "VB") + "_VB";
								
							}else{
								String adj_form = wordInlowercase + "_JJ";
								String vb_form = lemma(word, "VB") + "_VB";
								
								if (getFrequency(adj_form) > getFrequency(vb_form))
									result = adj_form;
								else
									result = vb_form;
							}
							
						}else{
							result = lemma(word, "VB") + "_VB";
						}
						
					
					// JJ
					}else if (posTag.startsWith("JJ")){
						
						/*
						if (st.hasMoreTokens()){
							nextTaggedWord = st.nextToken();
							orderNo ++;
						
							int nextIndex = nextTaggedWord.lastIndexOf('_');

							if (nextIndex > 0){
								nextWord = nextTaggedWord.substring(0, nextIndex);
								nextPosTag = nextTaggedWord.substring(nextIndex + 1, nextTaggedWord.length());
							}
						}
						
						if (nextPosTag != null && nextPosTag.startsWith("NN")){
							
							String twoWordsTerm;
							int found;
							
							if (Character.isUpperCase(word.charAt(0)) && Character.isUpperCase(nextWord.charAt(0))){
								twoWordsTerm = word + " " + nextWord;
								found = index(twoWordsTerm + "_NN");
								
							}else{
								twoWordsTerm = wordInlowercase + " " + lemma(nextWord, "NN");
								found = index(twoWordsTerm + "_NN");
							}

							
							if (found >= 0){
								String returnWord = nextWord;
								
								result = twoWordsTerm + "_NN";
								lastIsHave = false;
								nextTaggedWord = null;
								nextWord = null;
								nextPosTag = null;
								
								return new OrderedWord(result, returnWord, orderNo);
							}
						}
						*/

						if (wordInlowercase.endsWith("ed")){
							
							String vb_form = lemma(wordInlowercase, "VB") + "_VB";
							String adj_form = wordInlowercase + "_JJ";
						
							if (getFrequency(adj_form) > getFrequency(vb_form))
								result = adj_form;
							else
								result = vb_form;
						
						}else{
							
							if (index(taggedWord) > 0){
								result = taggedWord;
							}else{
								if (index(wordInlowercase + "_JJ") < 0 && wordInlowercase.endsWith("ing") && wordInlowercase.length() >= 5)
									result = wordInlowercase.substring(0, wordInlowercase.length() - 3) + "_VB";
								else
									result = lemma_adj(wordInlowercase, posTag) + "_JJ";
							}
						}
					
					// RB
					}else if (posTag.startsWith("RB")){
						
						result = wordInlowercase + "_RB";
					
					// CD
					}else if (posTag.startsWith("CD")){
						
						if (preWord != null && preWord.equalsIgnoreCase("at")){
							
							if (st.hasMoreTokens()){
								nextTaggedWord = st.nextToken();
								orderNo ++;
							
								int nextIndex = nextTaggedWord.lastIndexOf('_');

								if (nextIndex > 0){
									nextWord = nextTaggedWord.substring(0, nextIndex);
									nextPosTag = nextTaggedWord.substring(nextIndex + 1, nextTaggedWord.length());
								}
							}
							
							if (nextPosTag != null){
								
								String nextFilteredWord = nextWord.replaceAll("[^a-zA-Z]*", "").toUpperCase();
								
								if (nextFilteredWord.equals("AM") || nextFilteredWord.equals("PM")){
									
									String time = wordInlowercase.replaceAll("[^0-9][^0-9]*", ":");
									if (time.endsWith(":"))
										time = time.substring(0, time.length() - 1);
									
									result = time + " " + nextFilteredWord + "_TE";
									lastIsHave = false;
									nextTaggedWord = null;
									nextWord = null;
									nextPosTag = null;
									
									return new OrderedWord(result, "", orderNo);
								}
							}
							
							if (wordInlowercase.matches("[1-2][0-9][h:][r]*[0-6][0-9]")){
								String time = wordInlowercase.replaceAll("[^0-9][^0-9]*", ":");
								if (time.endsWith(":"))
									time = time.substring(0, time.length() - 1);

								result = time + "_TE";
							}else
								result = wordInlowercase + "_CD";

							//result = wordInlowercase + "_TE";
							
							
						}else{
							
							String numberString = word;

							/*
							boolean isTextNumber = word.matches("[A-Za-z][A-Za-z]*");
							
							while (st.hasMoreTokens()){
								nextTaggedWord = st.nextToken();
								orderNo ++;
							
								int nextIndex = nextTaggedWord.lastIndexOf('_');

								if (nextIndex > 0){
									nextWord = nextTaggedWord.substring(0, nextIndex);
									nextPosTag = nextTaggedWord.substring(nextIndex + 1, nextTaggedWord.length());
								}
								
								if (isTextNumber && (nextPosTag.startsWith("CD") || nextWord.equals("and")) && nextWord.matches("[A-Za-z][A-Za-z]*")){
									numberString = numberString + " " + nextWord;
								}else{
									break;
								}
							}*/
							
							
							boolean isCentNumber = CentNumbers.contains(wordInlowercase); 
							
							if (st.hasMoreTokens()){
								nextTaggedWord = st.nextToken();
								orderNo ++;
							
								int nextIndex = nextTaggedWord.lastIndexOf('_');

								if (nextIndex > 0){
									nextWord = nextTaggedWord.substring(0, nextIndex);
									nextPosTag = nextTaggedWord.substring(nextIndex + 1, nextTaggedWord.length());
								}
								
								if (isCentNumber && NineNumbers.contains(nextWord.toLowerCase())){
									numberString = numberString + " " + nextWord;
								}
							}

							if (numberString.contains(" ")){

								//if (numberString.endsWith(" and"))
								//	numberString = numberString.substring(0, numberString.length() - 3).trim();
								
								//System.out.println(numberString);
								
								lastIsHave = false;
								nextTaggedWord = null;
								nextWord = null;
								nextPosTag = null;

								return new OrderedWord(numberString + "_CD", numberString, orderNo); 
								
							}else{
								
								if (nextPosTag != null){
									
									String nextFilteredWord = nextWord.replaceAll("[^a-zA-Z]*", "").toUpperCase();
									
									if (nextFilteredWord.equals("AM") || nextFilteredWord.equals("PM")){
										
										String time = wordInlowercase.replaceAll("[^0-9][^0-9]*", ":");
										if (time.endsWith(":"))
											time = time.substring(0, time.length() - 1);
										
										result = time + " " + nextFilteredWord + "_TE";
										lastIsHave = false;
										nextTaggedWord = null;
										nextWord = null;
										nextPosTag = null;
										
										return new OrderedWord(result, "", orderNo);
									}
								}
							}
								
							
							/*	
							if (st.hasMoreTokens()){
								nextTaggedWord = st.nextToken();
								orderNo ++;
							
								int nextIndex = nextTaggedWord.lastIndexOf('_');

								if (nextIndex > 0){
									nextWord = nextTaggedWord.substring(0, nextIndex);
									nextPosTag = nextTaggedWord.substring(nextIndex + 1, nextTaggedWord.length());
								}
							}

							if (nextPosTag != null){
								
								String nextFilteredWord = nextWord.replaceAll("[^a-zA-Z]*", "").toUpperCase();
								
								if (nextFilteredWord.equals("AM") || nextFilteredWord.equals("PM")){
									
									String time = wordInlowercase.replaceAll("[^0-9][^0-9]*", ":");
									if (time.endsWith(":"))
										time = time.substring(0, time.length() - 1);
									
									result = time + " " + nextFilteredWord + "_TE";
									lastIsHave = false;
									nextTaggedWord = null;
									nextWord = null;
									nextPosTag = null;
									
									return new OrderedWord(result, "", orderNo);
								}
							}
							*/

							if (wordInlowercase.matches("[1-2][0-9][h:][r]*[0-6][0-9]")){
								String time = wordInlowercase.replaceAll("[^0-9][^0-9]*", ":");
								if (time.endsWith(":"))
									time = time.substring(0, time.length() - 1);

								result = time + "_TE";
							}else
								result = wordInlowercase + "_CD";
						}
							
					
					// DT
					}else if (posTag.startsWith("DT")){
						
						if (wordInlowercase.equals("no"))
							result = "not_RB";
						else
							result = wordInlowercase + "_OT";
					
					// PRP
					}else if (posTag.startsWith("PRP")){
						
						if (wordInlowercase.equals("it") || wordInlowercase.equals("itself") || wordInlowercase.equals("its"))
							result = wordInlowercase + "_OT";
						else
							result = wordInlowercase + "_PRP";

					// FW
					}else if (posTag.startsWith("FW")){
						
						result = wordInlowercase + "_FW";
					
					// Others
					}else 
						result = wordInlowercase + "_OT";
					
					
					if (wordInlowercase.equals("have") || wordInlowercase.equals("has") || wordInlowercase.equals("had") || wordInlowercase.equals("having"))
						lastIsHave = true;
					else
						lastIsHave = false;
					
					preTaggedWord = taggedWord;
					preWord = word;
					prePosTag = posTag;
	
					if (nextTaggedWord != null)
						return new OrderedWord(result, word, orderNo - 1);
					else
						return new OrderedWord(result, word, orderNo);
				}
			}
			
			
			if (wordInlowercase.equals("have") || wordInlowercase.equals("has") || wordInlowercase.equals("had") || wordInlowercase.equals("having"))
				lastIsHave = true;
			else
				lastIsHave = false;

			preTaggedWord = taggedWord;
			preWord = word;
			prePosTag = posTag;

		}
		
		return null;
		
	}
	
	
	
	private String lastWord(String nounPhrase) {
		// TODO Auto-generated method stub
		int index = nounPhrase.lastIndexOf(" ");
		
		if (index < 0)
			return nounPhrase;
		else{
			return nounPhrase.substring(index + 1, nounPhrase.length());
		}
	}

	
	public String lemma_adj(String word, String posTag) {
		
		if (getFrequency(word + "_JJ") > 0)
			return word;
		
		if (posTag.equals("JJR")){
			
			if (word.endsWith("ier"))
				return word.substring(0, word.length() - 3) + "y";
			else if (word.endsWith("er")){
				
				if (word.length() >= 4 && (word.charAt(word.length() - 3) == word.charAt(word.length() - 4)))
					return word.substring(0, word.length() - 3);
				else
					return word.substring(0, word.length() - 2);
			}else
				return word;
			
		}else if (posTag.equals("JJS")){
			
			if (word.endsWith("iest"))
				return word.substring(0, word.length() - 4) + "y";
			else if (word.endsWith("est")){
				
				if (word.length() >= 5 && (word.charAt(word.length() - 4) == word.charAt(word.length() - 5)))
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
	 * @throws IOException 
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
        
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


		Morphology morph = new Morphology();
		SimilarityArrayModel test1 = SimilarityArrayModel.readModel(dataPath+"/model/BigArray/wiki5_outlier_gutn");

        InputStreamReader isReader = new InputStreamReader(System.in);
        BufferedReader bufReader = new BufferedReader(isReader);
        while (true) {
            System.out.print("\n\nINPUT> ");
            System.out.flush();
            String line = bufReader.readLine();
            
            if (line == null || line.length() < 1 
                || line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit"))
                break;
            
            StanfordTermTokenizer test = new StanfordTermTokenizer(line, new String[]{"have", "is", "an", "the"}, morph, test1.vocabulary, test1.frequency, false);
            
            String output;
            while ((output = test.getNextValidWord().tagged_word) != null){ 
            	System.out.print(output);
            	
            	System.out.print(" ");
            }
        }
	}

}
