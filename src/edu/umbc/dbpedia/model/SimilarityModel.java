package edu.umbc.dbpedia.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Vector;

import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.TypedDependency;
import edu.umbc.dbpedia.util.ComplexPredicate;
import edu.umbc.dbpedia.util.Contradiction;
import edu.umbc.dbpedia.util.DependencyCheck;
import edu.umbc.dbpedia.util.OrderedWord;

public class SimilarityModel {

	MaxentTagger tagger;
	Morphology morph;
	public SimilarityArrayModel conceptSimModel1;
	public SimilarityArrayModel relationSimModel1;
	public SimilarityArrayModel conceptSimModel2;
	public SimilarityArrayModel relationSimModel2;
	public String[] ANTONYM_SET;
	DependencyCheck d_check;
    public static String dataPath;
    public static String dataPath1;
    
    
	
	public SimilarityModel() throws Exception {
        
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

        
		//String modelLocation = dataPath+"/model/stanford/left3words-distsim-wsj-0-18.tagger";
		//String modelLocation = dataPath+"/model/stanford/pos-tagger/english-left3words/english-left3words-distsim.tagger";
		//String modelLocation = dataPath+"/model/stanford/pos-tagger/wsj-left3words/wsj-0-18-left3words-distsim.tagger";
    	//String modelLocation = dataPath+"/model/stanford/pos-tagger/english-bidirectional/english-bidirectional-distsim.tagger";
    	String modelLocation = dataPath+"/model/stanford/pos-tagger/wsj-bidirectional/wsj-0-18-bidirectional-distsim.tagger";

		System.out.println("Reading model from file=" + modelLocation);
        tagger = new MaxentTagger(modelLocation);
        morph = new Morphology();
        
        conceptSimModel1 = null; // SimilarityArrayModel.readModel(dataPath+"/model/BigArray/webbase2012AllW2_S2");
		//relationSimModel1 = SimilarityArrayModel.readModel(dataPath+"/model/BigArray/ceilling_cut/webbase2012AllW5_S3");
        relationSimModel1 = SimilarityArrayModel.readModel(dataPath+"/model/BigArray/ceilling_cut/webbase2012AllPlusW5_S3");
		//relationSimModel1 = SimilarityArrayModel.readModel(dataPath+"/model/BigArray/ceilling_cut/webbase2012Allv2W5_S3");

		conceptSimModel2 = null; //SimilarityArrayModel.readModel(dataPath+"/model/BigArray/Gigawords2009AllW2_S2");
		relationSimModel2 = null; //SimilarityArrayModel.readModel(dataPath+"/model/BigArray/Gigawords2009AllW5_S3");
		
		//d_check = new DependencyCheck();
		
		System.out.println("Reading antonym set ... ");
		//File antonymFile = new File(dataPath1+"/testbed/STS/data/AntonymsLexicon_old");
		//File antonymFile = new File(dataPath1+"/testbed/STS/data/WNet-antonyms-clean-pos.txt");
		//File antonymFile = new File(dataPath1+"/testbed/STS/data/emptyLexicon");
		File antonymFile = new File(dataPath1+"/testbed/STS/data/AntonymsLexicon");

		

		
		BufferedReader antonymReader = new BufferedReader(new FileReader(antonymFile), 1000000);
		
		String rdline;
		rdline = antonymReader.readLine();
		int sizeOfAntonyms = Integer.valueOf(rdline);
		
		ANTONYM_SET = new String[sizeOfAntonyms];
		
		for (int i = 0; i < sizeOfAntonyms; i++){
			ANTONYM_SET[i] = antonymReader.readLine();
		}

		antonymReader.close();
		Arrays.sort(ANTONYM_SET);



	}
	
	

	public float getSimilarity(String phrase1, String phrase2, String choice, boolean includeLargeRB, boolean extendedStopwords, boolean supportAcronym, StringBuffer missedWordsBuffer){
		
		float sim = 0;
		String missedWords = "";
		
		SimilarityArrayModel conceptSimModel;
		SimilarityArrayModel relationSimModel;
		
		String corpus = "webbase";
		
		if (corpus.equals("webbase")){
			conceptSimModel = conceptSimModel1;
			relationSimModel = relationSimModel1;
		}else{
			conceptSimModel = conceptSimModel2;
			relationSimModel = relationSimModel2;
		}
		
		
		//if (phrase1.length() > 80)
		//	phrase1 = phrase1.replace("-", " ");
		
		//if (phrase2.length() > 80)
		//	phrase2 = phrase2.replace("-", " ");
		

    	ComplexPredicate firstCompound = new ComplexPredicate(phrase1, tagger, morph, relationSimModel, includeLargeRB, extendedStopwords, supportAcronym);
    	ComplexPredicate secondCompound = new ComplexPredicate(phrase2, tagger, morph, relationSimModel, includeLargeRB, extendedStopwords, supportAcronym);
    	
		sim = (float) ComplexPredicate.getSTS_Similarity(firstCompound, secondCompound, relationSimModel, ANTONYM_SET);
		//sim = (float) ComplexPredicate.getSimilarity(firstCompound, secondCompound, relationSimModel);
		
		/*
		System.out.println("Mapping from first phrase to second phrase:");
		for (int i=0; i < firstCompound.componentWords.length; i++){
			
			if (firstCompound.componentWords[i].mappingNo > -1)
				System.out.println(firstCompound.componentWords[i] + " --> " + secondCompound.componentWords[firstCompound.componentWords[i].mappingNo]);
			
		}

		System.out.println("Mapping from second phrase to first phrase:");
		for (int i=0; i < secondCompound.componentWords.length; i++){
			
			if (secondCompound.componentWords[i].mappingNo > -1)
				System.out.println(secondCompound.componentWords[i] + " --> " + firstCompound.componentWords[secondCompound.componentWords[i].mappingNo]);
			
		}
		*/
		
		if (d_check != null){
			
			Collection<TypedDependency> tdl1 = d_check.getDependencyCollection(phrase1);
			Collection<TypedDependency> tdl2 = d_check.getDependencyCollection(phrase2);
			
	 	    HashSet<Contradiction> contradictionSet1 = d_check.getContradictions(tdl1, tdl2, firstCompound, secondCompound);
	 	    HashSet<Contradiction> contradictionSet2 = d_check.getContradictions(tdl2, tdl1, secondCompound, firstCompound);
	 	    
	 	    if (contradictionSet1.size() + contradictionSet2.size() > 0){
	 	    	
	 	    	System.out.println(sim + " | " + contradictionSet1 + " | " + contradictionSet2);
	 	    }
	 	    	
	 	    //System.out.println("The number of contraditions is " + contradictionSet1.size());
			//System.out.println(contradictionSet1);
			
		    //System.out.println("The number of contraditions is " + contradictionSet2.size());
			//System.out.println(contradictionSet2);
		}


		
		for (OrderedWord orderedWord: firstCompound.componentWords){
			
			String posWord = orderedWord.tagged_word;
			if (posWord.endsWith("NN") || posWord.endsWith("VB") || posWord.endsWith("JJ") || posWord.endsWith("RB")){
				if (relationSimModel.index(posWord) < 0)
					missedWords += " " + posWord + ",";
			}
		}
		
		for (OrderedWord orderedWord: secondCompound.componentWords){
			
			String posWord = orderedWord.tagged_word;
			if (posWord.endsWith("NN") || posWord.endsWith("VB") || posWord.endsWith("JJ") || posWord.endsWith("RB")){
				if (relationSimModel.index(posWord) < 0)
					missedWords += " " + posWord + ",";
			}
		}
		
		
		if (missedWords.endsWith(","))
			missedWords = missedWords.substring(0, missedWords.length() - 1);
		
		missedWordsBuffer.setLength(0);
		missedWordsBuffer.append(missedWords);
		
		return sim;
		
	}

	
	public String getTopNSimilarWords(String posWord, String N_value, String choice, String corpus) throws Exception{
		
		String result;
		
		SimilarityArrayModel conceptSimModel;
		SimilarityArrayModel relationSimModel;
		
		if (corpus.equals("webbase")){
			conceptSimModel = conceptSimModel1;
			relationSimModel = relationSimModel1;
		}else{
			conceptSimModel = conceptSimModel2;
			relationSimModel = relationSimModel2;
		}

		if (choice.equals("concept")){
			result = conceptSimModel.printPCW(conceptSimModel.getSortedWordsBySimilarity(posWord), Integer.valueOf(N_value));
		}else{
			result = relationSimModel.printPCW(relationSimModel.getSortedWordsBySimilarity(posWord), Integer.valueOf(N_value));
		}
		
		
		return result;
		
	}

	public float getPhraseSimilarity(String phrase1, String phrase2){
		
		return getPhraseSimilarity(phrase1, phrase2, "0");
	}

	
	public float getPhraseSimilarity(String phrase1, String phrase2, String type){
		
		float maxSim = 0;
		float sim = 0;
		StringBuffer missedWords = new StringBuffer();
		
		if (type.equals("0")){
			sim = getSimilarity(phrase1, phrase2, "relation", false, true, false, missedWords);
			
			if (sim > maxSim)
				maxSim = sim;
			
		}else if  (type.equals("1")){
			sim = getSimilarity(phrase1, phrase2, "relation", false, true, false, missedWords);

			if (sim > maxSim)
				maxSim = sim;

			sim = getSimilarity(phrase1, phrase2, "relation", false, true, true, missedWords);

			if (sim > maxSim)
				maxSim = sim;
		
		}else if  (type.equals("2")){
			sim = getSimilarity(phrase1, phrase2, "relation", false, true, false, missedWords);

			if (sim > maxSim)
				maxSim = sim;

			sim = getSimilarity(phrase1, phrase2, "relation", false, true, true, missedWords);

			if (sim > maxSim)
				maxSim = sim;
		
			sim = getSimilarity(phrase1, phrase2, "relation", false, false, false, missedWords);

			if (sim > maxSim)
				maxSim = sim;

			sim = getSimilarity(phrase1, phrase2, "relation", false, false, true, missedWords);

			if (sim > maxSim)
				maxSim = sim;
		}
		
		return maxSim;
	}
	
	public void computePhraseSimilarityFromFileInput(String inputfile, String outputfile) throws IOException{
		
		BufferedReader textReader = new BufferedReader(new FileReader(inputfile));
		PrintWriter out = new PrintWriter(new FileWriter(outputfile));

		
		String curLine;
		int lineNo = 0;
		
		while ((curLine = textReader.readLine()) != null){
			
			int index = curLine.indexOf("	");
			lineNo ++;
			
			String phrase1 = curLine.substring(0, index);
			String phrase2 = curLine.substring(index + 1, curLine.length());
			
			float maxSim = 0;
			float sim = 0;
			StringBuffer missedWords = new StringBuffer();
			

			//sim = getSimilarity(phrase1, phrase2, "relation", true, false, false, missedWords);
			
			if (sim > maxSim)
				maxSim = sim;
			
			//sim = getSimilarity(phrase1, phrase2, "relation", false, false, false, missedWords);
			
			if (sim > maxSim)
				maxSim = sim;

			//sim = getSimilarity(phrase1, phrase2, "relation", true, true, false, missedWords);

			if (sim > maxSim)
				maxSim = sim;

			sim = getSimilarity(phrase1, phrase2, "relation", false, true, false, missedWords);

			if (sim > maxSim)
				maxSim = sim;

			//sim = getSimilarity(phrase1, phrase2, "relation", true, false, true, missedWords);
			
			if (sim > maxSim)
				maxSim = sim;
			
			//sim = getSimilarity(phrase1, phrase2, "relation", false, false, true, missedWords);

			if (sim > maxSim)
				maxSim = sim;
			
			
			//sim = getSimilarity(phrase1, phrase2, "relation", true, true, true, missedWords);

			if (sim > maxSim)
				maxSim = sim;

			//sim = getSimilarity(phrase1, phrase2, "relation", false, true, true, missedWords);

			if (sim > maxSim)
				maxSim = sim;
			


			//out.println(maxSim + "\t" + missedWords);
			out.println(maxSim);
			if (lineNo % 1000 == 0)
				System.out.println(lineNo);
				
		}

		out.close();
		
	}
	
	
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		SimilarityModel model = new SimilarityModel();
		
    	//BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
    	//String inline;

    	//System.out.println("Please enter to start");
    	//inline = input.readLine();
    	
    	//model.computePhraseSimilarityFromFileInput(dataPath1+"/testbed/STS/STS2012-train/STS.input.SMTeuroparl.txt", dataPath1+"/testbed/STS/output/abhay/STS.output.train.SMTeuroparl.txt");
    	//model.computePhraseSimilarityFromFileInput(dataPath1+"/testbed/STS/STS2012-test/STS.input.SMTeuroparl.txt", dataPath1+"/testbed/STS/output/abhay/STS.output.test.SMTeuroparl.txt");

    	//model.computePhraseSimilarityFromFileInput(dataPath1+"/testbed/STS/STS2012-train/STS.input.MSRpar.txt", dataPath1+"/testbed/STS/output/abhay/STS.output.train.MSRpar.txt");
    	//model.computePhraseSimilarityFromFileInput(dataPath1+"/testbed/STS/STS2012-test/STS.input.MSRpar.txt", dataPath1+"/testbed/STS/output/abhay/STS.output.test.MSRpar.txt");

    	//model.computePhraseSimilarityFromFileInput(dataPath1+"/testbed/STS/STS2012-train/STS.input.MSRvid.txt", dataPath1+"/testbed/STS/output/abhay/STS.output.train.MSRvid.txt");
    	//model.computePhraseSimilarityFromFileInput(dataPath1+"/testbed/STS/STS2012-test/STS.input.MSRvid.txt", dataPath1+"/testbed/STS/output/abhay/STS.output.test.MSRvid.txt");

    	//model.computePhraseSimilarityFromFileInput(dataPath1+"/testbed/STS/STS2012-test/STS.input.surprise.OnWN.txt", dataPath1+"/testbed/STS/output/abhay/STS.output.test.surprise.OnWN.txt");
    	//model.computePhraseSimilarityFromFileInput(dataPath1+"/testbed/STS/STS2012-test/STS.input.surprise.SMTnews.txt", dataPath1+"/testbed/STS/output/abhay/STS.output.test.surprise.SMTnews.txt");

		String batch;
		
		if (args.length > 0){
			batch = args[0];
		}else{
			batch = "10025";
		}

		/*
    	model.computePhraseSimilarityFromFileInput(dataPath1+"/testbed/STS/STS2012-train/STS.input.SMTeuroparl.txt", dataPath1+"/testbed/STS/output/STS.output.train.SMTeuroparl" + batch + ".txt");
    	model.computePhraseSimilarityFromFileInput(dataPath1+"/testbed/STS/STS2012-test/STS.input.SMTeuroparl.txt", dataPath1+"/testbed/STS/output/STS.output.test.SMTeuroparl" + batch + ".txt");
    	System.out.println("SMTeuroparl is done.");
    	*/
    	
    	model.computePhraseSimilarityFromFileInput(dataPath1+"/testbed/STS/STS2012-train/STS.input.MSRpar.txt", dataPath1+"/testbed/STS/output/STS.output.train.MSRpar" + batch + ".txt");
    	model.computePhraseSimilarityFromFileInput(dataPath1+"/testbed/STS/STS2012-test/STS.input.MSRpar.txt", dataPath1+"/testbed/STS/output/STS.output.test.MSRpar" + batch + ".txt");
    	System.out.println("MSRpar is done.");

    	model.computePhraseSimilarityFromFileInput(dataPath1+"/testbed/STS/STS2012-train/STS.input.MSRvid.txt", dataPath1+"/testbed/STS/output/STS.output.train.MSRvid" + batch + ".txt");
    	model.computePhraseSimilarityFromFileInput(dataPath1+"/testbed/STS/STS2012-test/STS.input.MSRvid.txt", dataPath1+"/testbed/STS/output/STS.output.test.MSRvid" + batch + ".txt");
    	System.out.println("MSRvid is done.");

    	model.computePhraseSimilarityFromFileInput(dataPath1+"/testbed/STS/STS2012-test/STS.input.surprise.OnWN.txt", dataPath1+"/testbed/STS/output/STS.output.test.surprise.OnWN" + batch + ".txt");
    	//model.computePhraseSimilarityFromFileInput(dataPath1+"/testbed/STS/STS2012-test/STS.input.surprise.SMTnews.txt", dataPath1+"/testbed/STS/output/STS.output.test.surprise.SMTnews" + batch + ".txt");
    	System.out.println("OnWN is done.");
    	
    	
    	model.computePhraseSimilarityFromFileInput(dataPath1+"/testbed/STS/test-core/STS.input.SMT.txt", dataPath1+"/testbed/STS/test-core/output/STS.output.SMT" + batch + ".txt");
    	System.out.println("SMT is done.");

    	model.computePhraseSimilarityFromFileInput(dataPath1+"/testbed/STS/test-core/STS.input.OnWN.txt", dataPath1+"/testbed/STS/test-core/output/STS.output.OnWN" + batch + ".txt");
    	System.out.println("OnWN is done.");

    	model.computePhraseSimilarityFromFileInput(dataPath1+"/testbed/STS/test-core/STS.input.headlines.txt", dataPath1+"/testbed/STS/test-core/output/STS.output.headlines" + batch + ".txt");
    	System.out.println("headlines is done.");

    	model.computePhraseSimilarityFromFileInput(dataPath1+"/testbed/STS/test-core/STS.input.FNWN.txt", dataPath1+"/testbed/STS/test-core/output/STS.output.FNWN" + batch + ".txt");
    	System.out.println("FNWN is done.");
    	
    	
    	
		/*
		String inputfile;
		String outputfile;
		
		if (args.length == 2){
			inputfile = args[0];
			outputfile = args[1];

	    	model.computePhraseSimilarityFromFileInput(inputfile, outputfile);
	    	System.out.println("Congratulations! work is done.");

		}else{
			System.out.println("Please give and only give the input file and output file!");
		}
		*/
		
		
		//model.computePhraseSimilarityFromFileInput(dataPath1+"/testbed/STS/JHU/allPairs.txt", dataPath1+"/testbed/STS/runtime/output/allPairs.out");

	}

}
