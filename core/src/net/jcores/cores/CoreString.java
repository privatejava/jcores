/*
 * CoreString.java
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

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jcores.CommonCore;
import net.jcores.interfaces.functions.F1;
import net.jcores.interfaces.functions.F1Object2Bool;
import net.jcores.interfaces.functions.F2ReduceObjects;
import net.jcores.options.Option;

/**
 * Wraps a number of String and exposes some convenience functions.  
 * 
 * @author Ralf Biedert
 * @since 1.0
 */
public class CoreString extends CoreObject<String> {

    /**
     * Creates an string core. 
     * 
     * @param supercore The common core. 
     * @param objects The strings to wrap.
     */
    public CoreString(CommonCore supercore, String... objects) {
        super(supercore, objects);
    }

    /**
     * Treats all strings as filenames and returns the corresponding files. <br/><br/>
     * 
     * Multi-threaded.<br/><br/>
     * 
     * @return A CoreFile object with all enclosed files.
     */
    public CoreFile file() {
        return new CoreFile(this.commonCore, map(new F1<String, File>() {
            public File f(String x) {
                return new File(x);
            }
        }).array(File.class));
    }

    /**
     * Filters all strings using the given regular expression. <br/><br/>
     * 
     * Multi-threaded.<br/><br/>
     * 
     * @param regex The regular expression to use.
     * @param options Currently none used.
     * 
     * @return A CoreString containing a filtered subset of our elements. 
     */
    public CoreString filter(final String regex, Option... options) {
        final Pattern p = Pattern.compile(regex);

        return new CoreString(this.commonCore, filter(new F1Object2Bool<String>() {
            public boolean f(String x) {
                final Matcher matcher = p.matcher(x);
                return matcher.matches();
            }
        }, options).array(String.class));
    }

    /**
     * Joins all string with an empty ("") joiner. <br/><br/>
     * 
     * Single-threaded.<br/><br/>
     * 
     * @return The joined string, or "" if there was nothing to do.
     */
    public String join() {
        return join("");
    }

    /**
     * Joins all strings to a single string.<br/><br/>
     * 
     * Single-threaded.<br/><br/>
     * 
     * @param joiner String used to join. 
     * @return The joined result or "" of there was nothing to do.
     */
    public String join(final String joiner) {
        if (size() == 0) return "";
        return reduce(new F2ReduceObjects<String>() {
            public String f(String stack, String next) {
                final StringBuilder sb = new StringBuilder();
                sb.append(stack);
                sb.append(joiner);
                sb.append(next);
                return sb.toString();
            }
        }).get(0);
    }

    /**
     * Splits all string using the splitter, returning an <code>expanded()</code> core.<br/><br/>
     * 
     * Multi-threaded.<br/><br/>
     * 
     * @param splitter A regular expression used to split the given strings. 
     * 
     * @return A an expanded CoreString with all split tokens. 
     */
    public CoreString split(final String splitter) {
        return map(new F1<String, List<String>>() {
            public List<String> f(String x) {
                return Arrays.asList(x.split(splitter));
            }
        }).expand(String.class).as(CoreString.class);
    }

    /**
     * Prints all strings to the console.<br/><br/>
     * 
     * Single-threaded.<br/><br/>
     */
    public void print() {
        if (size() == 0) return;

        for (String s : this.t) {
            if (s == null) continue;
            System.out.println(s);
        }
    }

    /**
     * Logs the enclosed strings with a default level.<br/><br/>
     * 
     * Single-threaded.<br/><br/>
     */
    public void log() {
        log(Level.INFO);
    }

    /**
     * Logs the given string using the given level.<br/><br/>
     * 
     * Single-threaded.<br/><br/>
     *  
     * @param level Logging level to use.
     */
    public void log(final Level level) {
        map(new F1<String, Object>() {
            public Object f(final String x) {
                CoreString.this.commonCore.log(x, level);
                return null;
            }
        });
    }

    /**
     * Replaces some pattern with a replacement.<br/><br/>
     * 
     * Multi-threaded.<br/><br/>
     * 
     * @param pattern The pattern to search for.
     * @param with The replacement.
     * @return A CoreString with all patterns replaced.
     */
    public CoreString replace(final String pattern, final String with) {
        final Pattern p = Pattern.compile(pattern);

        return new CoreString(this.commonCore, map(new F1<String, String>() {
            public String f(String x) {
                return p.matcher(x).replaceAll(with);
            }
        }).array(String.class));
    }

    /**
     * Creates URIs for all enclosed Strings.<br/><br/>
     * 
     * Multi-threaded.<br/><br/>
     * 
     * @return A CoreURI object with URIs for all enclosed strings.
     */
    public CoreURI uri() {
        return new CoreURI(this.commonCore, map(new F1<String, URI>() {
            public URI f(String x) {
                try {
                    return new URI(x);
                } catch (Exception e) {
                    return null;
                }
            }
        }).array(URI.class));
    }
}
