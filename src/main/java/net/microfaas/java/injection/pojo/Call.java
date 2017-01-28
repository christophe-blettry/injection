/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.microfaas.java.injection.pojo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author christophe
 */
@XmlRootElement
public class Call {

	private String name;
	private String ref;
	private String value;
	private String primitive;

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

	public String getPrimitive() {
		return primitive;
	}

	@XmlAttribute
	public void setPrimitive(String primitive) {
		this.primitive = primitive;
	}

	@Override
	public String toString() {
		return "Call{" + "name=" + name + ", ref=" + ref + ", value=" + value + ", primitive=" + primitive + '}';
	}

}
