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
package net.jcores.jre;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.AudioInputStream;
import javax.swing.JComponent;

import net.jcores.jre.cores.CoreAudioInputStreamJRE;
import net.jcores.jre.cores.CoreBufferedImageJRE;
import net.jcores.jre.cores.CoreClassJRE;
import net.jcores.jre.cores.CoreComponentJRE;
import net.jcores.jre.cores.CoreFileJRE;
import net.jcores.jre.cores.CoreJComponentJRE;
import net.jcores.jre.cores.CoreObjectJRE;
import net.jcores.jre.cores.CoreStringJRE;
import net.jcores.jre.cores.CoreURIJRE;
import net.jcores.shared.cores.CoreInputStream;
import net.jcores.shared.cores.CoreMap;
import net.jcores.shared.cores.CoreNumber;
import net.jcores.shared.cores.adapter.AbstractAdapter;
import net.jcores.shared.cores.adapter.MapAdapter;
import net.jcores.shared.interfaces.functions.F1;
import net.jcores.shared.options.Option;
import net.jcores.shared.options.OptionMapType;
import net.jcores.shared.utils.internal.Wrapper;
import net.jcores.shared.utils.internal.io.URIUtils;
import net.jcores.shared.utils.map.MapEntry;

/**
 * Keeps the common core and contains all <code>$</code>-operators for all our cores. This
 * class is the main entry point into jCores. You should use these functions as static
 * imports. See <a href="http://code.google.com/p/jcores/wiki/EclipseIntegration">the
 * Eclipse Integration Guide</a> on how to make your life much more simple.<br/>
 * <br/>
 * 
 * If you want to see your own core in here (or if you have any recommendations for new
 * cores) contact us in <a href="http://groups.google.com/group/jcores">the forum</a>.
 * 
 * @since 1.0
 * @author Ralf Biedert
 */
public class CoreKeeper {
    /** The common core shared by all other cores. */
    public final static CommonCoreJRE $ = new CommonCoreJRE(); 

    
    /**
     * Wraps the given object(s) and returns a parameterized CoreObject. This
     * is also the default method being used when wrapping <i>unknown</i> objects for
     * which no special core has been defined in here.
     * 
     * @param <T> Type of the object and returned core.
     * @param object A single object or object array to wrap.
     * @return A CoreObject wrapping the set of objects.
     */
    public static <T extends Object> CoreObjectJRE<T> $(T... object) {
        return new CoreObjectJRE<T>($, object);
    }


    /**
     * Wraps the given AudioInputStreams and returns a CoreAudioInputStream.
     * 
     * @param object The AudioInputStreams to wrap..
     * @return A CoreAudioInputStream wrapping the set of objects.
     */
    public static CoreAudioInputStreamJRE $(AudioInputStream... object) {
        return new CoreAudioInputStreamJRE($, object);
    }
    
    /**
     * Wraps number of numbers and returns a new CoreNumber.
     * 
     * @param object The numbers to wrap.
     * @return A CoreNumber wrapping the given compounds.
     */
    public static CoreNumber $(Number... object) {
        return new CoreNumber($, object);
    }

    /**
     * Wraps a single class and returns a new ClassCore. 
     * 
     * @param <T> Parameter of the classes' type.
     * @param clsses The classes to wrap.
     * @return A CoreClass wrapping the given classes.
     */
    @SuppressWarnings("unchecked")
    public static <T> CoreClassJRE<T> $(Class<T> clsses) {
        return new CoreClassJRE<T>($, new Class[] { clsses });
    }

    /**
     * Wraps number of classes and returns a new ClassCore. In most cases
     * only a single class should be wrapped.
     * 
     * @param <T> Parameter of the classes' type.
     * @param clsses The classes to wrap.
     * @return A CoreClass wrapping the given classes.
     */
    public static <T> CoreClassJRE<T> $(Class<T>... clsses) {
        return new CoreClassJRE<T>($, clsses);
    }
    
    /**
     * Wraps number of strings and returns a new CoreString.
     * 
     * @param object The Strings to wrap.
     * @return A CoreString wrapping the given strings.
     */
    public static CoreStringJRE $(String... object) {
        return new CoreStringJRE($, object);
    }

    /**
     * Wraps number of URIs and returns a new CoreURI.
     * 
     * @param object The URIs to wrap.
     * @return A CoreString wrapping the given URIs.
     */
    public static CoreURIJRE $(URI... object) {
        return new CoreURIJRE($, object);
    }
    
    /**
     * Wraps number of URLs and returns a new CoreURI.
     * 
     * @param object The URLs to wrap.
     * @return A CoreString wrapping the given URLs.
     */
    public static CoreURIJRE $(URL... object) {
        return new CoreURIJRE($, URIUtils.URIs(object));
    }

    /**
     * Wraps number of Files and returns a new CoreFile.
     * 
     * @param object The Files to wrap.
     * @return A CoreFile wrapping the given Files.
     */
    public static CoreFileJRE $(File... object) {
        return new CoreFileJRE($, object);
    }

    /**
     * Wraps number of InputStreams and returns a new CoreInputStream.
     * 
     * @param object The InputStreams to wrap.
     * @return A CoreInputStream wrapping the given InputStreams.
     */
    public static CoreInputStream $(InputStream... object) {
        return new CoreInputStream($, object);
    }

    /**
     * Wraps number of BufferedImages and returns a new CoreBufferedImage.
     * 
     * @param object The BufferedImage to wrap.
     * @return A CoreBufferedImage wrapping the given BufferedImages.
     */
    public static CoreBufferedImageJRE $(BufferedImage... object) {
        return new CoreBufferedImageJRE($, object);
    }

    /**
     * Wraps number of Components and returns a new CoreComponent.
     * 
     * @param object The Components to wrap.
     * @return A CoreComponent wrapping the given Components.
     */
    public static CoreComponentJRE $(Component... object) {
        return new CoreComponentJRE($, object);
    }
    
    /**
     * Wraps number of JComponents and returns a new CoreJComponent.
     * 
     * @param object The JComponents to wrap.
     * @return A CoreJComponent wrapping the given Components.
     */
    public static CoreJComponentJRE $(JComponent... object) {
        return new CoreJComponentJRE($, object);
    }


    /**
     * Wraps a generic Collection objects. Please note that the Collection is
     * transformed into an array, so for performance reasons usage of this
     * wrapper should be minimized. Also note that this function always returns
     * a parameterized, but vanilla CoreObject, which has to be cast using
     * <code>.as()</code> again.
     * 
     * @param collection The collection to transform and wrap.
     * @param <T> Type of the collection.
     * 
     * @return A CoreObject of the given type wrapping a converted array of the
     * collection.
     */
    @SuppressWarnings("unchecked")
    public static <T> CoreObjectJRE<T> $(Collection<T> collection) {
        return new CoreObjectJRE<T>($, (T[]) Wrapper.convert(collection, Object.class));
    }
    

    /**
     * Wraps a generic Collection objects. Please note that the Collection is
     * transformed into an array, so for performance reasons usage of this
     * wrapper should be minimized. Also note that this function always returns
     * a parameterized, but vanilla CoreObject, which has to be cast using
     * <code>.as()</code> again.
     * 
     * @param collection The collection to transform and wrap.
     * @param <T> Type of the collection.
     * 
     * @return A CoreObject of the given type wrapping a converted array of the
     * collection.
     */
    public static <T> CoreObjectJRE<T> $(List<T> collection) {
        return new CoreObjectJRE<T>($, collection);
    }

    
    /**
     * Wraps a map.
     * 
     * @param map The map to wrap
     * @param <K> Type of keys.
     * @param <V> Type of values.
     * 
     * @return A CoreMap of the given type wrapping a converted MapEntry array of the
     * collection.
     */
    public static <K, V> CoreMap<K, V> $(Map<K, V> map) {
        return new CoreMap<K, V>($, (AbstractAdapter<MapEntry<K, V>>) new MapAdapter<K, V>(map));
    }
    

    /**
     * Converts and wraps the given collection. Except that this method first converts
     * each element, this method equals <code>$(Collection<T> collection)</code>.
     * 
     * @param <Y> The source type prior to conversion.
     * @param <T> The target type after conversion.
     * 
     * @param collection The collection to convert and wrap.
     * @param converter The converter.
     * @param options Relevant options: <code>OptionMapType</code>.
     * 
     * @return A CoreObject of the given type wrapping a converted array of the
     * collection.
     */
    @SuppressWarnings({ "unchecked", "cast" })
    public static <Y, T> CoreObjectJRE<Y> $(Collection<T> collection, F1<T, Y> converter,
                                         Option... options) {

        // Destination type we use.
        Class<?> mapType = null;

        // Check options if we have a map type.
        for (Option option : options) {
            if (option instanceof OptionMapType) {
                mapType = ((OptionMapType) option).getType();
            }
        }

        return new CoreObjectJRE<Y>($, (Y[]) Wrapper.convert(collection, converter, (Class<Y>) mapType));
    }
}
