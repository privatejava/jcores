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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.jcores.managers.Manager;
import net.jcores.managers.ManagerClass;
import net.jcores.managers.ManagerStatistics;
import net.jcores.options.MessageType;
import net.jcores.utils.Reporter;

/**
 * Methods and object commonly required by the other cores. All methods in here are (and must be!) 
 * thread safe. 
 * 
 * @author Ralf Biedert
 */
public class CommonCore {

    /** Executes commands */
    private final ExecutorService executor;

    /** Commong logger */
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    /** Stores error reports */
    private final Reporter reporter = new Reporter();

    /** All managers we have */
    private final ConcurrentMap<Class<? extends Manager>, Manager> managers = new ConcurrentHashMap<Class<? extends Manager>, Manager>();

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
    }

    /**
     * Executes the given runnable count times.
     * 
     * @param r
     * @param count 
     */
    public void execute(Runnable r, int count) {
        for (int i = 0; i < count; i++)
            this.executor.execute(r);
    }

    /**
     * Sets a manager.
     * 
     * @param <T>
     *  
     * @param type
     * @param m
     * @return . 
     */
    @SuppressWarnings("unchecked")
    public <T extends Manager> T manager(Class<T> type, T m) {
        this.managers.putIfAbsent(type, m);
        return (T) this.managers.get(type);
    }

    /**
     * Gets a manager
     * 
     * @param <T>
     * @param type 
     * @return .
     */
    @SuppressWarnings("unchecked")
    public <T extends Manager> T manager(Class<T> type) {
        return (T) this.managers.get(type);
    }

    /**
     * Logs the given string to a logger. 
     * 
     * @param string
     * @param level
     */
    public void log(String string, Level level) {
        this.logger.log(level, string);
    }

    /**
     * Reports the problem to our internal problem queue. Use report() for all 
     * internal error and problem reporting and use log() for user requested 
     * logging.
     * 
     * @param type 
     * @param problem
     */
    public void report(MessageType type, String problem) {
        this.reporter.record(problem);
    }

    /**
     * Prints all problem reports. 
     */
    public void report() {
        this.reporter.printRecords();
    }
}
