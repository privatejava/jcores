/*
 * Option.java
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
package net.jcores.shared.options;

import java.io.File;

/**
 * Contains all available options.
 * 
 * @since 1.0
 * @author Ralf Biedert
 */
public class Option {

    /** We don't allow for user-created options S */
    protected Option() { /* */
    }

    /** If CoreFile.dir() should list directory entries as well */
    public final static OptionListDirectories LIST_DIRECTORIES = new OptionListDirectories();

    /** If a selection should be inverted. */
    public final static OptionInvertSelection INVERT_SELECTION = new OptionInvertSelection();

    /** Hash method to use (MD5) */
    public final static OptionHashMD5 HASH_MD5 = new OptionHashMD5();

    /** Print debugging information */
    public final static OptionDebug DEBUG = new OptionDebug();

    /** Drop Type (File) */
    public final static OptionDropType<File> DROPTYPE_FILES = new OptionDropTypeFiles();

    /**
     * Specifies that the map result should be of type. Useful if map returns various
     * types.
     * 
     * @param type Class to use.
     * @return An option of the given type.
     */
    public final static OptionMapType MAP_TYPE(Class<?> type) {
        return new OptionMapType(type);
    }

    /**
     * Specifies that the regular expression options to use.
     * 
     * @param options Options to use.
     * @return An option of the given type.
     */
    public final static OptionRegEx REGEX(int options) {
        return new OptionRegEx(options);
    }

    /**
     * Can be passed to <code>forEach()</code> / <code>map()</code> to
     * retrieve the current loop index.
     * 
     * @return An OptionIndexer.
     */
    public final static OptionIndexer INDEXER() {
        return new OptionIndexer();
    }

}
