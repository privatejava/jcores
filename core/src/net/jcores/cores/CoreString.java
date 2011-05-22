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

import static net.jcores.CoreKeeper.$;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jcores.CommonCore;
import net.jcores.interfaces.functions.F1;
import net.jcores.interfaces.functions.F1Object2Bool;
import net.jcores.options.MessageType;
import net.jcores.options.Option;
import net.jcores.options.OptionRegEx;
import net.jcores.utils.Compound;
import net.jcores.utils.internal.io.StreamUtils;

/**
 * Wraps a number of String and exposes some convenience functions. For example, 
 * to parse the content of a file as a set of key-value pairs:<br/><br/>
 * 
 * <code>$("file.properties").file().text().split("\n").hashmap()</code>
 * 
 * @author Ralf Biedert
 * @since 1.0
 */
public class CoreString extends CoreObject<String> {

    /** Used for serialization */
    private static final long serialVersionUID = -2412531498060577117L;

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
     * Returns the (UTF-8) byte data of the enclosed strings.<br/>
     * <br/>
     *      
     * Examples:
     * <ul>
     * <li><code>$("Hello World").bytes().get(0)</code> - Returns the binary data for the string.</li>
     * </ul> 
     * 
     * Multi-threaded.<br/>
     * <br/>
     * 
     * @return A CoreByteBuffer object with the byte data of all enclosed strings.
     */
    public CoreByteBuffer bytes() {
        return new CoreByteBuffer(this.commonCore, map(new F1<String, ByteBuffer>() {
            public ByteBuffer f(String x) {
                try {
                    byte[] bytes = x.getBytes("UTF-8");
                    return ByteBuffer.wrap(bytes);
                } catch (UnsupportedEncodingException e) {
                                                       //
                }
                return null;
            }
        }).array(ByteBuffer.class));
    }
    
    

    /**
     * Returns true if this core contains a string which has the given substring as one of 
     * its parts. This differs from {@link CoreObject}'s <code>contains()</code> method.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$("aaa", "bbb", "ccc").containssubstr("b")</code> - Returns true.</li>
     * </ul> 
     * 
     * Single-threaded. <br/>
     * <br/>
     * 
     * @param substring The substring to search for in this String core. A search for null will always return false.
     * @return True if the substring was found in one of the strings, false if not.
     */
    public boolean containssubstr(final String substring) {
        for (int i = 0; i < size(); i++) {
            if (this.t[i] != null && this.t[i].contains(substring)) return true;
        }

        return false;
    }

    
    /**
     * Decodes all strings from the application/x-www-form-urlencoded format.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$("index.php%3Fx%3D1").decode().print()</code> - Prints <code>index.php?x=1</code>.</li>
     * </ul> 
     * 
     * Multi-threaded.<br/>
     * <br/>
     * 
     * @return A CoreString object with all decode strings.
     */
    public CoreString decode() {
        return new CoreString(this.commonCore, map(new F1<String, String>() {
            public String f(String x) {
                try {
                    return URLDecoder.decode(x, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                
                return null;
            }
        }).array(String.class));
    }

    
    /**
     * Encodes all strings into the application/x-www-form-urlencoded format.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$("index.php?x=1").decode().print()</code> - Prints <code>index.php%3Fx%3D1</code>.</li>
     * </ul> 
     * 
     * Multi-threaded.<br/>
     * <br/>
     * 
     * @return A CoreString object with all encoded strings.
     */
    public CoreString encode() {
        return new CoreString(this.commonCore, map(new F1<String, String>() {
            public String f(String x) {
                try {
                    return URLEncoder.encode(x, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                
                return null;
            }
        }).array(String.class));
    }
    
    
    /**
     * Treats the contained string as a shell command and executes it, returning the output.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$("ls -la /").exec().print()</code> - Lists (on Unix systems) the top level directory and prints the result.</li>
     * </ul> 
     * 
     * Multi-threaded.<br/>
     * <br/>
     * 
     * @param options Not used at the moment.
     * 
     * @deprecated Not deprecated, but behavior might change in the future.   
     * @return A CoreString with all the emitted output.
     */
    @Deprecated
    public CoreString exec(Option ...options) {
        return new CoreString(this.commonCore, map(new F1<String, String>() {
            public String f(String x) {
                // TODO: Iteratively join command in case the command has a space in its path!
                final ProcessBuilder builder = new ProcessBuilder();
                builder.command(x.split(" "));
                try {
                    final Process start = builder.start();
                    start.waitFor();
                    return StreamUtils.readText(CoreString.this.commonCore, start.getInputStream());
                } catch (IOException e) {
                    $.report(MessageType.EXCEPTION, "Error invoking " + x);
                } catch (InterruptedException e) {
                    $.report(MessageType.EXCEPTION, "Error waiting for " + x);
                }
                
                return null;
            }
        }).array(String.class));
    }
   
    
    
    /**
     * Executes the given shell command on each of the contained strings in parallel. The core's content
     * may be used as <code>$1</code> within the command string. <br/>
     * <br/>
     *
     * Examples:
     * <ul>
     * <li><code>$("/a", "/b", "/c").exec("ls -la $1").print()</code> - Lists (on Unix systems) three different top level directories and prints the result.</li>
     * </ul> 
     * 
     * Multi-threaded.<br/>
     * <br/>
     * 
     * @param command The command to execute, e.g., <code>"ls $1"</code>.
     * @param options Not used at the moment.
     *  
     * @deprecated Not deprecated, but behavior might change in the future.   
     * @return A CoreString with all the emitted output.
     */
    @Deprecated
    public CoreString exec(final String command, Option ...options) {
        return new CoreString(this.commonCore, map(new F1<String, String>() {
            public String f(String x) {
                // TODO: Iteratively join command in case the command has a space in its path!
                final ProcessBuilder builder = new ProcessBuilder();
                final String cmd = x.replaceAll("^(.*)$", command);
                builder.command(cmd.split(" ")); // TODO: This is broken for strings with spaces
                try {
                    final Process start = builder.start();
                    start.waitFor();
                    return StreamUtils.readText(CoreString.this.commonCore, start.getInputStream());
                } catch (IOException e) {
                    $.report(MessageType.EXCEPTION, "Error invoking " + x);
                } catch (InterruptedException e) {
                    $.report(MessageType.EXCEPTION, "Error waiting for " + x);
                }
                
                return null;
            }
        }).array(String.class));
    }
    
    
    /**
     * Treats all strings as filenames and returns a {@link CoreString} object 
     * with the corresponding files.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$("test.txt").file().delete()</code> - Deletes the specified file.</li>
     * </ul> 
     * 
     * Multi-threaded.<br/>
     * <br/>
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
     * Filters all strings using the given regular expression. <br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$("").filter(".*").print()</code> - Filters absolutely nothing.</li>
     * </ul> 
     * 
     * Multi-threaded.<br/>
     * <br/>
     * 
     * @param regex The regular expression to use.
     * @param options Currently none used.
     * 
     * @return A CoreString containing a filtered subset of our elements.
     */
    @Override
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
     * Converts the content of this core to a <code>String -> String</code> map. Each element of this core 
     * will be segmented by the first occurance of either '=' or ':'. The content of the returned map is 
     * undefined for keys appearing double. So, if this core contains two elements of the form 
     * ("a:5" and "b=3") the resulting map would contain the keys ("a" and "b") with the values 
     * ("5" and "3") respectively.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$("application.props").file().text().split("\n").hashmap()</code> - Loads and parses application properties.</li>
     * </ul>  
     * 
     * Single-threaded.<br/>
     * <br/>
     * 
     * @param delimeters The delimeters to use. If none are specifed, the default ones will be used. 
     * 
     * @return A Map<String,String> object containing the entries of this core.  
     */
    public Map<String, String> hashmap(final String ... delimeters) {
        final Map<String, String> rval = new ConcurrentHashMap<String, String>();
        
        map(new F1<String, Void>() {
            @Override
            public Void f(String x) {
                final String[] delims = delimeters.length > 0 ? delimeters : new String[] {":=", "=", ":"};
                final Compound<String> best = $("token", "", "dist", ""+Integer.MAX_VALUE).compound(String.class);
                
                // Find best delimeter
                for (String string : delims) {  
                    final int index = x.indexOf(string);
                    if(index >= 0 && index < best.getInt("dist")) {
                        best.put("dist", index);
                        best.put("token", string);
                    }
                }
                
                final int dist = best.getInt("dist"); 
                if(dist < 0) return null;
                
                final String[] split = x.split(best.getString("token"));
                rval.put(split[0], split[1]);
                
                return null;
            }
        });
        
        return rval;
    }

    /**
     * Joins all string with an empty ("") joiner. <br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$("a", "b", "c").join()</code> - Returns <code>"abc"</code>.</li>
     * </ul>  
     * 
     * Single-threaded.<br/>
     * <br/>
     * 
     * @return The joined string, or "" if there was nothing to do.
     */
    public String join() {
        return join("");
    }

    /**
     * Joins all strings to a single string.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$("a", "b", "c").join(",")</code> - Returns <code>"a,b,c"</code>.</li>
     * </ul>  
     * 
     * Single-threaded.<br/>
     * <br/>
     * 
     * @param joiner String used to join.
     * @return The joined result or "" of there was nothing to do.
     */
    public String join(final String joiner) {
        if (size() == 0) return "";

        final StringBuilder sb = new StringBuilder();
        final int size = size();

        for (int i = 0; i < size; i++) {
            final String string = get(i);

            // We don't accept null elements
            if (string == null) continue;

            sb.append(string);

            if (i < size - 1) {
                sb.append(joiner);
            }
        }

        return sb.toString();
    }

    /**
     * Splits all strings using the splitter, returning an <code>expanded()</code> core.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$("a,b", "c,d").split(",").print()</code> - Will return a core with all elements split and prints <code>a</code>, <code>b</code>, <code>c</code> and <code>d</code> in some undefined order.</li>
     * </ul>  
     * 
     * Multi-threaded.<br/>
     * <br/>
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
     * Prints all strings to the console.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$("42").print()</code> - Is shorter than <code>System.out.println("42").</code></li>
     * </ul>  
     * 
     * Single-threaded.<br/>
     * <br/>
     * 
     * @return Returns this CoreString object again.
     */
    @Override
    public CoreString print() {
        if (size() == 0) return this;

        for (String s : this.t) {
            if (s == null) continue;
            System.out.println(s);
        }

        return this;
    }

    /**
     * Logs the enclosed strings with a default level.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$("Some message").log()</code> - Logs the message to the default logging facility.</li>
     * </ul>   
     * 
     * Single-threaded.<br/>
     * <br/>
     */
    public void log() {
        log(Level.INFO);
    }

    /**
     * Logs the given string using the given level.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$("Some message").log(Level.SEVERE)</code> - Logs the message to the default logging facility with high priority.</li>
     * </ul>   
     * 
     * Single-threaded.<br/>
     * <br/>
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
     * Returns a CoreString with all empty strings (i.e., length of <code>0</code>) set to <code>null</code>.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$("a", "", "b").nullempty().get(1)</code> - Returns null.</li>
     * </ul>    
     * 
     * Multi-threaded.<br/>
     * <br/>
     * 
     * @return A new core with all empty strings nulled. 
     */
    public CoreString nullempty() {
        return new CoreString(this.commonCore, (String[]) map(new F1<String, Object>() {
            public Object f(final String x) {
                return x.length() == 0 ? null : x;
            }
        }).unsafearray());
    }


    /**
     * Replaces a pattern in all contained strings with a replacement.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$("Hellx", "Wxrld").replace("x", "o")</code> - Sometimes an x should be an o.</li>
     * </ul>
     *     
     * Multi-threaded.<br/>
     * <br/>
     * 
     * @param pattern The pattern to search for.
     * @param with The replacement.
     * @param options Relevant options: {@link OptionRegEx}.
     * 
     * @return A CoreString with all patterns replaced.
     */
    public CoreString replace(final String pattern, final String with, Option... options) {
        final int regexOptions = $(options).cast(OptionRegEx.class).get(0, new OptionRegEx(0)).getOptions();
        final Pattern p = Pattern.compile(pattern, regexOptions);

        return new CoreString(this.commonCore, map(new F1<String, String>() {
            public String f(String x) {
                return p.matcher(x).replaceAll(with);
            }
        }).array(String.class));
    }


    /**
     * Trims whitespace in each string.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$(" a ", " b ").trim().join()</code> - Returns <code>"ab"</code>.</li>
     * </ul>
     *          
     * Multi-threaded.<br/>
     * <br/>
     * 
     * @return A CoreString with trimmed values. 
     */
    public CoreString trim() {
        return new CoreString(this.commonCore, map(new F1<String, String>() {
            public String f(final String x) {
                return x.trim();
            }
        }).array(String.class));
    }

    
    /**
     * Creates URIs for all enclosed Strings.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$("http://jcores.net/index.html").uri().download()</code> - Downloads the file at the given URI to the temporary directory.</li>
     * </ul>
     * 
     * Multi-threaded.<br/>
     * <br/>
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
