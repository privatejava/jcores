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

import java.lang.reflect.Array;

import net.jcores.CommonCore;
import net.jcores.interfaces.functions.F1Int2Int;
import net.jcores.interfaces.functions.F1Int2Object;
import net.jcores.options.Option;
import net.jcores.utils.Mapper;

/**
 * Please ignore this class.
 * 
 * @author Ralf Biedert
 */
@SuppressWarnings("serial")
@Deprecated
public class CoreInt extends Core {

    /** The array we work on (if no fallback was provided) */
    final int[] t;

    /**
     * Creates the core object for the given collection.
     * 
     * @param core 
     * @param t
     */
    public CoreInt(CommonCore core, int... t) {
        super(core);

        this.t = t;

    }

    /**
     * Return our content as an array.
     * 
     * @return .
     */
    public int[] array() {
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

    /**
     * Maps the core's content with the given function and returns the result.
     * 
     * @param f
     * @param options 
     * 
     * @return The mapped elements in a stable order   
     */
    public CoreInt map(final F1Int2Int f, Option... options) {
        final Mapper mapper = new Mapper(int.class, this.t.length) {
            @Override
            public void handle(int i) {
                int[] a = (int[]) this.array.get();
                a[i] = f.f(CoreInt.this.t[i]);
            }
        };

        map(mapper, options);

        return new CoreInt(this.commonCore, (int[]) mapper.getTargetArray());
    }

    /**
     * Maps the core's content with the given function and returns the result.
     * 
     * @param <R> 
     * @param f
     * @param options 
     * 
     * @return The mapped elements in a stable order   
     */
    @SuppressWarnings("unchecked")
    public <R> CoreObject<R> map(final F1Int2Object<R> f, Option... options) {
        final Mapper mapper = new Mapper(this.t.length) {
            @Override
            public void handle(int i) {
                R[] a = (R[]) this.array.get();

                final R out = f.f(CoreInt.this.t[i]);
                if (out == null) return;

                if (a == null) {
                    a = (R[]) updateArray(Array.newInstance(out.getClass(), this.size));
                }

                a[i] = out;
            }
        };

        map(mapper, options);

        return new CoreObject<R>(this.commonCore, (R[]) mapper.getTargetArray());
    }
}
