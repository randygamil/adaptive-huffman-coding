package com.adaptivehuffman;

import java.util.ArrayList;
import java.util.Collections;


public class AdaptiveHuffman {
	
	private Node nytNode;//ʼ��ָ��NYT�ڵ��ָ��
	private Node root;//ʼ��ָ����ڵ��ָ��
	private char[] codeStr;//����Ĵ�����Ļ��������ַ���
	private ArrayList<Character> alreadyExist;//����Ѿ����ֹ����ַ�
	ArrayList<Node> nodeList;//�������ң����µ��ϵ�˳�����������нڵ㣬��Ҫ���ڲ��ұ�����Ľڵ�
	private String tempCode = "";//�����в����ַ�����ʱ�õ�һ��ȫ�ֱ���
	
	/**
	 * ��ʼ���������ַ�����NYT�ڵ��Լ�rootָ��
	 * @param codeStr ��������ַ���
	 */
	public AdaptiveHuffman(char[] codeStr){
		this.codeStr = codeStr;
		alreadyExist = new ArrayList<Character>();
		nodeList = new ArrayList<Node>();
		
		//Initialize the nyt Node.
		nytNode = new Node("NEW", 0);
		nytNode.parent = null;
		root = nytNode;
		nodeList.add(nytNode);
	}
	
	/**
	 * �����㷨�����ɣ���������Ŀ����㷨������
	 * @return �����ַ��ı���
	 */
	public ArrayList<String> encode(){
		ArrayList<String> result = new ArrayList<String>();
		result.add("0");//��ʼʱ����һ��0����NEW
		char temp = 0;
		for ( int i=0; i<codeStr.length; i++ ) {
			temp = codeStr[i];
			result.add(getCode(temp));
			updateTree(temp);
		}
		return result;
	}
	
	/**
	 * �����㷨�����ɣ���������ؿ������㷨������
	 * @return �����õ����ַ���
	 */
	public String decode(){
		String result = "";
		String symbol = null;
		char temp = 0;
		Node p = root;
		
		//The first symbol is of course NEW, so find it by ASCII
		symbol = getByAsc(0);
		result += symbol;
		updateTree( symbol.charAt(0) );
		p = root;
		
		for ( int i=9; i<codeStr.length; i++ ) {
			temp = codeStr[i];
			
			if ( temp=='0' ){
				p = p.left;
			}
			else 
				p = p.right;
			
			symbol = visit(p);
			//If reach a leaf
			if ( symbol!=null ){
				if ( symbol=="NEW" ){
					symbol = getByAsc(i);
					i+=8;
				}
				result+=symbol;
				updateTree( symbol.charAt(0) );
				p = root;
			}
		}
		
		return result;
	}
	
	/**
	 * �ǳ���Ҫ��һ������������ÿ�ζ�����һ���ַ�֮������������Ľṹ����������붼
	 * ��Ҫ�������������
	 * @param c �¶�����ַ�
	 */
	private void updateTree(char c){
		/*
		 * If the character is not yet existed, create two nodes. The one is for the new character,
		 * the other is for its father node.
		 */
		Node toBeAdd = null;
		if ( !isAlreadyExist(c) ){
			Node innerNode = new Node(null, 1);//��Ҫ���NYT node���½ڵ㣬�ַ�ȫ��Ϊnull
			Node newNode = new Node(String.valueOf(c), 1);//������ַ����½ڵ�
			
			//��һ��һ��ע��ָ������Ӳ�Ҫ����
			innerNode.left = nytNode;
			innerNode.right = newNode;
			innerNode.parent = nytNode.parent;
			if ( nytNode.parent!=null )//һ��ʼnytnodeΪ���ڵ㣬֮�󶼻����else
				nytNode.parent.left = innerNode;
			else {
				root = innerNode;
			}
			nytNode.parent = innerNode;
			newNode.parent = innerNode;

			//����������֤nodeList�����Ԫ��˳����ȷ
			nodeList.add(1, innerNode);
			nodeList.add(1, newNode);
			alreadyExist.add(c);
			toBeAdd = innerNode.parent;
		} else {
			toBeAdd = findNode(c);
		}
		
		//ѭ��ֱ�����еĸ��ڵ�Ȩֵ����+1
		while ( toBeAdd!=null ) {
			Node bigNode = findBigNode(toBeAdd.frequency);
			/**
			 * ������������ǲ��ܽ����ģ�
			 * 1.������Ľڵ����������
			 * 2.����һ���ڵ�Ϊ��һ���ڵ�ĸ��ڵ�
			 */
			if ( toBeAdd!=bigNode && toBeAdd.parent!=bigNode && bigNode.parent!=toBeAdd)
				swapNode(toBeAdd, bigNode );
			toBeAdd.frequency++;
			toBeAdd = toBeAdd.parent;
		}
	}

	private boolean isAlreadyExist(char temp) {
		// TODO Auto-generated method stub
		for ( int i=0; i<alreadyExist.size(); i++ ) {
			if ( temp==alreadyExist.get(i) )
				return true;
		}
		return false;
	}
	
	//Get the symbol using the next 8 bit as a ASCII code.
	private String getByAsc(int index) {
		// TODO Auto-generated method stub
		int asc = 0;
		int tempInt = 0;
		for ( int i=7; i>=0; i-- ) {
			tempInt = codeStr[++index] - 48;
			asc += tempInt * Math.pow(2, i);
		}
		
		char ret = (char) asc;
		return String.valueOf(ret);
	}

	/**
	 * �������е�һ���ڵ�
	 * @param p �ڵ��ָ��
	 * @return ����ڵ����ַ����򷵻��ַ������򷵻�null
	 */
	private String visit(Node p) {
		// TODO Auto-generated method stub
		if ( p.letter!=null ){
			//The symbol has been found.
			return p.letter;
		} 
		return null;
	}

	/**
	 * ����ʱ���õķ��������������ַ�C����һ�����롣������ַ��Ѿ����ֹ�����Ӷ������в��ң�
	 * ������ַ��ǵ�һ�γ��֣��򷵻ظ��ַ���ASCII��,�ټ���NEW�ı�����Ϊǰ׺��
	 * @param c the input character
	 */
	private String getCode(char c){
		tempCode = "";
		
		getCodeByTree(root, String.valueOf(c), "");
		String result = tempCode;
		if ( result=="" ) {
			getCodeByTree(root, "NEW", "");
			result = tempCode;
			result += toBinary( getAscii(c) );
		}
		return result;
	}
	
	
	//Find the existing node in the tree
	private Node findNode(char c) {
		// TODO Auto-generated method stub
		String temp = String.valueOf(c);
		Node tempNode = null;
		for ( int i=0; i<nodeList.size(); i++ ) {
			tempNode = nodeList.get(i);
			if ( tempNode.letter!=null && tempNode.letter.equals(temp) )
				return tempNode;
		}
		return null;
	}

	/**
	 * ���������ڵ㡣ע�ⲻ��ͼ���������ֵ����Ϊ������ʱ������ͬ�ڵ������һ�𽻻��ġ�
	 * @param n1 Ȩֵ����+1�Ľڵ�
	 * @param n2 ���б�����Ľڵ�
	 */
	private void swapNode(Node n1, Node n2) {
		// TODO Auto-generated method stub
		//note that n1<n2
		//Swap the position in the list firstly
		int i1 = nodeList.indexOf(n1);
		int i2 = nodeList.indexOf(n2);
		nodeList.remove(n1);
		nodeList.remove(n2);
		nodeList.add( i1, n2);
		nodeList.add( i2, n1);
		
		//Swap the position in the tree then
		Node p1 = n1.parent;
		Node p2 = n2.parent;
		//If the two nodes have different parent node.
		if ( p1!=p2 ) {
			if ( p1.left==n1 ) {
				p1.left = n2;
			} else {
				p1.right = n2;
			}

			if ( p2.left==n2 ) {
				p2.left = n1;
			} else {
				p2.right = n1;
			}
		} else {
			p1.left = n2;
			p1.right = n1;
		}
		n1.parent = p2;
		n2.parent = p1;
	
	}

	/**
	 * �ҵ�Ȩֵ��ͬ�Ľڵ��б�������Ǹ�����Ϊ�ڵ��Ѿ����մ����ң����µ��ϵ�˳������nodeList���ˣ�
	 * ����ֻ��Ӻ���ǰ�ҵ���һ��Ȩֵ��ͬ�Ľڵ㼴��
	 * @param frequency Ȩֵ
	 * @return �ҵ��Ľڵ�
	 */
	private Node findBigNode(int frequency) {
		// TODO Auto-generated method stub
		Node temp = null;
		for ( int i=nodeList.size()-1; i>=0; i--) {
			temp = nodeList.get(i);
			if ( temp.frequency==frequency )
				break;
		}
		return temp;
	}
	
	/**
	 * һ���ݹ��㷨���������ṹ����ĳ���ַ���Huffman���롣����㷨������������Ϊ�Ȳ���������һ��
	 * ȫ�ֱ���tempCode���ڴ����õ��㷨��
	 * @param node ��ʼ���ҵĽڵ�
	 * @param letter Ҫ���ɱ�����ַ�
	 * @param code ���ɵ��ַ�
	 */
	private void getCodeByTree(Node node, String letter, String code) {
		// TODO Auto-generated method stub
		//Reach a leaf
		if ( node.left==null && node.right==null ) {
			if ( node.letter!=null && node.letter.equals(letter) )
				tempCode = code;
		} else {
			if ( node.left!=null ) {
				getCodeByTree(node.left, letter, code + "0");
			}
			if ( node.right!=null ) {
				getCodeByTree(node.right, letter, code + "1");
			}
		}
	}
	
	/**
	 * ����һ���ַ���ʮ����ASCII�롣���������ҪΪ�˴�����׶��ԡ�
	 * @param c ��Ҫ����ASCII����ַ�
	 * @return c��ʮ����ASCII��
	 */
	public static int getAscii(char c){
		return (int)c;
	}
	
	/**
	 * ��һ��ʮ��������תΪһ��8λ�����Ʊ���
	 * @param decimal a integer to be converted
	 * @return string with 0,1
	 */
	public static String toBinary(int decimal){
		String result = "";
		for ( int i=0; i<8; i++ ) {
			if ( decimal%2==0 )
				result = "0" + result;
			else 
				result = "1" + result;
			decimal /= 2;
		}
		return result;
	}
	
	/**
	 * ����ѹ���ʡ��������ַ���ԭʼ�����Ϊ8λASCII�롣
	 * @param text
	 * @param code
	 * @return
	 */
	public static double calCompRate(String text, ArrayList<String> code){
		double compRate = 0;
		double preNum = 8*text.length();
		double postNum = 0;
		for ( String s: code) {
			postNum += s.length();
		}
		
		compRate = preNum/postNum;
		System.out.println("If simply using ASCII code, there are in total " + (int)preNum + " bits.");
		System.out.println("If using huffman coding, there are in total " + (int)postNum + " bits.");
		System.out.println("The compress rate is: " + compRate);
		return compRate;
	}
	
	public static void displayList(ArrayList<String>  l){
		for ( int i=0; i<l.size(); i++ ) {
			System.out.println( l.get(i) );
		}
	}
	
	private static String catStr(ArrayList<String> l) {
		// TODO Auto-generated method stub
		String result = "";
		for ( String s: l ){
			result += s;
		}
		return result;
	}

	/**
	 * ����������������Ϊ�˵õ�ͳ�����ݣ�������������Ҫ�õ��ġ�
	 */
	//Get statistics by the final tree.
	private void getStatistics() {
		// TODO Auto-generated method stub
		ArrayList<Symbol> symbolList = new ArrayList<Symbol>();
		preOrder(root, symbolList);
		
//		System.out.println("Symbol size is: " + symbolList.size());
		Collections.sort(symbolList);
		calRange(symbolList);
		FileHandler.writeSymbolToFile("data/symboltable.txt", symbolList);
	}

	/**
	 * ����������ҵ����е������ַ�����ҪΪ�õ�ͳ������
	 * @param node ��ʼ�ڵ�
	 * @param symbolList �ַ�����
	 */
	public static void preOrder(Node node, ArrayList<Symbol> symbolList){
		if( node!=null ){
			if ( node.letter!=null && (!node.letter.equals("NEW")) ) {
				Symbol tempSymbol = new Symbol(node.letter, node.frequency);
				symbolList.add(  tempSymbol );
			}
			preOrder(node.left, symbolList);
			preOrder(node.right, symbolList);
		}
	}
	
	private void calRange(ArrayList<Symbol> symbolList) {
		// TODO Auto-generated method stub
		int total = codeStr.length;
		double low = 0;
		
		for ( Symbol tempSymbol: symbolList ){
			tempSymbol.probability = tempSymbol.frequency / (double)total;
			tempSymbol.low = low;
			tempSymbol.high = low + tempSymbol.probability;
			low += tempSymbol.probability;
		}
		System.out.println("low="+low);//It should be 1.
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		/**
		 * ����ʱ����һ�δ���
		 */
		String text = FileHandler.readFile("data/I have a dream.txt", true);
		AdaptiveHuffman ah = new AdaptiveHuffman( text.toCharArray() );
		ArrayList<String> code = ah.encode();
		FileHandler.writeFile("data/ihaveadreaminHuff.txt", catStr(code), true);
		ah.getStatistics();
		calCompRate(text, code);

		
		/**
		 * ����ʱ����һ��
		 */
//		String code = FileHandler.readFile("data/ihaveadreaminHuff.txt", false);
//		AdaptiveHuffman ah = new AdaptiveHuffman( code.toCharArray() );
//		String result = ah.decode();
//		FileHandler.writeFile("data/IhaveadreamFromHuff.txt", result, false);
		
		
	}


}
