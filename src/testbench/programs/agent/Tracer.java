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
import java.util.WeakHashMap;

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
	private boolean enforceLimit = false;
	
	//Resources
	private Map<String, RuleEvaluator> rules = new HashMap<>();
	private List<RuleEvaluator> evaluators = new ArrayList<>();
	private Set<String> subclassNames;
	private Set<Class<?>> subclasses = new HashSet<>();
	private Map<String, Set<String>> clToMethods;
	private PrintWriter output;
	
	private int uniqueIDCount = 2;
	
	private final Map<Object, Integer> idMap = new WeakHashMap<Object, Integer>() {
		
	};
	private HashMap<Thread, TraceStatus> traceStatus = new HashMap<>();
	private TraceStatus generalTraceStatus = TraceStatus.INIT;
	
	//Monitoring
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
		
		//Load subclasses
		loadSubclasses();
		
		//Create output file
		initOutputFile(outputFilename);
	}
	private void loadSubclasses() {
		System.out.println("- Loading subclasses...");
		
		for(String scStr : subclassNames) {
			try {
				subclasses.add(Class.forName(scStr));
			} catch (ClassNotFoundException | NullPointerException e) {
				System.out.println("  (e) Subclass load failed: " + e.getMessage());
			}
		}

		if(subclasses.isEmpty()) {
			System.out.println("  (w) Could not load any subclasses!");
			return;
		}
		
		System.out.println("  Found following interesting superclasses:");
		
		for(Class<?> c : subclasses) {
			System.out.println("  * " + c.getName());
		}
	}
	private void parseArgs(String args) throws Exception {
		String[] argTokens = args.split(",");
		
		if(argTokens.length < 5)
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
			case "limit":
				enforceLimit = Boolean.parseBoolean(value);
				break;
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
		
		for(RuleEvaluator re : rules.values()) {
			uniqueIDCount = Math.max(uniqueIDCount, re.getMaxCode());
		}
		uniqueIDCount++;
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
		output.flush();
	}
	private void finaliseOutputFile() {
		
		long diff = new Date().getTime() - startDate.getTime();
        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000);
		
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
	public boolean isMainClass(String className) {
		return mainClassName.equals(className);
	}
	public String getStartMethodName() {
		return startMethod;
	}
	public String getEndMethodName() {
		return endMethod;
	}
	public Set<Class<?>> getSuperclasses() {
		return subclasses;
	}
	public boolean shouldInstrument() {
		return generalTraceStatus != TraceStatus.STOPPED;
	}
	
	//Interface with instrumentation
	/**
	 * Called when the main hook entry method is reached. 
	 * Can be called several times.	
	 */
	public void onTraceStart() {
		generalTraceStatus = TraceStatus.IN_PROGRESS;
		
		System.out.println("(i) Tracer reached main class, trace is now in progress.");
		System.out.println("========================================================================");
	}
	/**
	 * Called when the main hook exit mehod is reached. Call only once.
	 */
	public void onTraceStop() {
		generalTraceStatus = TraceStatus.STOPPED;
		for(Entry<Thread, TraceStatus> e: traceStatus.entrySet()) {
			e.setValue(TraceStatus.STOPPED);
		}
		
		System.out.println("========================================================================");
		System.out.println("(i) Tracer reached end of traceable section, trace has stopped.");
		finaliseOutputFile();
		System.out.println("(i) Trace file is finalised and ready for use.");
		System.out.println("(i) Events: " + relevantExits);
		System.out.println("(i) Numbers: " + numTrNumbers);
		System.out.println("(i) Trace calls: " + entries);
	}
	/**
	 * Call for every method call that should be traced. 
	 * @param id
	 * @param cl
	 * @param method
	 * @param rv 
	 */
	public void add(Object implicitArgument, String cl, 
			        String method, String rv) {
		/*
		 * This method is flagged by trace status to suspend tracing
		 * when called: the reason for this is that calls may loop
		 * back to this method if it uses instrumented tools inside.
		 * 
		 * Flagging makes recursive calls being abandoned.
		 * 
		 * This method needs to be automatically
		 * or manually synchronized, to prevent threads from abandoning the trace
		 * because another thread flagged it.
		 * 
		 *  
		 */
		
		//Manage trace status per thread
		if(generalTraceStatus != TraceStatus.IN_PROGRESS) 
			return;
		if(!traceStatus.containsKey(Thread.currentThread()))
			traceStatus.put(Thread.currentThread(), TraceStatus.IN_PROGRESS);
		if(traceStatus.get(Thread.currentThread()) != TraceStatus.IN_PROGRESS)
			return;
		
		entries++;
		//Protect against infinite loops
		traceStatus.put(Thread.currentThread(), TraceStatus.WAITING);
		//If this is a constructor/static method (??), ignore
		if (implicitArgument == null || method.equals("<init>"))
			return;
		//Manage implicit argument's ID
		//If the object wasn't known yet, we will need to add an <init> statement
		int id = 0;
		
		synchronized(this) {
			try {
				if (!idMap.containsKey(implicitArgument)) {
					id = uniqueIDCount++;
					idMap.put(implicitArgument, id);
	
					addToTranslation(id, cl, "<init>", "null");
				} else
					id = idMap.get(implicitArgument);
			} catch (Exception e) {
				System.out.println("FAIL!");
				System.out.println("cl: " + cl);
				System.out.println("method: " + method);
				System.out.println("rv: " + rv);
				e.printStackTrace();
				System.out.println("Implicit argument: " + implicitArgument);
				System.exit(0);
			}
			if (!method.equals("<init>"))
				addToTranslation(id, cl, method, rv);
			if (enforceLimit && numTrNumbers >= MAX_NUM_NUMBERS_TR) {
				System.out.print("Reached limit: ");
				System.out.format("%,d", numTrNumbers);
				System.out.println(" numbers reached.");
			} else
				traceStatus.put(Thread.currentThread(), TraceStatus.IN_PROGRESS);
		}
	}
	/**
	 * Call if the return value is an object of which the string 
	 * @param implicitArgument
	 * @param cl
	 * @param method
	 * @param returnObject
	 */
	public void addObjectRV(Object implicitArgument, String cl, 
			        String method, Object returnObject) {
		add(implicitArgument, cl, method, 
			returnObject == null ? Integer.toString(manageObjectID(returnObject)) : "null");
	}
	private int manageObjectID(Object o) {
		if(!idMap.containsKey(o)) {
			idMap.put(o, uniqueIDCount++);
		}
		
		return idMap.get(o);
	}
	private void addToTranslation(int id, String cl, String method, String rv) {
		//Apply the same procedure as in the TRF protocol
		for(RuleEvaluator re : evaluators) {
			try {
				cl = cl.replace("/", ".");
				List<Integer> list = re.evaluate(id, cl, method, rv);
				
				outputTranslationBatch(list);
				
				if(list != null) {
					break;
				}
			} catch (Exception e) {
				System.err.println("Evaluation for '" + cl + "." + method + ":" + rv + "' failed: " + e.toString());
			}
		}
		
		if(numTrNumbers >= nextMileStone) {
			System.out.println(String.format("%,d", numTrNumbers) + " numbers recorded (" + String.format("%,d", entries) + " method exits, " 
							   + String.format("%,d", relevantExits) + " events, "
							   + 100.0*(double)numTrNumbers/(double)MAX_NUM_NUMBERS_TR + "%)");
			nextMileStone += DISPLAY_PROGRESS_MILESTONE;
		}
	}
	public int getId() {
		return uniqueIDCount++;
	}
	/**
	 * Call to protect any potential backlooping methods
	 * @return
	 */
	public boolean traceInProgress() {
		return traceStatus.get(Thread.currentThread()) == TraceStatus.IN_PROGRESS;
	}
	
	//Translation
	private void outputTranslationBatch(List<Integer> numbers) {
		if(numbers == null)
			return;
		
		for(Integer i : numbers) {
			output.print(i + "\n");
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

	public static enum TraceStatus {
		INIT, WAITING, IN_PROGRESS, STOPPED
	}
}
