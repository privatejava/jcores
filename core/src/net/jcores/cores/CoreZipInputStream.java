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
 * Wraps an input stream.
 * 
 * @author Ralf Biedert
 */
public class CoreZipInputStream extends CoreObject<ZipInputStream> {

    /**
     * @param supercore
     * @param t
     */
    public CoreZipInputStream(CommonCore supercore, ZipInputStream... t) {
        super(supercore, t);
    }

    /**
     * Unzips to the given directory.
     * 
     * @param destination
     */
    public void unzip(final String destination) {
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
    }

    /**
     * Lists all entries within all ZIP files.
     * 
     * @return .
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
     * Returns an input stream for the given ZIP-file-entry. 
     * 
     * NOTE: This only uses the first element within the core, if there is any.
     * 
     * @param path
     * 
     * @return The opened input stream for the given zip entry
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
