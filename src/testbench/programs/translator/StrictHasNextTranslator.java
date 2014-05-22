package testbench.programs.translator;

import java.io.FileNotFoundException;
import java.util.ArrayList;

public class StrictHasNextTranslator extends Translator {

	public StrictHasNextTranslator(String filename) throws FileNotFoundException {
		super(filename);
	}

	@Override
	public int[] translate() {
		ArrayList<Integer> word = new ArrayList<>();
		
		removeHeaderComments();
		
		//Parse file
		do {
			//Check ID
			//Ignore stuff that has the wrong ID format
			String nextId = nextID();
			String cl = nextDeclaringClass();
			if(nextId.equals("UNKOWN") || nextId.equals("STATIC"))
				continue;
			
			//Check class
			//If it seems to be a valid ID but it's the wrong class
			if(!(cl.contains("Itr") || cl.contains("terator")))
				continue;
			
			//Check method
			//If the ID and Class seem OK, check what method is inside
			int methodCode = 0;
			switch(nextMethod()) {
			case "hasNext":
				methodCode = 2;
				break;
			case "next":
				methodCode = 3;
				break;
			case "<init>":
				methodCode = 1;
				break;
			}
			
			//If the method was wrong
			if(methodCode == 0)
				continue;
			
			//Else everything seems correct
			//Add ID
			word.add(Integer.parseInt(nextId));
			//Add method code
			word.add(methodCode);
		} while(nextIfNext());
		
		sc.close();
		return listToArray(word);
	}

}
