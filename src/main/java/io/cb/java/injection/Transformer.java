/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cb.java.injection;

import static io.cb.java.injection.Agent.DEBUG;
import io.cb.java.injection.pojo.Context;
import io.cb.java.injection.pojo.Inject;
import io.cb.java.injection.pojo.Named;
import io.cb.java.injection.pojo.Singleton;
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
		try {
			String className = fullyQualifiedClassName.replaceAll("/", "\\.");
			if (DEBUG) {
				System.out.println(this.getClass().getName() + ".transform: className: " + className);
			}
			boolean transform = false;
			boolean singleton = false;
			ctClass = ClassPool.getDefault().get(className);
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
					System.out.println(this.getClass().getName() + ".transform: name: " + name);
				}
				if (ctField.hasAnnotation(Named.class)) {
					Named n = (Named) ctField.getAnnotation(Named.class);
					name = n.value();
					if (DEBUG) {
						System.out.println(this.getClass().getName() + ".transform: named: name: " + name);
					}
				}
				CtConstructor[] constructors = ctClass.getDeclaredConstructors();
				if (DEBUG) {
					System.out.println(this.getClass().getName() + ".transform: constructors: " + constructors.length);
				}
				for (CtConstructor constructor : constructors) {
					String line = "this." + ctField.getName() + "= (" + ctField.getType().getName()
							+ ")" + Context.class.getName() + "." + (singleton ? "getSingleton" : "getResource") + "(\"" + name + "\");";
					if (DEBUG) {
						System.out.println(this.getClass().getName() + ".transform: code line: " + line);
					}
					try {
						constructor.insertAfter(line);
					} catch (Exception ex) {
						System.err.println("transform: CannotCompileException: " + ex);
					}
				}
				transform = true;
			}
			if (transform) {
				if (DEBUG) {
					System.out.println(this.getClass().getName() + ".transform: transformed className: " + className);
				}
				try {
					byteCode = ctClass.toBytecode();
				} catch (CannotCompileException ex) {
					Logger.getLogger(Transformer.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		} catch (NotFoundException | ClassNotFoundException | IOException ex) {
			if (DEBUG) {
				System.err.println(ex);
			}
		} finally {
			if (ctClass != null) {
				ctClass.detach();
			}
		}
		return byteCode;
	}

}
