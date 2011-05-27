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

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.jcores.script.JCoresScript;

/**
 * Development time scripting environment.
 * 
 * @author Ralf Biedert
 * @since 1.0
 */
public class JCoresScriptDevtime extends JCoresScript {

    /*
     * (non-Javadoc)
     * 
     * @see net.jcores.script.JCoresScript#pack()
     */
    public JCoresScriptDevtime(String name, String[] args) {
        super(name, args);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.jcores.script.JCoresScript#pack()
     */
    @Override
    public void pack() {
        final ClassLoader systemloader = ClassLoader.getSystemClassLoader().getParent();
        URLClassLoader loader = $(getClass().getClassLoader()).get(URLClassLoader.class, null);

        // Check the current loader
        if (loader == null) {
            System.err.println("Unable to get the classpath for this script. Cannot pack. Sorry.");
            return;
        }

        // Get all URLs
        List<URL> urls = new ArrayList<URL>();
        while (loader != null && loader != systemloader) {
            urls.addAll(Arrays.asList(loader.getURLs()));
            loader = $(loader.getParent()).get(URLClassLoader.class, null);
        }

        // Now, go through all elements, when it's a JAR, unpack it, when its a dir, copy it
        final File tempdir = $.tempdir();
        for (URL url : urls) {
            final File file = $(url).file().get(0);

            if (file.getAbsolutePath().endsWith("jar")) {
                $(file).input().zipstream().unzip(tempdir.getAbsolutePath());
            } else {
                $(file).copy(tempdir.getAbsolutePath());
            }
        }

        // Purge old manifest
        $(tempdir.getAbsoluteFile() + "/META-INF/").file().delete();
        $(tempdir.getAbsoluteFile() + "/META-INF/").file().get(0).mkdirs();
        
        // And create the new one 
        final String appmain  = $(Thread.currentThread().getStackTrace()).get(-1).getClassName();
        final String manifest = $("Manifest-Version: 1.0", "Main-Class: " + appmain, "").join("\n");
        $(tempdir.getAbsoluteFile() + "/META-INF/MANIFEST.MF").file().delete().append(manifest);
        $(tempdir.getAbsoluteFile() + "/net/jcores/script/jcores.script.mode").file().delete().append("runtime");
        
        // Eventually pack the script
        $(tempdir).zip(this.name + ".jar");

        // Finally output what we did, and quit
        System.out.println("Application packed as '" + this.name + ".jar'");
        System.exit(0);
    }
}
