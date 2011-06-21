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

import static net.jcores.CoreKeeper.$;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jcores.CommonCore;
import net.jcores.interfaces.functions.F0;
import net.jcores.interfaces.functions.F1;
import net.jcores.interfaces.functions.F1Object2Bool;
import net.jcores.interfaces.functions.F2DeltaObjects;
import net.jcores.interfaces.functions.F2ReduceObjects;
import net.jcores.interfaces.functions.Fn;
import net.jcores.managers.ManagerDebugGUI;
import net.jcores.managers.ManagerDeveloperFeedback;
import net.jcores.options.MessageType;
import net.jcores.options.Option;
import net.jcores.options.OptionMapType;
import net.jcores.utils.Staple;
import net.jcores.utils.internal.Folder;
import net.jcores.utils.internal.Mapper;
import net.jcores.utils.internal.Wrapper;
import net.jcores.utils.internal.io.StreamUtils;
import net.jcores.utils.internal.lang.ObjectUtils;
import net.jcores.utils.map.Compound;

/**
 * The standard core that wraps a number of objects and exposes a number of methods to
 * act on them, some of them in parallel. For example,
 * to get the last three elements of an array of Strings, write:<br/>
 * <br/>
 * 
 * <code>$(strings).slice(-3, 3).array(String.class)</code><br/>
 * <br/>
 * 
 * If you implement your own core you should extend this class.<br/>
 * <br/>
 * 
 * A core is immutable. No method will ever change its content array (it is, however,
 * possible, that the individual elements enclosed might change).
 * 
 * @author Ralf Biedert
 * @since 1.0
 * 
 * @param <T> Type of the objects to wrap.
 */
public class CoreObject<T> extends Core {

    /** */
    private static final long serialVersionUID = -6436821141631907999L;

    /** The array we work on. */
    protected final T[] t;

    /**
     * Creates the core object for the given single object.
     * 
     * @param supercore CommonCore to use.
     * @param type Type of the object to wrap (in case it is null).
     * @param object Object to wrap.
     */
    @SuppressWarnings("unchecked")
    public CoreObject(CommonCore supercore, Class<?> type, T object) {
        super(supercore);

        // Check if we have an object. If not, and if there is no type, use an
        // empty Object array
        if (object != null) {
            this.t = (T[]) Array.newInstance(type, 1);
            this.t[0] = object;
        } else {
            this.t = (T[]) new Object[0];
        }
    }

    /**
     * Creates the core object for the given array.
     * 
     * @param supercore CommonCore to use.
     * @param objects Object to wrap.
     */
    public CoreObject(CommonCore supercore, T... objects) {
        super(supercore);

        this.t = objects;
    }

    /**
     * Returns a core containing all elements of this core and the other core.
     * Elements that are in both cores will appear twice.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$("a", "b").add($("c"))</code> - The resulting core contains 
     * <code>a</code>, <code>b</code> and <code>c</code>.</li>
     * </ul>
     * 
     * Single-threaded. <br/>
     * <br/>
     * 
     * @param toAdd The core to add to this core.
     * 
     * @return A CoreObject containing all objects of this core and the other
     * core.
     */
    @SuppressWarnings("unchecked")
    public CoreObject<T> add(CoreObject<T> toAdd) {
        if (size() == 0) return toAdd;
        if (toAdd.size() == 0) return this;

        final T[] copy = (T[]) Array.newInstance(this.t.getClass().getComponentType(), this.t.length + toAdd.t.length);
        System.arraycopy(this.t, 0, copy, 0, this.t.length);
        System.arraycopy(toAdd.t, 0, copy, this.t.length, toAdd.t.length);

        return new CoreObject<T>(this.commonCore, copy);
    }

    /**
     * Returns a core containing all elements of this core and the other array.
     * Elements that are in both will appear twice.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$("a", "b").add("c")</code> - The resulting core contains <code>a</code>, 
     * <code>b</code> and <code>c</code>.</li>
     * </ul>
     * 
     * Single-threaded. <br/>
     * <br/>
     * 
     * @param toAdd The array to add to this core.
     * 
     * @return A CoreObject containing all objects of this core and the other
     * array.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public CoreObject<T> add(T... toAdd) {
        return this.add(new CoreObject(this.commonCore, toAdd));
    }

    /**
     * Returns the core's content as an array of the given type. Elements that don't fit
     * into the given target type will be skipped.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$(list).array(String.class)</code> - Returns a String array for the given 
     * list. Elements that are no String will be returned as null.</li>
     * </ul>
     * 
     * 
     * Single-threaded.<br/>
     * <br/>
     * 
     * @param in Type of the target array to use.
     * @param <N> Type of the array.
     * 
     * @return An array containing the all assignable elements.
     */
    @SuppressWarnings("unchecked")
    public <N> N[] array(Class<N> in) {
        final N[] n = (N[]) Array.newInstance(in, 0);

        if (this.t != null)
            return (N[]) Arrays.copyOf(this.t, this.t.length, n.getClass());

        return (N[]) Array.newInstance(in, 0);
    }

    /**
     * Returns a core that tries to treat all elements as being of the given type.
     * Elements which don't match are ignored. Can also be used to load extensions 
     * (e.g., <code>somecore.as(CoreString.class)</code>). This function should not 
     * be called within hot-spots (functions called millions of times a second)
     * as it relies heavily on reflection.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$(objects).as(MyExtensionCore.class).method()</code> - If you wrote a jCores 
     * extension this is how  you could activate the extension for a given set of objects and 
     * execute one of its methods.</li>
     * <li><code>$("a", null, "c").compact().as(CoreString.class)</code> - Sometimes you call 
     * methods from a parent Core (like <code>compact()</code>, which is part of CoreObject, 
     * not of {@link CoreString}) that does returns a more general return type then what you 
     * want. Using <code>as()</code> you can cast the Core back.</li>
     * </ul>
     * 
     * 
     * Single-threaded. Heavyweight.<br/>
     * <br/>
     * 
     * @param <C> Type of the clazz.
     * @param clazz Core to be spawned and returned.
     * @return If successful, spawns a core of type <code>clazz</code> and returns it,
     * wrapping all contained elements.
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

            // NOTE: We do not swallow all execptions, because as() is a bit special and
            // we cannot return
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
     * Performs a generic call on each element of this core (for
     * example <code>core.call("toString")</code>). The return values will 
     * be stored in a {@link CoreObject}. This is a dirty but shorthand way
     * to call the same function on objects that don't share a common superclass. Should not be
     * called within hot-spots (functions called millions of times a second) as it relies heavily on reflection.<br/>
     * <br/>
     * 
     * 
     * Examples:
     * <ul>
     * <li><code>$(tA, tB, tC, tD).call("method").unique().size()</code> - Calls a method 
     * <code>method()</code> on some objects that have no common supertype (except {@link Object}),
     * and returns the number of distinct objects returned</li>
     * </ul>
     * 
     * Multi-threaded. Heavyweight.<br/>
     * <br/>
     * 
     * @param string The call to perform, e.g. <code>toString</code>
     * @param params Parameters the call takes
     * 
     * @return A CoreObject wrapping the results of each invocation.
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
        }).array(Object.class));
    }

    /**
     * Casts all elements to the given type or sets them null if they are not castable.<br/>
     * <br/>
     * 
     * 
     * Examples:
     * <ul>
     * <li><code>$(object).cast(String.class).get(0)</code> - Same as <code>(object instanceof 
     * String) ? (String) object : null</code>.</li>
     * </ul>
     * 
     * Multi-threaded. <br/>
     * <br/>
     * 
     * @param <N> Target type.
     * @param target Class to cast all elements
     * @return A CoreObject wrapping all cast elements.
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
     * Return a CoreClass for all enclosed objects' classes.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$(o1, o2).clazz()</code> - Wraps the classes for <code>o1</code> 
     * and <code>o2</code> in a {@link CoreClass}.</li>
     * </ul>
     * 
     * Multi-threaded. <br/>
     * <br/>
     * 
     * @return A new {@link CoreClass} containing the classes for all objects.
     */
    @SuppressWarnings("unchecked")
    public CoreClass<T> clazz() {
        return new CoreClass<T>(this.commonCore, map(new F1<T, Class<T>>() {
            @Override
            public Class<T> f(T x) {
                return (Class<T>) x.getClass();
            }
        }).array(Class.class));
    }

    /**
     * Returns a compacted core whose underlying array does not
     * contain null anymore, therefore the positions of elements will be moved to the left to
     * fill null values.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$("a", null, "b").compact()</code> - Returns a core that
     * only contains the elements <code>a</code> and <code>b</code> and has a 
     * size of <code>2</code> (the original core also contains <code>null</code> 
     * and has a size of <code>3</code>). </li>
     * </ul>
     * 
     * Single-threaded. <br/>
     * <br/>
     * 
     * @return A new CoreObject of the same type, with a (probably) reduced size without
     * any null element.
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
     * Creates a {@link Compound} out of this core's content. A Compound is a String -> Object 
     * map, which is useful for quickly creating complex objects which should be handled by the 
     * framework.<br/>
     * <br/>
     *     
     * Examples:
     * <ul>
     * <li><code>$("name", name, "age", age).compound()</code> - Quickly creates an 
     * untyped {@link Compound} with the keys <code>name</code> and <code>age</code> and 
     * the corresponding values.</li>
     * </ul>
     * 
     * 
     * Single-threaded. <br/>
     * <br/>
     * 
     * @return A new {@link Compound} with this core's content.
     */
    public Compound compound() {
        return Compound.create(this.t);
    }

    /**
     * Returns true if this core contains the given object. An object is contained if there is
     * another object in this core that is equal to it. <br/>
     * <br/>
     * 
     * Note that on a {@link CoreString} this method
     * does <b>not behave as</b> <code>String.contains()</code> (which checks for substrings). 
     * If you want to do a substring search, use <code>CoreString.containssubstr()</code>.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$(a, b, c, d).contains(c)</code> - Returns <code>true</code>.</li> 
     * <li><code>$("aa", "bb", "cc").contains("bb")</code> - Returns <code>true</code>.</li>
     * <li><code>$("aa", "bb", "cc").contains("b")</code> - Returns <b><code>false</code></b>!</li>
     * </ul>
     * 
     * Single-threaded. <br/>
     * <br/>
     * 
     * @param object The object to search for. A search for null will always return false.
     * @return True if the object is there, false if not.
     */
    public boolean contains(final T object) {
        // TODO: Parallelize me.
        for (int i = 0; i < size(); i++) {
            if (this.t[i] != null && this.t[i].equals(object)) return true;
        }

        return false;
    }
    
    
    /**
     * Counts how many times each unique item is contained in this core (i.e., computes a 
     * histogram). <br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$("a", "a", "b").contains().value("a")</code> - Returns 2.</li>
     * </ul>
     * 
     * Single-threaded. <br/>
     * <br/>
     * 
     * @return A CoreMap with the counts for each unique object.
     */
    @SuppressWarnings("boxing")
    public CoreMap<T, Integer> count() {
    	final Map<T, Integer> results = new HashMap<T, Integer>();
    	
    	// Now generate the histogram.
    	for(int i = 0; i < size(); i++) {
    	    final T e = this.t[i];
    	    if(e == null) continue;
    	    
    	    if(results.containsKey(e)) {
    	        results.put(e, results.get(e) + 1);
    	    } else {
    	        results.put(e, 1);
    	    }
    	}
    	
    	// Eventually return the results
    	return new CoreMap<T, Integer>(this.commonCore, Wrapper.convert(results));
    }


    /**
     * Prints debug output to the console. Useful for figuring out what's going wrong in a
     * chain of map() operations.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>...somecore.debug().map(f).debug()... </code> - Typical pattern to 
     * figure out why a function (<code>map()</code> in this case) might go wrong.</li> 
     * </ul>
     * 
     * Single-threaded. <br/>
     * <br/>
     * 
     * @return This object again.
     */
    @SuppressWarnings("unused")
    public CoreObject<T> debug() {

        // Print the result
        System.out.println(fingerprint(false));

        // And add it to the debug GUI
        ManagerDebugGUI debugGUI = this.commonCore.manager(ManagerDebugGUI.class);

        return this;
    }

    /**
     * Returns a core of length size() - 1 consisting of the results of the delta
     * function. Delta always takes two adjacent elements and execute stores the
     * delta function's output. In contrast to the common map operation this function
     * does not ignore <code>null</code> elements. If of two adjacent slots any
     * is <code>null</code>, the value <code>null</code> will be stored. <br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$("a", "b", "c").delta(joiner)</code> - If the given delta function
     * joins two elements then the resulting core contains <code>"ab"</code> and 
     * <code>"bc"</code>.</li> 
     * </ul>  
     * 
     * Multi-threaded. <br/>
     * <br/>
     * 
     * @param delta The delta function, taking two elements and return a result.
     * @param <R> Type of the result.
     * @param options Relevant options: <code>OptionMapType</code>.
     * 
     * @return A core of size n - 1 containing all deltas.
     */
    @SuppressWarnings("unchecked")
    public <R> CoreObject<R> delta(final F2DeltaObjects<T, R> delta, Option... options) {
        // Create mapper
        final int size = size();
        final Mapper<T, R> mapper = new Mapper<T, R>(this, options) {
            @Override
            public void handle(int i) {
                // We don't handle the last iteration
                if (i == size - 1) return;

                // Get our target-array (if it is already there)
                R[] a = this.returnArray.get();

                // Get the in-value from the source-array
                final T ii = CoreObject.this.t[i];
                final T jj = CoreObject.this.t[i + 1];

                // Convert
                if (ii == null || jj == null) return;

                final R out = delta.f(ii, jj);

                if (out == null) return;

                // If we haven't had an in-array, create it now, according to the return type
                if (a == null) {
                    a = updateReturnArray((R[]) Array.newInstance(out.getClass(), size));
                }

                // Eventually set the out value
                a[i] = out;
            }
        };

        // Map ...
        map(mapper, options);

        // ... and return result.
        return new CoreObject<R>(this.commonCore, mapper.getFinalReturnArray());

    }

    /**
     * Returns a single object that, if any of its functions is executed, the
     * corresponding function is executed on all enclosed elements. Only works 
     * if <code>c</code> is an interface and only on enclosed elements implementing 
     * <code>c</code>. From a performance perspective this method only makes sense 
     * if the requested operation is complex, as on simple methods the reflection 
     * costs will outweigh all benefits. Also note that all return values are skipped. <br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$(x1, x2, x3, x4, x5).each(XInterface.class).x()</code> - Given all 
     * objects implement <code>XInterface</code> the function <code>each()</code> returns
     * a new <code>X</code> object that, when <code>x()</code> is executed on it, the function
     * is executed on all enclosed objects in parallel.</li> 
     * </ul>  
     * 
     * Multi-threaded. Heavyweight.<br/>
     * <br/>
     * 
     * @param c The interface to use.
     * @param <X> The interface's type.
     * 
     * @return Something implementing c that acts on each element implementing c.
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

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof CoreObject)) return false;

        final CoreObject<?> other = (CoreObject<?>) obj;

        return Arrays.deepEquals(this.t, other.t);
    }

    /**
     * Expands contained arrays into a single array of the given type. This means, 
     * if this core wraps a number of cores, collections, lists or arrays, each of 
     * which are containing elements on their own, <code>expand()</code> will break 
     * up all of these lists and return a single CoreObject wrapping the union of 
     * everything that was previously held in them.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$("a", $("b", "c"), new String[]{"d", "e"}).expand(String.class)</code>
     *  - Returns a core of size <code>5</code>, directly containing the elements 
     * <code>a</code>, <code>b</code>, <code>c</code>,  <code>d</code> and <code>e</code>.</li> 
     * </ul>  
     * 
     * Single-threaded.<br/>
     * <br/>
     * 
     * @param <N> Type of the return core.
     * @param class1 Defines the class element of the returned core's array.
     * @return A CoreObject wrapping all broken up collections, arrays, ...
     */
    @SuppressWarnings("unchecked")
    public <N> CoreObject<N> expand(Class<N> class1) {
        int length = 0;

        if (size() == 0) return new CoreObject<N>(this.commonCore, class1, null);

        // Compute overall size
        for (T x : this.t) {
            if (x == null) continue;

            // Is it a collection?
            if (x instanceof Collection<?>) {
                length += ((Collection<?>) x).size();
                continue;
            }

            // Is it a core?
            if (x instanceof CoreObject<?>) {
                length += ((CoreObject<?>) x).size();
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

            // Is it a core?
            if (x instanceof CoreObject<?>) {
                Object[] array = ((CoreObject<?>) x).array(Object.class);
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
            } catch (IllegalArgumentException e) {
                //
            }

            // A single element?
            Array.set(n, offset++, x);
        }

        return new CoreObject<N>(this.commonCore, n);
    }

    /**
     * Sends a request to the developers requesting a feature with the given name. The
     * request will be sent to a server and collected. Information of the enclosed objects and the
     * feature request string will be transmitted as well.
     * 
     * Examples:
     * <ul>
     * <li><code>$("abba").featurerequest(".palindrome() -- Should be supported!")</code>
     * </li> 
     * </ul>  
     * 
     * @param functionName Call this function for example like this
     * $(myobjects).featurerequest(".compress() -- Should compress the given objects.");
     */
    public void featurerequest(String functionName) {
        this.commonCore.manager(ManagerDeveloperFeedback.class).featurerequest(functionName, fingerprint(true));
    }

    /**
     * Returns a new core with all null elements set to <code>fillValue</code>, the other 
     * elements are transferred unchanged.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$("a", null, "b").fill("x")</code> - Returns a core where the 
     * <code>null</code> element is set to <code>"x"</code></li> 
     * </ul>  
     * 
     * Single-threaded.<br/>
     * <br/>
     * 
     * @param fillValue Value used to fill up all <code>null</code> slots.
     * 
     * @return A filled up CoreObject.
     */
    public CoreObject<T> fill(T fillValue) {
        if (size() == 0) return this;

        final T[] copy = Arrays.copyOf(this.t, size());

        for (int i = 0; i < copy.length; i++) {
            copy[i] = copy[i] == null ? fillValue : copy[i];
        }

        return new CoreObject<T>(this.commonCore, copy);
    }

    /**
     * Filters the object using the given function. A compacted array will be returned
     * that contains only values for which f returned true.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$("a", "bb", "ccc").filter(f)</code> - Given the filter function returns
     * true for all elements with a length <code>&gt;=2</code> the resulting core contains
     * <code>"bb"</code> and <code>"ccc"</code>.</li> 
     * </ul>  
     * 
     * Multi-threaded.<br/>
     * <br/>
     * 
     * @param f If f returns true the object is kept.
     * @param options Supports INVERT_SELECTION if the filter logic should be inverted
     * (options that match will not be considered).
     * 
     * @return A new CoreObject of our type, containing only kept elements.
     */
    public CoreObject<T> filter(final F1Object2Bool<T> f, Option... options) {
        final boolean invert = $(options).contains(Option.INVERT_SELECTION);
        final CoreObject<T> rval = map(new F1<T, T>() {
            public T f(T x) {
                final boolean result = f.f(x);

                if ((!invert && result) || (invert == !result)) return x;

                return null;
            }
        });

        return rval.compact();
    }

    /**
     * Filters all object by their toString() value using the given regular
     * expression. Only elements that match the regular expression are being kept.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$("ax", "bx", "cy").filter(".y")</code> - Returns a core containing
     * <code>"cy"</code>.</li> 
     * </ul>  
     * 
     * Multi-threaded.<br/>
     * <br/>
     * 
     * @param regex The regular expression to use.
     * @param options Supports INVERT_SELECTION if the filter logic should be inverted
     * (options that match the regular expression will not be considered).
     * 
     * @return A CoreObject containing a filtered subset of our elements.
     */
    public CoreObject<T> filter(final String regex, Option... options) {
        final Pattern p = Pattern.compile(regex);

        return filter(new F1Object2Bool<T>() {
            public boolean f(T x) {
                final Matcher matcher = p.matcher(x.toString());
                return matcher.matches();
            }
        }, options);
    }

    /**
     * Generates a textual fingerprint for this element for debugging purporses.
     * 
     * @param detailed If the fingerprint should contain detailed information or not.
     * @return A user-readable string which can be printed.
     */
    protected String fingerprint(boolean detailed) {
        final StringBuilder sb = new StringBuilder();
        sb.append("@(");
        sb.append(getClass().getSimpleName());
        sb.append("; outerSize:");
        sb.append(size());
        sb.append("; innerSize:");

        // Append inner size
        if (this.t != null) {
            Object first = null;
            int ctr = 0;

            // Count elements and extract first nonnull element.
            for (int i = 0; i < this.t.length; i++) {
                if (this.t[i] != null) {
                    ctr++;
                    if (first == null) first = this.t[i];
                }
            }
            sb.append(ctr);

            // Append type of first element (disabled)
            if (first != null && detailed) {
                sb.append("; firstElement:");
                sb.append(first.getClass().getSimpleName());
            }
        } else {
            sb.append("null");
        }

        // Append fingerprint
        if (this.t != null && this.t.length <= 16) {
            sb.append("; fingerprint:");
            for (int i = 0; i < this.t.length; i++) {
                if (this.t[i] != null) {
                    sb.append(this.t[i].getClass().getSimpleName().charAt(0));
                } else
                    sb.append(".");
            }
        }

        sb.append(")");
        return sb.toString();
    }

    /**
     * Folds the given object, multi-threaded version. Fold removes two arbitrary elements,
     * executes <code>f()</code> on them and stores the result again. This is done in parallel until
     * only one element remains.<br/>
     * <br/>
     * 
     * It is guaranteed that each element will have been compared at least once, but the chronological-
     * or parameter-order when and where this occurs is, in contrast to <code>reduce()</code>, not defined.
     * <code>Null</code> elements are gracefully ignored.<br/>
     * <br/>
     * 
     * At present, <code>reduce()</code> is much faster for simple operations and small cores, as it involves much less
     * synchronization overhead, while <code>fold()</code> has advantages especially
     * with very complex <code>f</code> operators.<br/>
     * <br/>
     *
     * 
     * Examples:
     * <ul>
     * <li><code>$(1, 2, 3, 4).fold(fmax)</code> - When <code>fmax</code> returns the larger
     * of both objects the resulting core will contain <code>4</code>.</li> 
     * </ul>  
     * 
     * Multi-threaded. Heavyweight.<br/>
     * <br/>
     * 
     * @param f The reduce function. Takes two elements, returns one.
     * @param options Relevant options: <code>OptionMapType</code>.
     * @return A CoreObject, containing at most a single element.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public CoreObject<T> fold(final F2ReduceObjects<T> f, Option... options) {

        // In case we only have zero or one elements, don't do anything
        if (size() <= 1) return this;

        final AtomicReferenceArray array = new AtomicReferenceArray(this.t);
        final Folder<T> folder = new Folder<T>(this) {
            @Override
            public void handle(int i, int j, int destination) {
                // Get the in-value from the source-array

                final T ii = (T) array.get(i);
                final T jj = (T) array.get(j);

                if (ii == null && jj == null) return;
                if (ii == null && jj != null) {
                    array.set(destination, jj);
                    return;
                }

                if (ii != null && jj == null) {
                    array.set(destination, ii);
                    return;
                }

                array.set(destination, f.f(ii, jj));
            }
        };

        
        // Now do fold ...
        fold(folder, options);
        
        T[] target = Arrays.copyOf(this.t, 1);
        target[0] = (T) array.get(0);

        // ... and return result.
        return new CoreObject<T>(this.commonCore, target);
    }

    /**
     * Performs the given operation on each element and returns a new core. This is the
     * single-threaded version of map().<br/>
     * <br/>
     * 
     * 
     * Examples:
     * <ul>
     * <li><code>$("a", "b", null, "c").forEach(f)</code> - Performs the operation <code>f</code> on each
     * element of the core, except <code>null</code>.</li> 
     * </ul>  
     * 
     * Single-threaded.<br/>
     * <br/>
     * 
     * @param <R> Return type.
     * @param f Mapper function.
     * @param _options Relevant options: <code>OptionMapType</code>.
     * 
     * @return A CoreObject containing the mapped elements in a stable order.
     */
    @SuppressWarnings("unchecked")
    public <R> CoreObject<R> forEach(final F1<T, R> f, Option... _options) {

        // Create a mapper and iterate over it
        final Mapper<T, R> mapper = mapper(f, _options);
        for (int i = 0; i < size(); i++) {
            mapper.handle(i);
        }

        // ... and return result.
        return new CoreObject<R>(this.commonCore, mapper.getFinalReturnArray());
    }
    
    /**
     * Performs the given operation for each <code>n</code> elements of this core. 
     * Elements will only be used once, so for example <code>forEach(f, 2)</code> means 
     * that <code>f</code> will be called with elements <code>f(0, 1)</code>,  
     * <code>f(2, 3)</code> ... Remaining elements are ignored. This function also acts on
     * <code>null</code> elements. If you don't want that, <code>compact()</code> the core. 
     * 
     * Examples:
     * <ul>
     * <li><code>$("a", "b", null, "c").forEach(f)</code> - Performs the operation <code>f</code> on each
     * element of the core, except <code>null</code>.</li> 
     * </ul>
     * 
     * Single-threaded.<br/>
     * <br/> 
     * 
     * @param <R> The return type.
     * @param f The function to execute for each <code>n</code> elements.
     * @param n The number of elements to put into <code>f</code>
     * @return A core with the new elements.
     */
    @SuppressWarnings("unchecked")
    public <R> CoreObject<R> forEach(final Fn<T, R> f, int n) {
        if(size() == 0) return new CoreObject<R>(this.commonCore, null, null);
        
        R[] rval = null;
        T[] slice = Arrays.copyOf(this.t, n);
        
        int ptr = 0;
        int tptr = 0;
        
        // Now go over the array
        for (int i = 0; i < size(); i++) {
            T e = this.t[i];
            
            // When our current element is null, do nothing.
            if(e == null) continue;
            
            // Store element to slice
            slice[ptr++] = e;
            
            // If the slice is not full, continue
            if(ptr < n) continue;
            
            // Execute the call
            R result = f.f(slice);
            
            // If we have a result, create result array
            if(rval == null && result != null) {
                rval = (R[]) Array.newInstance(result.getClass(), size() / n);
            }
            
            // If we have the arry, store the result
            if(rval != null) {
                rval[tptr] = result;
            }
            
            // Increase the target ptr in any case and reset the source ptr 
            tptr++;
            ptr = 0;
        }

        // ... and return result.
        return new CoreObject<R>(this.commonCore, rval);
    }
    
        

    /**
     * Return the element at the the given relative position (0 <= x <= 1) or return 
     * <code>dflt</code> if that element
     * is null.<br/>
     * <br/>
     * 
     * 
     * Examples:
     * <ul>
     * <li><code>$("a", null, "c").get(0.5, "b")</code> - Returns <code>"b"</code>.</li> 
     * </ul>
     * 
     * Single-threaded. <br/>
     * <br/>
     * 
     * @param percent 0.0 returns the first element, 1.0 the last element, 0.5 returns the
     * element in the
     * middle, and so on.
     * @param dflt The value to return if null had been returned otherwise.
     * 
     * @return The value at the requested position, or dflt if there is none.
     */
    public T get(double percent, T dflt) {
        if (Double.isNaN(percent)) return dflt;
        if (percent < 0) return dflt;
        if (percent > 1) return dflt;

        if (size() == 0) return dflt;

        int offset = (int) (percent * size());
        if (offset >= this.t.length) return dflt;

        return this.t[offset];
    }

    /**
     * Return an element at the the relative position (0 <= x <= 1).<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$("a", "b", "c", "d", "e").get(0.75)</code> - Returns <code>"d"</code>.</li> 
     * </ul>
     * 
     * Single-threaded. <br/>
     * <br/>
     * 
     * @param percent 0.0 returns the first element, 1.0 the last element, 0.5 returns the
     * element in the
     * middle, and so on.
     * 
     * @return The value at the requested position, or null if there is none.
     */
    public T get(double percent) {
        return get(percent, null);
    }

    /**
     * Returns the first element that is an instance of the requested type.
     * 
     * Examples:
     * <ul>
     * <li><code>$(1, new Object(), "Hi").get(String.class, "Oops")</code> - Returns <code>"Hi"</code>.</li> 
     * </ul>
     * 
     * 
     * Single-threaded. Heavyweight.<br/>
     * <br/>
     * 
     * @param <X> Subtype to request.
     * @param request A subclass of our type to request.
     * @param dflt The value to return when no class was found.
     * 
     * @return The first object that is assignable to request, or dflt if there was no
     * such element.
     */
    @SuppressWarnings("unchecked")
    public <X extends T> X get(Class<X> request, X dflt) {
        for (int i = 0; i < size(); i++) {
            if (this.t[i] != null && request.isAssignableFrom(this.t[i].getClass()))
                return (X) this.t[i];
        }
        return dflt;
    }

    /**
     * Return the ith element.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$("a", "b", "c").get(-1)</code> - Returns <code>"c"</code>.</li> 
     * </ul>
     * 
     * 
     * Single-threaded. <br/>
     * <br/>
     * 
     * @param i Position to retrieve. Negative indices are treated as values starting at
     * the end (i.e., -1 is the last element, -2 the second-last, ...)
     * 
     * @return The element at the given position.
     */
    public T get(int i) {
        final int offset = indexToOffset(i);

        if (this.t == null || offset < 0) return null;

        return this.t[offset];
    }

    /**
     * Return the ith element or dflt if the element if otherwise <code>null</code> had
     * been returned.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$("a", "b", null).get(2, "c")</code> - Returns <code>"c"</code>.</li> 
     * </ul>
     * 
     * Single-threaded. <br/>
     * <br/>
     * 
     * @param i Position to retrieve. Negative indices are treated as values starting at
     * the end (i.e., -1 is the last element, -2 the second-last, ...)
     * @param dflt The value to return if null had been returned.
     * 
     * @return Unless dflt is null, this function is guaranteed to return a non-null
     * value.
     */
    public T get(int i, T dflt) {
        final T rval = get(i);
        return rval == null ? dflt : rval;
    }

    /**
     * Returns the first element, or, if there is none, return dflt.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$(name).get("Unknown")</code> - Returns <code>"Unknown"</code> 
     * if <code>name</code> is <code>null</code>.</li> 
     * </ul>
     * 
     * Single-threaded. <br/>
     * <br/>
     * 
     * @param dflt The value to return if get(0) is null.
     * @return Unless dflt is null, this function is guaranteed to return a non-null
     * value.
     */
    public T get(T dflt) {
        return get(0, dflt);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Arrays.deepHashCode(this.t);
    }

    /**
     * Checks if all elements are not null.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$("a", null, "b").hasAll()</code> - Returns <code>false</code>.</li> 
     * </ul>
     * 
     * Single-threaded. <br/>
     * <br/>
     * 
     * @return True if all elements are not null, false if a single element was null.
     */
    public boolean hasAll() {
        if (this.t == null) return false;

        for (int i = 0; i < this.t.length; i++) {
            if (this.t[i] == null) return false;
        }

        return true;
    }

    /**
     * Checks if the element has any element.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$(null, "b").hasAny()</code> - Returns <code>true</code>.</li> 
     * </ul>
     * 
     * Single-threaded. <br/>
     * <br/>
     * 
     * @return True if any element is set. False if all elements are null.
     */
    public boolean hasAny() {
        if (this.t == null) return false;

        for (int i = 0; i < this.t.length; i++) {
            if (this.t[i] != null) return true;
        }

        return false;
    }

    /**
     * If all elements are present, execute f0.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$(null, "b").ifAll(f)</code> - Does not execute f.</li> 
     * </ul>
     * 
     * Single-threaded. <br/>
     * <br/>
     * 
     * @param f0 The function to execute if all elements are given.
     * @return This core.
     */
    public CoreObject<T> ifAll(F0 f0) {
        if (hasAll()) f0.f();
        return this;
    }

    /**
     * Returns the first index positions for all objects equal to the given object, or null if no object
     * equalled the given one.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$("a", "c", "b").index("c", "b", "a")</code> - Returns a core <code>$(1, 2, 0)</code>.</li> 
     * </ul>
     * 
     * Single-threaded. <br/>
     * <br/>
     * 
     * @param objects The objects to return the first index for.
     * @return A {@link CoreNumber} object with the corresponding index position.
     */
    @SuppressWarnings("boxing")
    public CoreNumber index(T... objects) {
        if (objects == null) return new CoreNumber(this.commonCore, new Number[0]);
        Integer indices[] = new Integer[objects.length];

        
        // Check all objects ...
        for (int i = 0; i < objects.length; i++) {
            final T obj = objects[i];
            if (obj == null) continue;

            // If there is a match in our core
            for (int j = 0; j < size(); j++) {
                // If there is, store the index of our object at the corresponding query position.
                if (obj.equals(get(j))) {
                    indices[i] = j;
                    break;
                }
            }
        }
        
        return new CoreNumber(this.commonCore, indices);
    }

    /**
     * Returns a core intersected with another core.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$("x", "y", "z").intersect($("y", "z"))</code> - Returns a core <code>$("y", "z")</code>.</li> 
     * </ul>
     * 
     * Single-threaded. <br/>
     * <br/>
     * 
     * @param other The other core to intersect.
     * @return Returns a core enclosing only objects present in this and the other core.
     */
    public CoreObject<T> intersect(CoreObject<T> other) {
        if (size() == 0) return this;
        if (other.size() == 0) return other;

        final T[] copy = Arrays.copyOf(this.t, size());

        // Remove every element we in the other core
        for (int i = 0; i < copy.length; i++) {
            final T element = copy[i];
            if (element == null) continue;

            boolean found = false;

            // Check if the copy contains the element
            for (int j = 0; j < other.size(); j++) {
                final T x = other.get(j);
                if (x == null || !x.equals(element)) continue;
                found = true;
                break;
            }

            // If not in both, remove.
            if (!found) {
                copy[i] = null;
            }
        }

        // Return a compacted core.
        return new CoreObject<T>(this.commonCore, copy).compact();
    }

    /**
     * Returns a core intersected with another array.<br/>
     * <br/>
     * 
     * 
     * Examples:
     * <ul>
     * <li><code>$("x", "y", "z").intersect("a", "x", "b")</code> - Returns a core <code>$("x")</code>.</li> 
     * </ul>
     * 
     * Single-threaded. <br/>
     * <br/>
     * 
     * @param other The array to intersect.
     * @return Returns a core enclosing only objects present in this core and the other array.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public CoreObject<T> intersect(T... other) {
        return intersect(new CoreObject(this.commonCore, other));
    }

    /**
     * Returns the wrapped collection as a list.<br/>
     * <br/>
     *
     * Examples:
     * <ul>
     * <li><code>$(array).list()</code> - Returns a typed {@link List} for the given array.</li> 
     * </ul>
     * 
     * Single-threaded. <br/>
     * <br/>
     * 
     * @return A list containing all elements. Null values should be preserved.
     */
    public List<T> list() {
        if (this.t == null) return new ArrayList<T>();
        return new ArrayList<T>(Arrays.asList(this.t));
    }

    /**
     * Maps the core's content with the given function and returns the result. This is the
     * most fundamental function of jCores. If the core is of size 0 nothing is done, if it is of size 1 
     * <code>f</code> is executed directly. In all other cases <code>map</code> (at least in the 
     * current implementation) will go parallel <i>on demand</i>. It takes a test run for the 
     * first element and measures the time to process it. If the estimated time it takes to 
     * complete the rest of the core is less than the measured time it takes to go parallel (which 
     * has some overhead), no parallelization is being performed.<br/>
     * <br/>
     * 
     * As a general rule of thumb, <code>map</code> 
     * works relatively best on large cores (number of elements) and time-consuming <code>f</code>- 
     * operations and it works worst on small cores and very simple <code>f</code>. However, 
     * the good message is that these disadvantageous cores/functions only impact your 
     * application's performance in case you call <code>map</code> on them hundreds of 
     * thousands times a second, as the absolute overhead is still very small.<br/>
     * <br/>
     * 
     *
     * Examples:
     * <ul>
     * <li><code>$.range(1000).map(convert)</code> - Given <code>convert</code> performs some conversion, 
     * this would convert the numbers from 0 to 999 in parallel (using as many CPUs as there are 
     * available).</li> 
     * </ul>
     * 
     * Multi-threaded.<br/>
     * <br/>
     * 
     * <b>Warning:</b> The larger the core (number of objects) the more you should make sure 
     * that <code>f</code> is as <i>isolated</i> as possible, since sharing even a single object 
     * (e.g., a shared {@link Random} object like <code>$.random()</code>) among different mapper 
     * threads can have a dramatic performance impact. In some cases (e.g., many CPUs (>2), large 
     * core (<code>size >> 1000</code>), simple <code>f</code> with shared, synchronized variable 
     * access) the performance of <code>map</code> can even drop below the performance of 
     * <code>forEach</code>.<br/> <br/>
     * 
     * 
     * @param <R> Return type.
     * @param f Mapper function, must be thread-safe.
     * @param _options Relevant options: <code>OptionMapType</code>.
     * 
     * @return A CoreObject containing the mapped elements in a stable order.
     */
    @SuppressWarnings("unchecked")
    public <R> CoreObject<R> map(final F1<T, R> f, Option... _options) {

        // Map what we got
        final Mapper<T, R> mapper = mapper(f, _options);
        map(mapper, _options);

        // ... and return result.
        return new CoreObject<R>(this.commonCore, mapper.getFinalReturnArray());
    }

    /**
     * Prints all strings to the console. Almost the same as <code>string().print()</code>,
     * except that this method returns a CoreObject again.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$("a", "b", "c").print().intersect("a").print()</code> - The first output
     * will be <code>a b c</code>, then again <code>a</code>.</li> 
     * </ul>
     * 
     * Single-threaded.<br/>
     * <br/>
     * 
     * @return Returns this CoreObject object again.
     */
    public CoreObject<T> print() {
        if (size() == 0) return this;

        for (Object s : this.t) {
            if (s == null) continue;
            System.out.println(s);
        }

        return this;
    }
    

    /**
     * Prints all strings to the console in a single line with the given joiner. 
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$("a", "b", "c").print(",")</code> - Prints 
     * <code>a,b,c</code></li> 
     * </ul>
     * 
     * Single-threaded.<br/>
     * <br/>
     * 
     * @since 1.0
     * @param joiner The string to put in between the elements. 
     * @return Returns this CoreObject object again.
     */
    public CoreObject<T> print(String joiner) {
        System.out.println(string().join(joiner));
        return this;
    }
    

    /**
     * Returns a randomly selected object, including null values.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$("a", "b", "c").random()</code> - Returns ... well, we don't know yet.</li> 
     * </ul>
     * 
     * Single-threaded.<br/>
     * <br/>
     * 
     * @return A randomly selected object from this core.
     */
    public T random() {
        final int size = size();

        if (size == 0) return null;

        return this.t[this.commonCore.random().nextInt(size)];
    }

    /**
     * Returns a randomly selected subset, including null values. The elements will be
     * returned in a random order. Elements will never be drawn twice.<br/>
     * <br/>
     * 
     * 
     * Examples:
     * <ul>
     * <li><code>$("a", "b", "c", "d").random(0.5)</code> - Could return <code>$("c", "a")</code>, 
     * but never <code>$("b", "b")</code>.</li> 
     * </ul>
     * 
     * 
     * Single-threaded.<br/>
     * <br/>
     * 
     * @param percent Specifies how many percent of elements of this core should be in the
     * resulting core. For example <code>0.5</code> means that half of the elements of
     * this core will be randomly selected and returned, <code>0.0</code> means an empty
     * core will be returned and <code>1.0</code> a shuffled core will be returned.
     * 
     * @return A core enclosing randomly selected objects.
     */
    public CoreObject<T> random(double percent) {
        final double p = Math.max(Math.min(1.0, percent), 0.0);
        return random((int) (p * size()));
    }

    /**
     * Returns a randomly selected subset, including null values. The elements will be
     * returned in a random order.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$("a", "b", "c", "d").random(2)</code> - Same as <code>.random(0.5)</code>
     * in the example above.</li> 
     * </ul>
     * 
     * Single-threaded.<br/>
     * <br/>
     * 
     * @param newSize Specifies how many elements of this core should be in the
     * resulting core.
     * 
     * @return A core enclosing randomly selected objects.
     */
    public CoreObject<T> random(int newSize) {
        final int size = size();

        if (size == 0) return this;

        // Create a shuffletable
        final T[] copyOf = Arrays.copyOf(this.t, size);

        // Shuffle the copy
        for (int i = copyOf.length - 1; i >= 1; i--) {
            int j = this.commonCore.random().nextInt(i + 1);

            T x = copyOf[j];
            copyOf[j] = copyOf[i];
            copyOf[i] = x;
        }

        // And return the first newSize elements
        return new CoreObject<T>(this.commonCore, Arrays.copyOfRange(copyOf, 0, newSize));
    }

    /**
     * Reduces the given object, single-threaded version. Reduce takes the two leftmost elements,
     * executes <code>f()</code> on them and stores the result again as the leftmost element. This
     * is done until only one element remains. <code>Null</code> elements are gracefully ignored.<br/>
     * <br/>
     * 
     * In contrast to fold() the order in which two element might be reduced is well defined
     * from left to right. You should use <code>reduce()</code> for simple operations and <code>fold()</code> for very
     * complex operations.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$("a", "b", null, "c", "d").reduce(fjoin)</code> - When <code>fjoin</code> joins the left
     * and right String the resulting core will be <code>$("abcd")</code>.</li> 
     * </ul>  
     * 
     * Single-threaded.<br/>
     * <br/>
     * 
     * @param f The reduce function. Takes two elements, returns one.
     * @param options Relevant options: <code>OptionMapType</code>.
     * @return A CoreObject, containing at most a single element.
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
     * Returns a Core with the element order reversed.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$("a", "b", "c").reverse()</code> - Returns a core <code>$("c", "b", "a")</code>.</li> 
     * </ul>  
     * 
     * Single-threaded.<br/>
     * <br/>
     * 
     * @return A CoreObject with reversed element order.
     */
    public CoreObject<T> reverse() {
        if(size() == 0) return this;
        
        final T[] c = Arrays.copyOf(this.t, this.t.length);
        for (int i = 0; i < size(); i++) {
            c[i] = this.t[this.t.length - i];
        }

        return new CoreObject<T>(this.commonCore, c);
    }
    
    
    /**
     * Serializes this core into the given file. Objects that are not serializable
     * are ignored. The file can later be restored with the function 
     * <code>deserialize()</code> in {@link CoreFile}.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$("Hello", "World").serialize("data.ser")</code> - Writes the core to a file.</li> 
     * </ul>  
     * 
     * Single-threaded.<br/>
     * <br/>
     * 
     * @param path The location to which this core should be serialized.
     * @param options Currently not used.
     * @return This core.
     */
    public CoreObject<T> serialize(final String path, Option... options) {
        try {
            StreamUtils.serializeCore(this, new FileOutputStream(new File(path)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return this;
    }

    /**
     * Returns how many slots are in this core, counting null elements.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$("a", "b").size()</code> - Returns 2.</li> 
     * <li><code>$("c", null, "d").size()</code> - Returns 3.</li> 
     * <li><code>$(null, "e", "f").compact().size()</code> - Returns 2.</li> 
     * </ul>  
     * 
     * Single-threaded. <br/>
     * <br/>
     * 
     * @see Core#size()
     * @return .
     */
    @Override
    public int size() {
        if (this.t == null) return 0;
        return this.t.length;
    }

    /**
     * Returns a slice of this core. Element inside this slice start at <code>start</code>.
     * If <code>length</code> is positive it is treated as a length, if it is negative, it 
     * is treated as a (inclusive) end-index.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$("a", "b", "c", "d").slice(0, 2)</code> - Returns <code>$("a", "b")</code>.</li> 
     * <li><code>$("a", "b", "c", "d").slice(1, -1)</code> - Returns <code>$("b", "c", "d")</code>.</li> 
     * <li><code>$("a", "b", "c", "d").slice(-2, 2)</code> - Returns <code>$("c", "d")</code>.</li> 
     * </ul>  
     * 
     * Single-threaded. <br/>
     * <br/>
     * 
     * @param start The start position.
     * @param length If length is positive it is treated as length, if negative as a
     * starting position from the end (-1 equals the last position)
     * @return A ObjectCore wrapping all sliced elements.
     */
    public CoreObject<T> slice(final int start, final int length) {
        if (size() == 0) return this;

        final int i = indexToOffset(start);
        final int l = length > 0 ? length : indexToOffset(length) - i + 1;
        
        if (i < 0 || i >= size()) {
            this.commonCore.report(MessageType.MISUSE, "slice() - converted parameter start(" + start + " -> " + i + ") is outside bounds.");
            return new CoreObject<T>(this.commonCore, Arrays.copyOfRange(this.t, 0, 0));
        }
        if (l < 0 || l > size()) {
            this.commonCore.report(MessageType.MISUSE, "slice() - converted parameter length(" + length + " -> " + l + ") is outside bounds.");
            return new CoreObject<T>(this.commonCore, Arrays.copyOfRange(this.t, 0, 0));
        }

        return new CoreObject<T>(this.commonCore, Arrays.copyOfRange(this.t, i, i + l));
    }
    
    

    /**
     * Returns a slice of this core. Element inside this slice start at the relative <code>start</code>
     * and end at the relative <code>end</code>. It must hold <code>0.0 <= start <= end <= 1.0</code>.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$("a", "b", "c", "d").slice(0.0, 0.5)</code> - Returns <code>$("a", "b")</code>.</li> 
     * </ul>  
     * 
     * Single-threaded. <br/>
     * <br/>
     * 
     * @param start The relative start position.
     * @param end The relative end.
     * starting position from the end (-1 equals the last position)
     * @return A ObjectCore wrapping all sliced elements.
     */
    public CoreObject<T> slice(final double start, final double end) {
        if (size() == 0) return this;
        if (start > end) return slice(0, 0);
        
        final double s = $.limit(0, start, 1.0);
        final double e = $.limit(0, end, 1.0);

        int size = size();
        int a = (int) (s * size);
        int b = (int) (e * size);
        
        return slice(a, b-a);
    }


    /**
     * Returns a new, sorted core using the given {@link Comparator}.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$(x, y, z).sort(s)</code> - Returns a sorted core with an order specified by <code>s</code>.</li> 
     * </ul>  
     * 
     * Single-threaded. <br/>
     * <br/>
     * 
     * @param c Comparator to use. Caution, <code>c</code> will be called with <code>null</code> values
     * for non-existing elements, <code>compact()</code> the core before if you don't want
     * this behavior.
     * @return A CoreObject with sorted entries.
     */
    public CoreObject<T> sort(Comparator<T> c) {
        if (size() == 0) return this;

        final T[] copyOf = Arrays.copyOf(this.t, size());
        Arrays.sort(copyOf, c);

        return new CoreObject<T>(this.commonCore, copyOf);
    }

    /**
     * Returns a new, sorted core. If the elements of this core are not sortable
     * (i.e, implementing {@link Comparable}), simply this core will be returned 
     * again.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$("c", "a", "b").sort()</code> - Returns <code>$("a", "b", "c")</code>.</li> 
     * </ul>  
     * 
     * Single-threaded. <br/>
     * <br/>
     * 
     * @return A CoreObject with sorted entries.
     */
    public CoreObject<T> sort() {
        if (size() == 0) return this;

        final T[] copyOf = Arrays.copyOf(this.t, size());

        try {
            Arrays.sort(copyOf);
        } catch (ClassCastException e) {
            this.commonCore.report(MessageType.EXCEPTION, "Unable to sort core, elements not comparable: " + fingerprint(true));
            return this;
        }

        return new CoreObject<T>(this.commonCore, copyOf);
    }

    /**
     * Staples all elements. Assists, for example, in computing the average of a number
     * of elements. <code>staple()</code> is similar to <code>reduce()</code>, with the
     * exception that a given neutral element is used as a starting point, and some some 
     * derived value of each contained element might be connected with it. In the end a 
     * <code>Staple</code> will be returned, containing the stapled value and the actual 
     * number of elements used.<br/>
     * <br/>
     * 
     * Single-threaded. <br/>
     * <br/>
     * 
     * @param neutralElement The initial element.
     * @param sumAndNext A reduce function. Left will be the current sum, right will
     * be the next element.
     * 
     * @return A <code>Staple</code> object, with the current sum and the size.
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
     * Converts all elements to strings by calling <code>.toString()</code> on each
     * element.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$(1, 2).string()</code> - Returns <code>$("1", "2")</code>.</li> 
     * </ul>  
     * 
     * Multi-threaded. <br/>
     * <br/>
     * 
     * @return A CoreString containing all <code>toString()</code> output.
     */
    public CoreString string() {
        return map(new F1<T, String>() {
            public String f(T x) {
                return x.toString();
            }
        }).as(CoreString.class);
    }

    /**
     * Returns a core containing all elements of this core that are not
     * in the passed core.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$("a", "b", "c").subtract($("b"))</code> - Returns <code>$("a", "c")</code>.</li> 
     * </ul>  
     * 
     * Single-threaded. <br/>
     * <br/>
     * 
     * @param toSubtract The core to subtract from this core.
     * 
     * @return A CoreObject containing all objects of this core that are not
     * in the other core.
     */
    public CoreObject<T> subtract(CoreObject<T> toSubtract) {
        if (size() == 0 || toSubtract.size() == 0) return this;

        final T[] copy = Arrays.copyOf(this.t, size());

        // Remove every element we in the other core
        for (int i = 0; i < toSubtract.size(); i++) {
            final T element = toSubtract.get(i);
            if (element == null) continue;

            // Check if the copy contains the element
            for (int j = 0; j < copy.length; j++) {
                final T our = copy[j];
                if (our == null || !our.equals(element)) continue;
                copy[j] = null;
            }
        }

        return new CoreObject<T>(this.commonCore, copy);
    }

    /**
     * Returns a core containing all elements of this core and that are not
     * in the passed array.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$("a", "c").subtract("a", "c")</code> - Returns an empty core <code>$()</code>.</li> 
     * </ul>  
     * 
     * Single-threaded. <br/>
     * <br/>
     * 
     * @param toSubtract The array to subtract from this core.
     * 
     * @return A CoreObject containing all objects of this core that are not
     * in the other array.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public CoreObject<T> subtract(T... toSubtract) {
        return subtract(new CoreObject(this.commonCore, toSubtract));
    }

    /**
     * Returns a core containing only unique objects, i.e., object 
     * mutually un- <code>equal()</code>.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$("a", "c", "a", "b").unique()</code> - Returns <code>$("a", "c", "b")</code>.</li> 
     * </ul>  
     * 
     * Single-threaded. <br/>
     * <br/>
     * 
     * @return A CoreObject containing only unique, non-null objects.
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
     * Returns the core's array. Use of this method is strongly discouraged and 
     * usually only needed in a few very special cases. Do not change the array! <br/>
     * <br/>
     * 
     * Also, even though the method is parameterized, in some cases it does not return 
     * the type of array it indicates due to some black jCores magic (we sometimes haves
     * to 'guess' the type at runtime due to type erasure, which can go wrong when
     * just blindly returning our internal array).<br/>
     * <br/>
     * 
     * In most cases <code>array()</code> should be used instead.<br/>
     * <br/>
     * 
     * Single-threaded.<br/>
     * <br/>
     * 
     * @return A clone of our array.
     */
    public T[] unsafearray() {
        if (this.t == null) return null;
        return this.t.clone();
    }

    /**
     * Converts an index to an offset.
     * 
     * @param index
     * @return An index.
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

    @SuppressWarnings("rawtypes")
    protected final <R> Mapper mapper(final F1<T, R> f, final Option... options) {

        return new Mapper<T, R>(this, options) {
            @SuppressWarnings("unchecked")
            @Override
            public void handle(int i) {
                // Try to get our return array (could be there if we or someone successfully mapped)
                R[] a = this.returnArray.get();

                // Get the in-value from the source-array
                final T in = CoreObject.this.t[i];

                // Convert
                if (in == null) return;
                if (this.options.indexer != null) this.options.indexer.i(i);
                final R out = f.f(in);

                // When we had a results and if we haven't had an in-array, create it now, according to the return type
                if (out == null) return;
                if (a == null)
                    a = updateReturnArray((R[]) Array.newInstance(out.getClass(), this.core.size()));

                // Eventually set the out value
                a[i] = out;
            }
        };
    }
}
