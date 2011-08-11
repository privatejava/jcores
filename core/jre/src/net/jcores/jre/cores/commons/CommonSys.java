/*
 * CommonFile.java
 * 
 * Copyright (c) 2011, Ralf Biedert All rights reserved.
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
package net.jcores.jre.cores.commons;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import net.jcores.jre.CommonCore;
import net.jcores.jre.interfaces.functions.F0;
import net.jcores.jre.options.MessageType;
import net.jcores.jre.options.Option;

/**
 * Contains common system utilities. 
 * 
 * @author Ralf Biedert
 * @since 1.0
 *
 */
public class CommonSys extends CommonNamespace {
    
    /** 
     * Creates a common file object.
     * 
     * @param commonCore 
     */
    public CommonSys(CommonCore commonCore) {
        super(commonCore);
    }

    /**
     * Returns a temporary file.
     * 
     * @return A File object for a temporary file.
     */
    public File tempfile() {
        try {
            return File.createTempFile("jcores.", ".tmp");
        } catch (IOException e) {
            //
        }
    
        return new File("/tmp/jcores.failedtmp." + System.nanoTime() + ".tmp");
    }

    /**
     * Returns a temporary directory.
     * 
     * @return A File object for a temporary directory.
     */
    public File tempdir() {
        final File ffile = new File(tempfile().getAbsoluteFile() + ".dir/");
        if (!ffile.mkdirs()) {
            this.commonCore.report(MessageType.EXCEPTION, "Unable to create directory " + ffile);
        }
        return ffile;
    }

    /**
     * Executes the given function after at the given rate
     * indefinitely.
     * 
     * @param f0 The function to execute
     * @param rate The rate at which the function will be executed.
     */
    public void manyTimes(final F0 f0, long rate) {
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                f0.f();
            }
        }, 0, rate);
    }

    /**
     * Executes the given function once after the given delay.
     * 
     * @param f0 The function to execute
     * @param delay The delay after which the function will be executed.
     */
    public void oneTime(final F0 f0, long delay) {
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                f0.f();
            }
        }, delay);
    }

    /**
     * Puts the current thread to sleep for some time, without the need for any try/catch block.
     * 
     * @param time The time to sleep.
     * @return <code>true</code> if the sleep was interrupted, <code>false</code> if not.
     */
    public boolean sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            return true;
        }
    
        return false;
    }
    
    
    
    /**
     * Creates a unique ID. If nothing is specified each call delivers a
     * new, unique ID. TODO: Option.UID_SYSTEM, .UID_USER, .UID_APP, ...
     * 
     * @param options
     * @return Returns a unique ID.
     */
    public String uniqueID(Option... options) {
        return UUID.randomUUID().toString();
    }
}
