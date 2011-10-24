package com.adaptivehuffman;


public class Symbol implements Comparable<Symbol>{
	
	String letter;//Inner node's letter is null.
	int frequency;
	double low;
	double high;
	double probability;
	
	Symbol(String letter, int frequency){
		this.letter = letter;
		this.frequency = frequency;
	}

	Symbol(String letter, double p, double low, double high){
		this.letter = letter;
		this.probability = p;
		this.low = low;
		this.high = high;
	}
	
	
	@Override
	/**
	 * 这个是借口Comparable的方法，实现了它就可以给Symbol排序，或者说可以方便的调用
	 * Conllections.sort(List<T>)这个方法。
	 */
	public int compareTo(Symbol arg0) {
		// TODO Auto-generated method stub
		return letter.charAt(0) - arg0.letter.charAt(0);
	}
	
	public String toString(){
		return letter + " " + String.valueOf(frequency);
	}
}