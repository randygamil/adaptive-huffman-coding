package com.adaptivehuffman;

import java.util.ArrayList;
import java.util.Collections;


public class AdaptiveHuffman {
	
	/**
	 * A pointer that constantly points the NYT node
	 */
	private Node nytNode;
	
	/**
	 * A pointer that constantly points the root node
	 */
	private Node root;
	
	/**
	 * the inputting code string that needs to incode or decode
	 */
	private char[] codeStr;
	
	/**
	 * stores characters that exist before
	 */
	private ArrayList<Character> alreadyExist;
	
	/**
	 * Store all the node in the tree from left to right, from bottom to top.
	 * Mainly used for find the biggest node in a block
	 */
	ArrayList<Node> nodeList;
	
	/**
	 * A global variance used for generating code by tree
	 */
	private String tempCode = "";
	
	/**
	 * Initialize input string, nyt node and root pointer.
	 * @param codeStr the input string
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
	 * The trunk method of encoding. 
	 * @return all codes of symbols
	 */
	public ArrayList<String> encode(){
		ArrayList<String> result = new ArrayList<String>();
		result.add("0");//Represent NEW
		char temp = 0;
		for ( int i=0; i<codeStr.length; i++ ) {
			temp = codeStr[i];
			result.add(getCode(temp));
			updateTree(temp);
		}
		return result;
	}
	
	/**
	 * The trunk method of decoding. 
	 * @return code string aftering decoding.
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
	 * It's a very important method that is used for updating the structure of tree after read
	 * each symbol. Called both during encoding and decoding.
	 * @param c the next character
	 */
	private void updateTree(char c){
		/*
		 * If the character is not yet existed, create two nodes. The one is for the new character,
		 * the other is for its father node.
		 */
		Node toBeAdd = null;
		if ( !isAlreadyExist(c) ){
			Node innerNode = new Node(null, 1);//inner node with null letter
			Node newNode = new Node(String.valueOf(c), 1);//stores symbol
			
			//Pay attention to the linking process among nodes.
			innerNode.left = nytNode;
			innerNode.right = newNode;
			innerNode.parent = nytNode.parent;
			if ( nytNode.parent!=null )//In the first time the nyt node is root. 
				nytNode.parent.left = innerNode;
			else {
				root = innerNode;
			}
			nytNode.parent = innerNode;
			newNode.parent = innerNode;

			//The following two lines assure the right order in nodeList
			nodeList.add(1, innerNode);
			nodeList.add(1, newNode);
			alreadyExist.add(c);
			toBeAdd = innerNode.parent;
		} else {
			toBeAdd = findNode(c);
		}
		
		//Loop until all parent nodes are incremented.
		while ( toBeAdd!=null ) {
			Node bigNode = findBigNode(toBeAdd.frequency);
			/**
			 * The nodes should not be swapped in the following two situations:
			 * 1.The biggest node is itself.
			 * 2.The one node is the other's parent node.
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
	 * Visit a node in the tree
	 * @param p the pointer to the node
	 * @return letter if it's a leaf, otherwise null.
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
	 * Called when encoding. If the symbol is already existed, look for it in tree. Or else, 
	 * return the ASCII code of the character with a prefix of code of NEW.
	 * @param c the input character
	 * @return a code according to the input character
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
	 * Swap two nodes. Note that we should swap nodes but not only values, because the subtree
	 * is also needed to be swapped.
	 * @param n1 the node of which the frequency is to be incremented.
	 * @param n2 the biggest node in the block.
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
	 * Find the node with biggest index in a certain block. Just look for the first node with the 
	 * same frequency from the back.
	 * @param frequency 
	 * @return the found node
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
	 * A recursion function that is used to generate a huffman code of a given symbol.
	 * @param node the beginning node
	 * @param letter of which the code to be found
	 * @param code the generated code
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
	 * Return the ASCII code of the character c. Just for readability.
	 */
	public static int getAscii(char c){
		return (int)c;
	}
	
	/**
	 * Convert a decimal integer to a 8-bit binary code.
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
	 * Calculate the compress rate. Assuming that the original code is 8-bit ASCII code.
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
	 * The following three methods are for arithmetic coding.
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
	 * Traverse all the node in the tree using preorder.
	 * @param node the beginning node
	 * @param symbolList a symbol container
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
		 * Using when encoding.
		 */
//		String text = FileHandler.readFile("data/I have a dream.txt", true);
//		AdaptiveHuffman ah = new AdaptiveHuffman( text.toCharArray() );
//		ArrayList<String> code = ah.encode();
//		FileHandler.writeFile("data/ihaveadreaminHuff.txt", catStr(code), true);
//		ah.getStatistics();
//		calCompRate(text, code);

		
		/**
		 * Using when decoding.
		 */
//		String code = FileHandler.readFile("data/ihaveadreaminHuff.txt", false);
//		AdaptiveHuffman ah = new AdaptiveHuffman( code.toCharArray() );
//		String result = ah.decode();
//		FileHandler.writeFile("data/IhaveadreamFromHuff.txt", result, false);
		
		
	}


}
