/*
 * Wrapper.java
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
package net.jcores.utils;

import java.lang.reflect.Array;
import java.util.Collection;

import net.jcores.interfaces.functions.F1;

/**
 * @author rb
 *
 */
public class Wrapper {

    /**
     * Returns a CoreObject based on the given collection with the array type. 
     * @param list 
     * @param <T> 
     *    
     * @param type 
     * 
     * @return .
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] convert(Collection<?> list, Class<T> type) {
        if(list == null) return (T[]) Array.newInstance(Object.class, 0);
        return list.toArray((T[]) Array.newInstance(type, 0));
    }

    /**
     * Returns a CoreObject based on the given collection and the used converter.
     *    
     * @param <T> 
     * @param <Y> 
     * @param converter 
     * @param list 
     * @param mapType 
     * 
     * @return .
     */
    @SuppressWarnings("unchecked")
    public static <T, Y> Y[] convert(Collection<T> list, F1<T, Y> converter, Class<Y> mapType) {

        // TODO: Parallelize me! 
        // TODO: Make me work without size 
        final Object[] converted = (Object[]) Array.newInstance(mapType == null ? Object.class : mapType, list.size());

        int i = 0;
        
        for (T x : list) {
            converted[i++] = converter.f(x);
        }

        return (Y[]) converted;
    }
}
