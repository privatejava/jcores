/*
 * CommonCore.java
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

import static net.jcores.CoreKeeper.$;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import net.jcores.cores.Core;
import net.jcores.cores.CoreNumber;
import net.jcores.interfaces.functions.F0;
import net.jcores.managers.Manager;
import net.jcores.managers.ManagerClass;
import net.jcores.managers.ManagerDebugGUI;
import net.jcores.managers.ManagerStatistics;
import net.jcores.options.MessageType;
import net.jcores.options.Option;
import net.jcores.utils.internal.Reporter;

/**
 * The common core is a singleton object shared by (thus common to) all created
 * {@link Core} instances. It mainly contains helper and utility methods and takes care 
 * of the {@link Manager} objects. For example, to but the current thread to sleep (without the 
 * ugly try/catch), you would write:<br/><br/>
 * 
 * <code>$.sleep(1000);</code>
 * <br/><br/> 
 * 
 * Methods and object commonly required by the other cores. All methods in here are (and must be!) 
 * thread safe. 
 * 
 * @author Ralf Biedert
 * @since 1.0
 */
public class CommonCore {

    /** Executes commands */
    private final ExecutorService executor;

    /** Common logger */
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    /** Stores error reports */
    private final Reporter reporter = new Reporter();

    /** All managers we have */
    private final ConcurrentMap<Class<? extends Manager>, Manager> managers = new ConcurrentHashMap<Class<? extends Manager>, Manager>();

    /** Random variable */
    private final Random random = new Random();

    /**
     * Constructs the common core. 
     */
    CommonCore() {
        // Create an executor that does not prevent us from quitting.
        this.executor = Executors.newCachedThreadPool(new ThreadFactory() {
            public Thread newThread(Runnable r) {
                final Thread t = new Thread(r);
                t.setDaemon(true);
                return t;
            }
        });

        // Register managers we know
        manager(ManagerClass.class, new ManagerClass());
        manager(ManagerStatistics.class, new ManagerStatistics());
        manager(ManagerDebugGUI.class, new ManagerDebugGUI());
    }

    /**
     * Wraps number of ints and returns an Integer array.
     * 
     * @param object The numbers to wrap.
     * @return An Integer array.
     */
    @SuppressWarnings("boxing")
    public Integer[] box(int... object) {
        int i = 0;
        
        final Integer[] myIntegers = new Integer[object.length];
        for(int val : object) myIntegers[i++] = val;
        
        return myIntegers;
    }
    
    
    /**
     * Executes the given function in the Event Dispatch Thread (EDT) at some 
     * point in the future.
     * 
     * @param f0 The function to execute.
     */
    public void edt(final F0 f0) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                f0.f();
            }
        });
    }
    
    
    /**
     * Executes the given function in the Event Dispatch Thread (EDT) now, waiting until 
     * the function was executed.
     * 
     * @param f0 The function to execute.
     */
    public void edtnow(final F0 f0) {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    f0.f();
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
    
    
    /**
     * Executes the given runnable count times. Call this method with your 
     * given workers and a number of threads (usually number of CPUs).
     * 
     * @param r The runnable to execute. 
     * @param count Number of threads to spawn.
     */
    public void execute(Runnable r, int count) {
        for (int i = 0; i < count; i++)
            this.executor.execute(r);
    }

    /**
     * Sets a manager of a given type, only needed for core developers. 
     * 
     * @param <T> Manager's type.
     * @param clazz Manager's class.
     * @param manager The actual manager to put.
     * @return Return the manager that was already in the list, if there was one, or the current manager which was also set.
     */
    @SuppressWarnings("unchecked")
    public <T extends Manager> T manager(Class<T> clazz, T manager) {
        this.managers.putIfAbsent(clazz, manager);
        return (T) this.managers.get(clazz);
    }

    /**
     * Returns a manager of the given type, only needed for core developers.
     * 
     * @param <T> Manager's type.
     * @param clazz Manager's class.
     * @return Returns the currently set manager.
     */
    @SuppressWarnings("unchecked")
    public <T extends Manager> T manager(Class<T> clazz) {
        return (T) this.managers.get(clazz);
    }

    /**
     * Logs the given string. This method might, but is not required, to use the official Java logging 
     * facilities. 
     * 
     * @param string The string to log.
     * @param level Log level to use.
     */
    public void log(String string, Level level) {
        this.logger.log(level, string);
    }

    
    /**
     * Executes the given function after the given delay.
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
     * Creates a CoreNumber object with numbers ranging from 0 (inclusive) up to <code>end</code> (exclusive).  
     * 
     * @param end The last number (exclusive).
     * @return A core number object. 
     */
    public CoreNumber range(int end) {
        return range(0, end, 1);
    }
    
    /**
     * Creates a CoreNumber object with the given <code>start</code> (inclusive) and 
     * <code>end</code> (exclusive) and a stepping of +-1 (depending on whether start is smaller or larger than end).  
     * 
     * @param from The first number (inclusive) 
     * @param end The last number (exclusive).
     * @return A core number object. 
     */
    public CoreNumber range(int from, int end) {
        return range(from, end, from <= end ? 1 : -1);
    }

    
    /**
     * Creates a CoreNumber object with the given <code>start</code> (inclusive), <code>end</code> (exclusive) and stepping.  
     * 
     * @param from The first number (inclusive) 
     * @param end The last number (exclusive).
     * @param stepping The stepping
     *  
     * @return A core number object. 
     */
    public CoreNumber range(int from, int end, int stepping) {
        // FIXME: Stepping problems
        final int rval[] = new int[Math.abs((end-from)/stepping)];
        int ptr = 0;
        
        if(from <= end) {
            for(int i=from; i<end; i+=stepping) {
                rval[ptr++] = i;
            }
        } else {
            for(int i=from; i>end; i+=stepping) {
                rval[ptr++] = i;
            }            
        }
        
        return $(box(rval));
    }
    
    
    /**
     * Reports the problem to our internal problem queue, only used by core developers. Use report() for all 
     * internal error and problem reporting and use log() for user requested 
     * logging.
     * 
     * @param type Type of the message.
     * @param problem Problem description.
     */
    public void report(MessageType type, String problem) {
        this.reporter.record(problem);
    }

    /**
     * Prints all known problem reports to the console. This is the end-user 
     * version (which means, <i>you</i> can use it) to print what went wrong during
     * core operation. See the console for output.
     */
    public void report() {
        this.reporter.printRecords();
    }

    /**
     * Returns our shared {@link Random} object, initialized some time ago.
     * 
     * @return The initialized random object.
     */
    public Random random() {
        return this.random;
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
        final File file = new File(tempfile().getAbsoluteFile() + ".dir/");
        file.mkdirs();
        return file;
    }


    /**
     * Unboxes a number of Integers. 
     * 
     * @param object The numbers to unbox.
     * @return An int array.
     */
    @SuppressWarnings("boxing")
    public int[] unbox(Integer... object) {
        int i = 0;
        
        final int[] myIntegers = new int[object.length];
        for(int val : object) myIntegers[i++] = val;
        
        return myIntegers;
    }


    /**
     * Creates a unique ID. If nothing is specified each call delivers a 
     * new, unique ID. TODO: Option.UID_SYSTEM, .UID_USER, .UID_APP, ...
     *  
     * @param options 
     * @return Returns a unique ID.
     */
    public String uniqueID(Option ... options) {
        return UUID.randomUUID().toString();
    }
}
