/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.microfass.java.injection;

import com.sun.tools.attach.VirtualMachine;
import static net.microfass.java.injection.Agent.DEBUG;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.net.URLClassLoader;

/**
 *
 * @author Christophe Blettry (blech)
 */
public class InitInstrument {

	public static void loadAgent(String agentArguments) {
		String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
		int p = nameOfRunningVM.indexOf('@');
		String pid = nameOfRunningVM.substring(0, p);

		try {
			String jarFilePath = getInstrumentJar();
			VirtualMachine vm = VirtualMachine.attach(pid);
			vm.loadAgent(jarFilePath, agentArguments);
			vm.detach();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static String getInstrumentJar() throws IOException {
		ClassLoader cl = ClassLoader.getSystemClassLoader();
		URL[] urls = ((URLClassLoader) cl).getURLs();
		for (URL url : urls) {
			if (DEBUG) {
				System.out.println(InitInstrument.class.getName() + ".getInstrumentJar: url: " + url.getFile());
			}
			if (url.toString().contains("injection") && url.toString().endsWith(".jar")) {
				if (DEBUG) {
					System.out.println(InitInstrument.class.getName() + ".getInstrumentJar: jarFile: " + url.getFile());
				}
				File file = new File(url.getFile());
				if (file.exists()) {
					if (DEBUG) {
						System.out.println(InitInstrument.class.getName() + ".getInstrumentJar: found file: " + file.getAbsolutePath());
					}
					return file.getAbsolutePath();
				}
			}
		}
		throw new FileNotFoundException();
	}

}
