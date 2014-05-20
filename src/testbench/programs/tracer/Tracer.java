package testbench.programs.tracer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.IncompatibleThreadStateException;
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
 * A debugging tool designed to make a trace of all(?) method calls in target
 * application, over the network.
 * @author vincent
 *
 */
public class Tracer {
	public static final String PATH_ROOT = "gen/trace";
	public static final int DISPLAY_PROGRESS_MILESTONE = 1000;
	public static final int MAX_NUM_ENTRIES = 10000;
	
	private VirtualMachine vm;
	private PrintWriter output;
	private int entries = 0;
	private String mainClassName;
	
	
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
	
	public void setBreakPoints(List<Location> locations) {
		EventRequestManager evtRqManager = vm.eventRequestManager();
	}
	
	public void setBreakPointsAtEntry(List<Method> methods) {
		EventRequestManager evtRqManager = vm.eventRequestManager();
		for(Method m: methods) {
			MethodEntryRequest entryRequest = evtRqManager.createMethodEntryRequest();
			entryRequest.setSuspendPolicy(MethodEntryRequest.SUSPEND_ALL);
			entryRequest.enable();
		}
	}
	
	public void listenForMethodEntries() throws InterruptedException {
		output.println("-- Listening for method entries");
		
		EventRequestManager evtRqManager = vm.eventRequestManager();
		EventQueue evtQueue = vm.eventQueue();
		
		//Jump to main class
		EventSet toResume = jumpToMainClass();
		
		//Create event request once
		MethodEntryRequest entryRequest = evtRqManager.createMethodEntryRequest();
		entryRequest.setSuspendPolicy(MethodEntryRequest.SUSPEND_ALL);
		entryRequest.enable();
		
		toResume.resume();
		
		//Handle events as they come in
		while(true) {
			
			EventSet evtSet = evtQueue.remove();
			EventIterator evtIt = evtSet.eventIterator();
			
			while(evtIt.hasNext()) {
				Event event = evtIt.next();
				
				if(!(event instanceof MethodEntryEvent)) {
					if(event instanceof MethodExitEvent) {
						MethodExitEvent exitEvent = (MethodExitEvent) event;
						if(exitEvent.method().name().equals("main")) {
							//Signal end
							System.out.println("All method calls processed (" + entries + " calls)");
							vm.exit(0);
							return;
						}
					} else
						continue;
				}
				
				MethodEntryEvent entryEvent = (MethodEntryEvent)event;
				
				try {
					if(entryEvent.method().isStatic())
						output.println("[STATIC] " + entryEvent.method().declaringType().name() + " " + entryEvent.method().name());
					else {
						StackFrame frame;
						frame = entryEvent.thread().frame(0);
						output.println("[" + frame.thisObject().uniqueID() + "] " + entryEvent.method().declaringType().name() + " " + entryEvent.method().name());
					}
				} catch (Exception e) {
					output.println("[UNKNOWN] " + entryEvent.method().declaringType().name() + " " + entryEvent.method().name());
				}
				entries ++;
				
				if(entries % DISPLAY_PROGRESS_MILESTONE == 0) {
					System.out.println(entries + " entries recorded");
				}
				
				if(entries >= MAX_NUM_ENTRIES) {
					vm.exit(0);
					return;
				}
			}
			
			evtSet.resume();
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
				
				if(!entryEvent.method().name().equals("main")) {
					System.out.println("Skipped method " + entryEvent.method().name());
					continue;
				}
				
				//Signal success
				System.out.println("Jump to main class is complete.");
				output.println("[STATIC] " + entryEvent.method().declaringType().name() + " " + entryEvent.method().name());
				entries ++;
				
				//Kill event request
				initialEntryRequest.disable();
				
				//Add event request for when exiting main class
				MethodExitRequest finalExitRequest = evtRqManager.createMethodExitRequest();
				finalExitRequest.addClassFilter(mainClassName);
				finalExitRequest.setSuspendPolicy(MethodExitRequest.SUSPEND_ALL);
				finalExitRequest.enable();
				
				return evtSet;
			}
			
			evtSet.resume();
		}
	}
	
	private String chooseFileName() {
		int id = 0;
		
		while((new File(PATH_ROOT + id + ".tr")).exists()) {
			id ++;
		}
		
		return PATH_ROOT + id + ".tr";
	}
	
	private void initOutputFile() throws FileNotFoundException {
		String filename = chooseFileName();
		output = new PrintWriter(filename);
	}
	
	public void run(String[] args) {
		try {
			//Set some params
			mainClassName = args[1];
			
			//Connect to the VM
			connect(Integer.parseInt(args[0]));
			
			//Set output file
			initOutputFile();
			
			//Listen for events
			listenForMethodEntries();
			
		} catch (Exception e) {
			System.out.println("Trace failed: " + e.getMessage());
		}
		
		//Close file
		output.close();
	}
	
	//Launchpad
	public static final String CONNECTOR_NAME = "dt_socket";
	public static void main(String[] args) {
		(new Tracer()).run(args);
	}
}
