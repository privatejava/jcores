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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.ReentrantLock;

import net.jcores.utils.internal.lang.SubList;

/**
 * Wraps arbitrary collections.
 * 
 * TODO: Add logic that an array is created on demand which is being used for get() operations
 * up to a value until elements have already been extracted by an iterator (or by previous get()
 * operataions starting from 0).
 * 
 * @author Ralf Biedert
 * @since 1.0
 * @param <T> The type of the adapter.
 */
public final class CollectionAdapter<T> extends AbstractAdapter<T> implements List<T> {
    /** */
    private static final long serialVersionUID = 7010286694628298017L;

    /** Our primary collection iterator */
    Iterator<T> iterator;

    /** Our cache array */
    final AtomicReferenceArray<T> array;

    /** Specifies up to which index we have elements in our array cache */
    final AtomicInteger inCache = new AtomicInteger(-1);

    /** Locks access to the collection's iterator */
    final ReentrantLock collectionLock = new ReentrantLock();

    public CollectionAdapter(Collection<T> collection) {
        this.iterator = collection.iterator();
        this.array = new AtomicReferenceArray<T>(collection.size());
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.jcores.cores.adapter.AbstractAdapter#size()
     */
    @Override
    public int size() {
        return this.array.length();
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.jcores.cores.adapter.AbstractAdapter#get(int)
     */
    @Override
    public T get(int i) {
        cacheUntil(i);
        return this.array.get(i);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.jcores.cores.adapter.AbstractAdapter#iterator()
     */
    @Override
    public ListIterator<T> iterator() {
        return new ListIterator<T>() {
            int i = 0;
            
            @Override
            public boolean hasNext() {
                return this.i < size();
            }

            @Override
            public T next() {
                cacheUntil(this.i);
                return get(this.i++);
            }

            @Override
            public boolean hasPrevious() {
                return this.i > 0;
            }

            @Override
            public T previous() {
                this.i--;
                return get(this.i);
            }

            @Override
            public int nextIndex() {
                return this.i;
            }

            @Override
            public int previousIndex() {
                return this.i - 1;
            }

            @Override
            public void remove() {
            }

            @Override
            public void set(T e) {
            }

            @Override
            public void add(T e) {
            }
        };
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.jcores.cores.adapter.AbstractAdapter#array(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <N> N[] array(Class<N> in) {
        cacheAll();

        final N[] rval = (N[]) Array.newInstance(in, size());
        for (int i = 0; i < rval.length; i++) {
            rval[i] = (N) this.array.get(i);
        }

        return rval;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.jcores.cores.adapter.AbstractAdapter#unsafelist()
     */
    @Override
    public List<T> unsafelist() {
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.jcores.cores.adapter.AbstractAdapter#slice(int, int)
     */
    @Override
    public List<T> slice(int start, int end) {
        return subList(start, end);
    }

    protected void cacheAll() {
        cacheUntil(this.array.length());
    }

    protected void cacheUntil(int limit) {
        // When the cached value already exceeds the limit we dont have to do anything
        if (this.inCache.intValue() >= limit) return;

        this.collectionLock.lock();
        try {
            // Iterator might have been gone due to another thread that just exited the lock while we entered
            if (this.iterator == null) return;

            while (this.iterator.hasNext()) {
                int i = this.inCache.incrementAndGet();
                this.array.set(i, this.iterator.next());
                if (i > limit) break;
            }

            // Eventually dump the iterator to free up space
            if (!this.iterator.hasNext()) {
                this.iterator = null;
            }
        } finally {
            this.collectionLock.unlock();
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.List#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return this.size() == 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.List#contains(java.lang.Object)
     */
    @Override
    public boolean contains(Object o) {
        final ListIterator<T> i = iterator();
        while (i.hasNext()) {
            T next = i.next();
            if (next == null) continue;
            if (next.equals(o)) return true;
        }
        return false;
    }

    @Override
    public Object[] toArray() {
        cacheAll();

        final Object[] rval = (Object[]) Array.newInstance(Object.class, size());
        for (int i = 0; i < rval.length; i++) {
            rval[i] = this.array.get(i);
        }

        return rval;
    }

    @SuppressWarnings({ "unchecked", "hiding" })
    @Override
    public <T> T[] toArray(T[] a) {
        cacheAll();

        T[] rval = null;

        if (a.length >= this.array.length()) {
            rval = a;
        } else {
            rval = (T[]) Array.newInstance(a.getClass().getComponentType(), size());
        }

        for (int i = 0; i < rval.length; i++) {
            rval[i] = (T) this.array.get(i);
        }

        return rval;

    }

    @Override
    public boolean add(T e) {
        return false;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) return false;
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return false;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override
    public void clear() {}

    @Override
    public T set(int index, T element) {
        return null;
    }

    @Override
    public void add(int index, T element) {}

    @Override
    public T remove(int index) {
        return null;
    }

    @Override
    public int indexOf(Object o) {
        final ListIterator<T> i = iterator();
        while (i.hasNext()) {
            int index = i.nextIndex();
            T next = i.next();
            if (next == null) continue;
            if (next.equals(o)) return index;
        }

        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        cacheAll();
        for (int i = size() - 1; i >= 0; i--) {
            T next = get(i);
            if (next == null) continue;
            if (next.equals(o)) return i;
        }
        return -1;
    }

    @Override
    public ListIterator<T> listIterator() {
        return iterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        ListIterator<T> iterator2 = iterator();
        for (int i = 0; i < index; i++) {
            iterator2.next();
        }

        return iterator2;
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return new SubList<T>(this, fromIndex, toIndex);
    }
}
