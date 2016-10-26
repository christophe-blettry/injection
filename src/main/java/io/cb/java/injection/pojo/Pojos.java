/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cb.java.injection.pojo;

import static io.cb.java.injection.Agent.DEBUG;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author christophe
 */
@XmlRootElement
public class Pojos {

	private List<Pojo> pojo;
	private Map<String, Pojo> mapById = new HashMap<>();

	public Pojos() {
	}

	public List<Pojo> getPojos() {
		return pojo;
	}

	@XmlElement(name = "pojo")
	public void setPojos(List<Pojo> bean) {
		this.pojo = bean;
	}

	public Pojo getPojoById(String id) {
		return mapById.get(id);
	}

	private Pojos mapPojos() {
		if (DEBUG) {
			System.out.println(this.getClass().getName() + ".mapPojos: " + pojo.size());
		}
		if (pojo != null) {
			mapById = pojo.stream().collect(Collectors.toMap(Pojo::getId, Function.identity()));
		}
		return this;
	}

	public static Pojos loadXml(InputStream xml) {
		Pojos p = JAXB.unmarshal(xml, Pojos.class);
		p.mapPojos();
		if (DEBUG) {
			System.out.println(Pojos.class.getName() + "loadXml: pojos: " + p);
		}
		return p;
	}

	@Override
	public String toString() {
		return "Pojos{" + "pojo=" + pojo + ", mapById=" + mapById + '}';
	}

}
