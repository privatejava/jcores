/*
 * CoreFile.java
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
import java.util.Collection;

import net.jcores.CommonCore;
import net.jcores.interfaces.functions.F1;
import net.jcores.options.Option;
import net.jcores.options.OptionMapType;

/**
 * Thin wrapper for collections to convert them to arrays.
 * 
 * Deprecated ... rather do this conversion inside $()
 * 
 * @param <T> 
 * @author Ralf Biedert
 */
@Deprecated
public class CoreCollection<T> extends Core {

    protected final Collection<T> t;

    /**
     * @param supercore
     * @param t
     */
    public CoreCollection(CommonCore supercore, Collection<T> t) {
        super(supercore);

        this.t = t;
    }

    /**
     * Returns a CoreObject based on the given collection with the array type. 
     *    
     * @param type 
     * 
     * @return .
     */
    @SuppressWarnings("unchecked")
    public CoreObject<T> convert(Class<?> type) {
        return (CoreObject<T>) new CoreObject<Object>(this.commonCore, this.t.toArray((T[]) Array.newInstance(type, 0)));
    }

    /**
     * Returns a CoreObject based on the given collection and the used converter.
     *    
     * @param converter 
     * @param options 
     * @param <Y> 
     * 
     * @return .
     */
    @SuppressWarnings("unchecked")
    public <Y> CoreObject<Y> convert(F1<T, Y> converter, Option... options) {

        // Destination type we use.
        Class<?> mapType = null;

        // Check options if we have a map type.
        for (Option option : options) {
            if (option instanceof OptionMapType) {
                mapType = ((OptionMapType) option).getType();
            }
        }

        Object[] converted = (Object[]) Array.newInstance(mapType == null ? Object.class : mapType, size());

        int i = 0;

        // TODO: Parallelize me! 
        // TODO: Make me work without size 
        for (T x : this.t) {
            converted[i++] = converter.f(x);
        }

        return new CoreObject<Y>(this.commonCore, (Y[]) converted);
    }

    /* (non-Javadoc)
     * @see net.jcores.cores.Core#size()
     */
    @Override
    int size() {
        return this.t.size();
    }
}
