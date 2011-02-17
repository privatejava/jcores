/*
 * FileUtil.java
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
package net.jcores.utils.io;

import static net.jcores.CoreKeeper.$;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import net.jcores.CommonCore;
import net.jcores.options.MessageType;

/**
 * @author Ralf Biedert
 */
public class FileUtils {
    /**
     * Reads the content of file as text.
     * 
     * @param cc
     * @param file
     * @return .
     */
    public static String readText(CommonCore cc, File file) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }

            return sb.toString();
        } catch (FileNotFoundException e) {
            cc.report(MessageType.EXCEPTION, "File not found " + file);
        } catch (IOException e) {
            cc.report(MessageType.EXCEPTION, "Error reading from file " + file);
        } finally {
            if (reader != null) try {
                reader.close();
                } catch (IOException e) {
                cc.report(MessageType.EXCEPTION, "Error closing file " + file);
                }
        }

        return null;
    }

    /**
     * Zips a number of files into the target.
     * 
     * @param target
     * @param t
     */
    public static void zipFiles(File target, File[] t) {
        final byte[] buffer = new byte[32 * 1024]; // Create a buffer for copying
        int bytesRead;

        try {
            // Open output zip file
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(target));

            // Process all given files
            for (File file : t) {
                // If it is a file, store it directly, otherwise store subfiles
                final File toStore[] = file.isDirectory() ? $(file).dir().array(File.class) : $(file).array(File.class);
                final String absolute = file.getAbsolutePath();

                for (File file2 : toStore) {
                    // Now check for each item. If this item was added because the original entry denoted
                    // a file, then add this entry by its name only. Otherwise add the entry as something
                    // starting relative to its path
                    String entryname = file.isDirectory() ? file2.getAbsolutePath().substring(absolute.length() + 1) : file2.getName();

                    try {
                        final FileInputStream in = new FileInputStream(file2);
                        final ZipEntry entry = new ZipEntry(entryname);
                        out.putNextEntry(entry);
                        while ((bytesRead = in.read(buffer)) != -1)
                            out.write(buffer, 0, bytesRead);
                        in.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            // Close our result
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
