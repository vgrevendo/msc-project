package testbench.programs.translator;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import testbench.programs.translator.trf.EqualityEvaluator;
import testbench.programs.translator.trf.RuleEvaluator;
import testbench.programs.translator.trf.SubClassEvaluator;

/**
 * A translator based on a TRF rules text file.
 * Rules are detailed in activity overview
 * @author vincent
 *
 */
public class TRFTranslator extends Translator {
	private Map<String, RuleEvaluator> rules = new HashMap<>();

	public TRFTranslator(String filename, String rules) throws Exception {
		super(filename);
		
		System.out.println("This is TRF - will now scan rules...");
		loadRules(new Scanner(new File(rules)));
		System.out.println("Rules scanned with success, ready to translate acquisition.");
	}

	@Override
	public List<Integer> translate() {
		ArrayList<Integer> translation = new ArrayList<>();
		ArrayList<RuleEvaluator> evaluators = new ArrayList<>();
		
		//Build evaluators list for performance
		for(Entry<String, RuleEvaluator> ree : rules.entrySet())
			evaluators.add(ree.getValue());
		
		removeHeaderComments();
		
		//Parse trace file
		do {
			String nextId = nextID();
			
			if(nextId.equals("UNKNOWN") || nextId.equals("STATIC"))
				continue;
			
			for(RuleEvaluator re : evaluators) {
				try {
					List<Integer> list = re.evaluate(Integer.parseInt(nextId), 
													 nextDeclaringClass(), 
													 nextMethod(), nextReturnValue());
					
					if(list != null) {
						translation.addAll(list);
						break;
					}
				} catch (Exception e) {
					System.err.println("Evaluation for '" + nextDeclaringClass() + "' failed (skipping): " + e.getMessage());
				}
			}
		} while(nextIfNext());
		
		return translation;
	}
	
	private void loadRules(Scanner sc) throws Exception {
		while(sc.hasNextLine()) {
			String line = sc.nextLine();
			if(line.substring(0, 2).equals("--")) //If is comment
				continue;
			
			String prefix = line.split(":")[0];
			if(!rules.containsKey(prefix)) {
				switch(prefix) {
				case "subclass":
					rules.put("subclass", new SubClassEvaluator());
					break;
				case "equals":
					rules.put("equals", new EqualityEvaluator());
					break;
				default:
					throw new Exception("Unknown rule prefix: " + prefix);
				}
			}
			
			rules.get(prefix).addRule(line);
		}
	}

}
