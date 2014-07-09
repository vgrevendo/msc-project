package testbench.programs.tracer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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

import testbench.programs.translator.TRFTranslator;
import testbench.programs.translator.Translator;
import testbench.programs.translator.trf.EqualityEvaluator;
import testbench.programs.translator.trf.RuleEvaluator;
import testbench.programs.translator.trf.SubClassEvaluator;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.Connector.Argument;
import com.sun.jdi.connect.Connector.IntegerArgument;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventIterator;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.event.MethodExitEvent;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.MethodEntryRequest;
import com.sun.jdi.request.MethodExitRequest;

/**
 * <p>A debugging tool designed to make a trace of all method calls in target
 * application, over the network.</p>
 * 
 * <p>This new version now only listens to method exits, so that return values can be
 * signalled as well. This should not change anything to property checking.</p>
 * @author vincent
 *
 */
@SuppressWarnings("unused") 
public class TRFTracer {
	public static final String CONNECTOR_NAME = "dt_socket";
	public static final String PATH_ROOT = "gen/trace_TRF_";
	
	//Real-time translation
	public static final int DISPLAY_PROGRESS_MILESTONE = 1_000;
	public static final int MAX_NUM_NUMBERS_TR = 20_000_000;
	
	private Map<String, RuleEvaluator> rules = new HashMap<>();
	private List<RuleEvaluator> evaluators = new ArrayList<>();
	private Set<String> subclassNames;
	private Set<String> unloadedSubclassNames = new HashSet<>();
	private Set<ReferenceType> subclasses = new HashSet<>();
	 
	//Progress
	private int entries = 0;
	private int numTrNumbers = 0;
	private int relevantExits = 0;
	private int lastSignal = 0;
	
	private Date startDate;
	
	//Environment resources
	private VirtualMachine vm;
	private PrintWriter output;
	private String mainClassName;
	private String mainMethodName;
	private String exitMethodName;
	
	//JDI specific stuff
 	private void connect(int portNumber) throws Exception {
		//Make connection with distant virtual machine:
		// plug in the connector which uses a socket
		VirtualMachineManager vmManager = Bootstrap.virtualMachineManager();
		AttachingConnector connector = null;
		
		for(AttachingConnector ac: vmManager.attachingConnectors()) {
			if(ac.transport().name().equals(CONNECTOR_NAME)) {
				connector = ac;
				break;
			}
		}
		
		if(connector == null) {
			System.out.println("Could not find connector to the Virtual Machine! Exiting");
			return;
		}
		
		System.out.println("- Found connector, attaching to VM on port " + portNumber);
		
		//Get a pointer to the Virtual Machine in question
		// by specifying the connexion port
		Map<String, Argument> paramsMap = connector.defaultArguments();
		Connector.IntegerArgument portArg = (IntegerArgument) paramsMap.get("port"); 
		portArg.setValue(portNumber);
		
		try {
			vm = connector.attach(paramsMap);
		} catch (IllegalConnectorArgumentsException | IOException e) {
			System.out.println("Could not attach to virtual machine, here's why:");
			e.printStackTrace();
			throw new Exception("Unable to attach to virtual machine");
		}
		
		System.out.println("- Successfully attached to " + vm.name());
		if(vm.canGetMethodReturnValues())
			System.out.println("   (i) This virtual machine can report method return values");
		else
			System.out.println("   (w) This virtual machine can't report method return values!");
	}
	
 	//Tracer methods
	private void listenForMethodExits() throws InterruptedException {
		EventRequestManager evtRqManager = vm.eventRequestManager();
		EventQueue evtQueue = vm.eventQueue();
		
		//Jump to main class
		EventSet toResume = jumpToMainClass();
		
		//Create method exit event request on exit
		// Add some subclass filters
		loadReferences();
		
		System.out.println("- Creating filtered request for final exit method");
		MethodExitRequest request = evtRqManager.createMethodExitRequest();
		request.addClassFilter(mainClassName);
		request.setSuspendPolicy(MethodExitRequest.SUSPEND_ALL);
		request.enable();
		
		System.out.println("- Creating filtered requests for " + subclasses.size() + " types");
		
		for(ReferenceType t : subclasses) {
			requestMethodExit(t, evtRqManager);
		}
		
		System.out.println("- " + unloadedSubclassNames.size() + " types still have to wait, creating onload requests");
		
		for(String cl : unloadedSubclassNames) {
			ClassPrepareRequest prepRequest = evtRqManager.createClassPrepareRequest();
			prepRequest.setSuspendPolicy(ClassPrepareRequest.SUSPEND_ALL);
			prepRequest.addClassFilter(cl);
			prepRequest.enable();
		}
		
		System.out.println("- Trace is starting NOW");
		toResume.resume();
		
		long startTime = System.currentTimeMillis();
		
		//Handle events as they come in
		while(true) {
			//Iterate over incoming requests (when the vm is suspended)
			EventSet evtSet = evtQueue.remove();
			for(Event event : evtSet) {
				entries++;
				
				//Only method exits are interesting to us, here.
				if(!(event instanceof MethodExitEvent)){
					//But check for class preparations as well!
					
					if(event instanceof ClassPrepareEvent) {
						loadReferenceAndRequest((ClassPrepareEvent)event, evtRqManager);
					}
					
					continue;
				}
				
				
				MethodExitEvent exitEvent = (MethodExitEvent) event;
				int id = 0;

				try {
					if(exitEvent.method().isStatic())
						continue;
					else {
						StackFrame frame;
						frame = exitEvent.thread().frame(0);
						id = (int) frame.thisObject().uniqueID();
					}
				} catch (Exception e) {
					continue;
				}
				
				translate(id, exitEvent);
				
				if(exitEvent.method().declaringType().name().equals(mainClassName) && exitEvent.method().name().equals(exitMethodName)) {
					System.out.println("Reached STOP method of class " + mainClassName + ": exiting.");
					return;
				}
				
				if(numTrNumbers % DISPLAY_PROGRESS_MILESTONE == 0 && lastSignal != numTrNumbers) {
					System.out.println(numTrNumbers + " numbers recorded (" + entries + " method exits, " 
									   + relevantExits + " events, "
									   + 100.0*(double)numTrNumbers/(double)MAX_NUM_NUMBERS_TR + "%) ETA: "
									   + getRemainingTime(startTime, numTrNumbers));
					lastSignal = numTrNumbers;
				}
				
				if(numTrNumbers >= MAX_NUM_NUMBERS_TR) {
					System.out.println("Hit maximum number of entries: " + MAX_NUM_NUMBERS_TR);
					return;
				}
			}
			
			evtSet.resume();
		}
	}
	private String getRemainingTime(long start, int progression) {
		try {
			double duration = System.currentTimeMillis() - start;
			double leftover = MAX_NUM_NUMBERS_TR - progression;
			
			double mins = duration * leftover / (60_000.0 * (double) progression);
			return Integer.toString((int)mins) + "min";
		} catch (Exception e) {}
		
		return "unknown";
	}
	private EventSet jumpToMainClass() throws InterruptedException {
		System.out.println("- Listening for main class entry (" + mainClassName + ")");
		
		EventRequestManager evtRqManager = vm.eventRequestManager();
		EventQueue evtQueue = vm.eventQueue();
		
		//Create event request once for the main class, in order to skip 
		// all the useless calls at the beginning
		MethodEntryRequest initialEntryRequest = evtRqManager.createMethodEntryRequest();
		initialEntryRequest.addClassFilter(mainClassName);
		initialEntryRequest.setSuspendPolicy(MethodEntryRequest.SUSPEND_ALL);
		initialEntryRequest.enable();
		
		while(true) {
			
			EventSet evtSet = evtQueue.remove();
			EventIterator evtIt = evtSet.eventIterator();
			
			while(evtIt.hasNext()) {
				Event event = evtIt.next();
				
				if(!(event instanceof MethodEntryEvent)) {
					continue;
				}
				
				MethodEntryEvent entryEvent = (MethodEntryEvent)event;
				
				if(entryEvent.method().name().equals(mainMethodName)) {
					//Signal success
					System.out.println("- Jump to main class is complete.");
					output.println("-- Now recording method exits in " + entryEvent.method().declaringType().name() + "." + entryEvent.method().name());
					entries ++;
					
					//Kill event request
					initialEntryRequest.disable();
					
					return evtSet;
				} else {
					System.out.println("Skipped method " + entryEvent.method().name() + " from class " + entryEvent.method().declaringType().name());
					continue;
				}
				
			}
			
			evtSet.resume();
		}
	}
	
	//File management
	private String chooseFileName() {
		int id = 0;
		String prefix = PATH_ROOT;
		
		while((new File(prefix + id + ".tr")).exists()) {
			id ++;
		}
		
		return prefix + id + ".tr";
	}
	private void initOutputFile(String path) throws FileNotFoundException {
		String filename = null;
		if(path == null)
			filename = chooseFileName();
		else {
			File f = new File(path);
			if(f.exists())
				f.delete();
			filename = path;
		}
		output = new PrintWriter(filename);
		
		//Write header
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
	
	//Translation methods from TRF
	private void loadRules(Scanner sc) throws Exception {
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
		
		if(subclassNames.isEmpty())
			throw new Exception("No subclasses are being filtered, rest is not implemented!");
		
		System.out.println("- Rules were loaded with success.");
	}
	private void loadReferences() {
		System.out.println("- Now loading references (" + subclassNames.size() + " subclass names):");
		for(String name : subclassNames) {
			System.out.println("  * '" + name + "':");
			
			List<ReferenceType> types = vm.classesByName(name);
			if(types != null && !types.isEmpty()) {
				for(ReferenceType t : types) {
					System.out.println("      " + t.name());
				}
				subclasses.addAll(types);
			}
			else {
				System.out.println("       (w) Could not find reference type, will wait til loaded.");
				unloadedSubclassNames.add(name);
			}
		}
	}
	private MethodExitRequest loadReferenceAndRequest(ClassPrepareEvent prepEvent,
													  EventRequestManager mgr) {
		ReferenceType t = prepEvent.referenceType();
		
		System.out.println("- Type '" + t.name() + "' has been prepared");
		subclasses.add(t);
		System.out.println("- Cancelling preparation request");
		prepEvent.request().disable();
		mgr.deleteEventRequest(prepEvent.request());
		
		unloadedSubclassNames.remove(t.name());
		return requestMethodExit(t, mgr);
	}
	private MethodExitRequest requestMethodExit(ReferenceType t, EventRequestManager mgr) {
		System.out.println("- Generating method exit request for " + t.name());
		MethodExitRequest request2 = mgr.createMethodExitRequest();
		request2.setSuspendPolicy(MethodExitRequest.SUSPEND_ALL);
		request2.addClassFilter(t);
		//request2.addClassFilter("java.util.Iterator"); 
		request2.putProperty("filter", t.name());
		request2.enable();
		
		System.out.println("  Request has been successfully added!");
		
		return request2;
	}
	private void translate(int id, MethodExitEvent event) {
		String cl = (String) event.request().getProperty("filter");
		String method = event.method().name();
		String rv = event.returnValue() == null ? "null" : event.returnValue().toString();
		
		if(cl == null) {
			System.err.println("Evaluation for '" + event.method().declaringType().toString() + "." + method + ":" + rv + "' failed (skipping)");
			return;
		}
		
		//Apply the same procedure as in the TRF protocol
		for(RuleEvaluator re : evaluators) {
			try {
				List<Integer> list = re.evaluate(id, cl, method, rv);
				
				outputTranslationBatch(list);
			} catch (Exception e) {
				System.err.println("Evaluation for '" + cl + "." + method + ":" + rv + "' failed (skipping)");
			}
		}
	}
	private void outputTranslationBatch(List<Integer> numbers) {
		if(numbers == null) {
			return;
		}
		
		for(Integer i : numbers) {
			output.print(i + " ");
		}
		
		relevantExits++;
		numTrNumbers += numbers.size();
	}
	protected String reconstructMethod(String cl, String method, String rv) {
		return cl + "." + method + ":" + rv;
	}
	
	//Launchpad	
	public static void main(String[] args) {
		(new TRFTracer()).run(args);
	}
	public void run(String[] args) {
		try {
			startDate = new Date();
			System.out.println("[TRACER - TRF version]");
			
			//Set some params
			mainClassName = args[1];
			mainMethodName = args[2];
			exitMethodName = args[3];
			
			//Connect to the VM
			connect(Integer.parseInt(args[0]));
			
			//Load TRF rules
			loadRules(new Scanner(new File(args[4])));			
			
			//Set output file
			if(args.length >= 6)
				initOutputFile(args[5]);
			else
				initOutputFile(null);
			
			//Listen for events
			listenForMethodExits();
		} catch (Exception e) {
			System.err.println("Trace exception occurred!");
			e.printStackTrace();
		}
		
		//Close file
		finaliseOutputFile();
		//Quit
		try { vm.exit(0); }
		catch(Exception e) {}
	}
}
