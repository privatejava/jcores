/*
 * Mapper.java
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
package net.jcores.utils;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Used by the cores when calling the inner core's mapping function.
 * 
 * @author rb
 */
public abstract class Mapper {
    /** If set the return array will be created automatically */
    protected final Class<?> returnType;

    /** Size of the return array. */
    protected final int size;

    /** Contains the return array. */
    @SuppressWarnings("unchecked")
    protected final AtomicReference array = new AtomicReference();

    /**
     * Creates an empty mapper with the given size.
     * 
     * @param size
     */
    public Mapper(int size) {
        this(null, size);
    }

    /**
     * Creates a mapper with an existing return array of the given size.
     * 
     * @param class1
     * @param size
     */
    public Mapper(Class<?> class1, int size) {
        this.returnType = class1;
        this.size = size;
    }

    /**
     * Return the size of the array. 
     * 
     * @return .
     */
    public int size() {
        return this.size;
    }

    /**
     * Overwrite this method and handle element number i.
     * 
     * @param i
     */
    public abstract void handle(int i);

    /**
     * Get the resulting array.
     * 
     * @return .
     */
    public Object getTargetArray() {
        return this.array.get();
    }

    /**
     * Returns the return array type.
     * 
     * @return .
     */
    public Class<?> getReturnType() {
        return this.returnType;
    }

    /**
     * Tries to update the array and returns the most recent result. 
     * 
     * @param object
     * @return .
     */
    @SuppressWarnings("unchecked")
    public Object updateArray(Object object) {
        this.array.compareAndSet(null, object);
        return this.array.get();
    }
}
