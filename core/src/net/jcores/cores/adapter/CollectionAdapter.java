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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;


/**
 * TODO: Add logic that an array is created on demand which is being used for get() operations 
 * up to a value until elements have already been extracted by an iterator (or by previous get() 
 * operataions starting from 0).
 * 
 * @author Ralf Biedert
 *
 * @param <T>
 */
public final class CollectionAdapter<T> extends AbstractAdapter<T> {
    /**  */
    private static final long serialVersionUID = 7010286694628298017L;
    
    /** */
    final Collection<T> collection;

    public CollectionAdapter(Collection<T> collection) {
        this.collection = collection;
    }
    
    /* (non-Javadoc)
     * @see net.jcores.cores.adapter.AbstractAdapter#size()
     */
    @Override
    public int size() {
        return this.collection.size();
    }

    /* (non-Javadoc)
     * @see net.jcores.cores.adapter.AbstractAdapter#get(int)
     */
    @Override
    public T get(int i) {
        return null;        // return this.list.get(i);
    }

    /* (non-Javadoc)
     * @see net.jcores.cores.adapter.AbstractAdapter#iterator()
     */
    @Override
    public ListIterator<T> iterator() {
        return null; // return this.list.listIterator();
    }

    /* (non-Javadoc)
     * @see net.jcores.cores.adapter.AbstractAdapter#array(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <N> N[] array(Class<N> in) {
        return (N[]) this.collection.toArray((T[]) Array.newInstance(in, 0));
    }

    /* (non-Javadoc)
     * @see net.jcores.cores.adapter.AbstractAdapter#unsafelist()
     */
    @Override
    public List<T> unsafelist() {
        return new ArrayList<T>(this.collection);
    }

    /* (non-Javadoc)
     * @see net.jcores.cores.adapter.AbstractAdapter#slice(int, int)
     */
    @Override
    public List<T> slice(int start, int end) {
        return new ArrayList<T>();
    }
}