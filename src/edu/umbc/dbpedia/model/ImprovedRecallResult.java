package edu.umbc.dbpedia.model;

import java.util.ArrayList;

public class ImprovedRecallResult implements Comparable<ImprovedRecallResult> {

	double bestTotalFitness;
	int numberOfLinks;
	public static double Similar_Property_Threshold = 1.0;
	public static double Similar_Property_CondiProb_Threshold = 0.001;
	
	// hooked with nodesInOrder and linksInOrder arrays using the same index.
	public GeneralizingClassElement[] mappedClassInOrder;
	public ArrayList<SimilarPropertyElement>[] property_clusterInOrder;
	
	public ImprovedRecallResult(int numOfNodes, int numOfLinks) {
		// TODO Auto-generated constructor stub
		mappedClassInOrder = new GeneralizingClassElement[numOfNodes];
		property_clusterInOrder = new ArrayList[numOfLinks];
		numberOfLinks = numOfLinks;
		bestTotalFitness = 0;
	}

	public int compareTo(ImprovedRecallResult o) {
		// TODO Auto-generated method stub
		return (int) Math.signum((this.bestTotalFitness - o.bestTotalFitness));
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
