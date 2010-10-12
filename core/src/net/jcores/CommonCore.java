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

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.jcores.managers.Manager;
import net.jcores.managers.ManagerClass;
import net.jcores.managers.ManagerDebugGUI;
import net.jcores.managers.ManagerStatistics;
import net.jcores.options.MessageType;
import net.jcores.utils.Reporter;

/**
 * The common core is a singleton object shared by (thus common to) all created
 * core instances. It mainly contains helper and utility methods and takes care 
 * of the managers.<br/><br/> 
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
     * Sets a manager of a given type.
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
     * Returns a manager of the given type.
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
     * Reports the problem to our internal problem queue. Use report() for all 
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
     * Prints all known problem reports to the console. 
     */
    public void report() {
        this.reporter.printRecords();
    }

    /**
     * Returns our random object.
     * 
     * @return The initialized random object.
     */
    public Random random() {
        return this.random;
    }
}
