package testbench.programs.translator;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>Translate the input trace by taking into account the object's identifier,
 * the class's name, the method's name and its return value. Only consider calls
 * that are linked to collections and iterators.</p>
 * <p>Iterator method calls are encoded the following way:
 * <ul>
 * </ul></p>
 * @author vincent
 *
 */
public class SafeIterTranslator extends Translator {

	public SafeIterTranslator(String filename) throws FileNotFoundException {
		super(filename);
	}

	@Override
	public List<Integer> translate() {
		ArrayList<Integer> word = new ArrayList<>();
		Set<String> collectionClasses = new HashSet<>();
		
		removeHeaderComments();
		
		int classesNotFound = 0;
		int[] stats = new int[10];
		
		//Parse file
		do {
			//Check ID
			//Ignore stuff that has the wrong ID format
			String nextId = nextID();
			String cl = nextDeclaringClass();
			String method = nextMethod();
			int methodCode = 0;
			int returnValue = 0;
			
			if(nextId.equals("UNKOWN") || nextId.equals("STATIC"))
				continue;
			
			//Check class
			//If it's iterator-related
			if(cl.contains("Itr") || cl.contains("terator")) {
				//Check method
				//If the ID and Class seem OK, check what method is inside
				switch(method) {
				case "next":
					methodCode = 4;
					break;
				case "finalize":
					methodCode = 5;
					break;
				}
			} else { //is it collection related?
				boolean collectionRelated = collectionClasses.contains(cl);
				
				try {
					if(!collectionRelated && Collection.class.isAssignableFrom(Class.forName(cl))) {
						collectionClasses.add(cl);
						collectionRelated = true;
					}
				} catch (ClassNotFoundException e) {
					classesNotFound++;
				}
				
				//If it's collection related
				if(collectionRelated) {
					switch(method) {
					case "remove":
					case "add":
						methodCode = 2;
						break;
					case "<init>":
						methodCode = 1;
						break;
					case "iterator":
						methodCode = 3;
						//Also record return value
						returnValue = Integer.parseInt(nextReturnValue().replaceAll("[^0-9]", ""));
						break;
					}
				}
			}
			
			//If the method was wrong or still default
			if(methodCode == 0)
				continue;
			
			stats[methodCode]++;
			
			//Else everything seems correct
			//Add ID
			word.add(Integer.parseInt(nextId));
			//Add method code
			word.add(methodCode);
			//Add return value
			word.add(returnValue);
		} while(nextIfNext());
		
		System.out.println("Translation done. Number of classes not found: " + classesNotFound);
		System.out.println("Stats: " + Arrays.toString(stats));
		
		sc.close();
		return word;
	}

}
