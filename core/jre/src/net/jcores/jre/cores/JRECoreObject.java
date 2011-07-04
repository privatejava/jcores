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

import net.jcores.jre.CoreKeeper;
import net.jcores.jre.JRECommonCore;
import net.jcores.shared.CommonCore;
import net.jcores.shared.cores.CoreObject;
import net.jcores.shared.cores.adapter.AbstractAdapter;
import net.jcores.shared.interfaces.functions.F1;

/**
 * @author Ralf Biedert
 *
 * @param <T>
 */
public class JRECoreObject<T> extends CoreObject<T> {

    /**
     * @param supercore
     * @param adapter
     */
    public JRECoreObject(CommonCore supercore, AbstractAdapter<T> adapter) {
        super(supercore, adapter);
    }

    /**
     * @param supercore
     * @param object
     */
    public JRECoreObject(JRECommonCore supercore, T... object) {
        super(supercore, object);
    }

    /** */
    private static final long serialVersionUID = -5306674726982099085L;

    
    
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
     * @return A {@link CoreObject} with the wrapped objects. Elements which could not be wrapped are set to <code>null</code>.
     */
    public <W> CoreObject<W> wrap(final Class<W> wrapper) {
        final JRECoreClass<W> w = CoreKeeper.$(wrapper);
        
        return forEach(new F1<T, W>() {
            @SuppressWarnings("unchecked")
            @Override
            public W f(T x) {
                // Check if the object is already assignable
                if(wrapper.isAssignableFrom(x.getClass())) return (W) x;
                
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
     * <li><code>$(o1, o2).clazz()</code> - Wraps the classes for <code>o1</code> 
     * and <code>o2</code> in a {@link JRECoreClass}.</li>
     * </ul>
     * 
     * Multi-threaded. <br/>
     * <br/>
     * 
     * @return A new {@link JRECoreClass} containing the classes for all objects.
     */
    @SuppressWarnings("unchecked")
    public JRECoreClass<T> clazz() {
        return new JRECoreClass<T>(this.commonCore, map(new F1<T, Class<T>>() {
            @Override
            public Class<T> f(T x) {
                return (Class<T>) x.getClass();
            }
        }).array(Class.class));
    }

}
