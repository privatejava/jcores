/*
 * CoreMap.java
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
package net.jcores.cores;

import java.util.Map;

import net.jcores.CommonCore;

/**
 * Wraps a single Map. This core is currently experimental. <br/><br/>
 * 
 * 
 * @author Ralf Biedert
 * @param <K> The type of keys.
 * @param <V> The type of values.
 * @since 1.0
 */
public class CoreMap<K, V> extends CoreObject<K> {

    /** Used for serialization */
    private static final long serialVersionUID = 5115270057138570660L;
    
    /** Internal map data */
    private Map<K, V> map;

    /**
     * Wraps a map.
     * 
     * @param supercore The shared CommonCore.
     * @param map The map to wrap.
     */
    @SuppressWarnings("unchecked")
    public CoreMap(CommonCore supercore, Map<K, V> map) {
        super(supercore, (K[]) map.keySet().toArray());
        this.map = map;
    }
    
    /**
     * Returns the value of a given key.<br/>
     * <br/>
     * 
     * Single-threaded. <br/>
     * <br/>
     * 
     * @param key The key to query.
     * @return The value for the given key.
     */
    public V value(K key) {
        return this.map.get(key);
    }
}
