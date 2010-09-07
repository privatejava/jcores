/*
 * CoreObject.java
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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import net.jcores.CommonCore;
import net.jcores.interfaces.functions.F0;
import net.jcores.interfaces.functions.F1;
import net.jcores.interfaces.functions.F1Object2Bool;
import net.jcores.interfaces.functions.F1Object2Int;
import net.jcores.interfaces.functions.F2DeltaObjects;
import net.jcores.interfaces.functions.F2ReduceObjects;
import net.jcores.managers.ManagerStatistics;
import net.jcores.options.MessageType;
import net.jcores.options.Option;
import net.jcores.options.OptionMapType;
import net.jcores.utils.Folder;
import net.jcores.utils.Mapper;
import net.jcores.utils.Staple;
import net.jcores.utils.lang.ObjectUtils;

/**
 * A core that wraps a number of objects. 
 * 
 * @author Ralf Biedert
 * 
 * @param <T> 
 */
public class CoreObject<T> extends Core {

    /** The array we work on. */
    protected final T[] t;

    /**
     * Creates the core object for the given collection.
     * 
     * @param supercore 
     * @param type 
     * @param t
     */
    @SuppressWarnings("unchecked")
    public CoreObject(CommonCore supercore, Class<?> type, T t) {
        super(supercore);

        // Check if we have an object. If not, and if there is no type, use an 
        // empty Object array 
        if (t != null) {
            this.t = (T[]) Array.newInstance(type, 1);
            this.t[0] = t;
        } else {
            this.t = (T[]) new Object[0];
        }
    }

    /**
     * Creates the core object for the given collection.
     * 
     * @param supercore 
     * @param t
     */
    public CoreObject(CommonCore supercore, T... t) {
        super(supercore);

        this.t = t;

        // DELETEME: Testing if it makes sense to use assertions to provide switchable
        // logging and performance measuring. 
        assert this.commonCore.manager(ManagerStatistics.class).hashCode() != 0;
    }

    /**
     * Returns the core's content as an array of the given type.
     * 
     * @param in 
     * @param <N> 
     * 
     * @return .
     */
    @SuppressWarnings("unchecked")
    public <N> N[] array(Class<N> in) {
        N[] n = (N[]) Array.newInstance(in, 0);

        if (this.t != null)
            return (N[]) Arrays.copyOf(this.t, this.t.length, n.getClass());

        return (N[]) Array.newInstance(in, 0);
    }

    /**
     * Returns a core that tries to treat all elements as being of the given type. Elements which 
     * don't match are ignored.  
     * 
     * TODO: Check if this method is sound ...
     * 
     * @param <C>
     * @param clazz
     * @return .
     */
    @SuppressWarnings({ "unchecked", "null" })
    public <C extends Core> C as(Class<C> clazz) {
        try {
            final Constructor<?>[] constructors = clazz.getConstructors();
            Constructor<C> constructor = null;

            // Find a proper constructor!
            for (Constructor<?> c : constructors) {
                if (c.getParameterTypes().length != 2) continue;
                if (!c.getParameterTypes()[0].equals(CommonCore.class)) continue;

                // Sanity check.
                if (constructor != null)
                    System.err.println("There should only be one constructor per core! And here comes your exception ... ;-)");

                constructor = (Constructor<C>) c;
            }

            Object newT[] = null;
            Class<? extends Object[]> requestedType = null;

            // Try to convert our array. If that fails, create an empty one ...
            try {
                requestedType = (Class<? extends Object[]>) constructor.getParameterTypes()[1];
                newT = Arrays.copyOf(this.t, size(), requestedType);
            } catch (ArrayStoreException e) {
                this.commonCore.report(MessageType.EXCEPTION, "Unable to convert our array " + this.t + " to the requested type " + requestedType + ". Returning empty core.");
                newT = (Object[]) Array.newInstance(requestedType.getComponentType(), 0);
            }

            return constructor.newInstance(this.commonCore, newT);

            // NOTE: We do not swallow all execptions, because as() is a bit special and we cannot return
            // anyhting that would still be usable.
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            this.commonCore.report(MessageType.EXCEPTION, "No constructor found for " + clazz);
            System.err.println("No suitable constructor found!");
        }

        return null;
    }

    /**
     * Performs a generic call on each element of this core. 
     * 
     * @param string
     * @param params
     * 
     * @return .
     */
    @SuppressWarnings("null")
    public CoreObject<Object> call(final String string, final Object... params) {
        final int len = params == null ? 0 : params.length;
        final Class<?>[] types = new Class[len];

        // Convert classes.
        for (int i = 0; i < len; i++) {
            types[i] = params[i].getClass();
        }

        return new CoreObject<Object>(this.commonCore, map(new F1<T, Object>() {
            public Object f(T x) {
                try {
                    final Method method = x.getClass().getMethod(string, types);
                    return method.invoke(x, params);
                } catch (SecurityException e) {
                    CoreObject.this.commonCore.report(MessageType.EXCEPTION, "SecurityException for " + x + " (method was " + string + ")");
                } catch (NoSuchMethodException e) {
                    CoreObject.this.commonCore.report(MessageType.EXCEPTION, "NoSuchMethodException for " + x + " (method was " + string + ")");
                } catch (IllegalArgumentException e) {
                    CoreObject.this.commonCore.report(MessageType.EXCEPTION, "IllegalArgumentException for " + x + " (method was " + string + ")");
                } catch (IllegalAccessException e) {
                    CoreObject.this.commonCore.report(MessageType.EXCEPTION, "IllegalAccessException for " + x + " (method was " + string + ")");
                } catch (InvocationTargetException e) {
                    CoreObject.this.commonCore.report(MessageType.EXCEPTION, "InvocationTargetException for " + x + " (method was " + string + ")");
                }

                return null;
            }
        }).array());
    }

    /**
     * Casts all elements to the given type or sets them null if they are not castable.
     * 
     * @param <N>
     * @param target
     * @return R
     */
    public <N> CoreObject<N> cast(final Class<N> target) {
        return map(new F1<T, N>() {
            @SuppressWarnings("unchecked")
            @Override
            public N f(T x) {
                if (target.isAssignableFrom(x.getClass())) return (N) x;
                return null;
            }
        }, new OptionMapType(target));
    }

    /**
     * Returns a compacted object whose underlying array does not 
     * contain null anymore. 
     * 
     * @return . 
     */
    public CoreObject<T> compact() {
        // No size == no fun.
        if (size() == 0) return this;

        final T[] tmp = Arrays.copyOf(this.t, this.t.length);
        int dst = 0;

        // Now process our array
        for (int i = 0; i < this.t.length; i++) {
            if (this.t[i] == null) continue;

            tmp[dst++] = this.t[i];
        }

        return new CoreObject<T>(this.commonCore, Arrays.copyOf(tmp, dst));
    }

    /**
     * Returns true if this core contains the given object.
     * 
     * @param object
     *  
     * @return True if the object is there, false if not. 
     */
    public boolean contains(final T object) {
        for (int i = 0; i < size(); i++) {
            if (this.t[i] != null && this.t[i].equals(object)) return true;
        }

        return false;
    }

    /**
     * Returns a core of length size() - 1 consisting of the results of the delta function.
     *  
     * @param delta
     * @param <R> 
     * @param options 
     * 
     * @return . 
     */
    @SuppressWarnings("unchecked")
    public <R> CoreObject<R> delta(final F2DeltaObjects<T, R> delta, Option... options) {

        Class<?> mapType = null;

        // Check options if we have a map type.
        for (Option option : options) {
            if (option instanceof OptionMapType) {
                mapType = ((OptionMapType) option).getType();
            }
        }

        // Create mapper
        final Mapper mapper = new Mapper(mapType, size() - 1) {
            @Override
            public void handle(int i) {
                // Get our target-array (if it is already there)
                R[] a = (R[]) this.array.get();

                // Get the in-value from the source-array
                final T ii = CoreObject.this.t[i];
                final T jj = CoreObject.this.t[i + 1];

                // Convert
                if (ii == null || jj == null) return;

                final R out = delta.f(ii, jj);

                if (out == null) return;

                // If we haven't had an in-array, create it now, according to the return type
                if (a == null) {
                    a = (R[]) updateArray(Array.newInstance(out.getClass(), this.size));
                }

                // Eventually set the out value
                a[i] = out;
            }
        };

        // Map ...
        map(mapper, options);

        // ... and return result.
        return new CoreObject<R>(this.commonCore, (R[]) mapper.getTargetArray());

    }

    /**
     * Works only for interfaces!
     * 
     * Tries to treat each element of this collections as if of type c and executes the function. 
     * @param c 
     * @param <X> 
     * 
     * @return Something implementing c that acts on each element which matches.
     */
    @SuppressWarnings("unchecked")
    public <X> X each(final Class<X> c) {
        if (c == null || !c.isInterface()) {
            System.err.println("You must pass an interface.");
            return null;
        }

        // Get only assignable classes of our collection
        final CoreObject<X> filtered = cast(c);

        // Provide an invocation handler
        return (X) ObjectUtils.getProxy(new InvocationHandler() {
            public Object invoke(Object proxy, final Method method, final Object[] args)
                                                                                        throws Throwable {
                filtered.map(new F1<X, Object>() {
                    public Object f(X x) {

                        try {
                            method.invoke(x, args);
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }

                        return null;
                    }
                });

                return null;
            }
        }, c);
    }

    /**
     * Expands contained arrays to a single array of the given type.  
     * 
     * @param <N>
     * @param class1 The class elements of the core should be.
     * @return .
     */
    @SuppressWarnings("unchecked")
    public <N> CoreObject<N> expand(Class<N> class1) {
        int length = 0;

        if (this.t == null) return new CoreObject<N>(this.commonCore, class1, null);

        // Compute overall size 
        for (T x : this.t) {
            if (x == null) continue;

            // Is it a collection?
            if (x instanceof Collection<?>) {
                length += ((Collection<?>) x).size();
                continue;
            }

            // An array?
            try {
                length += Array.getLength(x);
                continue;
            } catch (IllegalArgumentException e) {
                //
            }

            // A single object?!
            length++;
        }

        // Generate array
        N[] n = (N[]) Array.newInstance(class1, length);
        int offset = 0;

        // Copy to array 
        for (T x : this.t) {
            if (x == null) continue;

            // Is it a collection?
            if (x instanceof Collection<?>) {
                Object[] array = ((Collection<?>) x).toArray();
                System.arraycopy(array, 0, n, offset, array.length);
                offset += array.length;
                continue;
            }

            // An array?
            try {
                int size = Array.getLength(x);
                System.arraycopy(x, 0, n, offset, size);
                offset += size;
                continue;
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            } catch (ArrayStoreException e) {
                //
            }

            // A single element?
            Array.set(n, offset, x);
        }

        return new CoreObject<N>(this.commonCore, n);
    }

    /**
     * Returns a new core with all null elements set to fillValue.
     * 
     * @param fillValue 
     * 
     * @return . 
     */
    public CoreObject<T> fill(T fillValue) {
        if (this.t == null) return this;

        final T[] copy = Arrays.copyOf(this.t, size());

        for (int i = 0; i < copy.length; i++) {
            copy[i] = copy[i] == null ? fillValue : copy[i];
        }

        return new CoreObject<T>(this.commonCore, copy);
    }

    /**
     * Filters the object using the given function. A compacted array will be returned that
     * contains only values for which f returned true.
     * 
     * @param f If f returns true the object is kept. 
     * @param options
     * 
     * @return . 
     */
    public CoreObject<T> filter(final F1Object2Bool<T> f, Option... options) {
        CoreObject<T> rval = map(new F1<T, T>() {
            public T f(T x) {
                if (f.f(x)) return x;
                return null;
            }
        });

        return rval.compact();
    }

    /**
     * Reduces the given object (multithreaded version). 
     * 
     * NOTE: At present, reduce() is much faster for simple operations, as it involves much 
     * less synchronization overhead. Right now, only use fold for very complex operations.  
     * 
     * @param f
     * @param options
     * @return .
     */
    @SuppressWarnings("unchecked")
    public CoreObject<T> fold(final F2ReduceObjects<T> f, Option... options) {

        // In case we only have zero or one elements, don't do anything
        if (size() <= 1) return this;

        final Folder folder = new Folder(null, size()) {
            @Override
            public void handle(int i, int j, int destination) {
                // Get our target-array (if it is already there)
                T[] a = (T[]) this.array.get();

                // Get the in-value from the source-array
                final T ii = a[i];
                final T jj = a[j];

                if (ii == null && jj == null) return;
                if (ii == null && jj != null) {
                    a[destination] = jj;
                    return;
                }

                if (ii != null && jj == null) {
                    a[destination] = ii;
                    return;
                }

                a[destination] = f.f(ii, jj);
            }
        };

        // Update the target array and fold ...
        folder.updateArray(Arrays.copyOf(this.t, size()));

        // Now do fold ...
        fold(folder, options);

        // ... and return result.
        return new CoreObject<T>(this.commonCore, Arrays.copyOf((T[]) folder.getTargetArray(), 1));
    }

    /**
     * Return the an element at the the relative position
     * 
     * @param percent 0 == first element, 1 == last element. 
     * 
     * @return .
     */
    public T get(double percent) {
        if (Double.isNaN(percent)) return null;
        if (percent < 0) return null;
        if (percent > 1) return null;

        return this.t[(int) (percent * size())];
    }

    /**
     * Return the ith element.
     * 
     * @param i
     * 
     * @return .
     */
    public T get(int i) {
        final int offset = indexToOffset(i);

        if (this.t == null || offset < 0) return null;

        return this.t[offset];
    }

    /**
     * Return the ith element or dflt if the element does not exist..
     * 
     * @param i
     * @param dflt 
     * 
     * @return .
     */
    public T get(int i, T dflt) {
        final T rval = get(i);
        return rval == null ? dflt : rval;
    }

    /**
     * Returns the first element, or, if there is none, return dflt.
     * 
     * @param dflt
     * @return .
     */
    public T get(T dflt) {
        return get(0, dflt);
    }

    /**
     * Checks if all elements are not null.
     * 
     * @return .
     */
    public boolean hasAll() {
        return size() == compact().size();
    }

    /**
     * Checks if the element has any element.
     * 
     * @return .
     */
    public boolean hasAny() {
        return compact().size() > 0;
    }

    /**
     * If all elements are present, execute f0.
     * 
     * @param f0 S>
     */
    public void ifAll(F0 f0) {
        if (hasAll()) f0.f();
    }

    /**
     * Returns the wrapped collection as a list
     * 
     * @return .
     */
    public List<T> list() {
        if (this.t == null) return new ArrayList<T>();
        return Arrays.asList(this.t);
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
    public <R> CoreObject<R> map(final F1<T, R> f, Option... options) {

        Class<?> mapType = null;

        // Check options if we have a map type.
        for (Option option : options) {
            if (option instanceof OptionMapType) {
                mapType = ((OptionMapType) option).getType();
            }
        }

        // Create mapper
        final Mapper mapper = new Mapper(mapType, size()) {
            @Override
            public void handle(int i) {
                // Get our target-array (if it is already there)
                R[] a = (R[]) this.array.get();

                // Get the in-value from the source-array
                final T in = CoreObject.this.t[i];

                // Convert
                if (in == null) return;
                final R out = f.f(in);
                if (out == null) return;

                // If we haven't had an in-array, create it now, according to the return type
                if (a == null) {
                    a = (R[]) updateArray(Array.newInstance(out.getClass(), this.size));
                }

                // Eventually set the out value
                a[i] = out;
            }
        };

        // Map ...
        map(mapper, options);

        // ... and return result.
        return new CoreObject<R>(this.commonCore, (R[]) mapper.getTargetArray());
    }

    /**
     * Maps the core's content with the given function and returns the result.
     * 
     * @param f
     * @param options 
     * 
     * @return The mapped elements in a stable order   
     */
    public CoreInt map(final F1Object2Int<T> f, Option... options) {
        final Mapper mapper = new Mapper(int.class, size()) {
            @Override
            public void handle(int i) {
                int[] a = (int[]) this.array.get();
                a[i] = f.f(CoreObject.this.t[i]);
            }
        };

        map(mapper, options);

        return new CoreInt(this.commonCore, (int[]) mapper.getTargetArray());
    }

    /**
     * Reduces the given object (single thread version) 
     * 
     * @param f
     * @param options
     * @return .
     */
    @SuppressWarnings("unchecked")
    public CoreObject<T> reduce(final F2ReduceObjects<T> f, Option... options) {
        T stack = null;

        for (int i = 0; i < size(); i++) {
            T current = this.t[i];

            // Nothing to do for null elements
            if (current == null) continue;

            // Init stack with first element found
            if (stack == null) {
                stack = current;
                continue;
            }

            stack = f.f(stack, current);
        }

        // We have to use the stack, because we otherwise we might 
        // create an array of another type, and not the type f returned. 
        final Class<T> type = stack != null ? (Class<T>) stack.getClass() : null;

        return new CoreObject<T>(this.commonCore, type, stack);
    }

    /**
     * Returns how many items are in this core. 
     * 
     * @return .
     */
    @Override
    public int size() {
        if (this.t == null) return 0;
        return this.t.length;
    }

    /**
     * Returns a slice of this core.
     * 
     * @param start
     * @param length If length is positive it is treated as length, if negative as a starting position from the end (-1 == last position)
     * @return .
     */
    public CoreObject<T> slice(final int start, final int length) {
        if (this.t == null) return this;

        final int i = indexToOffset(start);
        final int l = length > 0 ? length : indexToOffset(length) - i + 1;

        return new CoreObject<T>(this.commonCore, Arrays.copyOfRange(this.t, i, i + l));
    }

    /**
     * Returns a new, sorted core.
     * 
     * @param c
     * @return .
     */
    public CoreObject<T> sort(Comparator<T> c) {
        if (this.t == null) return this;

        final T[] copyOf = Arrays.copyOf(this.t, size());
        Arrays.sort(copyOf, c);

        return new CoreObject<T>(this.commonCore, copyOf);
    }

    /**
     * Assists computing the average of a number of elements.
     * 
     * @param neutralElement The initial element
     * @param sumAndNext A reduce function. Left will be the current sum, right will 
     * be the next element.
     * 
     * @return An Average object, with the current sum and the size. 
     */
    public Staple<T> staple(T neutralElement, F2ReduceObjects<T> sumAndNext) {
        final int size = size();
        if (size == 0) return new Staple<T>(neutralElement, 1);

        int count = 0;
        T sum = neutralElement;

        for (int i = 0; i < size; i++) {
            if (this.t[i] == null) continue;

            sum = sumAndNext.f(sum, this.t[i]);

            count++;
        }

        return new Staple<T>(sum, count);
    }

    /**
     * Converts all elements to strings.
     * 
     * @return .
     */
    public CoreString string() {
        return map(new F1<T, String>() {
            public String f(T x) {
                return x.toString();
            }
        }).as(CoreString.class);
    }

    /**
     * Returns a core containing only unique objects.
     * 
     * @return .
     */
    public CoreObject<T> unique() {
        if (size() == 0) return this;

        final T[] copy = Arrays.copyOf(this.t, size());

        // Now check for each element
        for (int i = 1; i < copy.length; i++) {
            // If it is null, no nothing
            if (copy[i] == null) continue;

            // Check each previous element
            for (int j = 0; j < i; j++) {
                // If the current element is equal to a previous element,
                // this element can be nulled
                if (copy[i].equals(copy[j])) {
                    copy[i] = null;
                    break;
                }
            }
        }

        // Return the new, unique core.
        return new CoreObject<T>(this.commonCore, copy).compact();
    }

    /**
     * Return our content as an array. 
     * 
     * NOTE: Calling this method is not safe, as sometimes arrays of type Object[] can be returned 
     * (especially in the case of empty cores) which are not mappable to more specific arrays. E.g:
     * 
     * String[] strings = emptystringcore.array()
     * 
     * This is due to the case that empty cores do not know of what type they really are.
     * 
     * @return .
     */
    protected T[] array() {
        return this.t;
    }

    /**
     * Converts an index to an offset.
     * 
     * @param index
     * @return
     */
    protected final int indexToOffset(int index) {
        final int size = size();

        if (index >= size) return -1;

        // We also support negative indices. 
        if (index < 0) {
            if (-index > size) return -1;
            return size + index;
        }

        return index;
    }
}
