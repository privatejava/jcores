/*
 * Compound.java
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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Represents a compound object, consisting of several smaller objects.
 * 
 * @author Ralf Biedert
 * @param <T>
 */
public class Compound<T> extends HashMap<String, T> {

    /** */
    private static final long serialVersionUID = 6962084609409112972L;

    /**
     * Creates a new compound.
     * 
     * @param <T>
     * 
     * @param objects An array of the form <code>Key1, Value1, Key2, Value2, ...</code>.
     * Keys have to be strings.
     * @return A compound with the specified content or an empty one of none was
     * spawnable.
     */
    @SuppressWarnings("unchecked")
    public static <T> Compound<T> create(Object... objects) {
        final Compound<Object> rval = new Compound<Object>();

        if (objects == null || objects.length < 2 || objects.length % 2 != 0)
            return (Compound<T>) rval;

        String key = null;

        for (int i = 0; i < objects.length; i++) {
            if (i % 2 == 0) {
                key = (String) objects[i];
            } else {
                rval.put(key, objects[i]);
            }
        }

        return (Compound<T>) rval;
    }

    /**
     * Returns the given key as an integer.
     * 
     * @param key
     * 
     * @return .
     */
    public int getInt(String key) {
        final Object object = get(key);

        // If we didnt have anything, return 0
        if (object == null) return 0;

        // If the object if of type number
        if (object instanceof Number) { return ((Number) object).intValue(); }

        // If the object if of type number
        if (object instanceof String) { return Integer.parseInt((String) object); }

        // Last resort, return the hash code ...
        return object.hashCode();
    }

    /**
     * Returns the given key as a string.
     * 
     * @param key
     * 
     * @return .
     */
    public String getString(String key) {
        final Object object = get(key);

        if (object == null) return null;

        return object.toString();
    }

    /**
     * Returns the given key as an integer.
     * 
     * @param key
     * 
     * @return .
     */
    public double getDouble(String key) {
        final Object object = get(key);

        // If we didnt have anything, return 0
        if (object == null) return 0;

        // If the object if of type number
        if (object instanceof Number) { return ((Number) object).doubleValue(); }

        // If the object if of type number
        if (object instanceof String) { return Double.parseDouble((String) object); }

        // Last resort, return the hash code ...
        return object.hashCode();
    }

    /**
     * Returns a typed map.
     * 
     * @param <M>
     * @param clazz
     * @return .
     */
    @SuppressWarnings("unchecked")
    public <M> Map<String, M> map(Class<M> clazz) {
        // Check all elements are of the given type
        final Set<String> keySet2 = keySet();
        for (String string : keySet2) {
            final Object l = get(string);
            if (l == null) continue;
            if(clazz.isAssignableFrom(l.getClass())) continue;
            
            remove(string);
        }
        
        return (Map<String, M>) this;
    }

    /**
     * Puts the given integer into the slot named key
     * 
     * @param key
     * @param value
     */
    @SuppressWarnings("boxing")
    public void put(String key, int value) {
        put(key, Integer.valueOf(value));
    }

    /**
     * Puts the given integer into the slot named key
     * 
     * @param key
     * @param value
     */
    @SuppressWarnings("boxing")
    public void put(String key, double value) {
        put(key, Double.valueOf(value));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.AbstractMap#toString()
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("{ ");

        final Set<String> keySet = keySet();
        for (String string : keySet) {
            sb.append(string);
            sb.append(":");
            sb.append(get(string));
            sb.append(" ");
        }

        sb.append("}");
        return sb.toString();
    }
}
