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

import static net.jcores.CoreKeeper.$;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;

import net.jcores.CommonCore;
import net.jcores.interfaces.functions.F1;
import net.jcores.interfaces.functions.F1Object2Bool;
import net.jcores.options.MessageType;
import net.jcores.options.Option;
import net.jcores.utils.internal.io.FileUtils;
import net.jcores.utils.internal.io.StreamUtils;
import net.jcores.utils.internal.sound.SoundUtils;

/**
 * Wraps a number of files and exposes some convenience functions. For example, 
 * to list all files in a given path (denoted by a file-object), 
 * write:<br/><br/>
 * 
 * <code>$(path).dir().print()</code>
 * 
 * 
 * @author Ralf Biedert
 * @since 1.0
 */
public class CoreFile extends CoreObject<File> {

    /** Used for serialization */
    private static final long serialVersionUID = -8743359735096052185L;

    /**
     * Creates a file core.
     * 
     * @param supercore The common core.
     * @param files The files to wrap.
     */
    public CoreFile(CommonCore supercore, File... files) {
        super(supercore, files);
    }

    /**
     * Appends the object.toString() to all given files. Usually only called with a single
     * enclosed file object. <br/>
     * <br/>
     * 
     * Multi-threaded.<br/>
     * <br/>
     * 
     * @param object The object to write to all enclosed files.
     * 
     * @return The same core file object (<code>this</code>).
     */
    public CoreFile append(Object object) {
        if (object == null) return this;

        final String string = object.toString();

        map(new F1<File, Object>() {
            public Object f(File x) {
                try {
                    PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(x, true)), "UTF-8"));
                    printWriter.append(string);
                    printWriter.flush();
                    printWriter.close();
                } catch (FileNotFoundException e) {
                    CoreFile.this.commonCore.report(MessageType.EXCEPTION, e.getLocalizedMessage());
                } catch (UnsupportedEncodingException e) {
                    CoreFile.this.commonCore.report(MessageType.EXCEPTION, e.getLocalizedMessage());
                }
                return null;
            }
        });

        return this;
    }
    
    
    /**
     * Treats the given files as audio files and returns a {@link CoreAudioInputStream} for them.<br/>
     * <br/>
     * 
     * Multi-threaded.<br/>
     * <br/>
     * 
     * @return The new core for the {@link AudioInputStream} objects.
     */
    public CoreAudioInputStream audio() {
        return new CoreAudioInputStream(this.commonCore, map(new F1<File, AudioInputStream>() {
            @Override
            public AudioInputStream f(File x) {
                return SoundUtils.getStream(x);
            }
        }).array(AudioInputStream.class));
    }
    

    /**
     * Copies all enclosed files to the destination. If <code>destination</code> is a directory all enclosed objects
     * will be copied into that directory. If the destination is a file then it will be overwritten. In that case, and
     * if this core encloses multiple files or directories, it is undefined what the content of <code>destination</code>
     * will be afterwards<br/>
     * <br/>
     * 
     * Multi-threaded.<br/>
     * <br/>
     * 
     * @param destination The destination to write to. Can be a directory or a file. Directories <b>must end with a slash
     * (<code>/</code>) or pre-exist</b>, otherwise they will be treated as files!
     * 
     * @return The new core file object, containing all files that have been copied..
     */
    public CoreFile copy(String destination) {
        if (destination == null) {
            this.commonCore.report(MessageType.MISUSE, "Destination null for copy().");
            return this;
        }

        final File dest = new File(destination);
        
        return new CoreFile(this.commonCore, map(new F1<File, File[]>() {
            @Override
            public File[] f(File x) {
                return FileUtils.copy(CoreFile.this.commonCore, x, dest);
            }
        }).expand(File.class).array(File.class));
    }

    /**
     * Opens the enclosed file streams as binary files and reads their data into byte
     * buffers.
     * File stream which could not be opened will be returned as null.<br/>
     * <br/>
     * 
     * Multi-threaded.<br/>
     * <br/>
     * 
     * @return A CoreByteBuffer with binary content.
     */
    public CoreByteBuffer data() {
        return new CoreByteBuffer(this.commonCore, map(new F1<File, ByteBuffer>() {
            public ByteBuffer f(File x) {
                try {
                    final FileChannel channel = new FileInputStream(x).getChannel();
                    final long size = channel.size();

                    final ByteBuffer buffer = ByteBuffer.allocate((int) size);
                    int read = channel.read(buffer);

                    if (read != size) {
                        CoreFile.this.commonCore.report(MessageType.EXCEPTION, "Error reading data() from " + x + ". Size mismatch (" + read + " != " + size + ")");
                        return null;
                    }

                    channel.close();
                    return buffer;
                } catch (FileNotFoundException e) {
                    CoreFile.this.commonCore.report(MessageType.EXCEPTION, "Error reading data() from " + x + ". File not found!");
                    return null;
                } catch (IOException e) {
                    CoreFile.this.commonCore.report(MessageType.EXCEPTION, "Error reading data() from " + x + ". IOException!");
                    return null;
                }
            }
        }).array(ByteBuffer.class));
    }

    /**
     * Deletes the given file objects, recursively. Also deletes directories. Unless the
     * files or directories are write protected or locked they should be gone afterwards.<br/>
     * <br/>
     * 
     * Multi-threaded.<br/>
     * <br/>
     * 
     * @return The same core file object (<code>this</code>).
     */
    public CoreFile delete() {
        map(new F1<File, Void>() {
            public Void f(File x) {
                int lastSize = Integer.MAX_VALUE;
                List<File> list = $(x).dir(Option.LIST_DIRECTORIES).list();

                while (list.size() < lastSize) {
                    lastSize = list.size();

                    for (File file : list) {
                        file.delete();
                    }

                    list = $(x).dir(Option.LIST_DIRECTORIES).list();
                }

                x.delete();

                return null;
            }
        });

        return this;
    }

    /**
     * De-serializes the previously serialized core from the enclosed file. Objects that
     * are not serializable
     * are ignored.<br/>
     * <br/>
     * 
     * Single-threaded. Size-of-one.<br/>
     * <br/>
     * 
     * @param <T> Type of the returned core's content.
     * @param type Type of the returned core's content.
     * @param options Currently not used.
     * @return The previously serialized core (using <code>.serialize()</code>).
     * @see CoreObject
     */
    public <T> CoreObject<T> deserialize(Class<T> type, Option... options) {

        if (size() > 1)
            this.commonCore.report(MessageType.MISUSE, "deserialize() should not be used on cores with more than one class!");

        // Try to restore the core, and don't forget to set the commonCore
        try {
            final CoreObject<T> core = StreamUtils.deserializeCore(type, new FileInputStream(get(0)));
            if (core != null) {
                core.commonCore = this.commonCore;
                return core;
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return new CoreObject<T>(this.commonCore, type, null);
    }

    /**
     * Lists the contents of all sub directories. A CoreFile with all found files
     * in all sub directories is returned.<br/>
     * <br/>
     * 
     * Multi-threaded.<br/>
     * <br/>
     * 
     * @param options Relevant options: <code>OptionListDirectories</code>.
     * 
     * @return A CoreFile with all found files (and, if selected, directories).
     */
    public CoreFile dir(Option... options) {

        // Check if we should emit diretories
        final boolean listDirs = $(options).contains(Option.LIST_DIRECTORIES);

        return map(new F1<File, File[]>() {
            @Override
            public File[] f(File x) {
                return FileUtils.dir(x, listDirs);
            }
        }).expand(File.class).unique().as(CoreFile.class);
    }

    /**
     * Returns the file sizes for all enclose file objects<br/>
     * <br/>
     * 
     * Multi-threaded.<br/>
     * <br/>
     * 
     * 
     * @return A CoreFile containing a filtered subset of our elements.
     */
    public CoreNumber filesize() {
        return new CoreNumber(this.commonCore, map(new F1<File, Long>() {
            @SuppressWarnings("boxing")
            public Long f(final File x) {
                return x.length();
            }
        }).array(Long.class));
    }

    /**
     * Filters all files by their name using the given regular expression. <br/>
     * <br/>
     * 
     * Multi-threaded.<br/>
     * <br/>
     * 
     * @param regex The regular expression to use.
     * @param options Currently none used.
     * 
     * @return A CoreFile containing a filtered subset of our elements.
     */
    @Override
    public CoreFile filter(final String regex, Option... options) {
        final Pattern p = Pattern.compile(regex);
        return new CoreFile(this.commonCore, filter(new F1Object2Bool<File>() {
            public boolean f(File x) {
                final Matcher matcher = p.matcher(x.getAbsolutePath());
                return matcher.matches();
            }
        }, options).array(File.class));
    }

    
    /**
     * Tries to load all enclosed files as images.<br/>
     * <br/>
     * 
     * Multi-threaded.<br/>
     * <br/>
     * 
     * @return A CoreBufferedImage with the loaded images.
     */
    public CoreBufferedImage images() {
        return new CoreBufferedImage(this.commonCore, map(new F1<File, BufferedImage>() {
            public BufferedImage f(File x) {
                try {
                    return ImageIO.read(x);
                } catch (IOException e) {
                    CoreFile.this.commonCore.report(MessageType.EXCEPTION, "Error loading image " + x);
                }
                return null;
            }
        }).array(BufferedImage.class));
    }

    
    /**
     * Opens the given file objects as input streams. File stream which could not be
     * opened
     * will be returned as null.<br/>
     * <br/>
     * 
     * Multi-threaded.<br/>
     * <br/>
     * 
     * @return A CoreInputStream with the opened files.
     */
    public CoreInputStream input() {
        return new CoreInputStream(this.commonCore, map(new F1<File, InputStream>() {
            public InputStream f(File x) {
                try {
                    return new BufferedInputStream(new FileInputStream(x));
                } catch (FileNotFoundException e) {}
                return null;
            }
        }).array(InputStream.class));
    }

    /**
     * Returns all lines of all files joint. A core will be returned in which each
     * entry is a String containing the specific file's content. This is a shorthand
     * notation for <code>inputstream().text()</code><br/>
     * <br/>
     * 
     * Multi-threaded.<br/>
     * <br/>
     * 
     * @return A CoreString object containing the files' contents.
     */
    public CoreString text() {
        return new CoreString(this.commonCore, map(new F1<File, String>() {
            public String f(final File x) {
                return FileUtils.readText(CoreFile.this.commonCore, x);
            }
        }).array(String.class));
    }

    /**
     * Converts all files to URIs.<br/>
     * <br/>
     * 
     * Multi-threaded.<br/>
     * <br/>
     * 
     * @return A CoreURI object with all converted files.
     */
    public CoreURI uri() {
        return new CoreURI(this.commonCore, map(new F1<File, URI>() {
            public URI f(File x) {
                return x.toURI();
            }
        }).array(URI.class));
    }

    /**
     * Puts all enclosed files into the ZIP file <code>target</code>. If files are enclosed individually they will be
     * stored as a top-level entry. If directories are enclosed in this core, the relative paths below that directory
     * are preserved.<br/>
     * <br/>
     * 
     * Single-threaded.<br/>
     * <br/>
     * 
     * @param target The file to write the ZIP to.
     * @param options Currently none used.
     * @return This Core again.
     */
    public CoreFile zip(String target, Option... options) {
        FileUtils.zipFiles(new File(target), this.t);
        return this;
    }
}
