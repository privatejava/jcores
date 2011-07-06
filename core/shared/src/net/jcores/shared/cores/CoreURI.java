/*
 * CoreFile.java
 * 
 * Copyright (c) 2010, Ralf Biedert All rights reserved.
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
package net.jcores.shared.cores;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import net.jcores.shared.CommonCore;
import net.jcores.shared.cores.adapter.AbstractAdapter;
import net.jcores.shared.interfaces.functions.F1;
import net.jcores.shared.options.MessageType;

/**
 * Wraps a number of URIs and exposes some convenience functions. For example, 
 * to download a file to some the <code>downloads</code> directory:<br/><br/>
 * 
 * <code>$(uri).download("downloads")</code>
 * 
 * @author Ralf Biedert
 * 
 * @since 1.0
 */
public class CoreURI extends CoreObject<URI> {

    /** Used for serialization */
    private static final long serialVersionUID = 7366734773387957013L;

    /**
     * Creates an URI core.
     * 
     * @param supercore The common core.
     * @param objects The adapter to wrap.
     */
    public CoreURI(CommonCore supercore, URI... objects) {
        super(supercore, objects);
    }
    
    /**
     * Creates an URI core.
     * 
     * @param supercore The common core.
     * @param adapter The adapter to wrap.
     */
    public CoreURI(CommonCore supercore, AbstractAdapter<URI> adapter) {
        super(supercore, adapter);
    }


    /**
     * Opens the associated input stream.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$("http://jcores.net/index.html").uri().input()</code> - Opens an input stream for the given URI.</li>
     * </ul>
     * 
     * Multi-threaded.<br/>
     * <br/>
     * 
     * @return A CoreInputStream object enclosing the opened input streams.
     */
    public CoreInputStream input() {
        return new CoreInputStream(this.commonCore, map(new F1<URI, InputStream>() {
            public InputStream f(URI x) {
                try {
                    final URL url = x.toURL();
                    final InputStream openStream = url.openStream();
                    return openStream;
                } catch (MalformedURLException e) {
                    CoreURI.this.commonCore.report(MessageType.EXCEPTION, "URI " + x + " could not be transformed into an URL.");
                } catch (IOException e) {
                    CoreURI.this.commonCore.report(MessageType.EXCEPTION, "URI " + x + " could not be opened for reading.");
                }

                return null;
            }
        }).array(InputStream.class));
    }
}