package edu.umbc.dbpedia.model;

public class QueueElement implements Comparable<QueueElement>{
	
	public String item;
	public double value;

	public QueueElement(String par_word, double par_value) {
		// TODO Auto-generated constructor stub
		item = par_word;
		value = par_value;
	}

	public String toString(){
		//return item + ": " + value;
		return item;
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public int compareTo(QueueElement o) {
		// TODO Auto-generated method stub
		return (int) Math.signum((this.value - o.value));
	}

}
