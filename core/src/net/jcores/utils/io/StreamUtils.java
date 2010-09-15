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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.jcores.CommonCore;
import net.jcores.options.MessageType;

/**
 * @author Ralf Biedert
 */
public class StreamUtils {
    /**
     * Unzips the given stream.
     * 
     * @param inputStream
     * @param destinationDirectory
     * @throws IOException
     */
    public static void doUnzip(InputStream inputStream, String destinationDirectory)
                                                                                    throws IOException {
        final int BUFFER = 8 * 1024;
        final List<String> zipFiles = new ArrayList<String>();
        final File unzipDestinationDirectory = new File(destinationDirectory);

        unzipDestinationDirectory.mkdirs();

        final ZipInputStream zipFile = new ZipInputStream(inputStream);

        ZipEntry nextEntry = zipFile.getNextEntry();

        // Process each entry
        while (nextEntry != null) {
            // grab a zip file entry
            final String currentEntry = nextEntry.getName();
            final File destFile = new File(unzipDestinationDirectory, currentEntry);

            if (currentEntry.endsWith(".zip")) {
                zipFiles.add(destFile.getAbsolutePath());
            }

            // grab file's parent directory structure
            final File destinationParent = destFile.getParentFile();

            // create the parent directory structure if needed
            destinationParent.mkdirs();

            try {
                // extract file if not a directory
                if (!nextEntry.isDirectory()) {
                    final BufferedInputStream is = new BufferedInputStream(zipFile);
                    int currentByte;
                    // establish buffer for writing file
                    byte data[] = new byte[BUFFER];

                    // write the current file to disk
                    final FileOutputStream fos = new FileOutputStream(destFile);
                    final BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);

                    // read and write until last byte is encountered
                    while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
                        dest.write(data, 0, currentByte);
                    }
                    dest.flush();
                    dest.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

            //zipFile.closeEntry();
            //nextEntry = zipFile.closeEntry();
            nextEntry = zipFile.getNextEntry();
        }

        zipFile.close();
    }

    /**
     * Reads the content of file as text.
     * 
     * @param cc 
     * @param is
     * @return .
     */
    public static String readText(CommonCore cc, InputStream is) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }

            return sb.toString();
        } catch (IOException e) {
            cc.report(MessageType.EXCEPTION, "Error reading from stream " + is);
        }

        return null;
    }

    /**
     * Returns an input stream for the requested path.
     * 
     * @param zipFile
     * @param path
     * @return .
     * @throws IOException
     */
    public static InputStream getInputStream(ZipInputStream zipFile, String path)
                                                                                 throws IOException {
        ZipEntry nextEntry = zipFile.getNextEntry();

        // Process each entry
        while (nextEntry != null) {
            // grab a zip file entry
            final String currentEntry = nextEntry.getName();

            if (!currentEntry.equals(path)) {
                nextEntry = zipFile.getNextEntry();
                continue;
            }

            return new BufferedInputStream(zipFile);
        }
        return null;
    }

    /**
     * Lists all element within the given stream.
     * 
     * @param zipFile
     * @return .
     * @throws IOException 
     */
    public static List<String> list(ZipInputStream zipFile) throws IOException {
        final List<String> rval = new ArrayList<String>();
        ZipEntry nextEntry = zipFile.getNextEntry();

        // Process each entry
        while (nextEntry != null) {
            rval.add(nextEntry.getName());
            nextEntry = zipFile.getNextEntry();
        }

        return rval;
    }
}
