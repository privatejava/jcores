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

import net.jcores.CommonCore;
import net.jcores.interfaces.functions.F1;
import net.jcores.utils.io.StreamUtils;

/**
 * Wraps an input stream.
 * 
 * @author Ralf Biedert
 */
public class CoreInputStream extends CoreObject<InputStream> {

    /**
     * @param supercore
     * @param t
     */
    public CoreInputStream(CommonCore supercore, InputStream... t) {
        super(supercore, t);
    }

    /**
     * Unzips to the given directory.
     * 
     * @param destination
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
     * Returns all lines of all files joint.
     * 
     * @return .
     */
    public CoreString text() {
        return new CoreString(this.commonCore, map(new F1<InputStream, String>() {
            public String f(final InputStream x) {
                return StreamUtils.readText(CoreInputStream.this.commonCore, x);
            }
        }).array());
    }
}
