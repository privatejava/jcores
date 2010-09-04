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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.jcores.CommonCore;
import net.jcores.interfaces.functions.F1;
import net.jcores.options.MessageType;
import net.jcores.options.Option;
import net.jcores.utils.io.FileUtils;

/**
 * @author rb
 */
public class CoreFile extends CoreObject<File> {

    /**
     * @param supercore
     * @param t
     */
    public CoreFile(CommonCore supercore, File... t) {
        super(supercore, t);
    }

    /**
     * Returns all lines of all files joint.
     * 
     * @return .
     */
    public CoreString text() {
        return new CoreString(this.commonCore, map(new F1<File, String>() {
            public String f(final File x) {
                return FileUtils.readText(CoreFile.this.commonCore, x);
            }
        }).array());
    }

    /**
     * Deletes the given objects, recursively.
     * 
     * @return .
     */
    public CoreFile delete() {
        map(new F1<File, Object>() {
            public Object f(File x) {
                // TODO: Care for directories
                x.delete();
                return null;
            }
        });

        return this;
    }

    /**
     * Lists the contents of the subdirectories. 
     * @param options 
     * 
     * @return .
     */
    public CoreFile dir(Option ... options) {
        
        final boolean listDirs = $(options).contains(Option.LIST_DIRECTORIES);  
        
        return map(new F1<File, File[]>() {
            @Override
            public File[] f(File x) {
                final List<File> rval = new ArrayList<File>();

                // Get top level files ...
                List<File> next = new ArrayList<File>();
                File[] listed = x.listFiles();
                next.addAll(Arrays.asList(listed));

                while (next.size() > 0) {
                    listed = next.toArray(new File[0]);

                    next.clear();

                    for (File file : listed) {
                        if (!file.isDirectory()) {
                            rval.add(file);
                            continue;
                        }
                        
                        if(listDirs) {
                            rval.add(file);
                        }

                        File[] listFiles = file.listFiles();
                        
                        if (listFiles == null) continue;
                        
                        next.addAll(Arrays.asList(listFiles));
                    }
                }

                return rval.toArray(new File[0]);
            }
        }).expand(File.class).unique().as(CoreFile.class);
    }

    /**
     * Appends the object.toString() to the given files 
     * 
     * @param object
     * 
     * @return . 
     */
    public CoreFile append(Object object) {
        final String string = object.toString();

        map(new F1<File, Object>() {
            public Object f(File x) {
                try {
                    PrintWriter printWriter = new PrintWriter(new BufferedOutputStream(new FileOutputStream(x, true)));
                    printWriter.append(string);
                    printWriter.close();
                } catch (FileNotFoundException e) {
                    CoreFile.this.commonCore.report(MessageType.EXCEPTION, e.getLocalizedMessage());
                }

                return null;
            }
        });

        return this;
    }
}
