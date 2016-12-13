/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cb.java.injection.pojo;

import static io.cb.java.injection.Agent.DEBUG;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Christophe Blettry (blech)
 */
public class Context {

	private final List<Pojos> pojos = new ArrayList<>();
	private final ConcurrentHashMap<String, Object> singletonMap = new ConcurrentHashMap<>();

	private Context() {
	}

	public <T> T getSingleton(String id) throws PojoException {
		if (DEBUG) {
			System.out.println(Context.class.getName() + ".getSingleton: id:" + id);
		}
		return getResource(id, true);
	}

	public <T> T getResource(String id) throws PojoException {
		if (DEBUG) {
			System.out.println(Context.class.getName() + ".getResource: id:" + id);
		}
		return getResource(id, false);
	}

	public <T> T getResource(String id, boolean singleton) throws PojoException {
		if (DEBUG) {
			System.out.println(Context.class.getName() + ".getResource: id:" + id + ", singleton: " + singleton);
		}
		try {
			Pojo pojo = pojos.stream().filter(p -> p.getPojoById(id) != null).map(p -> p.getPojoById(id)).findFirst().orElseThrow(() -> new PojoNotFoundException(id));
			if (DEBUG) {
				System.out.println(Context.class.getName() + ".getResource: pojo:" + pojo);
			}
			Class classe = Class.forName(pojo.getClasse());
			if (DEBUG) {
				System.out.println(Context.class.getName() + ".getResource: classe:" + classe.getName());
			}
			return (T) getResource(pojo, classe, singleton);
		} catch (ClassNotFoundException ex) {
			throw new PojoException(ex);
		}
	}

	private <T> T getResource(Pojo pojo, Class<T> classe, boolean singleton) throws PojoException {
		if (singleton && singletonMap.containsKey(pojo.getId())) {
			return (T) singletonMap.get(pojo.getId());
		}
		if (DEBUG) {
			System.out.println(Context.class.getName() + ".getResource: classe: " + classe + ", singleton :" + singleton);
			System.out.println(Context.class.getName() + ".getResource: pojo: " + pojo);
			System.out.println(Context.class.getName() + ".getResource: pojo: properties: " + pojo.getProperties());
			System.out.println(Context.class.getName() + ".getResource: pojo: methods: " + pojo.getMethods());
		}
		try {
			if (DEBUG) {
				System.out.println(Context.class.getName() + ".getResource: before new instance: ");
			}
			T instance = classe.newInstance();
			if (DEBUG) {
				System.out.println(Context.class.getName() + ".getResource: after new instance: ");
			}
			if (pojo.getProperties() != null) {
				for (Property p : pojo.getProperties()) {
					if (DEBUG) {
						System.out.println(Context.class.getName() + ".getResource: loadProperty: " + p);
					}
					loadProperty(classe, p, instance);
				}
			}
			if (pojo.getMethods() != null) {
				for (Call call : pojo.getMethods()) {
					if (DEBUG) {
						System.out.println(Context.class.getName() + ".getResource: loadMethod: " + call);
					}
					loadMethod(classe, call, instance);
				}
			}
			if (singleton) {
				singletonMap.putIfAbsent(pojo.getId(), instance);
			}
			if (DEBUG) {
				System.out.println(Context.class.getName() + ".getResource: return instance " + instance);
			}
			return instance;
		} catch (NoSuchFieldException | SecurityException | InstantiationException | IllegalAccessException | NoSuchMethodException | IllegalArgumentException | InvocationTargetException ex) {
			if (DEBUG) {
				System.err.println(Context.class.getName() + ".getResource: failed for class " + classe.getName());
			}
			throw new PojoException(ex);
		}
	}

	private <T> void loadMethod(Class<T> classe, Call call, T instance) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Object o = null;
		if (call.getRef() == null && (call.getValue() != null && call.getPrimitive() != null)) {
			o = getPrimitiveValue(call.getValue(), call.getPrimitive());
		}
		if (call.getRef() != null) {
			o = getResource(call.getRef());
		}
		if (DEBUG) {
			System.out.println(Context.class.getName() + ".loadMethod: invoke with " + o);
		}
		Method method = classe.getDeclaredMethod(call.getName(), o == null ? null : o.getClass());
		if (DEBUG) {
			System.out.println(Context.class.getName() + ".loadMethod: method " + method + " exist");
		}
		method.invoke(instance, o);
		if (DEBUG) {
			System.out.println(Context.class.getName() + ".loadMethod: invoke for " + method + " done");
		}
	}

	private Object getPrimitiveValue(String value, String primitive) {
		if (primitive.equalsIgnoreCase("String")) {
			return value;
		}
		if (primitive.equalsIgnoreCase("int") || primitive.equalsIgnoreCase("integer")) {
			return Integer.parseInt(value);
		}
		if (primitive.equalsIgnoreCase("long")) {
			return Long.parseLong(value);
		}
		if (primitive.equalsIgnoreCase("boolean")) {
			return Boolean.parseBoolean(value);
		}
		if (primitive.equalsIgnoreCase("char") || primitive.equalsIgnoreCase("character")) {
			return value.isEmpty() ? Character.MIN_VALUE : value.charAt(0);
		}
		if (primitive.equalsIgnoreCase("double")) {
			return Double.parseDouble(value);
		}
		if (primitive.equalsIgnoreCase("float")) {
			return Float.parseFloat(value);
		}
		if (primitive.equalsIgnoreCase("short")) {
			return Short.parseShort(value);
		}
		if (primitive.equalsIgnoreCase("byte")) {
			return Byte.parseByte(value);
		}
		return null;
	}

	private void loadProperty(Class classe, Property p, Object instance) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		Field f = classe.getDeclaredField(p.getName());
		if (DEBUG) {
			System.out.println(Context.class.getName() + ".loadProperty: field: " + f.getName() + ", type :" + f.getType());
			System.out.println(Context.class.getName() + ".loadProperty: setter: " + getSetterName(f.getName()));
		}
		f.setAccessible(true);
		/*
				if (p.getRef() != null) {
						f.set(instance, getResource(p.getRef()));
						continue;
				}*/

		if (p.getRef() != null) {
			if (DEBUG) {
				System.out.println(Context.class.getName() + ".loadProperty: property ref " + p.getRef());
			}
			try {
				Method m = classe.getDeclaredMethod(getSetterName(f.getName()), f.getType());
				if (DEBUG) {
					System.out.println(Context.class.getName() + ".loadProperty: method for " + getSetterName(f.getName()) + " exist");
				}
				Object o = getResource(p.getRef());
				if (DEBUG) {
					System.out.println(Context.class.getName() + ".loadProperty: invoke for " + getSetterName(f.getName()) + " with " + o);
				}
				m.invoke(instance, o);
				if (DEBUG) {
					System.out.println(Context.class.getName() + ".loadProperty: invoke for " + getSetterName(f.getName()) + " done");
				}
			} catch (Exception ex) {
				if (DEBUG) {
					System.out.println(Context.class.getName() + ".loadProperty: invoke for " + f.getName() + " failed :" + ex);
				}
				f.set(instance, getResource(p.getRef()));
			}
			return;
		}
		//si privé on force l'accessibilité
		Type type = f.getType();
		if (type.equals(String.class)) {
			f.set(instance, p.getValue());
			return;
		}
		if (f.getType().isPrimitive()) {
			if (type.equals(Integer.TYPE)) {
				f.setInt(instance, Integer.parseInt(p.getValue()));
			}
			if (type.equals(Long.TYPE)) {
				f.setLong(instance, Long.parseLong(p.getValue()));
			}
			if (type.equals(Boolean.TYPE)) {
				f.setBoolean(instance, Boolean.parseBoolean(p.getValue()));
			}
			if (type.equals(Character.TYPE)) {
				f.setChar(instance, (p.getValue().isEmpty() ? Character.MIN_VALUE : p.getValue().charAt(0)));
			}
			if (type.equals(Double.TYPE)) {
				f.setDouble(instance, Double.parseDouble(p.getValue()));
			}
			if (type.equals(Float.TYPE)) {
				f.setFloat(instance, Float.parseFloat(p.getValue()));
			}
			if (type.equals(Short.TYPE)) {
				f.setShort(instance, Short.parseShort(p.getValue()));
			}
			if (type.equals(Byte.TYPE)) {
				f.setByte(instance, Byte.parseByte(p.getValue()));
			}
		}
	}

	public static Context loadResource(String... xml) {
		Context context = new Context();
		for (String s : xml) {
			File file = new File(s);
			if (file.exists()) {
				try {
					Pojos _pojos = Pojos.loadXml(new FileInputStream(file));
					context.addPojo(_pojos);
				} catch (FileNotFoundException ex) {
					Logger.getLogger(Context.class.getName()).log(Level.SEVERE, null, ex);
				}
			} else {
				InputStream is;
				if ((is = ClassLoader.getSystemResourceAsStream(s)) != null) {
					Pojos _pojos = Pojos.loadXml(is);
					context.addPojo(_pojos);
				}
			}
		}
		return context;
	}

	private void addPojo(Pojos _pojos) {
		for (Pojo _pojo : _pojos.getPojos()) {
			if (_pojo.getImportFile() != null) {
				loadResource(_pojo.getImportFile());
			}
		}
		pojos.add(_pojos);
	}

	private String getSetterName(String fieldName) {
		return "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
	}

	public void dump(PrintStream printer) {
		pojos.forEach(printer::println);
	}

}
