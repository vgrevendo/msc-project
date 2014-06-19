package testbench.programs.translator;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Translate the input trace by taking into account the object's identifier,
 * the class's name, the method's name and its return value. Discard all
 * references to non-iterator related method calls.</p>
 * <p>Iterator method calls are encoded the following way: * 
 * <ul>
 * <li>1 for an init call over an iterator;</li>
 * <li>2 for a hasNext call over an iterator, returning false;</li>
 * <li>3 for a hasNext call over an iterator, returning true;</li>
 * <li>4 for a next call over an iterator.</li>
 * </ul></p>
 * @author vincent
 *
 */
public class StrictHasNextTranslator extends Translator {

	public StrictHasNextTranslator(String filename) throws FileNotFoundException {
		setFileSource(filename);
	}

	@Override
	public List<Integer> translate() {
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
				methodCode = nextReturnValue().equals("false") ? 2 : 3;
				break;
			case "next":
				methodCode = 4;
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
		return word;
	}

}
