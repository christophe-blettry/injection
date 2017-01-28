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
public class PojoException extends RuntimeException  {

	public PojoException() {
	}

	public PojoException(String message) {
		super(message);
	}

	public PojoException(String message, Throwable cause) {
		super(message, cause);
	}

	public PojoException(Throwable cause) {
		super(cause);
	}

}
