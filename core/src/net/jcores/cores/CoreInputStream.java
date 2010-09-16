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

import static net.jcores.CoreKeeper.$;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

import net.jcores.CommonCore;
import net.jcores.interfaces.functions.F1;
import net.jcores.options.Option;
import net.jcores.options.OptionHash;
import net.jcores.utils.io.StreamUtils;

/**
 * Wraps an input stream and exposes some convenience functions.  
 * 
 * @since 1.0
 * @author Ralf Biedert
 */
public class CoreInputStream extends CoreObject<InputStream> {

    /**
     * Creates an input stream core. 
     * 
     * @param supercore The common core. 
     * @param objects The input stream to wrap.
     */
    public CoreInputStream(CommonCore supercore, InputStream... objects) {
        super(supercore, objects);
    }

    /**
     * Treats the given input streams as <code>ZipInputStreams</code> and tries to unzip them to 
     * the given directory, creating sub directories as necessary. This is a shorthand notation 
     * for <code>zipstream().unzip()</code><br/><br/>
     * 
     * Multi-threaded.<br/><br/>
     * 
     * @param destination The destination to write to.
     */
    public void unzip(final String destination) {
        map(new F1<InputStream, Void>() {
            @Override
            public Void f(InputStream x) {
                try {
                    StreamUtils.doUnzip(x, destination);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
    }

    /**
     * Converts the given input streams to zip streams.<br/><br/>
     * 
     * Multi-threaded.<br/><br/>
     * 
     * @return A CoreZipInputStream. 
     */
    public CoreZipInputStream zipstream() {
        return map(new F1<InputStream, ZipInputStream>() {
            public ZipInputStream f(InputStream x) {
                return new ZipInputStream(x);
            }
        }).as(CoreZipInputStream.class);
    }

    /**
     * Returns all lines of all files joint. A core will be returned in which each 
     * entry is a String containing the specific file's content.<br/><br/>
     * 
     * Multi-threaded.<br/><br/>
     * 
     * @return A CoreString containing all contained text.
     */
    public CoreString text() {
        return new CoreString(this.commonCore, map(new F1<InputStream, String>() {
            public String f(final InputStream x) {
                return StreamUtils.readText(CoreInputStream.this.commonCore, x);
            }
        }).array(String.class));
    }

    /**
     * Creates a hash of the given input streams.<br/><br/>
     * 
     * Multi-threaded.<br/><br/>
     * 
     * @param options Relevant options: <code>OptionHashMD5</code>.
     * 
     * @return A CoreString containing the generated hashes.
     */
    public CoreString hash(Option... options) {
        final String method = $(options).get(OptionHash.class, Option.HASH_MD5).getMethod();
       
        return new CoreString(this.commonCore, map(new F1<InputStream, String>() {
            public String f(final InputStream x) {
                return StreamUtils.generateHash(x, method);
            }
        }).array(String.class));
    }
}
