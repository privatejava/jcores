/*
 * JCoresScriptDevTime.java
 * 
 * Copyright (c) 2011, Ralf Biedert All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer. Redistributions in binary form must reproduce the
 * above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of the author nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */
package net.jcores.script.scriptmodes;

import static net.jcores.CoreKeeper.$;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import junit.data.Data;
import net.jcores.interfaces.functions.F0;
import net.jcores.script.JCoresScript;
import net.jcores.script.util.console.JCoresConsole;
import net.jcores.utils.internal.system.ProfileInformation;

/**
 * Development time scripting environment.
 * 
 * @author Ralf Biedert
 * @since 1.0
 */
public class JCoresScriptRuntime extends JCoresScript {

	/** Our console */
	private JCoresConsole consoleWindow = null;
	
	/** The banner to print */ 
	private String banner;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jcores.script.JCoresScript#pack()
	 */
	public JCoresScriptRuntime(String name, String[] args) {
		super(name, args);
		
		final ProfileInformation pi  = $.profileInformation();
		this.banner = this.name + " Console - jCores Script (" + pi.numCPUs + " CPUs)";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jcores.script.JCoresScript#pack()
	 */
	@Override
	public void pack() {
		// Check if we should create a console
		if (System.console() == null && this.console) {
			// In case we need a console, create it in the EDT
			$.edtnow(new F0() {
				@Override
				public void f() {
					initUI();
					consoleWindow = new JCoresConsole(banner);
				}
			});
		} 
	}
	
	/**
	 * Set the system user interface
	 */
	private void initUI() {
		// Whoever designed these APIs must have had an Exception fetish ...
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
	}
}
