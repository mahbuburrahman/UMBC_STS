package edu.umbc.dbpedia.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.TreeMap;

public class SimilarityTreeMapModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TreeMap<Integer, TreeMap<Integer, Float>> sim_matrix;
	public int sizeOfVocabulary;
	public String[] vocabulary;
	//public int frequency[];
    public static String dataPath;
    public static String dataPath1;
	

	public SimilarityTreeMapModel(String filename) {
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
		sim_matrix = new TreeMap<Integer, TreeMap<Integer, Float>>();
		
		try{
			File vocabularyFile = new File(filename + ".voc");
			BufferedReader vocabularyReader = new BufferedReader(new FileReader(vocabularyFile), 1000000);
			
			String rdline;
			rdline = vocabularyReader.readLine();
			sizeOfVocabulary = Integer.valueOf(rdline);
			vocabulary = new String[sizeOfVocabulary];
			//frequency = new int[sizeOfVocabulary];
			
			for (int i = 0; i < sizeOfVocabulary; i++){
				vocabulary[i] = vocabularyReader.readLine();
			}
	
			vocabularyReader.close();
			
		} catch (Exception e){
			System.out.println(e.getMessage());
			e.printStackTrace(System.out);
			System.exit(-1);
		}

		
	}
	
	
	public void loadInitialSimilarity(LSA_Model gutenberg_lsa, LSA_Model wiki_lsa){
		
		long count = 0;
		
		for (int i = 0; i < sizeOfVocabulary; i++){

			TreeMap<Integer, Float> innerMap = new TreeMap<Integer, Float>();
			sim_matrix.put(i, innerMap);
			
			for (int j = 0; j <= i; j++){
			
				float gutn_sim = gutenberg_lsa.getCosineSimilarity(i, j);
				float wiki_sim = wiki_lsa.getCosineSimilarity(i, j);
				
				if (wiki_sim > 0.02 || gutn_sim > 0.02){
					
					count ++;
					
					/*
					if (wiki_sim > 0)
						innerMap.put(j, wiki_sim);
					else
						innerMap.put(j, gutn_sim);
					*/
				}
			}
			
			if (i % 100 == 0)
				System.out.println(i + " rows have been processed.");
		}
		
		System.out.println("There are totally " + count + " non-zero similarity values");
	}

	public int index(String word){
		return Arrays.binarySearch(vocabulary, word);
	}

	public float getSimilarity(String word1, String word2){
		
		int index1 = index(word1);
		int index2 = index(word2);
		
		if (index1 < index2){
			int temp = index2;
			index2 = index1;
			index1 = temp;
		}

		TreeMap<Integer, Float> innerMap = sim_matrix.get(index1);
		
		if (innerMap == null)
			return 0;
		
		Float similarity = innerMap.get(index2);
		
		if (similarity == null)
			return 0;
		else
			return similarity;
	}

	public boolean setSimilarity(String word1, String word2, float sim){
		
		int index1 = index(word1);
		int index2 = index(word2);
		
		if (index1 < index2){
			int temp = index2;
			index2 = index1;
			index1 = temp;
		}

		TreeMap<Integer, Float> innerMap = sim_matrix.get(index1);
		
		if (innerMap == null)
			return false;
		
		Float similarity = innerMap.get(index2);
		
		if (similarity == null)
			return false;
		else{
			innerMap.put(index2, sim);
			return true;
		}
	}
	
	

	static public void writeModel(String modelname, SimilarityTreeMapModel model) throws IOException{

		FileOutputStream fileOut;
		ObjectOutputStream objOut;
		fileOut = new FileOutputStream(modelname + ".treemap_sim_model");
		objOut = new ObjectOutputStream(fileOut);
		objOut.writeObject(model);
		objOut.close();
		System.out.println("seriliazation done!");

	}

	
	static public SimilarityTreeMapModel readModel(String modelname) throws IOException, ClassNotFoundException{
        
		FileInputStream fileIn = new FileInputStream(modelname + ".treemap_sim_model");
        ObjectInputStream objIn = new ObjectInputStream(fileIn);
        return (SimilarityTreeMapModel) objIn.readObject();
	}

	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		LSA_Model gutn = new LSA_Model(dataPath+"/model/SVD/Gutenberg2010AllW5");
		LSA_Model wiki = new LSA_Model(dataPath+"/model/SVD/Wikipedia2006AllW5");
		
		SimilarityTreeMapModel model = new SimilarityTreeMapModel(dataPath+"/model/SVD/Wikipedia2006AllW5");
		
		model.loadInitialSimilarity(gutn, wiki);
		System.out.println("Finished loading!");
		
		SimilarityTreeMapModel.writeModel(dataPath+"/model/TreeMap/wiki5", model);
		System.out.println("Finished serialization!");
		
	}

}
