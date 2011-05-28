/*
 * ClassManager.java
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
package net.jcores.managers;

import static net.jcores.CoreKeeper.$;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import net.jcores.cores.CoreObject;
import net.jcores.interfaces.functions.F1;
import net.jcores.options.Option;

/**
 * Manager for classes, contains week references to spawned objects. 
 * 
 * @since 1.0
 * @author Ralf Biedert
 */
public class ManagerClass extends Manager {
    /**
     * Keeps the things we need to know 
     */
    static class Container {
        /** Weak references */
        Collection<WeakReference<?>> references = new ConcurrentLinkedQueue<WeakReference<?>>();

        /** Our queue (TODO: needed? we have to traverse all elements anyway) */
        ReferenceQueue<?> queue = new ReferenceQueue<Object>();
    }

    /** Maps classes to known objects created*/
    private final ConcurrentMap<Class<?>, Container> objects = new ConcurrentHashMap<Class<?>, Container>();

    /** Maps interfaces to classes */
    private final ConcurrentMap<Class<?>, Class<?>> implementors = new ConcurrentHashMap<Class<?>, Class<?>>();

    /**
     * Registers a given object.
     * 
     * @param <T> Type of the object.
     * @param clazz Object's class.
     * @param object Object to register.
     * @return Return the object.
     */
    public <T> T registerObject(Class<T> clazz, T object) {
        Container container = null;

        if (!this.objects.containsKey(clazz)) {
            this.objects.putIfAbsent(clazz, new Container());
        }

        container = this.objects.get(clazz);
        container.references.add(new WeakReference<T>(object));

        return object;
    }

    /**
     * Registers an implementor (class implementing the given interface).
     * 
     * @param <T> Type. 
     * @param iface The interface.
     * @param implementor Class implementing the given interface.
     */
    public <T> void registerImplementor(Class<?> iface, Class<?> implementor) {
        this.implementors.put(iface, implementor);
    }

    /**
     * Returns all implementors for the given interface.
     * 
     * @param <T> Type.
     * @param iface Interface to request.
     * @return Array of all implementors.
     */
    public <T> Class<?>[] getImplementors(Class<T> iface) {
        Class<?> class1 = this.implementors.get(iface);
        if (class1 == null) return new Class[0];
        return new Class[] { class1 };
    }

    /**
     * Returns all objects alive for clazz.
     * 
     * @param <T> Type.
     * @param clazz Requested class.
     * @return Array of all objects.
     */
    @SuppressWarnings("unchecked")
    public <T> T[] getAllObjectsFor(Class<T> clazz) {
        final Container container = this.objects.get(clazz);

        if (container == null) return (T[]) new Object[0];

        final Collection<WeakReference<?>> collection = container.references;

        CoreObject<T> convert = $(collection, new F1<WeakReference<?>, T>() {
            public T f(WeakReference<?> x) {
                Object object = x.get();

                // Check if the object is still there ...
                if (object == null) {
                    collection.remove(x);
                }

                return (T) object;
            }
        }, Option.MAP_TYPE(clazz));

        return convert.compact().array(clazz);
    }
}
