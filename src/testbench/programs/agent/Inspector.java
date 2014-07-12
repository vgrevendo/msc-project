package testbench.programs.agent;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import testbench.Tools;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.Modifier;
import javassist.CtMethod;
import javassist.NotFoundException;

/**
 * Inside agent to remodel classes for inspection preparation.
 * @author vincent
 *
 */
public class Inspector implements ClassFileTransformer {
	private final static String TRACER_PATH = "testbench.programs.agent.Tracer";
	
	//Ressources
	private final Tracer tracer;
	private final Set<Class<?>> superclasses;
	private final Set<CtClass> superctclasses;
	
	//Instrumentation monitoring
	private final Map<Class<?>, Set<String>> instrumentedMethods = new HashMap<Class<?>, Set<String>>(); 
	
	public Inspector(Tracer tracer) {
		this.tracer = tracer;
		this.superclasses = tracer.getSuperclasses();
		this.superctclasses = new HashSet<>();
		
		for(Class<?> c:superclasses) {
			try {
				CtClass ctc = ClassPool.getDefault().get(c.getName());
				superctclasses.add(ctc);
				if(ctc == null)
					System.out.println("(w) On inspector build, could not load: " + c.getName());
			} catch (Exception e) {
				System.out.println("(w) On inspector build, could not load: " + c.getName());
			}
		}
		
		System.out.println("(i) Added " + superclasses.size() + " CTC classes to Inspector's collection");
		System.out.println("(i) Inspector is online.");
	}

	@Override
	public byte[] transform(ClassLoader loader, String cl, Class<?> klass,
			ProtectionDomain pd, byte[] rawClass)
			throws IllegalClassFormatException {
		if(tracer.isMainClass(cl))
			try {
				return instrumentMainClass(cl, klass, rawClass);
			} catch (IOException | RuntimeException | NotFoundException
					| CannotCompileException | Error e1) {
				System.out.println("(e) Could not instrument main class for tracer hooks:");
				e1.printStackTrace();
				return rawClass;
			}
		
		//To do better with subclassing issues
		String superclassName = isInterestingClass(klass);
		if(superclassName == null)
			return rawClass;
		
		if(superclassName.isEmpty()) {
			//In case of first-time loading
			try {
				return instrumentLoadingClass(cl, rawClass);
			} catch (Throwable e) {
				e.printStackTrace();
				return rawClass;
			}
		} else {
			//In case of retransformation
			System.out.println("(i) Inspector: '" + cl + "' for superclass '" + superclassName + "':");
			if(klass.isInterface()) {
				System.out.println("    This class is an interface, ignored.");
				return rawClass;
			}
			//Find methods to instrument
			Set<String> methods = tracer.getMethodsToInstrument(superclassName);
			try {
				//Instrument class		
				return instrumentClass(methods, cl, klass, rawClass);
			} catch (Exception | Error e) {
				System.out.println("(e) Could not instrument class, here's why:");
				e.printStackTrace();
				return rawClass;
			}
		}
	}
	
	//Instrumentation
	private byte[] instrumentLoadingClass(String cl, byte[] rawCode) 
			throws IOException, RuntimeException, NotFoundException, CannotCompileException {
		int constrCount = 0, methodCount = 0, 
				alreadyInstrumented = 0, empty = 0, notfound = 0, natMethods = 0;

		//BUILD JAVAASSIST CLASS
		ClassPool ctp = ClassPool.getDefault();
		CtClass ctc = ctp.makeClass(new ByteArrayInputStream(rawCode));
		
		String superclassName = isInterestingClass(ctc); 
		if(superclassName == null) {
			return rawCode;
		}
		
		Tools.println("Inspector: '" + ctc.getName() + "' is being loaded.", Tools.ANSI_GREEN);
		
		Set<String> methods = tracer.getMethodsToInstrument(superclassName);
		
		for(String method : methods) {
			if(method.equals("<init>")) { //Modify the constructor instead!
				CtConstructor[] constructors = ctc.getConstructors();
				
				if(constructors.length == 0) {
					continue;
				}
				
				for(CtConstructor ctCons : constructors) {
					ctCons.insertAfter("{"
							+ TRACER_PATH + ".getTracer().add(this, \"" + cl + "\",\""
							+ method + "\","
							+ "\"null\");"
							+ "}");
				}
				
				constrCount ++;
			} else {
				CtMethod ctm;
				try {
					ctm = ctc.getDeclaredMethod(method);
					if(Modifier.isNative(ctm.getModifiers())) {
						natMethods++;
						continue;
					}
					
					if(ctm.isEmpty()) {
						empty++;
						continue;
					}
					
					//Get return type
					CtClass returnType = ctm.getReturnType();
					//Add ID retrieval statement and tracer information operation
					if(returnType.isPrimitive()) {
						if(returnType.getName().contains("Void") 
								|| returnType.getName().contains("void"))
							ctm.insertAfter("{"
								+ TRACER_PATH + ".getTracer().getTracer().add(this, \"" + cl + "\",\""
								+ method + "\",\"void\");"
								+ "}");
						else
							ctm.insertAfter("{"
								+ TRACER_PATH + ".getTracer().getTracer().add(this, \"" + cl + "\",\""
								+ method + "\","
								+ getWrapperName(returnType) + ".toString($_));"
								+ "}");
							
					}
					else {
						ctm.insertAfter("{"
								+ TRACER_PATH + ".getTracer().getTracer().addObjectRV(this, \"" + cl + "\",\""
									+ method + "\",$_);"
								+ "}");
					}
					
					methodCount++;
				} catch (NotFoundException e) {
					notfound++;
				}
				
			}
			
			
		}
		
		//Return the modified class's code
		byte[] changedCode = ctc.toBytecode();
		ctc.detach();
		Tools.println("    constructors: " + constrCount + ", methods: " + methodCount + 
				             ", already handled: " + alreadyInstrumented + ", not found:" + notfound + 
				             ", empty: " + empty + ", native: " + natMethods, Tools.ANSI_GREEN);
		
		return changedCode;
	}
	private byte[] instrumentClass(Set<String> methods, String cl, Class<?> klass, byte[] rawCode) 
			throws IOException, RuntimeException, CannotCompileException, 
			       NotFoundException {
		int constrCount = 0, methodCount = 0, alreadyInstrumented = 0;
		
		//BUILD JAVAASSIST CLASS
		ClassPool ctp = ClassPool.getDefault();
		CtClass ctc = ctp.makeClass(new ByteArrayInputStream(rawCode));
		
		for(String method : methods) {
			if(instrumentedMethods.containsKey(klass) && instrumentedMethods.get(klass).contains(method)) {
				alreadyInstrumented++;
				continue;
			}
			
			if(method.equals("<init>")) { //Modify the constructor instead!
				CtConstructor[] constructors = ctc.getConstructors();
				
				if(constructors.length == 0) {
					System.out.println("    (w) No constructors found in this class!");
					continue;
				}
				
				for(CtConstructor ctCons : constructors) {
					ctCons.insertAfter("{"
							+ TRACER_PATH + ".getTracer().add(this, \"" + cl + "\",\""
							+ method + "\","
							+ "\"null\");"
							+ "}");
				}
				
				constrCount ++;
				setClassMethodInstrumented(klass, method);
			} else {
				CtMethod ctm;
				try {
					ctm = ctc.getDeclaredMethod(method);
					if(Modifier.isNative(ctm.getModifiers())) {
						System.out.println("    (w) Could not instrument " + ctm.getName() + ": this method is native!");
						continue;
					}
					
					if(ctm.isEmpty()) {
						System.out.println("    '" + method + "' is emtpy in this class, will instrument superclass.");
						Class<?> superclass = klass.getSuperclass();
						Agent.addToRetransform(superclass);
						continue;
					}
					
					//Get return type
					CtClass returnType = ctm.getReturnType();
					//Add ID retrieval statement and tracer information operation
					if(returnType.isPrimitive())
						ctm.insertAfter("{"
								+ TRACER_PATH + ".getTracer().getTracer().add(this, \"" + cl + "\",\""
								+ method + "\","
								+ getWrapperName(returnType) + ".toString($_));"
								+ "}");
					else {
						ctm.insertAfter("{"
								+ TRACER_PATH + ".getTracer().getTracer().addObjectRV(this, \"" + cl + "\",\""
									+ method + "\",$_);"
								+ "}");
					}
					
					methodCount++;
					setClassMethodInstrumented(klass, method);
				} catch (NotFoundException e) {
					System.out.println("    Could not find '" + method + "' in this class, will instrument superclass.");
					
					Class<?> superclass = klass.getSuperclass();
					Agent.addToRetransform(superclass);
				}
				
			}
			
			
		}
		
		//Return the modified class's code
		byte[] changedCode = ctc.toBytecode();
		ctc.detach();
		System.out.println("    " + constrCount + " constructors, " + methodCount + " methods instrumented, " + alreadyInstrumented + " already instrumented.");
		
		return changedCode;
	}
	private byte[] instrumentMainClass(String cl, Class<?> klass, byte[] rawCode) 
			throws IOException, RuntimeException, NotFoundException, CannotCompileException {
		System.out.println("(i) Inspector is instrumenting main class... ");
		
		//BUILD JAVAASSIST CLASS
		ClassPool ctp = ClassPool.getDefault();
		CtClass ctc = ctp.makeClass(new ByteArrayInputStream(rawCode));
		
		
		//Instrument start method
		CtMethod ctm = ctc.getDeclaredMethod(tracer.getStartMethodName());
		ctm.insertBefore("{ " + TRACER_PATH + ".getTracer().onTraceStart(); }");
		
		//Instrument end method
		CtMethod ctm2 = ctc.getDeclaredMethod(tracer.getEndMethodName());
		ctm2.insertAfter("{ testbench.programs.agent.Tracer.getTracer().onTraceStop(); }");
		
		//Return the modified class's code
		byte[] changedCode = ctc.toBytecode();
		ctc.detach();

		return changedCode;
	}

	//TOOLS
	private String getWrapperName(CtClass type) {
		if(!type.isPrimitive())
			return type.getName();
		
		String name = type.getName();
		return name.substring(0, 1).toUpperCase() + name.substring(1);
			
	}
	private void setClassMethodInstrumented(Class<?> klass, String method) {
		if(!instrumentedMethods.containsKey(klass)) {
			instrumentedMethods.put(klass, new HashSet<String>());
		}
		
		instrumentedMethods.get(klass).add(method);
	}
	private String isInterestingClass(Class<?> klass) {
		//If no class is being loaded (and not retransformed), the argument is null, and therefore it
		// has to be interpreted with Javassist to know which superclasses it has.
		if(klass == null)
			return ""; 
		
		for(Class<?> c: superclasses) {
			if(c.isAssignableFrom(klass))
				return c.getName();
		}
		
		return null;
	}
	private String isInterestingClass(CtClass ctc) throws NotFoundException {
		for(CtClass ctc2 : superctclasses) {
			try {
				if(ctc.subtypeOf(ctc2))
					return ctc2.getName();
			} catch(Throwable e) {
				
			}
		}
		
		return null;
	}
}
