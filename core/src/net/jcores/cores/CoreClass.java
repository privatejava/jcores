/*
 * CoreClass.java
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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import net.jcores.CommonCore;
import net.jcores.interfaces.functions.F1;
import net.jcores.managers.ManagerClass;
import net.jcores.options.MessageType;

/**
 * Wraps class objects, usually only one, and exposes some convenience functions 
 * on them. 
 * 
 * @author Ralf Biedert
 * @since 1.0
 * @param <T> Type of the classes' objects. 
 */
public class CoreClass<T> extends CoreObject<Class<T>> {
    /** Our class manager */
    protected final ManagerClass manager;

    /** All known constructors. */
    protected final Map<Class<?>[], Constructor<T>> constructors = new HashMap<Class<?>[], Constructor<T>>();

    /**
     * Creates a new CoreClass.
     * 
     * @param supercore CommonCore to use.
     * @param clazzes Classes to wrap.
     */
    public CoreClass(CommonCore supercore, Class<T>... clazzes) {
        super(supercore, clazzes);

        this.manager = supercore.manager(ManagerClass.class);
    }

    /**
     * Spawns the cored class with the given objects as args. This function will only 
     * operate on the core's first entry (get(0)). If the wrapped class is an interface,
     * the last implementor registered with <code>implementor()</code> will be spawned.
     * <br/><br/>
     * Single-threaded, size-of-one.<br/><br/>
     * 
     * Note that this function is still under development.
     * 
     * @param args Arguments to pass to the constructor. 
     *  
     * @return The newly created object, or null if no object could be created.
     */
    @SuppressWarnings("unchecked")
    public T spawn(Object... args) {

        if (size() > 1)
            this.commonCore.report(MessageType.MISUSE, "spawn() should not be used on cores with more than one class!");

        // Get the class we operate on 
        Class<T> request = get(null);
        if (request == null) return null;
        Class<T> toSpawn = request;

        // TODO: Selection of implementor could need some improvement
        if (request.isInterface()) {
            toSpawn = (Class<T>) this.manager.getImplementors(request)[0];
        }

        // Quick pass for most common option
        if (args == null || args.length == 0) {
            try {
                return this.manager.registerObject(request, toSpawn.newInstance());
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        // Get constructor types ...
        Class<?>[] types = $(args).map(new F1<Object, Class<?>>() {
            public Class<?> f(Object x) {
                return x.getClass();
            }
        }).array(Class.class);

        try {
            Constructor<T> constructor = null;

            // Get constructor from cache ... (try to)
            synchronized (this.constructors) {
                constructor = this.constructors.get(types);

                // Put a new constructor if it wasn't cached before
                if (constructor == null) {
                    constructor = toSpawn.getConstructor(types);
                    this.constructors.put(types, constructor);
                }
            }

            return this.manager.registerObject(request, constructor.newInstance(args));

            // NOTE: We do not swallow all execptions silently, becasue spawn() is a bit special and 
            // we cannot return anything that would still be usable.
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        // TODO Make sure to only use weak references, so that we don't run out of memory and prevent 
        // garbage colleciton.

        return null;
    }

    /**
     * Registers an implementor for the currently wrapped interface.<br> <br/>
     * 
     * Single-threaded, size-of-one.<br/><br/>
     * 
     * @param implemenetor A class implementing the interface enclosed in get(0).
     */
    public void implementor(Class<?> implemenetor) {
        if (size() > 1)
            this.commonCore.report(MessageType.MISUSE, "implementor() should not be used on cores with more than one class!");

        // Get the class we operate on 
        final Class<T> clazz = get(null);
        if (clazz == null) return;

        this.manager.registerImplementor(clazz, implemenetor);
    }

    /**
     * Returns all objects that have been spawned of this type.<br/><br/>
     * 
     * Single-threaded, size-of-one.<br/><br/>
     * 
     * @return A core object with all classes jCores has spawned (using <code>spawn()</code>) of the enclosed class (get(0)).
     */
    @SuppressWarnings("unchecked")
    public CoreObject<T> spawned() {
        if (get(null) == null)
            return new CoreObject<T>(this.commonCore, (T[]) new Object[0]);
        return new CoreObject<T>($, this.manager.getAllObjectsFor(get(0)));
    }
}
