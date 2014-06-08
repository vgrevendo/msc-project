package testbench.programs.translator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

public abstract class Translator {
	protected Scanner sc;
	private String[] currentTokens;
	
	public Translator(String filename) throws FileNotFoundException {
		sc = new Scanner(new File(filename));
	}

	//To be implemented by the extending class
	public abstract List<Integer> translate();

	protected void removeHeaderComments() {
		String line = sc.nextLine();
		while(line.substring(0,2).equals("--")) {
			line = sc.nextLine();
		} 
		currentTokens = line.split(" ");
	}

	protected boolean hasNextLine() {
		return sc.hasNextLine();
	}

	protected String nextID() {
		return currentTokens[0].substring(1, currentTokens[0].length()-1);
	}

	protected String nextMethod() {
		return currentTokens[2];
	}

	protected String nextDeclaringClass() {
		return currentTokens[1];
	}
	
	protected String nextReturnValue() {
		if(currentTokens.length > 4) {
			if(currentTokens[3].contains("void"))
				return "void";
			else
				return "## " + currentTokens[currentTokens.length-1];
		}
		
		return currentTokens[3];
	}
	
	protected void next() {
		currentTokens = sc.nextLine().split(" ");
	}
	
	protected boolean nextIfNext() {
		if(sc.hasNextLine()) {
			next();
			return true;
		}
		return false;
	}
	
	protected int[] listToArray(List<Integer> wordList) {
		//Convert to primitive array
		int[] pword = new int[wordList.size()];
		int idx = 0;
		for(int i : wordList) {
			pword[idx] = i;
			idx++;
		}
		
		return pword;
	}

}