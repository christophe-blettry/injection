/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cb.java.injection.pojo;

import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author christophe
 */
@XmlRootElement(name="pojo")
public class Pojo {

	private String id;
	private String classe;
	private List<Property> properties;

	public Pojo() {
	}

	public String getId() {
		return id;
	}

	@XmlAttribute
	public void setId(String id) {
		this.id = id;
	}

	public String getClasse() {
		return classe;
	}

	@XmlAttribute(name="class")
	public void setClasse(String classe) {
		this.classe = classe;
	}

	public List<Property> getProperties() {
		return properties;
	}

	@XmlElement(name = "property")
	public void setProperties(List<Property> properties) {
		this.properties = properties;
	}

	@Override
	public String toString() {
		return "Pojo{" + "id=" + id + ", classe=" + classe + ", properties=" + properties + '}';
	}


}
