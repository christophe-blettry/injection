/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cb.java.injection;

import java.lang.instrument.Instrumentation;

/**
 *
 * @author Christophe Blettry (blech)
 */
public class Agent {

	private static Instrumentation instrumentation;
	public static boolean DEBUG = false;

	public static void agentmain(String agentArguments, Instrumentation instr) {
		instrumentation = instr;
		instrumentation.addTransformer(new Transformer(agentArguments));
		if(DEBUG) System.out.println(Agent.class.getName()+".agentmain");
	}

	public static void premain(String agentArguments, Instrumentation instr) {
		instrumentation = instr;
		instrumentation.addTransformer(new Transformer(agentArguments));
		if(DEBUG) System.out.println(Agent.class.getName()+".premain");
	}

	public static void initialize(String agentArguments) {
		if (instrumentation == null) {
			InitInstrument.loadAgent(agentArguments);
		}
	}
}