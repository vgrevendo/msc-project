package testbench.programs.agent;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.Modifier;
import javassist.CtMethod;
import javassist.NotFoundException;

/**
 * Inside agent to remodel classes for inspection preparation.
 * @author vincent
 *
 */
public class Inspector implements ClassFileTransformer {
	private final Tracer tracer;
	private final Set<String> classesToInstrument;
	
	public Inspector(Tracer tracer) {
		this.tracer = tracer;
		this.classesToInstrument = translateClassNames(tracer.getClassesToInstrument());
	}

	@Override
	public byte[] transform(ClassLoader loader, String cl, Class<?> klass,
			ProtectionDomain pd, byte[] rawClass)
			throws IllegalClassFormatException {
		//To do better with subclassing issues
		if(!classesToInstrument.contains(cl))
			return rawClass;
		
		System.out.println("(i) Inspector is handling '" + cl + "' which is flagged for modification...");
		
		//Find methods to instrument
		Set<String> methods = tracer.getMethodsToInstrument(cl);
		
		//Instrument class		
		try {
			return instrumentClass(methods, cl, rawClass);
		} catch (Exception e) {
			System.out.println("(e) Could not instrument class, here's why:");
			e.printStackTrace();
		}
		
		return rawClass;
	}
	
	//Instrumentation
	private byte[] instrumentClass(Set<String> methods, String cl, byte[] rawCode) 
			throws IOException, RuntimeException, CannotCompileException, 
			       NotFoundException {
		//BUILD JAVAASSIST CLASS
		ClassPool ctp = ClassPool.getDefault();
		CtClass ctc = ctp.makeClass(new ByteArrayInputStream(rawCode));
		
		//Add a field for storing the reference to the tracer
		CtClass tracerClass = ctp.get("testbench.programs.agent.Tracer");
		CtField tracerField = new CtField(tracerClass, "_tracer", ctc);
		ctc.addField(tracerField, "Tracer.getTracer()");
		
		//Add a field to store the unique ID once it was received
		CtClass integerClass = ctp.get("java.lang.Integer");
		CtField idField = new CtField(integerClass, "_id", ctc);
		idField.setModifiers(Modifier.PUBLIC);
		ctc.addField(idField, "0");
		
		for(String method : methods) {
			CtMethod ctm = ctc.getDeclaredMethod(method);
			
			//Get return type
			CtClass returnType = ctm.getReturnType();
			
			//Add ID retrieval statement and tracer information operation
			if(returnType.isPrimitive())
				ctm.insertAfter("{"
								+ "if(_id <= 0) _id = _tracer.getId();"
								+ "_tracer.add(_id, " + cl + ","
													  + method + ","
													  + "$w.toString($_));"
								+ "}");
			else {
				ctm.insertAfter("{"
								+ "if(_id <= 0) _id = _tracer.getId();"
								+ "_tracer.add(_id, " + cl + ","
								+ method + ","
								+ "Integer.toString($_._id);"
						+ "}");
				
			}
		}
		
		//Return the modified class's code
		byte[] changedCode = ctc.toBytecode();
		ctc.detach();
		
		return changedCode;
	}

	//Tools
	private Set<String> translateClassNames(Set<String> classesToInstrument) {
		Set<String> translatedSet = new HashSet<>();
		
		for(String cl : classesToInstrument) {
			String newName = cl.replace('.','/');
			translatedSet.add(newName);
		}

		return translatedSet;
	}
}
