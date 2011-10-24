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
	 * ����ǽ��Comparable�ķ�����ʵ�������Ϳ��Ը�Symbol���򣬻���˵���Է���ĵ���
	 * Conllections.sort(List<T>)���������
	 */
	public int compareTo(Symbol arg0) {
		// TODO Auto-generated method stub
		return letter.charAt(0) - arg0.letter.charAt(0);
	}
	
	public String toString(){
		return letter + " " + String.valueOf(frequency);
	}
}