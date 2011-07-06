/*
 * JRECoreObject.java
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
package net.jcores.jre.cores;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import net.jcores.jre.CoreKeeper;
import net.jcores.shared.CommonCore;
import net.jcores.shared.cores.CoreObject;
import net.jcores.shared.cores.adapter.AbstractAdapter;
import net.jcores.shared.interfaces.functions.F1;
import net.jcores.shared.options.MessageType;

/**
 * @author Ralf Biedert
 * 
 * @param <T>
 */
public class CoreObjectJRE<T> extends CoreObject<T> {

    /**
     * @param supercore
     * @param adapter
     */
    public CoreObjectJRE(CommonCore supercore, AbstractAdapter<T> adapter) {
        super(supercore, adapter);
    }

    /**
     * @param supercore
     * @param object
     */
    public CoreObjectJRE(CommonCore supercore, T... object) {
        super(supercore, object);
    }
    

    /**
     * Creates the core object for the given array.
     * 
     * @param supercore CommonCore to use.
     * @param objects Object to wrap.
     */
    public CoreObjectJRE(CommonCore supercore, List<T> objects) {
        super(supercore, objects);
    }

    /**
     * Creates the core object for the given array.
     * 
     * @param supercore CommonCore to use.
     * @param objects Object to wrap.
     */
    public CoreObjectJRE(CommonCore supercore, Collection<T> objects) {
        super(supercore, objects);
    }


    /** */
    private static final long serialVersionUID = -5306674726982099085L;

    /**
     * Performs a generic call on each element of this core (for
     * example <code>core.call("toString()")</code>), or returns a field (for
     * example <code>core.call("field")</code>). The return values will
     * be stored in a {@link CoreObject}. This is a dirty but shorthand way
     * to call the same function on objects that don't share a common superclass. Should not be
     * called within hot-spots (functions called millions of times a second) as it relies heavily on reflection.<br/>
     * <br/>
     * 
     * 
     * Examples:
     * <ul>
     * <li><code>$(tA, tB, tC, tD).call("method()").unique().size()</code> - Calls a method <code>method()</code> on
     * some objects that have no common supertype (except {@link Object}), and returns the number of distinct objects
     * returned</li>
     * <li><code>$(tA, tB, tC, tD).call("field")</code> - Gets the value of the field <code>field</code> on each of the
     * elements, and returns a core with the results.</li>
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
        final CommonCore cc = this.commonCore;

        // Convert classes.
        for (int i = 0; i < len; i++) {
            types[i] = params[i].getClass();
        }

        final boolean methodcall = string.endsWith("()");
        final String call = methodcall ? string.substring(0, string.length() - 2) : string;

        // If this is a method call ...
        if (methodcall) { return map(new F1<T, Object>() {
            public Object f(T x) {
                try {
                    final Method method = x.getClass().getDeclaredMethod(call, types);
                    method.setAccessible(true);
                    return method.invoke(x, params);
                } catch (SecurityException e) {
                    cc.report(MessageType.EXCEPTION, "SecurityException for " + x + " (method was " + string + ")");
                } catch (NoSuchMethodException e) {
                    cc.report(MessageType.EXCEPTION, "NoSuchMethodException for " + x + " (method was " + string + ")");
                } catch (IllegalArgumentException e) {
                    cc.report(MessageType.EXCEPTION, "IllegalArgumentException for " + x + " (method was " + string + ")");
                } catch (IllegalAccessException e) {
                    cc.report(MessageType.EXCEPTION, "IllegalAccessException for " + x + " (method was " + string + ")");
                } catch (InvocationTargetException e) {
                    cc.report(MessageType.EXCEPTION, "InvocationTargetException for " + x + " (method was " + string + ")");
                }

                return null;
            }
        }); }

        // Or a field access
        return map(new F1<T, Object>() {
            public Object f(T x) {
                try {
                    final Field field = x.getClass().getDeclaredField(call);
                    field.setAccessible(true);
                    return field.get(x);
                } catch (SecurityException e) {
                    cc.report(MessageType.EXCEPTION, "SecurityException for " + x + " (method was " + string + ")");
                } catch (IllegalArgumentException e) {
                    cc.report(MessageType.EXCEPTION, "IllegalArgumentException for " + x + " (method was " + string + ")");
                } catch (IllegalAccessException e) {
                    cc.report(MessageType.EXCEPTION, "IllegalAccessException for " + x + " (method was " + string + ")");
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
    }

    /**
     * For each of the contained elements an object of the type <code>wrapper</code> is
     * being created with the element passed as the first argument to the constructor. If the element is
     * already of the type <code>wrapper</code>, nothing is being done for that element.<br/>
     * <br/>
     * 
     * Single-threaded. Heavyweight.<br/>
     * <br/>
     * 
     * @param wrapper The class to spawn for each element.
     * @param <W> The type of the wrapper.
     * 
     * @return A {@link CoreObject} with the wrapped objects. Elements which could not be wrapped are set to
     * <code>null</code>.
     */
    public <W> CoreObject<W> wrap(final Class<W> wrapper) {
        final CoreClassJRE<W> w = CoreKeeper.$(wrapper);

        return forEach(new F1<T, W>() {
            @SuppressWarnings("unchecked")
            @Override
            public W f(T x) {
                // Check if the object is already assignable
                if (wrapper.isAssignableFrom(x.getClass())) return (W) x;

                return w.spawn(x).get(0);
            }
        });
    }

    /**
     * Return a CoreClass for all enclosed objects' classes.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$(o1, o2).clazz()</code> - Wraps the classes for <code>o1</code> and <code>o2</code> in a
     * {@link CoreClassJRE}.</li>
     * </ul>
     * 
     * Multi-threaded. <br/>
     * <br/>
     * 
     * @return A new {@link CoreClassJRE} containing the classes for all objects.
     */
    @SuppressWarnings("unchecked")
    public CoreClassJRE<T> clazz() {
        return new CoreClassJRE<T>(this.commonCore, map(new F1<T, Class<T>>() {
            @Override
            public Class<T> f(T x) {
                return (Class<T>) x.getClass();
            }
        }).array(Class.class));
    }
}
