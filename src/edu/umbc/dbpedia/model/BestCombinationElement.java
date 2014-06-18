package edu.umbc.dbpedia.model;

public class BestCombinationElement implements Comparable<BestCombinationElement> {

	double bestTotalFitness;
	int numberOfLinks;
	
	public int[] bestNodeChoiceInOrder;
	public int[] bestLinkChoiceInOrder;
	public boolean[] bestLinkChoiceReverseInOrder;
	
	public BestCombinationElement(double fitness, int[] nodeChoice, int[] linkChoice, boolean[] linkChoiceReverse) {
		// TODO Auto-generated constructor stub
		bestTotalFitness = fitness;
		bestNodeChoiceInOrder = nodeChoice;
		bestLinkChoiceInOrder = linkChoice;
		bestLinkChoiceReverseInOrder = linkChoiceReverse;
		numberOfLinks = bestLinkChoiceInOrder.length;
	}

	public int compareTo(BestCombinationElement o) {
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
