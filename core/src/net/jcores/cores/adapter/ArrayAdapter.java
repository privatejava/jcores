/*
 * ArrayAdapter.java
 * 
 * Copyright (c) 2011, Ralf Biedert All rights reserved.
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
package net.jcores.cores.adapter;


public final class ArrayAdapter<T> extends AbstractAdapter<T> {
    /** */
    final T[] array;

    public ArrayAdapter(T... array) {
        this.array = array;
    }
    
    /* (non-Javadoc)
     * @see net.jcores.cores.adapter.AbstractAdapter#size()
     */
    @Override
    public int size() {
        return this.array.length;
    }

    /* (non-Javadoc)
     * @see net.jcores.cores.adapter.AbstractAdapter#get(int)
     */
    @Override
    public T get(int i) {
        return this.array[i];
    }

    /* (non-Javadoc)
     * @see net.jcores.cores.adapter.AbstractAdapter#iterator()
     */
    @Override
    public AIterator<T> iterator() {
        return new AIterator<T>() {
            
            int i = 0;

            /* (non-Javadoc)
             * @see net.jcores.cores.adapter.AbstractAdapter.AIterator#hasNext()
             */
            @Override
            public boolean hasNext() {
                if(this.i < ArrayAdapter.this.array.length - 1) return true;
                return false;
            }

            /* (non-Javadoc)
             * @see net.jcores.cores.adapter.AbstractAdapter.AIterator#next()
             */
            @Override
            public T next() {
                return ArrayAdapter.this.array[this.i++];
            }

            /* (non-Javadoc)
             * @see net.jcores.cores.adapter.AbstractAdapter.AIterator#nextIndex()
             */
            @Override
            public int nextIndex() {
                return this.i;
            }
        };
    }
}
