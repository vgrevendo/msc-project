package testbench.programs.agent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Map.Entry;

import sun.rmi.rmic.iiop.Generator.OutputType;
import testbench.programs.translator.trf.EqualityEvaluator;
import testbench.programs.translator.trf.RuleEvaluator;
import testbench.programs.translator.trf.SubClassEvaluator;

/**
 * <p>This class should be inserted at runtime, and called upon
 * via static ways to make trace operations. It should store the
 * reference to the output file, and the reference to the 
 * translation rules.</p>
 * <p>Insertion should be feasible via the classpath at runtime.</p>
 * <p>This is a singleton class.</p>
 * @author vincent
 *
 */
public class Tracer {
	//INSTANCE
	//Constants
	private final static int DISPLAY_PROGRESS_MILESTONE = 10_000;
	private final static int MAX_NUM_NUMBERS_TR = 20_000_000;
	
	//Arguments
	private String trfFilename;
	private String outputFilename;
	private String mainClassName;
	private String startMethod;
	private String endMethod;
	
	//Resources
	private Map<String, RuleEvaluator> rules = new HashMap<>();
	private List<RuleEvaluator> evaluators = new ArrayList<>();
	private Set<String> subclassNames;
	private Map<String, Set<String>> clToMethods;
	private PrintWriter output;
	
	private int uniqueIDCount = 2;
	
	//Monitoring
	private boolean traceInProgress = false;
	private Date startDate;
	private int relevantExits = 0;
	private int numTrNumbers = 0;
	private int entries = 0;
	
	private int nextMileStone = DISPLAY_PROGRESS_MILESTONE;
	
	//Loading
	/**
	 * Create instance on TRF and output filename.
	 * @param filename
	 * @throws Exception 
	 */
	private Tracer(String args) throws Exception {
		//Parse arguments
		parseArgs(args);
		
		//Load TRF
		loadTRF(trfFilename);
		
		//Create output file
		initOutputFile(outputFilename);
	}
	
	private void parseArgs(String args) throws Exception {
		String[] argTokens = args.split(",");
		
		if(argTokens.length != 5)
			throw new Exception("Expecting arguments trf, out, hook, inmethod, outmethod");
		
		for(String token : argTokens) {
			String[] values = token.split("=");
			String label = values[0];
			String value = values[1];
			 
			switch(label) {
			case "trf":
				trfFilename = value; break;
			case "out":
				outputFilename = value; break;
			case "hook":
				mainClassName = value.replace('.', '/'); break;
			case "inmethod":
				startMethod = value; break;
			case "outmethod":
				endMethod = value; break;
			default:
				throw new Exception("Unrecognised option '" + label + "'");
			}
		}
		
		startDate = new Date();
	}
	
	private void loadTRF(String filename) throws Exception {
		Scanner sc = new Scanner(new File(filename));
		
		System.out.println("- will now load TRF rules...");
		SubClassEvaluator subClassEvaluator = new SubClassEvaluator();
		
		while(sc.hasNextLine()) {
			String line = sc.nextLine();
			if(line.substring(0, 2).equals("--")) //If is comment
				continue;
			
			String prefix = line.split(":")[0];
			if(!rules.containsKey(prefix)) {
				switch(prefix) {
				case "subclass":
					rules.put("subclass", subClassEvaluator);
					break;
				case "equals":
					rules.put("equals", new EqualityEvaluator());
					break;
				default:
					throw new Exception("Unknown rule prefix: " + prefix);
				}
			}
			
			try {
				rules.get(prefix).addRule(line);
			} catch (Exception e) {
				System.out.println("Could not parse rule: '" + line + "'! Skipped");
			}
		}
		
		//Build evaluators list for performance
		for(Entry<String, RuleEvaluator> ree : rules.entrySet())
			evaluators.add(ree.getValue());
		
		//To speed up the debugger, remember which subclasses are important
		subclassNames = subClassEvaluator.getSubclassNames();
		clToMethods = subClassEvaluator.getClassToMethodNames();
		
		if(subclassNames.isEmpty()) {
			sc.close();
			throw new Exception("No subclasses are being filtered, rest is not implemented!");
		}
		
		System.out.println("- Rules were loaded with success.");
		sc.close();
	}
	
	private void initOutputFile(String filename) throws FileNotFoundException {
		File f = new File(filename);
		if(f.exists())
			f.delete();
		
		output = new PrintWriter(f);
		
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date date = new Date();
		output.println("-- Listening for method exits in main class " + mainClassName);
		output.println("-- This trace was generated on " + dateFormat.format(date));
	}
	
	private void finaliseOutputFile() {
		
		long diff = new Date().getTime() - startDate.getTime();
        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000);
		
		output.println();
		output.println("-- Numbers: " + numTrNumbers + ", Events: " + relevantExits);
		output.println("-- It took " + diffHours + "h" + diffMinutes + "min" + diffSeconds + "s to complete this trace");
		output.close();
	}
	
	//Interface with Inspector
	public Set<String> getClassesToInstrument() {
		return new HashSet<>(subclassNames);
	} 
	public Set<String> getMethodsToInstrument(String clName) {
		return clToMethods.get(clName);
	}
	public boolean isStartMethod(String className, String methodName) {
		return mainClassName.equals(className) && methodName.equals(methodName);
	}
	public boolean isEndMethod(String className, String methodName) {
		return mainClassName.equals(className) && methodName.equals(methodName);
	}
	
	
	//Accessors
	public int getId() {
		return uniqueIDCount++;
	}
	
	//Hooks
	/**
	 * Called when the main hook entry method is reached. 
	 * Can be called several times.	
	 */
	public void onTraceStart() {
		traceInProgress = true;
		
		System.out.println("(i) Tracer reached main class, trace is now in progress.");
	}
	/**
	 * Called when the main hook exit mehod is reached. Call only once.
	 */
	public void onTraceStop() {
		traceInProgress = false;
		
		System.out.println("(i) Tracer reached end of traceable section, trace has stopped.");
		finaliseOutputFile();
		System.out.println("(i) Trace file is finalised and ready for use.");
	}
	/**
	 * Call for every method call that should be traced.
	 * @param id
	 * @param cl
	 * @param method
	 * @param rv
	 */
	public void add(int id, String cl, String method, String rv) {
		entries++;
		
		if(!traceInProgress) 
			return;
		
		//Apply the same procedure as in the TRF protocol
		for(RuleEvaluator re : evaluators) {
			try {
				List<Integer> list = re.evaluate(id, cl, method, rv);
				
				outputTranslationBatch(list);
				
				if(list != null)
					break;
			} catch (Exception e) {
				System.err.println("Evaluation for '" + cl + "." + method + ":" + rv + "' failed (skipping)");
			}
		}
		
		if(numTrNumbers >= nextMileStone) {
			System.out.println(numTrNumbers + " numbers recorded (" + entries + " method exits, " 
							   + relevantExits + " events, "
							   + 100.0*(double)numTrNumbers/(double)MAX_NUM_NUMBERS_TR + "%)");
			nextMileStone += DISPLAY_PROGRESS_MILESTONE;
		}
	}
	
	//Translation
	private void outputTranslationBatch(List<Integer> numbers) {
		if(numbers == null)
			return;
		
		for(Integer i : numbers) {
			output.print(i + " ");
		}
		
		relevantExits++;
		numTrNumbers += numbers.size();
	}
	
	//SINGLETON
	private static Tracer instance;
	
	public static void loadRules(String args) throws Exception {
		instance = new Tracer(args);
	}
	
	public static Tracer getTracer() {
		return instance;
	}
}
