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
package net.jcores.cores;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipInputStream;

import net.jcores.CommonCore;
import net.jcores.interfaces.functions.F1;
import net.jcores.utils.io.StreamUtils;

/**
 * Wraps a number of ZipInputStreams and exposes some convenience functions.  
 * 
 * @author Ralf Biedert
 * @since 1.0
 */
public class CoreZipInputStream extends CoreObject<ZipInputStream> {

    /**
     * Creates an ZipInputStream core. 
     * 
     * @param supercore The common core. 
     * @param objects The strings to wrap.
     */
    public CoreZipInputStream(CommonCore supercore, ZipInputStream... objects) {
        super(supercore, objects);
    }

    /**
     * Unzips all enclosed streams to the given directory. Usually only called with a single enclosed object. <br/><br/>
     * 
     * Multi-threaded.<br/><br/>
     * 
     * @param destination The destination to unzip the given files to. All necessary directories will be created.
     * @return Return <code>this</code>.
     */
    public CoreZipInputStream unzip(final String destination) {
        map(new F1<ZipInputStream, Void>() {
            @Override
            public Void f(ZipInputStream x) {
                try {
                    StreamUtils.doUnzip(x, destination);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });

        return this;
    }

    /**
     * Lists all entries within all ZIP files. Usually only called with a single enclosed 
     * element.<br/><br/>
     * 
     * Multi-threaded.<br/><br/>
     * 
     * @return A CoreString, enclosing a list of all entries is returned. 
     */
    public CoreString dir() {
        return map(new F1<ZipInputStream, List<String>>() {
            @Override
            public List<String> f(ZipInputStream x) {
                try {
                    return StreamUtils.list(x);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }).expand(String.class).as(CoreString.class);
    }

    /**
     * Returns an input stream for the given ZIP-file-entry. This only uses the first element 
     * within the core, if there is any.<br/><br/>
     * 
     * Single-threaded, size-of-one.<br/><br/>

     * @param path The zip-entry-path to obtain.
     * 
     * @return The opened InputStream for the given zip entry, or null if nothing was found.
     */
    public InputStream get(String path) {
        final ZipInputStream zipInputStream = get(0);

        if (zipInputStream == null) return null;

        try {
            return StreamUtils.getInputStream(zipInputStream, path);
        } catch (IOException e) {
            // 
        }

        return null;
    }
}
