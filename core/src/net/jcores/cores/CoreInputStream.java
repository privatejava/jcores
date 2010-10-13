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
import java.nio.ByteBuffer;
import java.util.zip.ZipInputStream;

import net.jcores.CommonCore;
import net.jcores.interfaces.functions.F1;
import net.jcores.options.MessageType;
import net.jcores.options.Option;
import net.jcores.options.OptionHash;
import net.jcores.utils.io.StreamUtils;

/**
 * Wraps an input stream and exposes some convenience functions. <br/><br/>
 * 
 * Note that some functions <b>consume</b> the input stream and close it 
 * afterwards. After calling a consuming function the associated stream may not 
 * be used anymore. As a result, no two consuming methods may be called, either
 * on the same core, on the wrapped streams or on trailing cores. Instead,
 * a fresh InputStream has to be provided every time. This means, you must 
 * do:<br/><br/>
 * 
 * <code>
 * // Fine<br/> 
 * $(...).input().consuming("x")<br/>
 * $(...).input().consuming("y")<br/>
 * </code><br/>
 * 
 * instead of:<br/><br/>
 * 
 * <code>
 * // Illegal<br/>
 * input = $(...).input();<br/>
 * input.consuming("x")<br/>
 * input.consuming("y")<br/>
 * </code><br/>
 * 
 * or:<br/><br/>
 * 
 * <code>
 * // Illegal<br/>
 * input = $(...).input().consuming("x").consuming("y");
 * </code><br/><br/>
 * 
 * Unfortunately, at the time of writing, consuming methods are the only way to 
 * ensure streams and file handles are closed properly. On some platforms
 * (like Mac OS) not closing streams usually has a negligible effect, on other 
 * platforms (Win32) you might run into trouble overwriting files.    
 * 
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
     * Closes all contained streams.<br/><br/>
     * 
     * Single-threaded. Consuming.<br/><br/>
     */
    public void close() {
        for (int i = 0; i < size(); i++) {
            final InputStream inputStream = get(i);
            try {
                inputStream.close();
            } catch (IOException e) {
                this.commonCore.report(MessageType.EXCEPTION, "Error closing stream " + inputStream + ".");
            }
        }
    }

    /**
     * Treats the given input streams as <code>ZipInputStreams</code> and tries to unzip them to 
     * the given directory, creating sub directories as necessary. This is a shorthand notation 
     * for <code>zipstream().unzip()</code><br/><br/>
     * 
     * Multi-threaded. Consuming.<br/><br/>
     * 
     * @param destination The destination to write to.
     */
    public void unzip(final String destination) {
        map(new F1<InputStream, Void>() {
            @Override
            public Void f(InputStream x) {
                try {
                    StreamUtils.doUnzip(x, destination);
                    x.close();
                } catch (IOException e) {
                    CoreInputStream.this.commonCore.report(MessageType.EXCEPTION, "IO error processing " + x + ".");
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
     * Multi-threaded. Consuming.<br/><br/>
     * 
     * @return A CoreString containing all contained text.
     */
    public CoreString text() {
        return new CoreString(this.commonCore, map(new F1<InputStream, String>() {
            public String f(final InputStream x) {
                String readText = StreamUtils.readText(CoreInputStream.this.commonCore, x);

                try {
                    x.close();
                } catch (IOException e) {
                    CoreInputStream.this.commonCore.report(MessageType.EXCEPTION, "Error closing stream " + x + ".");
                }

                return readText;
            }
        }).array(String.class));
    }

    /**
     * Creates a hash of the given input streams.<br/><br/>
     * 
     * Multi-threaded. Consuming.<br/><br/>
     * 
     * @param options Relevant options: <code>OptionHashMD5</code>.
     * 
     * @return A CoreString containing the generated hashes.
     */
    public CoreString hash(Option... options) {
        final String method = $(options).get(OptionHash.class, Option.HASH_MD5).getMethod();

        return new CoreString(this.commonCore, map(new F1<InputStream, String>() {
            public String f(final InputStream x) {
                String generateHash = StreamUtils.generateHash(x, method);

                try {
                    x.close();
                } catch (IOException e) {
                    CoreInputStream.this.commonCore.report(MessageType.EXCEPTION, "Error closing stream " + x + ".");
                }

                return generateHash;
            }
        }).array(String.class));
    }

    /**
     * Uses the enclosed input streams and reads their data into byte buffers.<br/><br/> 
     * 
     * Multi-threaded. Consuming.<br/><br/>
     * 
     * @return A CoreByteBuffer with binary content. 
     */
    public CoreByteBuffer data() {
        return new CoreByteBuffer(this.commonCore, map(new F1<InputStream, ByteBuffer>() {
            public ByteBuffer f(InputStream x) {
                ByteBuffer byteData = StreamUtils.getByteData(x);

                try {
                    x.close();
                } catch (IOException e) {
                    CoreInputStream.this.commonCore.report(MessageType.EXCEPTION, "Error closing stream " + x + ".");
                }

                return byteData;
            }
        }).array(ByteBuffer.class));
    }
}
