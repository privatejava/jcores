/*
 * Options.java
 * 
 * Copyright (c) 2011, Ralf Biedert, DFKI. All rights reserved.
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
package net.jcores.jre.utils.internal;

import java.util.Collection;
import java.util.LinkedList;

import net.jcores.jre.options.InvertSelection;
import net.jcores.jre.options.OnFailure;
import net.jcores.jre.options.Option;

/**
 * Convenience class to process options.
 * 
 * @author Ralf Biedert
 * @since 1.0
 */
public class Options {
    public static Options $(Option... options) {
        return new Options(options);
    }

    /** The options we work on */
    private final Option options[];

    /** Handlers for onFailre options */
    Collection<OnFailure> onFailures;
    
    /** If inverted was given */
    boolean invert = false;

    /**
     * Constructs a new options object.
     * 
     * @param options
     */
    private Options(Option... options) {
        this.options = options == null ? new Option[0] : options;

        // Only do something if we have to ...
        if (this.options.length > 0) {
            // Setup additional structures
            this.onFailures = new LinkedList<OnFailure>();

            // Process all options
            for (Option option : this.options) {
                if (option instanceof OnFailure) {
                    this.onFailures.add((OnFailure) option);
                }
                
                if (option instanceof InvertSelection) {
                    this.invert = !this.invert;
                }
            }
        }
    }

    /**
     * @since 1.0
     * @param object
     * @param exception
     * @param code
     * @param message
     */
    public void failure(Object object, Exception exception, String code, String message) {
        // Quick check if we should do anything ...
        if (this.onFailures == null) return;

        for (OnFailure f : this.onFailures) {
            f.getListener().onFailure(object, exception, code, message);
        }
    }
    
    
    /**
     * @since 1.0
     * @return .
     */
    public boolean invert() {
        return this.invert;
    }

}