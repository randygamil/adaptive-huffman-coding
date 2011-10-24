package com.adaptivehuffman;

import java.util.ArrayList;
import java.util.Collections;


public class AdaptiveHuffman {
	
	private Node nytNode;//始终指向NYT节点的指针
	private Node root;//始终指向根节点的指针
	private char[] codeStr;//传入的待编码的或待解码的字符串
	private ArrayList<Character> alreadyExist;//存放已经出现过的字符
	ArrayList<Node> nodeList;//按从左到右，从下到上的顺序存放树中所有节点，主要用于查找编号最大的节点
	private String tempCode = "";//从树中查找字符编码时用的一个全局变量
	
	/**
	 * 初始化待编码字符串，NYT节点以及root指针
	 * @param codeStr 待编码的字符串
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
	 * 编码算法的主干，可以清楚的看出算法的流程
	 * @return 所有字符的编码
	 */
	public ArrayList<String> encode(){
		ArrayList<String> result = new ArrayList<String>();
		result.add("0");//初始时加入一个0代表NEW
		char temp = 0;
		for ( int i=0; i<codeStr.length; i++ ) {
			temp = codeStr[i];
			result.add(getCode(temp));
			updateTree(temp);
		}
		return result;
	}
	
	/**
	 * 解码算法的主干，可以清楚地看出来算法的流程
	 * @return 解码后得到的字符串
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
	 * 非常重要的一个方法，用于每次读入了一个字符之后更新整个树的结构。编码跟解码都
	 * 需要调用这个方法。
	 * @param c 新读入的字符
	 */
	private void updateTree(char c){
		/*
		 * If the character is not yet existed, create two nodes. The one is for the new character,
		 * the other is for its father node.
		 */
		Node toBeAdd = null;
		if ( !isAlreadyExist(c) ){
			Node innerNode = new Node(null, 1);//将要替代NYT node的新节点，字符全部为null
			Node newNode = new Node(String.valueOf(c), 1);//存放新字符的新节点
			
			//这一段一定注意指针的连接不要出错
			innerNode.left = nytNode;
			innerNode.right = newNode;
			innerNode.parent = nytNode.parent;
			if ( nytNode.parent!=null )//一开始nytnode为根节点，之后都会进入else
				nytNode.parent.left = innerNode;
			else {
				root = innerNode;
			}
			nytNode.parent = innerNode;
			newNode.parent = innerNode;

			//以下两步保证nodeList里面的元素顺序正确
			nodeList.add(1, innerNode);
			nodeList.add(1, newNode);
			alreadyExist.add(c);
			toBeAdd = innerNode.parent;
		} else {
			toBeAdd = findNode(c);
		}
		
		//循环直到所有的父节点权值都被+1
		while ( toBeAdd!=null ) {
			Node bigNode = findBigNode(toBeAdd.frequency);
			/**
			 * 以下两种情况是不能交换的：
			 * 1.编号最大的节点就是它自身
			 * 2.其中一个节点为另一个节点的父节点
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
	 * 访问树中的一个节点
	 * @param p 节点的指针
	 * @return 如果节点有字符，则返回字符，否则返回null
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
	 * 编码时调用的方法。根据输入字符C返回一个编码。如果该字符已经出现过，则从二叉树中查找；
	 * 如果该字符是第一次出现，则返回该字符的ASCII码,再加上NEW的编码作为前缀。
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
	 * 交换两个节点。注意不能图方便而交换值，因为交换的时候是连同节点的子树一起交换的。
	 * @param n1 权值即将+1的节点
	 * @param n2 块中编号最大的节点
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
	 * 找到权值相同的节点中编号最大的那个。因为节点已经按照从左到右，从下到上的顺序存放在nodeList中了，
	 * 所以只需从后往前找到第一个权值相同的节点即可
	 * @param frequency 权值
	 * @return 找到的节点
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
	 * 一个递归算法，根据树结构生成某个字符的Huffman编码。这个算法并不完美，因为迫不得已用了一个
	 * 全局变量tempCode。期待更好的算法。
	 * @param node 开始查找的节点
	 * @param letter 要生成编码的字符
	 * @param code 生成的字符
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
	 * 返回一个字符的十进制ASCII码。这个方法主要为了代码的易读性。
	 * @param c 需要生成ASCII码的字符
	 * @return c的十进制ASCII码
	 */
	public static int getAscii(char c){
		return (int)c;
	}
	
	/**
	 * 把一个十进制整数转为一个8位二进制编码
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
	 * 计算压缩率。设所有字符的原始编码均为8位ASCII码。
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
	 * 以下三个方法都是为了得到统计数据，在算术编码中要用到的。
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
	 * 用先序遍历找到树中的所有字符。主要为得到统计数据
	 * @param node 起始节点
	 * @param symbolList 字符容器
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
		 * 编码时用这一段代码
		 */
		String text = FileHandler.readFile("data/I have a dream.txt", true);
		AdaptiveHuffman ah = new AdaptiveHuffman( text.toCharArray() );
		ArrayList<String> code = ah.encode();
		FileHandler.writeFile("data/ihaveadreaminHuff.txt", catStr(code), true);
		ah.getStatistics();
		calCompRate(text, code);

		
		/**
		 * 解码时用这一段
		 */
//		String code = FileHandler.readFile("data/ihaveadreaminHuff.txt", false);
//		AdaptiveHuffman ah = new AdaptiveHuffman( code.toCharArray() );
//		String result = ah.decode();
//		FileHandler.writeFile("data/IhaveadreamFromHuff.txt", result, false);
		
		
	}


}
