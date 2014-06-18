/*
 * Copyright 2009 Author: Lushan Han at Ebiquity Lab, UMBC
 *  
 */
package edu.umbc.dbpedia.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.Vector;

public class LSA_Model {

	public double[][] U;
	public int rows;
	public int cols;
	public int sizeOfVocabulary;
	public String[] vocabulary;
	public int frequency[];
	public String modelName;
	public int FREQUENCY_LIMIT = 700; 
    public static String dataPath;
    public static String dataPath1;
    
	
	public LSA_Model(String filename){
		this(filename, 700);
        
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

	}
	
	
	public LSA_Model(String filename, int frequencyThreshold){
        
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

		
		try{
			FREQUENCY_LIMIT = frequencyThreshold;
			
			modelName = filename;
			/* Read vocabulary */
			File vocabularyFile = new File(filename + ".voc");
			BufferedReader vocabularyReader = new BufferedReader(new FileReader(vocabularyFile), 1000000);
			
			String rdline;
			rdline = vocabularyReader.readLine();
			sizeOfVocabulary = Integer.valueOf(rdline);
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
			

			File LSAModelFile = new File(filename + ".U");
			BufferedReader LSAModelReader = new BufferedReader(new FileReader(LSAModelFile), 4000000);
			
			rdline = LSAModelReader.readLine().trim();
			
			rows = Integer.valueOf(rdline.substring(0, rdline.indexOf(' ')));
			cols = Integer.valueOf(rdline.substring(rdline.indexOf(' ') + 1, rdline.length()));

			if (rows != sizeOfVocabulary){
				System.out.println("The vocabulary file is inconsistent with the LSA model file!");
				System.exit(-1);
			}

			U = new double[rows][cols];
			
			for (int i = 0; i < rows; i++){
				rdline = LSAModelReader.readLine();
				StringTokenizer st = new StringTokenizer(rdline, " ");
				
				int j = 0;
				while (st.hasMoreTokens()){
					U[i][j] = Double.valueOf(st.nextToken());
					j++;
				}
				
				if (j != cols) {
					System.out.println(i + "th row in the LSA model file only has " + j + " columns!");
					System.exit(-1);
				}
			}
			
			LSAModelReader.close();
			

		} catch (Exception e){
			System.out.println(e.getMessage());
			e.printStackTrace(System.out);
			System.exit(-1);
		}

	}
	
	public FloatElement[] getSortedWordsByCosineSimWithSamePOS(String word){
		
		int index1 = index(word);
		ArrayList<FloatElement> correlateWords = new ArrayList<FloatElement>();
		
		for (int i=0; i<sizeOfVocabulary; i++){
			
			String pos = word.substring(word.length() - 2, word.length());
			if (!vocabulary[i].endsWith(pos))
				continue;
			
			if (i == index1)
				continue;
				
			float cosineSim = 0;
			int frequency = 0;
			
			try {
				frequency =  getFrequency(i);
				
				if (frequency < FREQUENCY_LIMIT) 
					continue;

				cosineSim = getCosineSimilarity(index1, i);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				continue;
			}
			
			FloatElement element = new FloatElement(vocabulary[i], cosineSim, 0, frequency);
			correlateWords.add(element);
		}
		
		FloatElement[] result = correlateWords.toArray(new FloatElement[correlateWords.size()]);
		Arrays.sort(result);
		
		return result;
		
	}

	
	public FloatElement[] getSortedWordsByCosineSim(String word){
		
		int index1 = index(word);
		ArrayList<FloatElement> correlateWords = new ArrayList<FloatElement>();
		
		for (int i=0; i<sizeOfVocabulary; i++){
			
			if (i == index1)
				continue;
				
			float cosineSim = 0;
			int frequency = 0;
			
			try {
				frequency =  getFrequency(i);
				
				if (frequency < FREQUENCY_LIMIT) 
					continue;

				cosineSim = getCosineSimilarity(index1, i);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				continue;
			}
			
			FloatElement element = new FloatElement(vocabulary[i], cosineSim, 0, frequency);
			correlateWords.add(element);
		}
		
		FloatElement[] result = correlateWords.toArray(new FloatElement[correlateWords.size()]);
		Arrays.sort(result);
		
		return result;
		
	}

	
	public float getCosineSimilarity(String word1, String word2) {
		
		int index1 = index(word1);
		int index2 = index(word2);
		return getCosineSimilarity(index1, index2);
	}

	
	public float getWordSimilarity(String word1, String word2) {
		
		Vector<Integer> wordSet1 = new Vector<Integer>();
		Vector<Integer> wordSet2 = new Vector<Integer>();
		
		int position1 = (index(word1 + "_") + 1) * -1;
		int position2 = (index(word2 + "_") + 1) * -1;
		
		while (vocabulary[position1].startsWith(word1 + "_")){
			
			if (getFrequency(position1) > 500)
				wordSet1.add(position1);
			
			position1 ++;
		}
		
		while (vocabulary[position2].startsWith(word2 + "_")){
			
			if (getFrequency(position2) > 500)
				wordSet2.add(position2);
			
			position2 ++;
		}
		
		float maxValue = 0;
		
		for (Integer taggedWord1: wordSet1){
			for (Integer taggedWord2: wordSet2){
				float sim = getCosineSimilarity(taggedWord1, taggedWord2);
				if (sim > maxValue)
					maxValue = sim;
			}
		}
		
		return maxValue;
	}
	
	
	public int getWordFrequency(String word){
		
		int freq = 0;
		
		int position = (index(word + "_") + 1) * -1;
		
		while (vocabulary[position].startsWith(word + "_")){
			
			freq += frequency[position];
			position ++;
		}
		
		return freq;
		
	}
	
	public double[] unionTwoVectors(String word1, String word2){
		
		int index1 = index(word1);
		int index2 = index(word2);

		double[] vector1 = U[index1];
		double[] vector2 = U[index2];

		double square_sum_1 = 0;
		double square_sum_2 = 0;

		for (int i=0; i < cols; i++){
			square_sum_1 += vector1[i] * vector1[i];
			square_sum_2 += vector2[i] * vector2[i];
		}

		double norm1 = Math.sqrt(square_sum_1);
		double norm2 = Math.sqrt(square_sum_2);

		double[] vectorFromUnion = new double[vector1.length];
		
		for (int i=0; i < vectorFromUnion.length; i++){
			vectorFromUnion[i] = vector1[i] / norm1 + vector2[i] / norm2;
		}
		
		return vectorFromUnion;
		
	}
	
	
	public double[] productTwoVectors(String word1, String word2){
		
		int index1 = index(word1);
		int index2 = index(word2);

		double[] vector1 = U[index1];
		double[] vector2 = U[index2];

		double[] vectorFromProduct = new double[vector1.length];
		
		for (int i=0; i < vectorFromProduct.length; i++){
			vectorFromProduct[i] = (vector1[i] * vector2[i]);
		}
		
		return vectorFromProduct;
		
	}
	
	public float getCosineSimilarity(double[] vector1, double[] vector2) {
		
		double square_sum_1 = 0;
		double square_sum_2 = 0;

		for (int i=0; i < cols; i++){
			square_sum_1 += vector1[i] * vector1[i];
			square_sum_2 += vector2[i] * vector2[i];
		}
		
		double norm1 = Math.sqrt(square_sum_1);
		double norm2 = Math.sqrt(square_sum_2);
		
		if (norm1 == 0 || norm2 == 0)
			return 0;
		
		double dot_product = 0;

		for (int i=0; i < cols; i++){
			dot_product += vector1[i] * vector2[i];
		}
		
		return (float) (dot_product / (norm1 * norm2));

	}
	
	public float getCosineSimilarity(int index1, int index2) {
		
		if (index1 < 0 || index2 < 0)
			return 0;
		
		double[] vector1 = U[index1];
		double[] vector2 = U[index2];
		
		double square_sum_1 = 0;
		double square_sum_2 = 0;

		for (int i=0; i < cols; i++){
			square_sum_1 += vector1[i] * vector1[i];
			square_sum_2 += vector2[i] * vector2[i];
		}
		
		double norm1 = Math.sqrt(square_sum_1);
		double norm2 = Math.sqrt(square_sum_2);
		
		if (norm1 == 0 || norm2 == 0)
			return 0;
		
		double dot_product = 0;

		for (int i=0; i < cols; i++){
			dot_product += vector1[i] * vector2[i];
		}
		
		return (float) (dot_product / (norm1 * norm2));

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
	
	public double[] getVector(String word){
		int index1 = index(word);
		return U[index1]; 
	}
	
	
	public int index(String word){
		return Arrays.binarySearch(vocabulary, word);
	}
	
	public String printPCW(Object[] sortedWords, int size){
		
		StringBuffer temp = new StringBuffer();
		
		for (int i=0; i<size; i++){
			temp.append(sortedWords[i] + ", ");
		}
		
		System.out.println(temp);
		
		return temp.toString();

	}
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		/*
		CoOccurModelByArrays test = new CoOccurModelByArrays(new String[]{"ant", "bat", "cat", "dog"}, "test");
		test.addCoOccurrence("cat", "dog");
		test.addCoOccurrence("ant", "dog");
		
		test.saveModel();
		*/

		//LSA_Model test1 = new LSA_Model(dataPath+"/model/SVD/gutn_ukwac_part_AllW2");
		//LSA_Model test2 = new LSA_Model(dataPath+"/model/SVD/ukwac2012AllW2");
		//LSA_Model test3 = new LSA_Model(dataPath+"/model/SVD/Gutenberg2010AllW2");

		
		LSA_Model test1 = new LSA_Model(dataPath+"/model/SVD/webbase2012AllW2");
		LSA_Model test2 = new LSA_Model(dataPath+"/model/SVD/Gutenberg2010AllW2");
		//LSA_Model test2 = new LSA_Model(dataPath+"/model/SVD/webbase2012AcW2");
		LSA_Model test3 = new LSA_Model(dataPath+"/model/SVD/Gigawords2009AllW2");
		//LSA_Model test4 = new LSA_Model(dataPath+"/model/SVD/giga_web_AllW5");
		
		//LSA_Model test1 = new LSA_Model(dataPath+"/model/SVD/Gutenberg2010AllW5");
		//LSA_Model test1 = new LSA_Model(dataPath+"/model/SVD/gutn_ukwac_AllW5");
		//LSA_Model test2 = new LSA_Model(dataPath+"/model/SVD/Gutenberg2010AllW5");
		//LSA_Model test3 = new LSA_Model(dataPath+"/model/SVD/ukwac2012AllW5");
		//LSA_Model test4 = new LSA_Model(dataPath+"/model/SVD/Wikipedia2006AllW5");
		
		//LSA_Model test5 = new LSA_Model(dataPath+"/model/SVD/gutn_ukwac_AllW2");
		//LSA_Model test6 = new LSA_Model(dataPath+"/model/SVD/Gutenberg2010AllW2");
		//LSA_Model test7 = new LSA_Model(dataPath+"/model/SVD/ukwac2012AllW2");
		//LSA_Model test8 = new LSA_Model(dataPath+"/model/SVD/Wikipedia2006AllW2");
		//LSA_Model test3 = new LSA_Model(dataPath+"/model/SVD/gutn_ukwac_part_AllW2");

		//LSA_Model test2 = new LSA_Model(dataPath+"/model/SVD/gutn_ukwac_part_AllW2_400");
		//LSA_Model test3 = new LSA_Model(dataPath+"/model/SVD/Gutenberg2010AllW2");
		//LSA_Model test4 = new LSA_Model(dataPath+"/model/SVD/ukwac2012AllW2");
		
		//LSA_Model test2 = new LSA_Model(dataPath+"/model/SVD/Wikipedia2006AllW5");
		//LSA_Model test3 = new LSA_Model(dataPath+"/model/SVD/20120611/Guten2010_Wiki2006_AllW5");
		//LSA_Model test3 = new LSA_Model(dataPath+"/model/SVD/20120611/Wikipedia2006AllW3");
		//LSA_Model test3 = new LSA_Model(dataPath+"/model/SVD/Wikipedia2006AllW3");

		//CoOccurModelByArrays test2 = new CoOccurModelByArrays("3esl");
		
		/*
		CoOccurModelByArrays test = new CoOccurModelByArrays("simple");
		System.out.println(test.getCoOccurrence("ant", "cat"));
		test.addCoOccurrence("ant", "cat");
		test.addCoOccurrence("ANT", "cat");
		test.addCoOccurrence("DOG", "CAT");
		test.addFrequency("ant");
		System.out.println(test.getCoOccurrence("dog", "cat"));
		System.out.println("frequency is " + test.getFrequency("ant"));
		test.saveModel();
		*/
		System.out.println("The cosine similarity between eat and consume is " + test1.getCosineSimilarity("supply_VB", "consume_VB"));
		System.out.println("The cosine similarity between eat and consume is " + test2.getCosineSimilarity("supply_VB", "consume_VB"));
		System.out.println("The cosine similarity between eat and consume is " + test3.getCosineSimilarity("supply_VB", "consume_VB"));
		
		System.out.println("done!");
	}

}
