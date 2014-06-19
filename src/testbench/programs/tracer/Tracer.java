package testbench.programs.tracer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import testbench.programs.translator.TRFTranslator;
import testbench.programs.translator.Translator;

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
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventIterator;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.event.MethodExitEvent;
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
public class Tracer {
	public static final String CONNECTOR_NAME = "dt_socket";
	public static final String PATH_ROOT = "gen/trace";
	public static final int DISPLAY_PROGRESS_MILESTONE = 1000;
	public static final int MAX_NUM_ENTRIES = 1_000_000;
	
	//Real-time translation
	public static final boolean REAL_TIME_TR = true;
	public static final int DEF_SB_CAPACITY = 70_000;
	public static final int MAX_NUM_NUMBERS_TR = 3_000_000;
	public static final int TR_BUFFER_SIZE = 1_000;
	
	private int numTrNumbers = 0;
	private Translator trf;
	
	//Environment resources
	private VirtualMachine vm;
	private PrintWriter output;
	private int entries = 0;
	private String mainClassName;
	private String mainMethodName;
	private String exitMethodName;
	
	private StringBuilder sb = new StringBuilder(DEF_SB_CAPACITY);
	
	//Instance
 	public void connect(int portNumber) throws Exception {
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
		
		System.out.println("Found connector, attaching to VM on port " + portNumber);
		
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
		
		System.out.println("Successfully attached to " + vm.name());
		if(vm.canGetMethodReturnValues())
			System.out.println("This virtual machine can report method return values");
		else
			System.out.println("This virtual machine can't report method return values!");
	}
	
	public List<Method> getAllMethods() {
		System.out.println("Retrieving methods list");
		
		ArrayList<Method> methods = new ArrayList<>();
		
		List<ReferenceType> types = vm.allClasses();
		for(ReferenceType t: types) {
			methods.addAll(t.visibleMethods());
		}
		
		return methods;
	}
	
	public List<Location> methodsToLocations(List<Method> methods) {
		List<Location> locations = new ArrayList<>();
		for(Method m: methods) {
			locations.add(m.location());
		}
		return locations;
	}
	
	public void listenForMethodExits() throws InterruptedException {
		EventRequestManager evtRqManager = vm.eventRequestManager();
		EventQueue evtQueue = vm.eventQueue();
		
		//Jump to main class
		EventSet toResume = jumpToMainClass();
		
		//Create method exit event request oncentry
		MethodExitRequest exitRequest = evtRqManager.createMethodExitRequest();
		exitRequest.setSuspendPolicy(MethodEntryRequest.SUSPEND_ALL);
		exitRequest.enable();
		
		toResume.resume();
		
		//Handle events as they come in
		while(true) {
			
			EventSet evtSet = evtQueue.remove();
			EventIterator evtIt = evtSet.eventIterator();
			
			while(evtIt.hasNext()) {
				Event event = evtIt.next();
				
				//Only method exits are interesting to us, here.
				if(!(event instanceof MethodExitEvent))
					continue;
				
				MethodExitEvent exitEvent = (MethodExitEvent) event;
				String traceLine = "";

				try {
					if(exitEvent.method().isStatic())
						traceLine = "[STATIC]";
					else {
						StackFrame frame;
						frame = exitEvent.thread().frame(0);
						traceLine = "[" + frame.thisObject().uniqueID() + "]";
					}
				} catch (Exception e) {
					traceLine = "[UNKNOWN]";
				}
				
				addToTrace(traceLine + " " + exitEvent.method().declaringType().name() + 
							   " " + exitEvent.method().name() + 
						       " " + exitEvent.returnValue());
				
				if(exitEvent.method().declaringType().name().equals(mainClassName) && exitEvent.method().name().equals(exitMethodName)) {
					System.out.println("Reached STOP method of class " + mainClassName + ": exiting.");
					return;
				}
				
				if(entries % DISPLAY_PROGRESS_MILESTONE == 0) {
					System.out.println(entries + " entries recorded");
				}
				
				if(!REAL_TIME_TR && entries >= MAX_NUM_ENTRIES) {
					System.out.println("Hit maximum number of entries: " + MAX_NUM_ENTRIES);
					return;
				}
				
				if(REAL_TIME_TR && numTrNumbers >= MAX_NUM_NUMBERS_TR) {
					System.out.println("Hit maximum number of entries: " + MAX_NUM_NUMBERS_TR);
					return;
				}
			}
			
			evtSet.resume();
		}
		
		
	}
	
	private void addToTrace(String s) {
		entries++;
		
		if(REAL_TIME_TR) {
			sb.append(s);
			sb.append("\n");
			if(entries % TR_BUFFER_SIZE == 0) {
				trf.setStringSource(sb.toString());
				outputTranslationBatch(trf.translate());
				sb = new StringBuilder();
			}
		} else {
			output.println(s);
		}
		
	}
	
	public EventSet jumpToMainClass() throws InterruptedException {
		System.out.println("Listening for main class entry (" + mainClassName + ")");
		
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
					System.out.println("Jump to main class is complete.");
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
	
	private String chooseFileName() {
		int id = 0;
		String prefix = PATH_ROOT;
		if(REAL_TIME_TR)
			prefix += "_TRF_";
		
		while((new File(prefix + id + ".tr")).exists()) {
			id ++;
		}
		
		return prefix + id + ".tr";
	}
	
	private void initOutputFile() throws FileNotFoundException {
		String filename = chooseFileName();
		output = new PrintWriter(filename);
		
		//Write header
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date date = new Date();
		output.println("-- Listening for method exits");
		output.println("-- This trace was generated on " + dateFormat.format(date));
	}
	
	public void run(String[] args) {
		try {
			if(REAL_TIME_TR)
				trf = new TRFTranslator(args[4]);
			
			//Set some params
			mainClassName = args[1];
			mainMethodName = args[2];
			exitMethodName = args[3];
			
			//Connect to the VM
			connect(Integer.parseInt(args[0]));
			
			//Set output file
			initOutputFile();
			
			//Listen for events
			listenForMethodExits();
			
			//Quit
			vm.exit(0);
			
		} catch (Exception e) {
			System.out.println("Trace failed: " + e.getMessage());
			e.printStackTrace();
		}
		
		//Close file
		output.close();
	}
	
	//Launchpad
	
	public static void main(String[] args) {
		(new Tracer()).run(args);
	}

	//Real-time translation
	private void outputTranslationBatch(List<Integer> numbers) {
		for(Integer i : numbers) {
			output.print(i + " ");
		}
		
		numTrNumbers += numbers.size();
		
		System.out.println("Output " + numbers.size() + " numbers (total: " + numTrNumbers + ")");
	}
}
