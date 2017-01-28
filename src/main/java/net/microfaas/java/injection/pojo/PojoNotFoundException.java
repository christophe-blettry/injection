/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.microfaas.java.injection.pojo;

/**
 *
 * @author Christophe Blettry (blech)
 */
public class PojoNotFoundException extends PojoException {

	public PojoNotFoundException() {
	}

	public PojoNotFoundException(String message) {
		super(message);
	}

	public PojoNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public PojoNotFoundException(Throwable cause) {
		super(cause);
	}

}
