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
import java.util.UUID;
import java.util.concurrent.Future;

import net.jcores.jre.CommonCore;
import net.jcores.jre.interfaces.functions.F0;
import net.jcores.jre.options.KillSwitch;
import net.jcores.jre.options.MessageType;
import net.jcores.jre.options.Option;
import net.jcores.jre.utils.internal.Options;

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
     * Executes the given function with the given delay (delay in the
     * sense of wait time between two invocations) indefinitely.
     * 
     * @param f0 The function to execute
     * @param delay The delay at which the function will be executed.
     * @param options May accept a {@link KillSwitch}.
     */
    public void manyTimes(final F0 f0, final long delay, Option... options) {
        final Options options$ = Options.$(options);
        final KillSwitch killswitch = options$.killswitch();
        final Future<?> submit = this.commonCore.executor().getExecutor().submit(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        f0.f();
                        sleep(delay);
                    } catch (Exception e) {
                        CommonSys.this.commonCore.report(MessageType.EXCEPTION, "Exception while executing " + f0 + ": " + e.getMessage());
                    }

                    // Check if we should terminate
                    if (killswitch != null && killswitch.terminated()) return;
                }
            }
        });

        // Register the future
        if(killswitch != null) killswitch.register(submit);
    }

    /**
     * Executes the given function once after the given delay (delay in the
     * sense of time until the first execution happens).
     * 
     * @param f0 The function to execute
     * @param delay The delay after which the function will be executed.
     * @param options May accept a {@link KillSwitch}.
     */
    public void oneTime(final F0 f0, final long delay, Option... options) {
        final Options options$ = Options.$(options);
        final KillSwitch killswitch = options$.killswitch();
        final Future<?> submit = this.commonCore.executor().getExecutor().submit(new Runnable() {
            @Override
            public void run() {
                try {
                    sleep(delay);
                    
                    // Check if we should terminate
                    if (killswitch != null && killswitch.terminated()) return;
                    
                    f0.f();
                } catch (Exception e) {
                    CommonSys.this.commonCore.report(MessageType.EXCEPTION, "Exception while executing " + f0 + ": " + e.getMessage());
                }
            }
        });

        // Register the future
        if(killswitch != null) killswitch.register(submit);
    }

    /**
     * Puts the current thread to sleep for some time, without the need for any try/catch block.
     * 
     * @param time The time to sleep.
     * @return A value of <code>0</code> if the sleep was successful, or else the amount
     * of milliseconds which we woke up too early.
     */
    public long sleep(long time) {
        final long start = System.currentTimeMillis();
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            this.commonCore.report(MessageType.EXCEPTION, "Sleep interrupted");
            return time - (System.currentTimeMillis() - start);
        }

        return 0;
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
