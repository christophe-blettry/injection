/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.microfaas.java.injection;

import static net.microfaas.java.injection.Agent.DEBUG;
import net.microfaas.java.injection.pojo.Context;
import net.microfaas.java.injection.pojo.Inject;
import net.microfaas.java.injection.pojo.Named;
import net.microfaas.java.injection.pojo.PojoException;
import net.microfaas.java.injection.pojo.Singleton;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.NotFoundException;

/**
 *
 * @author Christophe Blettry (blech)
 */
public class Transformer implements ClassFileTransformer {

	public static final HashMap<String, byte[]> transformMap = new HashMap<>();
	public static final List<String> ignoredPackages = Arrays.asList("javax/", "java/", "sun/", "com/sun/");

	private Context context;

	private final String agentArguments;

	public Transformer(String agentArguments) {
		this.agentArguments = agentArguments;
		if (this.agentArguments != null && (this.agentArguments.matches(".*-debug\\s+") || this.agentArguments.matches(".*-debug$"))) {
			Agent.DEBUG = true;
		}
		if (DEBUG) {
			System.out.println(this.getClass().getName() + ".<init>: agentArguments" + this.agentArguments);
		}
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	private boolean skipClass(String className) {
		if (ignoredPackages.stream().anyMatch((name) -> (className.startsWith(name)))) {
			return true;
		}
		return false;
	}

	@Override
	public byte[] transform(ClassLoader loader, String fullyQualifiedClassName, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		if (skipClass(fullyQualifiedClassName)) {
			return null;
		}
		CtClass ctClass = null;
		byte[] byteCode = null;
		String className = fullyQualifiedClassName.replaceAll("/", "\\.");
		if (DEBUG) {
			System.out.println(this.getClass().getName() + ".transform: className: " + className);
		}
		try {
			ctClass = ClassPool.getDefault().get(className);
			byteCode = transformClass(context, className, ctClass);
		} catch (NotFoundException | ClassNotFoundException | IOException ex) {
			if (DEBUG) {
				System.err.println("ERR "+this.getClass().getName() + ".transform: className: " + className + ": " + ex);
			}
		} finally {
			if (ctClass != null) {
				ctClass.detach();
			}
		}
		return byteCode;
	}

	private byte[] transformClass(Context _context, String className, CtClass ctClass) throws ClassNotFoundException, NotFoundException, IOException {
		boolean transform = false;
		boolean singleton = false;
		byte[] byteCode = null;
		CtField[] ctFields = ctClass.getDeclaredFields();
		for (CtField ctField : ctFields) {
			if (!(ctField.hasAnnotation(Inject.class) || ctField.hasAnnotation(Singleton.class))) {
				continue;
			}
			if (ctField.hasAnnotation(Singleton.class)) {
				singleton = true;
			}
			String name = ctField.getName();
			if (DEBUG) {
				System.out.println(Transformer.class.getName() + ".transformClass: name: " + name);
			}
			if (ctField.hasAnnotation(Named.class)) {
				Named n = (Named) ctField.getAnnotation(Named.class);
				name = n.value();
				if (DEBUG) {
					System.out.println(Transformer.class.getName() + ".transformClass: named: name: " + name);
				}
			}
			CtConstructor[] constructors = ctClass.getDeclaredConstructors();
			if (DEBUG) {
				System.out.println(Transformer.class.getName() + ".transformClass: constructors: " + constructors.length);
			}
			for (CtConstructor constructor : constructors) {
				
				try {
					if (singleton) {
						_context.getSingleton(name);
					} else {
						_context.getResource(name);
					}
				} catch (PojoException ex) {
					System.err.println("ERR "+Transformer.class.getName() +"transformClass: className: " + className+": CannotCompileException: getResource or getSingleton failed" + ex);
					continue;
				}
				String line = null;
				if (DEBUG) {
					System.out.println(Transformer.class.getName() + ".transformClass: try to write line of code");
				}
				try {
					line = "this." + ctField.getName() + "= (" + ctField.getType().getName()
							+ ")" + Context.class.getName()
							+ "." + "getContext(\"" + _context.getId() + "\")"
							+ "." + (singleton ? "getSingleton" : "getResource") + "(\"" + name + "\");";
					if (DEBUG) {
						System.out.println(Transformer.class.getName() + ".transformClass: code line: " + line);
					}
					constructor.insertAfter(line);
				} catch (Exception ex) {
					System.err.println("ERR "+Transformer.class.getName() +"transformClass: className: " + className + ": CannotCompileException: line of code : " + line + ", " + ex);
				}
			}
			transform = true;
		}
		if (transform) {
			if (DEBUG) {
				System.out.println(Transformer.class.getName() + ".transformClass: transformed className: " + className);
			}
			try {
				byteCode = ctClass.toBytecode();
			} catch (CannotCompileException ex) {
				Logger.getLogger(Transformer.class.getName()).log(Level.SEVERE, null, ex);
			}
		} else {
			if (DEBUG) {
				System.out.println(Transformer.class.getName() + ".transformClass: className: " + className + " not transformed");
			}
		}
		return byteCode;

	}

}
