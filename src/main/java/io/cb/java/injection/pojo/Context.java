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
import java.lang.reflect.Field;
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

	private static final List<Pojos> pojos = new ArrayList<>();
	private static final ConcurrentHashMap<String, Object> singletonMap = new ConcurrentHashMap<>();

	public static <T> T getSingleton(String id) throws PojoException {
		return getResource(id, true);
	}

	public static <T> T getResource(String id) throws PojoException {
		return getResource(id, false);
	}

	public static <T> T getResource(String id, boolean singleton) throws PojoException {
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
			throw new PojoException();
		}
	}

	private static <T> T getResource(Pojo pojo, Class<T> classe, boolean singleton) throws PojoException {
		if (singleton && singletonMap.containsKey(pojo.getId())) {
			return (T) singletonMap.get(pojo.getId());
		}
		try {
			T instance = classe.newInstance();
			for (Property p : pojo.getProperties()) {
				Field f = classe.getDeclaredField(p.getName());
				//si privé on force l'accessibilité
				f.setAccessible(true);
				if (p.getRef() != null) {
					f.set(instance, getResource(p.getRef()));
					continue;
				}
				Type type = f.getType();
				if (type.equals(String.class)) {
					f.set(instance, p.getValue());
					continue;
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
				}
			}
			if (singleton) {
				singletonMap.putIfAbsent(pojo.getId(), instance);
			}
			return instance;
		} catch (NoSuchFieldException | SecurityException | InstantiationException | IllegalAccessException ex) {
			throw new PojoException(ex);
		}
	}

	private Context() {
	}

	public static boolean loadResource(String... xml) {
		boolean loaded = false;
		for (String s : xml) {
			File file = new File(s);
			if (file.exists()) {
				try {
					pojos.add(Pojos.loadXml(new FileInputStream(file)));
					loaded = true;
				} catch (FileNotFoundException ex) {
					Logger.getLogger(Context.class.getName()).log(Level.SEVERE, null, ex);
				}
			} else {
				InputStream is;
				if ((is = ClassLoader.getSystemResourceAsStream(s)) != null) {
					pojos.add(Pojos.loadXml(ClassLoader.getSystemResourceAsStream(s)));
					loaded = true;
				}
			}
		}
		return loaded;
	}

}
