package edu.umbc.dbpedia.model;


public class STS_Example {

	public STS_Example() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		SimilarityModel model = new SimilarityModel();
		System.out.println(model.getPhraseSimilarity("I love playing", "He loves study"));
		System.out.println(model.getPhraseSimilarity("US", "United States"));
		System.out.println(model.getPhraseSimilarity("What does food people means", "go to supermarket"));
		System.out.println(model.getPhraseSimilarity("5 PM", "17:00"));
		System.out.println(model.getPhraseSimilarity("25", "twenty five"));		
		
		System.out.println(model.getPhraseSimilarity("I love playing", "she loves study", "2"));
		System.out.println(model.getPhraseSimilarity("US", "United States", "2"));
		System.out.println(model.getPhraseSimilarity("UMBC", "University of Maryland Baltimore County", "2"));
		System.out.println(model.getPhraseSimilarity("What does food people means", "go to supermarket", "2"));
		System.out.println(model.getPhraseSimilarity("largest", "bigest"));
		
	}

}
