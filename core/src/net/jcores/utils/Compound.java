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
import java.util.Set;

/**
 * Represents a compound object, consisting of several smaller objects.
 * 
 * @author Ralf Biedert
 */
public class Compound extends HashMap<String, Object> {

    /** */
    private static final long serialVersionUID = 6962084609409112972L;

    /**
     * Creates a new compound.
     * 
     * @param objects An array of the form <code>Key1, Value1, Key2, Value2, ...</code>.
     * Keys have to be strings.
     * @return A compound with the specified content or an empty one of none was
     * spawnable.
     */
    public static Compound create(Object... objects) {
        final Compound rval = new Compound();

        if (objects == null || objects.length < 2 || objects.length % 2 != 0)
            return rval;

        String key = null;

        for (int i = 0; i < objects.length; i++) {
            if (i % 2 == 0) {
                key = (String) objects[i];
            } else {
                rval.put(key, objects[i]);
            }
        }

        return rval;
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
