/*
 * CoreJComponent
 * 
 * Copyright (c) 2010, Ralf Biedert All rights reserved.
 * 
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

import net.jcores.CommonCore;
import net.jcores.interfaces.functions.F1;
import net.jcores.options.Option;
import net.jcores.utils.Compound;

/**
 * Wraps a number of {@link Compound} elements and exposes some convenience
 * functions. For example, to extract only compound-values with the given key <code>"uri"</code>, 
 * write:<br/><br/>
 * 
 * <code>$(compounds).value("uri")</code>
 * 
 * 
 * @author Ralf Biedert
 * @param <T> The type of the compound.
 * 
 * @since 1.0
 */
public class CoreCompound<T> extends CoreObject<Compound<T>> {

    /** Used for serialization */
    private static final long serialVersionUID = 5810590185749402495L;

    /**
     * Creates an Compound core.
     * 
     * @param supercore The common core.
     * @param objects The Compounds to wrap.
     */
    public CoreCompound(CommonCore supercore, Compound<T>... objects) {
        super(supercore, objects);
    }

    /**
     * Creates a core wrapping only values for the given key.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$(compounds).value("name", String.class).unique().print()</code> - Extracts all values of all Compounds with the given key, removes doubles, and prints the elements.</li>
     * </ul>
     * Multi-threaded.<br/>
     * <br/>
     * 
     * @param key The key to extract of the compound.
     * @param type Type of the core to return (must match the type of the elements of
     * key).
     * @return A new {@link CoreObject} is returned containing only elements of the given
     * key.
     */
    public CoreObject<T> value(final String key, Class<T> type) {
        return map(new F1<Compound<T>, T>() {
            public T f(final Compound<T> x) {
                return x.get(key);
            }
        }, Option.MAP_TYPE(type));
    }
}
