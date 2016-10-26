/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cb.java.injection.pojo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author christophe
 */
@XmlRootElement
public class Property {

	private String name;
	private String ref;
	private String value;

	public String getName() {
		return name;
	}

	@XmlAttribute
	public void setName(String name) {
		this.name = name;
	}

	public String getRef() {
		return ref;
	}

	@XmlAttribute
	public void setRef(String ref) {
		this.ref = ref;
	}

	public String getValue() {
		return value;
	}

	@XmlAttribute
	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "Property{" + "name=" + name + ", ref=" + ref + ", value=" + value + '}';
	}


	
}
