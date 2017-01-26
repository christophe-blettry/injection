/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cb.java.injection;

/**
 *
 * @author christophe
 */
public class InjectionException extends Exception {

	public InjectionException() {
	}

	public InjectionException(String message) {
		super(message);
	}

	public InjectionException(String message, Throwable cause) {
		super(message, cause);
	}

	public InjectionException(Throwable cause) {
		super(cause);
	}

	
}
