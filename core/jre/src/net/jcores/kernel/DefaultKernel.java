/*
 * DefaultKernel.java
 * 
 * Copyright (c) 2011, Ralf Biedert, DFKI. All rights reserved.
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
package net.jcores.kernel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author Ralf Biedert
 * @since 1.0
 * 
 */
public class DefaultKernel implements Kernel {
    /** All services we know */
    Collection<Service> services = new ConcurrentLinkedQueue<Service>();

    /** A quick look cache for commons requests */
    Map<Class<?>, Object> cache = new ConcurrentHashMap<Class<?>, Object>();

    /*
     * (non-Javadoc)
     * 
     * @see net.jcores.kernel.Kernel#register(net.jcores.kernel.Service)
     */
    public Kernel register(Service service) {
        this.services.add(service);
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.jcores.kernel.Kernel#deregister(net.jcores.kernel.Service)
     */
    public Kernel deregister(Service service) {
        this.services.remove(service);

        // Scan the cache. If there is any value similar to the service, remove the
        // key-value set.
        final Set<Class<?>> keySet = this.cache.keySet();
        for (Class<?> c : keySet) {
            final Object object = this.cache.get(c);
            if (object.equals(service.getService())) {
                this.cache.remove(c);
            }
        }

        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.jcores.kernel.Kernel#list()
     */
    @Override
    public Collection<Service> list() {
        return new ArrayList<Service>(this.services);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.jcores.kernel.Kernel#register(java.util.Collection)
     */
    @Override
    public Kernel register(Collection<? extends Service> service) {
        for (Service s : service) {
            register(s);
        }

        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.jcores.kernel.Kernel#deregister(java.util.Collection)
     */
    @Override
    public Kernel deregister(Collection<? extends Service> service) {
        for (Service s : service) {
            deregister(s);
        }

        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.jcores.kernel.Kernel#get(java.lang.Class, net.jcores.kernel.Kernel.Get[])
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Class<T> service, Get... options) {
        // Check if we can answer the request by performing a cache lookup
        Object object = this.cache.get(service);
        if (object != null) return (T) object;

        // Find the next best service
        for (Service s : this.services) {
            if (service.isAssignableFrom(s.getService().getClass())) {
                this.cache.put(service, s.getService());
                return (T) s.getService();
            }
        }

        return null;
    }

}
