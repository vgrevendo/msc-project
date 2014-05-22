package testbench.programs.translator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Keep IDs as they are, and give the following codes to method calls:
 * 1 to Iterator init
 * 2 to Iterator hasNext
 * 3 to Iterator next
 * 4 to *
 * Give 0 to UNKNOWN or STATIC IDs
 * @author vincent
 *
 */
public class NaiveHasNextTranslator  {
	private Scanner sc;
	private String[] currentTokens;

	public NaiveHasNextTranslator(String filename) throws FileNotFoundException {
		sc = new Scanner(new File(filename));
	}
	
	public int[] translate() {
		ArrayList<Integer> word = new ArrayList<>();
		
		removeHeaderComments();
		
		//Parse file
		do {
			//ID
			String nextId = nextID();
			switch(nextId) {
			case "UNKNOWN":
			case "STATIC":
				word.add(0);
				break;
			default:
				word.add(Integer.parseInt(nextId));
			}
			
			//Check class
			String cl = nextDeclaringClass();
			String nextMethod = nextMethod();
			if(cl.contains("Itr") || cl.contains("terator")){
				//If ok, translate the command
				switch(nextMethod) {
				case "hasNext":
					word.add(2);
					break;
				case "next":
					word.add(3);
					break;
				case "<init>":
					word.add(1);
					break;
				default:
					word.add(4);
				}
			} else {
				//Else wrong class, * case
				word.add(4);
			}
		} while(hasNextLine());
		
		//Convert to primitive array
		int[] pword = new int[word.size()];
		int idx = 0;
		for(int i : word) {
			pword[idx] = i;
			idx++;
		}
		
		sc.close();
		return pword;
	}
	
	//Internal tools
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
		if(currentTokens == null)
			currentTokens = sc.nextLine().split(" ");
		return currentTokens[0].substring(1, currentTokens[0].length()-1);
	}
	
	protected String nextMethod() {
		String token = currentTokens[2];
		currentTokens = null;
		return token;
	}
	
	protected String nextDeclaringClass() {
		return currentTokens[1];
	}

}
