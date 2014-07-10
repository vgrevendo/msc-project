package testbench.programs.agent;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Set;

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
	
	private final Tracer tracer;
	
	public Inspector(Tracer tracer) {
		this.tracer = tracer;
		
		System.out.println("(i) Inspector is online.");
	}

	@Override
	public byte[] transform(ClassLoader loader, String cl, Class<?> klass,
			ProtectionDomain pd, byte[] rawClass)
			throws IllegalClassFormatException {
		if(tracer.isMainClass(cl))
			try {
				return instrumentMainClass(cl, rawClass);
			} catch (IOException | RuntimeException | NotFoundException
					| CannotCompileException | Error e1) {
				System.out.println("(e) Could not instrument main class for tracer hooks:");
				e1.printStackTrace();
				return rawClass;
			}
		
		//To do better with subclassing issues
		String superclassName = tracer.isInterestingSubclass(klass);
		if(superclassName == null)
			return rawClass;
		
		System.out.println("(i) Inspector: '" + cl + "' for superclass '" + superclassName + "':");
		
		//Find methods to instrument
		Set<String> methods = tracer.getMethodsToInstrument(superclassName);
		
		//Instrument class		
		try {
			return instrumentClass(methods, cl, rawClass);
		} catch (Exception | Error e) {
			System.out.println("(e) Could not instrument class, here's why:");
			e.printStackTrace();
			return rawClass;
		}
	}
	
	//Instrumentation
	private byte[] instrumentClass(Set<String> methods, String cl, byte[] rawCode) 
			throws IOException, RuntimeException, CannotCompileException, 
			       NotFoundException {
		int constrCount = 0, methodCount = 0;
		
		//BUILD JAVAASSIST CLASS
		ClassPool ctp = ClassPool.getDefault();
		CtClass ctc = ctp.makeClass(new ByteArrayInputStream(rawCode));
		
		for(String method : methods) {
			if(method.equals("<init>")) { //Modify the constructor instead!
				CtConstructor[] constructors = ctc.getConstructors();
				
				if(constructors.length == 0) {
					System.out.println("    (w) No constructors found in this class!");
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
					if(Modifier.isNative(ctm.getModifiers()) || ctm.isEmpty()) {
						System.out.println("    (w) Could not instrument " + ctm.getName() + ": this method has no body!");
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
				} catch (NotFoundException e) {
					System.out.println("    (w) Could not find '" + method + "' in this class, skipping");
				}
				
			}
			
			
		}
		
		//Return the modified class's code
		byte[] changedCode = ctc.toBytecode();
		ctc.detach();
		System.out.println("    " + constrCount + " constructors, " + methodCount + " methods instrumented.");
		
		return changedCode;
	}
	private byte[] instrumentMainClass(String cl, byte[] rawCode) 
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
}
