/*
 * Core.java
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
package net.jcores;

import java.util.Collection;
import java.util.concurrent.locks.Lock;

import net.jcores.cores.CoreClass;
import net.jcores.cores.CoreInt;
import net.jcores.cores.CoreLock;
import net.jcores.cores.CoreObject;
import net.jcores.cores.CoreString;
import net.jcores.interfaces.functions.F1;
import net.jcores.options.Option;
import net.jcores.options.OptionMapType;
import net.jcores.utils.Wrapper;

/**
 * Keeps the common core and contains Johnnys($) for all our cores. If you want to see your own core 
 * in here (or if you have any recommendations for new cores) send a mail to Ralf (rb@xeoh.net).   
 * 
 * @author Ralf Biedert
 */
public class CoreKeeper {
    /** The common core shared by all other cores. */
    public final static CommonCore $ = new CommonCore();

    /**
     * Wraps the given object.
     * 
     * @param <T>
     * @param object
     * @return .
     */
    public static <T extends Object> CoreObject<T> $(T... object) {
        return new CoreObject<T>($, object);
    }

    /**
     * Wraps the given ints (experimental!!!)
     * 
     * Not really deprecated, but not ready for use.
     * 
     * @param object
     * @return .
     */
    @Deprecated
    public static CoreInt $(int... object) {
        return new CoreInt($, object);
    }

    /**
     * Use of this method is not recommended!
     * 
     * @param <T>
     * @param o
     * @return .
     */
    @Deprecated
    public static <T> CoreClass<T> $(Class<T>... o) {
        return new CoreClass<T>($, o);
    }

    /**
     * Creates a class core.
     * 
     * @param <T>
     * @param o
     * @return .
     */
    @SuppressWarnings("unchecked")
    public static <T> CoreClass<T> $(Class<T> o) {
        return new CoreClass<T>($, o);
    }

    /**
     * String cores.
     * 
     * @param o
     * @return .
     */
    public static CoreString $(String... o) {
        return new CoreString($, o);
    }

    /**
     * Lock core.
     * 
     * @param o
     * @return .
     */
    public static CoreLock $(Lock... o) {
        return new CoreLock($, o);
    }

    /**
     * Wraps the given collection.
     * 
     * @param t 
     * @param <T> 
     * 
     * @return .
     */
    @SuppressWarnings("unchecked")
    public static <T> CoreObject<T> $(Collection<T> t) {
        return new CoreObject<T>($, (T[]) Wrapper.convert(t, Object.class));
    }

    /**
     * Wraps the given collection.
     * 
     * @param <Y> 
     * @param <T> 
     * 
     * @param t 
     * @param converter 
     * @param options 
     * 
     * @return .
     */
    @SuppressWarnings({ "unchecked", "cast" })
    public static <Y, T> CoreObject<Y> $(Collection<T> t, F1<T, Y> converter,
                                         Option... options) {

        // Destination type we use.
        Class<?> mapType = null;

        // Check options if we have a map type.
        for (Option option : options) {
            if (option instanceof OptionMapType) {
                mapType = ((OptionMapType) option).getType();
            }
        }

        return new CoreObject<Y>($, (Y[]) Wrapper.convert(t, converter, (Class<Y>) mapType));
    }
}
