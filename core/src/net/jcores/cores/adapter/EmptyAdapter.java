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

import java.util.ListIterator;


public final class EmptyAdapter<T> extends AbstractAdapter<T> {
    /** */
    public EmptyAdapter() {
    }
    
    /* (non-Javadoc)
     * @see net.jcores.cores.adapter.AbstractAdapter#size()
     */
    @Override
    public int size() {
        return 0;
    }

    /* (non-Javadoc)
     * @see net.jcores.cores.adapter.AbstractAdapter#get(int)
     */
    @Override
    public T get(int i) {
        return null;
    }

    /* (non-Javadoc)
     * @see net.jcores.cores.adapter.AbstractAdapter#iterator()
     */
    @Override
    public ListIterator<T> iterator() {
        return new ListIterator<T>() {
            
            /* (non-Javadoc)
             * @see java.util.ListIterator#hasNext()
             */
            @Override
            public boolean hasNext() {
                return false;
            }

            /* (non-Javadoc)
             * @see java.util.ListIterator#next()
             */
            @Override
            public T next() {
                return null;
            }

            @Override
            public boolean hasPrevious() {
                return false;
            }

            @Override
            public T previous() {
                return null;
            }

            @Override
            public int nextIndex() {
                return 0;
            }

            @Override
            public int previousIndex() {
                return 0;
            }

            @Override
            public void remove() {
                // 
            }

            @Override
            public void set(T e) {
                // 
            }

            @Override
            public void add(T e) {
                //
            }
        };
    }
}
