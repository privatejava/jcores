/*
 * JCoresScript.java
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
package net.jcores.script;

import net.jcores.script.scriptmodes.JCoresScriptDevtime;
import net.jcores.script.scriptmodes.JCoresScriptRuntime;

/**
 * JCores scripting allows you to quickly hack a small (script-like) Java / jCores application
 * and create a self contained JAR. Usually this is done with small jCores applications that
 * should be portable. To create a <i>script</i>, put these lines at the very beginning
 * of your application's main:<br/>
 * <br/>
 * 
 * <code>JCoresScript.SCRIPT("MyApp", args).pack()</code><br/>
 * <br/>
 * 
 * This will produce a file <code>MyApp.jar</code>, that is fully self contained and can be
 * started with <code>java -jar MyApp.jar</code>.<br/><br/>
 * 
 * <b>NOTE:</b> Scripting does NOT work properly with dynamic class loading. If you application
 * does any sort of classpath tweaks, or loads code on demand, packing the script will probably 
 * succeed, but running it will fail. 
 * 
 * @author Ralf Biedert
 * @since 1.0
 */
public abstract class JCoresScript {
    /** The name of this script */
    protected final String name;

    /** The args the application received at start */
    protected final String[] args;

    /** If set to true, a console will always be shown */
	protected boolean console = false;

    /**
     * We don't need this
     * 
     * @param args
     */
    protected JCoresScript(String name, String[] args) {
        this.name = name;
        this.args = args;

    }

 
    /**
     * Creates a script object for the current application. Call at the very first 
     * position of your <code>main()</code>.
     * 
     * @param name The name of your application.
     * @param args The command line arguments that were passed to main.
     * @return A {@link JCoresScript} object.
     */
    public static JCoresScript SCRIPT(String name, String args[]) {

        // If we have the flag the devtime created, go into runtime mode
        if (JCoresScript.class.getResource("jcores.script.mode") != null) { return new JCoresScriptRuntime(name, args); }

        return new JCoresScriptDevtime(name, args);
    }
    
    /**
     * Call this method if you want your script to always show a console. If a terminal
     * or console is detected at runtime nothing is being done. If no console is detected,
     * a console window will be spawned.
     * 
     * @param show If a console should be shown. Defaults to <code>false</code>.
     * @return This object.
     */
    public JCoresScript console(boolean show) {
    	this.console = show;
    	return this;
    }

    /**
     * Packs the current script into a single file ready for
     * deployment.
     */
    public abstract void pack();
}
