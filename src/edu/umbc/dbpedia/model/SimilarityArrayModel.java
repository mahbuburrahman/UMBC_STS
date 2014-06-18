package edu.umbc.dbpedia.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.TreeMap;

import com.mdimension.jchronic.Chronic;
import com.mdimension.jchronic.Options;
import com.mdimension.jchronic.utils.Span;
import com.mdimension.jchronic.utils.Time;

import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.umbc.nlp.tool.NumberConverter;
import edu.umbc.nlp.tool.NumberException;
import edu.umbc.nlp.tool.StringSimilarity;

public class SimilarityArrayModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public float[] sim_array;  
	public int sizeOfVocabulary;
	public String[] vocabulary;
	public int frequency[];
    public static String dataPath;
    public static String dataPath1;


	public SimilarityArrayModel(String filename) {
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

        
        /* Read vocabulary */
		
		try{
			File vocabularyFile = new File(filename + ".voc");
			BufferedReader vocabularyReader = new BufferedReader(new FileReader(vocabularyFile), 1000000);
			
			String rdline;
			rdline = vocabularyReader.readLine();
			sizeOfVocabulary = Integer.valueOf(rdline);
			
			int m = sizeOfVocabulary * (sizeOfVocabulary + 1) / 2;
			sim_array = new float[m];
			vocabulary = new String[sizeOfVocabulary];
			frequency = new int[sizeOfVocabulary];
			
			for (int i = 0; i < sizeOfVocabulary; i++){
				vocabulary[i] = vocabularyReader.readLine();
			}
	
			vocabularyReader.close();
			
			/* Read Frequency array */
			File frequencyFile = new File(filename + ".frq");
			BufferedReader frequencyReader = new BufferedReader(new FileReader(frequencyFile), 1000000);
			
			for (int i = 0; i < sizeOfVocabulary; i++){
				frequency[i] = Integer.valueOf(frequencyReader.readLine());
			}
			
			frequencyReader.close();

			
		} catch (Exception e){
			System.out.println(e.getMessage());
			e.printStackTrace(System.out);
			System.exit(-1);
		}

		
	}
	
	public int getFrequency(int index){

		if (index >= 0)
			return frequency[index];
		else
			return 0;
	}
	
	
	public int getFrequency(String word){
		int index = index(word);
		if (index >= 0)
			return frequency[index];
		else
			return 0;
	}
	
	public float getSimilarity(int i, int j){
		
		if (i < j){
			int temp = j;
			j = i;
			i = temp;
		}

		int n = sizeOfVocabulary;
		
		int k = (2 * n - j + 1) * j / 2 + i - j;
		
		return sim_array[k];
		
		
	}

	public void setSimilarity(int i, int j, float sim){
		
		if (i < j){
			int temp = j;
			j = i;
			i = temp;
		}

		int n = sizeOfVocabulary;
		
		int k = (2 * n - j + 1) * j / 2 + i - j;
		
		sim_array[k] = sim;
		
		
	}

	public void loadInitialSimilarity_LSA(LSA_Model lsa){
		
		int count = 0;
		
		for (int i = 0; i < sizeOfVocabulary; i++){

			for (int j = 0; j <= i; j++){
			
				float lsa_sim = lsa.getCosineSimilarity(i, j);
				
				if (lsa_sim > 0){

					setSimilarity(i, j, lsa_sim);
					count ++;
					
				}else
					setSimilarity(i, j, 0);
			}
			
			if (i % 1000 == 0)
				System.out.println(i + " rows have been processed.");
		}
		
		System.out.println("There are totally " + count + " non-zero similarity values");

	}

	
	public void loadInitialSimilarity_outlier(LSA_Model gutenberg_lsa, LSA_Model wiki_lsa){
		
		int count = 0;
		
		for (int i = 0; i < sizeOfVocabulary; i++){

			for (int j = 0; j <= i; j++){
			
				float gutn_sim = gutenberg_lsa.getCosineSimilarity(i, j);
				float wiki_sim = wiki_lsa.getCosineSimilarity(i, j);
				
				/*
				//if (wiki_sim > 0.4 && gutn_sim < 0.1 && wiki_lsa.frequency[i] > 1000 && wiki_lsa.frequency[j] > 1000 && gutenberg_lsa.frequency[i] > 3000 && gutenberg_lsa.frequency[j] > 3000){
				if (gutn_sim > 0.4 && wiki_sim < 0.1 && wiki_lsa.frequency[i] > 2000 && wiki_lsa.frequency[j] > 2000 && gutenberg_lsa.frequency[i] > 4000 && gutenberg_lsa.frequency[j] > 4000){
					String word1 = vocabulary[i];
					String word2 = vocabulary[j];
					if ((word1.endsWith("NN") && word2.endsWith("NN")) || (word1.endsWith("VB") && word2.endsWith("VB")))
					//if ((word1.endsWith("VB") && word2.endsWith("VB")))
						System.out.println(word1 + " " + word2 + " :" + wiki_sim + ", " + gutn_sim);
				}
				*/
				float variation = (float) 0.20;
				
				if (wiki_sim > 0){

					if (gutenberg_lsa.frequency[i] > 4000 && gutenberg_lsa.frequency[j] > 4000){
						if (wiki_sim - gutn_sim > variation || gutn_sim - wiki_sim > variation){
							if ((wiki_sim + gutn_sim) / 2 > 0){
								setSimilarity(i, j, (wiki_sim + gutn_sim) / 2);
								count ++;
							}else
								setSimilarity(i, j, 0);
						}else{
							setSimilarity(i, j, wiki_sim);
							count ++;
						}
					
					}else{
						setSimilarity(i, j, wiki_sim);
						count ++;
					}
					
				}else if (gutn_sim > 0){
					
					if (gutenberg_lsa.frequency[i] > 4000 && gutenberg_lsa.frequency[j] > 4000){
						if (gutn_sim - wiki_sim < variation){
							setSimilarity(i, j, gutn_sim);
							count ++;
						}else if ((wiki_sim + gutn_sim) / 2 > 0) {
							setSimilarity(i, j, (wiki_sim + gutn_sim) / 2);
							count ++;
						}else
							setSimilarity(i, j, 0);
							
					}else
						setSimilarity(i, j, 0);
					
				}else
					setSimilarity(i, j, 0);
			}
			
			if (i % 1000 == 0)
				System.out.println(i + " rows have been processed.");
		}
		
		System.out.println("There are totally " + count + " non-zero similarity values");

	}


	public int index(String word){
		return Arrays.binarySearch(vocabulary, word);
	}

	
	public float getSimilarity(String word1, String word2){
		
		String lemma1 = word1.substring(0, word1.lastIndexOf("_"));
		String lemma2 = word2.substring(0, word2.lastIndexOf("_"));


		if (index(word1) > 0 && getFrequency(word1) == 0 && Character.isUpperCase(word1.charAt(0)) && word1.endsWith("_JJ")){
			word1 = lemma1 + "_NN";
		}

		if (index(word2) > 0 && getFrequency(word2) == 0 && Character.isUpperCase(word2.charAt(0)) && word2.endsWith("_JJ")){
			word2 = lemma2 + "_NN";
		}

		
		if (word1.equalsIgnoreCase(word2))
			return 1;
		
		if (lemma1.equalsIgnoreCase(lemma2))
			return 1;


		if (word1.endsWith("_RB") && word2.endsWith("_JJ")){
			if (lemma1.equals(lemma2 + "ly"))
				return 1;
		}
		
		if (word2.endsWith("_RB") && word1.endsWith("_JJ")){
			if (lemma2.equals(lemma1 + "ly"))
				return 1;
		}
		
		if (word1.contains(".") || word2.contains(".")){
			
			String cleanedWord1 = word1.replace(".", "");
			String cleanedWord2 = word2.replace(".", "");
			
			if (cleanedWord1.equalsIgnoreCase(cleanedWord2))
				return 1;
		}
		
		if (word1.endsWith("_NN") && word2.endsWith("_NN")){
			
			if (lemma1.length() >= 4 && lemma2.equals(lemma1 + "ern")){
				if (lemma2.matches("[A-Za-z]*"))
					return 1;
			}

			if (lemma2.length() >= 4 && lemma1.equals(lemma2 + "ern")){
				if (lemma1.matches("[A-Za-z]*"))
					return 1;
			}

			if (lemma1.contains(" ") && lemma2.matches("[A-Z][A-Z][A-Z]*")){
				
				String acronym = "";
				
				for (int i=0; i < lemma1.length(); i++){
					if (Character.isUpperCase(lemma1.charAt(i)))
						acronym = acronym + lemma1.charAt(i);
				}
				
				if (acronym.equals(lemma2))
					return 1;
			}

			if (lemma2.contains(" ") && lemma1.matches("[A-Z][A-Z][A-Z]*")){

				String acronym = "";
				
				for (int i=0; i < lemma2.length(); i++){
					if (Character.isUpperCase(lemma2.charAt(i)))
						acronym = acronym + lemma2.charAt(i);
				}
				
				if (acronym.equals(lemma1))
					return 1;
			}
			
			/*
			if ((name1.contains(" ") && !name2.contains(" ")) || (name2.contains(" ") && !name1.contains(" "))){
				
				if (name1.substring(name1.lastIndexOf(" ") + 1, name1.length()).equalsIgnoreCase(name2.substring(name2.lastIndexOf(" ") + 1, name2.length()))){
					return 1;
				}
			}
			*/
		}
		
		if (word1.endsWith("_CD") && (word2.endsWith("_JJ") || word2.endsWith("_RB"))){
			
			String number2 = word2.substring(0, word2.length() - 3);

			Double double2 = null;
			
			try {
				double2 = (double) NumberConverter.parse(number2);
			} catch (NumberException e1) {
			}

			if (double2 != null)
				word2 = number2 + "_CD";
		}

		if (word2.endsWith("_CD") && (word1.endsWith("_JJ") || word1.endsWith("_RB"))){
			
			String number1 = word1.substring(0, word1.length() - 3);

			Double double1 = null;
			
			try {
				double1 = (double) NumberConverter.parse(number1);
			} catch (NumberException e1) {
			}

			if (double1 != null)
				word1 = number1 + "_CD";
		}

		if (word1.endsWith("_CD") && word2.endsWith("_CD")){
			
			String number1 = word1.substring(0, word1.length() - 3);
			String number2 = word2.substring(0, word2.length() - 3);
			
			number1 = number1.replaceAll(",", "");
			number2 = number2.replaceAll(",", "");
			
			Double double1 = null;
			Double double2 = null;
			
			try{
				double1 = Double.parseDouble(number1);
			}catch (Exception e){
				try {
					double1 = (double) NumberConverter.parse(number1);
				} catch (NumberException e1) {
				}
			}
			
			try{
				double2 = Double.parseDouble(number2);
			}catch (Exception e){
				try {
					double2 = (double) NumberConverter.parse(number2);
				} catch (NumberException e1) {
				}
			}
			
			if (double1 != null && double2 != null){
				
				if (double1.equals(double2))
					return 1;
				else
					return 0;
			}
			
			if (number1.equals(number2))
				return 1;
			else
				return 0;
		}
		
		
		if (word1.endsWith("_PRP") && word2.endsWith("_PRP")){
			
			String prp1 = word1.substring(0, word1.length() - 4).toLowerCase();
			String prp2 = word2.substring(0, word2.length() - 4).toLowerCase();
			
			if ((prp1.equals("i") && prp2.equals("me")) || (prp1.equals("me") && prp2.equals("i")))
				return (float) 0.99;

			if ((prp1.equals("we") && prp2.equals("us")) || (prp1.equals("us") && prp2.equals("we")))
				return (float) 0.99;

			if ((prp1.equals("they") && prp2.equals("them")) || (prp1.equals("them") && prp2.equals("they")))
				return (float) 0.99;

			if ((prp1.equals("he") && prp2.equals("him")) || (prp1.equals("him") && prp2.equals("he")))
				return (float) 0.99;

			if ((prp1.equals("she") && prp2.equals("her")) || (prp1.equals("her") && prp2.equals("she")))
				return (float) 0.99;

		}
		
		
		if (word1.endsWith("_TE") && word2.endsWith("_TE")){
			
			String time1 = word1.substring(0, word1.length() - 3);
			String time2 = word2.substring(0, word2.length() - 3);
			
			Options options = new Options();
		    options.setNow(Time.construct(2013, 3, 11, 0, 0, 0, 0));
			
			Span span1 = Chronic.parse(time1, options);
			Span span2 = Chronic.parse(time2, options);
			
			if (span1 != null && span2 != null && span1.equals(span2))
				return 1;
			else
				return 0;
			
		}
		
		if (word1.endsWith("_TE")){
			
			word1 = "o'clock_RB";
		}

		if (word2.endsWith("_TE")){
			
			word2 = "o'clock_RB";
		}
		
		if (word1.contains("-") && !word2.contains("-")){
			
			int index = lemma1.lastIndexOf("-");
			String prefix = lemma1.substring(0, index);
			String suffix = lemma1.substring(index + 1, lemma1.length());
			
			if (prefix.equalsIgnoreCase(lemma2) || suffix.equalsIgnoreCase(lemma2))
				return 1;
		}
		
		if (word2.contains("-") && !word1.contains("-")){
			
			int index = lemma2.lastIndexOf("-");
			String prefix = lemma2.substring(0, index);
			String suffix = lemma2.substring(index + 1, lemma2.length());
			
			if (prefix.equalsIgnoreCase(lemma1) || suffix.equalsIgnoreCase(lemma1))
				return 1;
		}
		
		if (word1.contains(" ") && !word2.contains(" ")){
			
			int index = lemma1.lastIndexOf(" ");
			String prefix = lemma1.substring(0, index);
			String suffix = lemma1.substring(index + 1, lemma1.length());
			
			if (prefix.equalsIgnoreCase(lemma2) || suffix.equalsIgnoreCase(lemma2) || suffix.equalsIgnoreCase(lemma2 + "s"))
				return 1;
		}
		
		if (word2.contains(" ") && !word1.contains(" ")){
			
			int index = lemma2.lastIndexOf(" ");
			String prefix = lemma2.substring(0, index);
			String suffix = lemma2.substring(index + 1, lemma2.length());
			
			if (prefix.equalsIgnoreCase(lemma1) || suffix.equalsIgnoreCase(lemma1))
				return 1;
		}

		
		int index1 = index(word1);
		int index2 = index(word2);
		
		if (index1 < 0 || index2 < 0){

			int prefixLengthForEquality = 6;
			
			if (word1.length() >= prefixLengthForEquality && word2.length() >= prefixLengthForEquality && !word1.contains(" ") && !word2.contains(" ")){
				if (word1.substring(0, prefixLengthForEquality).equalsIgnoreCase(word2.substring(0, prefixLengthForEquality)))
					return 1;
			}

			if (lemma1.length() == lemma2.length() && word1.substring(word1.length() - 3, word1.length()).equals(word2.substring(word2.length()-3, word2.length()))){
				
				String lowcaseLemma1 = lemma1.toLowerCase();
				String lowcaseLemma2 = lemma2.toLowerCase();
				
				int matches = 0;
				
				for (int i = 0; i < lowcaseLemma1.length(); i++ ){
					
					if (lowcaseLemma1.charAt(i) == lowcaseLemma2.charAt(i))
						matches ++;
				}
				
				if ((double) matches / lowcaseLemma1.length() > 0.65)
					return 1;
			}
			
			if (index1 < 0){
				word1 = posWordToLowerCase(word1);
				index1 = index(word1);
			}
			
			if (index2 < 0){
				word2 = posWordToLowerCase(word2);
				index2 = index(word2);
			}
			
			if (index1 > 0 && index2 > 0){
				
				return getSimilarity(index1, index2);
			}
			
			if (word1.substring(0, word1.lastIndexOf("_")).equalsIgnoreCase(word2.substring(0, word2.lastIndexOf("_"))))
				return 1;
			
			String cleanForm1 = lemma1.replaceAll("[^A-Za-z0-9][^A-Za-z0-9]*", "");
			String cleanForm2 = lemma2.replaceAll("[^A-Za-z0-9][^A-Za-z0-9]*", "");
			
			if (cleanForm1.length() >= 5 && lemma1.length() - cleanForm1.length() < 3 && lemma2.length() - cleanForm2.length() < 3){
				if (cleanForm1.equals(cleanForm2))
					return 1;
			}
			
			//deal with misspelled words
			if (lemma1.matches("[A-Za-z][A-Za-z]*") && lemma2.matches("[A-Za-z][A-Za-z]*") && !word1.endsWith("_PRP") && !word2.endsWith("_PRP")){
				
				if (StringSimilarity.getSim(lemma1.toLowerCase(), lemma2.toLowerCase()) > 0.66)
					return 1;
			}
				
			
			return 0;
		}
		
		
		float sim = getSimilarity(index1, index2);

		if (word1.endsWith(" to_VB") || word2.endsWith(" to_VB")){
			
			if (word1.endsWith(" to_VB"))
				word1 = word1.substring(0, word1.length() - 6) + "_VB";
				
			if (word2.endsWith(" to_VB"))
				word2 = word2.substring(0, word2.length() - 6) + "_VB";
			
			index1 = index(word1);
			index2 = index(word2);
			
			float sim2 = getSimilarity(index1, index2);

			if (sim2 > sim)
				sim = sim2;
		}
		
		/*
		if (word1.endsWith("_VB") && word2.endsWith("_VB")){
			if (2 * sim > 1)
				return 1;
			else
				return 2 * sim;
		}
		*/
		
		return sim;
		
	}

	
	String posWordToLowerCase(String posWord){
		
		int index = posWord.lastIndexOf('_');
		String word = posWord.substring(0, index);
		String posTag = posWord.substring(index + 1, posWord.length());

		return word.toLowerCase() + "_" + posTag;
	}
	
	
	String fromNumberToWords(String number){
		
		if (number.equalsIgnoreCase("1"))
			return "one";
		else if (number.equalsIgnoreCase("2"))
			return "two";
		else if (number.equalsIgnoreCase("3"))
			return "three";
		else if (number.equalsIgnoreCase("4"))
			return "four";
		else if (number.equalsIgnoreCase("5"))
			return "five";
		else if (number.equalsIgnoreCase("6"))
			return "six";
		else if (number.equalsIgnoreCase("7"))
			return "seven";
		else if (number.equalsIgnoreCase("8"))
			return "eight";
		else if (number.equalsIgnoreCase("9"))
			return "nine";
		else if (number.equalsIgnoreCase("10"))
			return "ten";
		else
			return number;
	}

	public boolean setSimilarity(String word1, String word2, float sim){
		
		int index1 = index(word1);
		int index2 = index(word2);
		
		if (index1 < 0 || index2 < 0)
			return false;

		setSimilarity(index1, index2, sim);
		
		return true;
	}
	
	
	public FloatElement[] getSortedWordsBySimilarity(String word) throws Exception{
		
		int index1 = index(word);
		
		if (index1 < 0)
			throw new Exception("The word " + word + " is unknown to our vocabulary!");
			
		ArrayList<FloatElement> correlateWords = new ArrayList<FloatElement>();
		
		for (int i=0; i<sizeOfVocabulary; i++){
			
			if (i == index1)
				continue;
				
			float sim = 0;
			int frequency = 0;
			
			try {
				frequency =  getFrequency(i);
				
				if (frequency < 500) 
					continue;

				sim = getSimilarity(index1, i);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				continue;
			}
			
			FloatElement element = new FloatElement(vocabulary[i], sim, 0, frequency);
			correlateWords.add(element);
		}
		
		FloatElement[] result = correlateWords.toArray(new FloatElement[correlateWords.size()]);
		Arrays.sort(result);
		
		return result;
		
	}

	
	public FloatElement[] getSortedWordsBySimilarityWithSamePOS(String word){
		
		int index1 = index(word);
		ArrayList<FloatElement> correlateWords = new ArrayList<FloatElement>();
		
		for (int i=0; i<sizeOfVocabulary; i++){
			
			String pos = word.substring(word.length() - 2, word.length());
			
			if (!vocabulary[i].endsWith(pos))
				continue;
			
			if (i == index1)
				continue;
				
			float sim = 0;
			int frequency = 0;
			
			try {
				frequency =  getFrequency(i);
				
				if (frequency < 1000) 
					continue;

				sim = getSimilarity(index1, i);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				continue;
			}
			
			FloatElement element = new FloatElement(vocabulary[i], sim, 0, frequency);
			correlateWords.add(element);
		}
		
		FloatElement[] result = correlateWords.toArray(new FloatElement[correlateWords.size()]);
		Arrays.sort(result);
		
		return result;
		
	}

	
	public String printPCW(Object[] sortedWords, int size){
		
		StringBuffer temp = new StringBuffer();
		
		for (int i=0; i<size; i++){
			temp.append(sortedWords[i] + ", ");
		}
		
		System.out.println(temp);
		
		return temp.toString();

	}


	static public void writeModel(String modelname, SimilarityArrayModel model) throws IOException{

		FileOutputStream fileOut;
		ObjectOutputStream objOut;
		fileOut = new FileOutputStream(modelname + ".BigArray_model");
		objOut = new ObjectOutputStream(fileOut);
		objOut.writeObject(model);
		objOut.close();
		System.out.println("seriliazation done!");

	}

	
	static public SimilarityArrayModel readModel(String modelname) throws IOException, ClassNotFoundException{
        
		FileInputStream fileIn = new FileInputStream(modelname + ".BigArray_model");
        ObjectInputStream objIn = new ObjectInputStream(fileIn);
        return (SimilarityArrayModel) objIn.readObject();
	}

	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws Exception 
	 */
	public static void main(String[] args) throws IOException, Exception {
		// TODO Auto-generated method stub

		/*
		LSA_Model gutn = new LSA_Model(dataPath+"/model/SVD/Gutenberg2010AllW5");
		LSA_Model wiki = new LSA_Model(dataPath+"/model/SVD/Wikipedia2006AllW5");
		
		SimilarityArrayModel model = new SimilarityArrayModel(dataPath+"/model/BigArray/wiki5");
		
		model.loadInitialSimilarity_outlier(gutn, wiki);
		System.out.println("Finished loading!");
		
		SimilarityArrayModel.writeModel(dataPath+"/model/BigArray/wiki5_outlier_gutn", model);
		System.out.println("Finished serialization!");
		*/
		
		/*
		LSA_Model webbase = new LSA_Model(dataPath+"/model/SVD/webbase2012AllW2");
		
		SimilarityArrayModel model = new SimilarityArrayModel(dataPath+"/model/BigArray/webbase2012AllW2");
		
		model.loadInitialSimilarity_LSA(webbase);
		System.out.println("Finished loading!");
		
		SimilarityArrayModel.writeModel(dataPath+"/model/BigArray/webbase2012AllW2", model);
		System.out.println("Finished serialization!");
		*/
		
		/*
		LSA_Model gigawords = new LSA_Model(dataPath+"/model/SVD/Gigawords2009AllW5");
		
		SimilarityArrayModel model = new SimilarityArrayModel(dataPath+"/model/BigArray/Gigawords2009AllW5");
		
		model.loadInitialSimilarity_LSA(gigawords);
		System.out.println("Finished loading!");
		
		SimilarityArrayModel.writeModel(dataPath+"/model/BigArray/Gigawords2009AllW5", model);
		System.out.println("Finished serialization!");
		*/
		
		
		
		//String modelLocation = "./edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger";
    	String modelLocation = dataPath+"/model/stanford/pos-tagger/english-left3words/english-left3words-distsim.tagger";
        System.out.println("Reading model from file=" + modelLocation);
        MaxentTagger tagger = new MaxentTagger(modelLocation);
        Morphology morpha = new Morphology();
		
		//LSA_Model gutn = new LSA_Model(dataPath+"/model/SVD/Gutenberg2010AllW5", 6500);
		//LSA_Model wiki = new LSA_Model(dataPath+"/model/SVD/Wikipedia2006AllW5", 1000);
		
		//SimilarityArrayModel model0 = SimilarityArrayModel.readModel(dataPath+"/model/BigArray/wiki5_outlier_gutn");
		//SimilarityArrayModel model1 = SimilarityArrayModel.readModel(dataPath+"/model/BigArray/wiki5_outlier_gutn_step1");
		//SimilarityArrayModel model2 = SimilarityArrayModel.readModel(dataPath+"/model/BigArray/wiki5_outlier_gutn_step2");
		//SimilarityArrayModel model3 = SimilarityArrayModel.readModel(dataPath+"/model/BigArray/wiki5_outlier_gutn_step3");

		SimilarityArrayModel model0 = SimilarityArrayModel.readModel(dataPath+"/model/BigArray/webbase2012AllW2");
		SimilarityArrayModel model1 = SimilarityArrayModel.readModel(dataPath+"/model/BigArray/Gigawords2009AllW2");
		//SimilarityArrayModel model1 = SimilarityArrayModel.readModel(dataPath+"/model/BigArray/webbase2012AllW2_S1");
		//SimilarityArrayModel model2 = SimilarityArrayModel.readModel(dataPath+"/model/BigArray/webbase2012AllW2_S2");

		//SimilarityArrayModel model0 = SimilarityArrayModel.readModel(dataPath+"/model/BigArray/Gigawords2009AllW5");
		//SimilarityArrayModel model1 = SimilarityArrayModel.readModel(dataPath+"/model/BigArray/Gigawords2009AllW5_S2");
		//SimilarityArrayModel model2 = SimilarityArrayModel.readModel(dataPath+"/model/BigArray/Gigawords2009AllW5_S3");

		
		SimilarityArrayModel model = model0;
		
		System.out.println("Input the word.");
		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		String word = input.readLine();
		int adj_vb_discriminator_threshold = 500;
		
		while (!word.equals("quit")){
			
			System.out.println("Input the gloss.");
			String gloss = input.readLine();
			
			if (word.endsWith("_NN"))
				gloss = "I see " + gloss;
			else if (word.endsWith("_VB"))
				gloss = "to " + gloss;
			
			String taggedGloss = tagger.tagString(gloss).replace('/', '_');
			
			StringTokenizer st = new StringTokenizer(taggedGloss, " ");

			int tokenNo = 0;
			while (st.hasMoreElements()){
				
				String token = st.nextToken();
				tokenNo ++;

				if (word.endsWith("_NN")){
					if (tokenNo <= 2) continue;
				}else if (word.endsWith("_VB")){
					if (tokenNo <= 1) continue;
				}
				
				int index = token.lastIndexOf('_');
				String token_word = token.substring(0, index);
				String token_posTag = token.substring(index + 1, token.length());
				String token_lemma = "";
				
				if (token_posTag.startsWith("NN")){
					
					if (token_posTag.startsWith("NNP"))
						token_lemma = token_word + "_NN";
					else{
						//lemmatizedWord = morpha.stem(word, "NN").word() + "_NN";
						token_lemma = morpha.lemma(token_word, "NN") + "_NN";
					}	
					
				}else if (token_posTag.startsWith("VB")){
					
					if (token_posTag.equals("VBN")){
						
						//lemmatizedWord = word + "_JJ";
						//lemmatizedWord = morpha.stem(word, "VB").word() + "_VB";
						token_lemma = morpha.lemma(token_word, "VB") + "_VB";
						
					}else{
						//lemmatizedWord = morpha.stem(word, "VB").word() + "_VB";
						token_lemma = morpha.lemma(token_word, "VB") + "_VB";
					}
					
				}else if (token_posTag.startsWith("JJ")){
					
					if (token_word.endsWith("ed")){
						
						//lemmatizedWord = morpha.stem(word, "VB").word() + "_VB";
						token_lemma = morpha.lemma(token_word, "VB") + "_VB";
						
						if (model.getFrequency(token_lemma) < adj_vb_discriminator_threshold && model.getFrequency(token_word + "_JJ") > adj_vb_discriminator_threshold)
							token_lemma = token_word + "_JJ";
						
					}else
						token_lemma = token_word + "_JJ";
						
				
				}else if (token_posTag.startsWith("RB")){
					
					if (token_word.endsWith("ly"))
						token_lemma = token_word + "_RB";
					else
						token_lemma = token_word + "_JJ";
					
				
				}else if (token_posTag.equals("FW") || token_posTag.equals("WP")){
					
					token_lemma = token_word + "_NN";
				}

				
				float sim = model.getSimilarity(word, token_lemma);
				
				//if (sim > 0.2)
				System.out.print(token_lemma + ":" + sim + " ");
			}

			System.out.println();
			
			System.out.println("Input the word.");
			word = input.readLine();
			
		}
		
		
		System.out.println("done!");
		
		
	}

}
