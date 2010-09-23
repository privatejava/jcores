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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import net.jcores.CommonCore;
import net.jcores.interfaces.functions.F1;
import net.jcores.options.MessageType;
import net.jcores.utils.io.StreamUtils;

/**
 * Wraps a number of URIs and exposes some convenience functions.  
 * 
 * @author Ralf Biedert
 * 
 * @since 1.0
 */
public class CoreURI extends CoreObject<URI> {

    /**
     * Creates an ZipInputStream core. 
     * 
     * @param supercore The common core. 
     * @param objects The strings to wrap.
     */
    public CoreURI(CommonCore supercore, URI... objects) {
        super(supercore, objects);
    }

    /**
     * Downloads the enclosed URIs to a temporary directories and returns core 
     * containing their filenames.<br/><br/>
     * 
     * Multi-threaded.<br/><br/>
     * 
     * @return A CoreFile object enclosing the files of all downloaded URIs.
     */
    public CoreFile download() {
        return new CoreFile(this.commonCore, map(new F1<URI, File>() {
            public File f(URI x) {
                try {
                    final URL url = x.toURL();
                    final InputStream openStream = url.openStream();
                    final File file = File.createTempFile("jcores.download.", ".tmp");

                    StreamUtils.saveTo(openStream, file);

                    openStream.close();

                    return file;
                } catch (MalformedURLException e) {
                    CoreURI.this.commonCore.report(MessageType.EXCEPTION, "URI " + x + " could not be transformed into an URL.");
                } catch (IOException e) {
                    CoreURI.this.commonCore.report(MessageType.EXCEPTION, "URI " + x + " could not be opened for reading.");
                }

                return null;
            }
        }).array(File.class));
    }

    /**
     * Downloads the enclosed URIs to the given directory, using the filename encoded 
     * within the uri and returns a core containing their filenames.<br/><br/>
     * 
     * Multi-threaded.<br/><br/>
     * 
     * @param path The directory to which the files will be downloaded. 
     * 
     * @return A CoreFile object enclosing the files of all downloaded URIs.
     */
    public CoreFile download(final String path) {
        return new CoreFile(this.commonCore, map(new F1<URI, File>() {
            public File f(URI x) {
                try {
                    final String filepath = $(x.getPath()).split("/").get(-1);
                    final URL url = x.toURL();
                    final InputStream openStream = url.openStream();
                    final File file = new File(path + "/" + filepath);

                    StreamUtils.saveTo(openStream, file);

                    openStream.close();

                    return file;
                } catch (MalformedURLException e) {
                    CoreURI.this.commonCore.report(MessageType.EXCEPTION, "URI " + x + " could not be transformed into an URL.");
                } catch (IOException e) {
                    CoreURI.this.commonCore.report(MessageType.EXCEPTION, "URI " + x + " could not be opened for reading.");
                }

                return null;
            }
        }).array(File.class));
    }

    /**
     * Tries to convert all URIs to local File objects.<br/><br/>
     * 
     * Multi-threaded.<br/><br/>
     * 
     * @return A CoreFile object enclosing all successfully converted file handles
     */
    public CoreFile file() {
        return new CoreFile(this.commonCore, map(new F1<URI, File>() {
            public File f(URI x) {
                try {
                    return new File(x);
                } catch (Exception e) {
                    //
                }
                return null;
            }
        }).array(File.class));
    }

}