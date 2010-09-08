/*
 * Core.java
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

import net.jcores.CommonCore;

/**
 * Just a test
 * 
 * @author Ralf Biedert
 */
public class CoreByte extends Core {

    /** The array we work on (if no fallback was provided) */
    final byte[] t;

    /**
     * Creates the core object for the given collection.
     * 
     * @param core 
     * @param t
     */
    public CoreByte(CommonCore core, byte... t) {
        super(core);

        this.t = t;

    }

    /**
     * Return our content as an array.
     * 
     * @return .
     */
    public byte[] array() {
        return this.t;
    }

    /**
     * Returns how many items are in this core. 
     * 
     * @return .
     */
    @Override
    public int size() {
        return this.t.length;
    }

    /**
     * Return the ith element.
     * 
     * @param i
     * 
     * @return .
     */
    public int get(int i) {
        if (i >= this.t.length) return 0;
        return this.t[i];
    }
}
