package testbench.programs.translator;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * Keep IDs as they are, and give the following codes to method calls:
 * 1 to Iterator init
 * 2 to Iterator hasNext
 * 3 to Iterator next
 * 4 to *
 * Give 0 to UNKNOWN or STATIC IDs
 * @deprecated
 * @author vincent
 *
 */
public class NaiveHasNextTranslator extends Translator  {
	public NaiveHasNextTranslator(String filename) throws FileNotFoundException {
		super(filename);
	}
	
	@Override
	public List<Integer> translate() {
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
			if(cl.contains("Itr") || cl.contains("terator")){
				//If ok, translate the command
				switch(nextMethod()) {
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
		} while(nextIfNext());
		
		sc.close();
		return word;
	}
}
