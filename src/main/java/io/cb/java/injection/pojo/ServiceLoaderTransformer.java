/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cb.java.injection.pojo;

import static io.cb.java.injection.Agent.DEBUG;
import io.cb.java.injection.InjectionException;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.NotFoundException;
import javassist.bytecode.ClassFile;
import javassist.bytecode.Descriptor;

/**
 *
 * @author christophe
 */
public class ServiceLoaderTransformer {

	Map<String, byte[]> modifiedByteClasses = new HashMap<>();

	public ServiceLoaderTransformer() {
	}

	public Map<String, byte[]> getModifiedByteClasses() {
		return modifiedByteClasses;
	}

	public void transform(Context context, Map<String, byte[]> loadedByteClasses) throws InjectionException {
		for (Map.Entry<String, byte[]> e : loadedByteClasses.entrySet()) {
			try {
				ClassFile cf = new ClassFile(new DataInputStream(new ByteArrayInputStream(e.getValue())));
				CtClass ctClass = ClassPool.getDefault().makeClass(cf);
				transform(context, e.getKey(), ctClass, loadedByteClasses);
				ctClass.detach();
			} catch (IOException | ClassNotFoundException | NotFoundException ex) {
				throw new InjectionException(ex);
			}
		}
	}

	private void transform(Context _context, String className, CtClass ctClass, Map<String, byte[]> loadedByteClasses) throws ClassNotFoundException, NotFoundException, IOException {
		if (modifiedByteClasses.containsKey(className)) {
			return;
		}
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
				System.out.println(ServiceLoaderTransformer.class.getName() + ".transform: name: " + name);
			}
			if (ctField.hasAnnotation(Named.class)) {
				Named n = (Named) ctField.getAnnotation(Named.class);
				name = n.value();
				if (DEBUG) {
					System.out.println(ServiceLoaderTransformer.class.getName() + ".transform: named: name: " + name);
				}
			}
			CtConstructor[] constructors = ctClass.getDeclaredConstructors();
			if (DEBUG) {
				System.out.println(ServiceLoaderTransformer.class.getName() + ".transform: constructors: " + constructors.length);
			}
			for (CtConstructor constructor : constructors) {
				/*
				try {
					if (singleton) {
						_context.getSingleton(name);
					} else {
						_context.getResource(name);
					}
				} catch (PojoException ex) {
					System.err.println("transform: className: " + className + ": CannotCompileException: getResource or getSingleton failed" + ex);
					continue;
				}*/

				String line = null;
				if (DEBUG) {
					System.out.println(ServiceLoaderTransformer.class.getName() + ".transform: try to write line of code");
				}
				try {
					String typeName = ctField.getType().getName();
				} catch (NotFoundException ex) {
					//class not already loaded
					String classToLoad = Descriptor.toClassName(ctField.getFieldInfo().getDescriptor());
					if (DEBUG) {
						System.out.println(ServiceLoaderTransformer.class.getName() + ".transform: classToLoad: " + classToLoad);
					}
					if (loadedByteClasses.containsKey(classToLoad)) {
						ClassFile cf = new ClassFile(new DataInputStream(new ByteArrayInputStream(loadedByteClasses.get(classToLoad))));
						CtClass _ctClass = ClassPool.getDefault().makeClass(cf);
						transform(_context, classToLoad, _ctClass, loadedByteClasses);
					}
				}

				try {
					line = "this." + ctField.getName() + "= (" + ctField.getType().getName()
							+ ")" + Context.class.getName()
							+ "." + "getContext(\"" + _context.getId() + "\")"
							+ "." + (singleton ? "getSingleton" : "getResource") + "(\"" + name + "\");";
					if (DEBUG) {
						System.out.println(ServiceLoaderTransformer.class.getName() + ".transform: code line: " + line);
					}
					constructor.insertAfter(line);
				} catch (Exception ex) {
					System.err.println("ERR "+ServiceLoaderTransformer.class.getName() +".transform: className: " + className + ": CannotCompileException: line of code : " + line + ", " + ex);
				}
				/*
				try {
					if (singleton) {
						_context.getSingleton(name);
					} else {
						_context.getResource(name);
					}
				} catch (PojoException ex) {
					System.err.println("transform: className: " + className + ": getResource or getSingleton failed" + ex);
					continue;
				}*/
			}
			transform = true;
		}
		if (transform) {
			if (DEBUG) {
				System.out.println(ServiceLoaderTransformer.class.getName() + ".transform: transformed className: " + className);
			}
			try {
				byteCode = ctClass.toBytecode();
			} catch (CannotCompileException ex) {
				System.err.println("ERR "+ServiceLoaderTransformer.class.getName() + ".transform: className: " + className+": toByteCode failed: "+ex);
			}
		} else {
			if (DEBUG) {
				System.out.println(ServiceLoaderTransformer.class.getName() + ".transform: className: " + className + " not transformed");
			}
		}
		if (byteCode != null) {
			modifiedByteClasses.put(className, byteCode);
		}
	}

}
