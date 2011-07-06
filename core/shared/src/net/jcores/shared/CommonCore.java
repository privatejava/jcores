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
package net.jcores.shared;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import net.jcores.shared.cores.Core;
import net.jcores.shared.cores.CoreNumber;
import net.jcores.shared.cores.CoreObject;
import net.jcores.shared.cores.adapter.ListAdapter;
import net.jcores.shared.cores.commons.CommonAlgorithmic;
import net.jcores.shared.cores.commons.CommonSys;
import net.jcores.shared.interfaces.functions.F0;
import net.jcores.shared.managers.Manager;
import net.jcores.shared.managers.ManagerClass;
import net.jcores.shared.managers.ManagerDebugGUI;
import net.jcores.shared.managers.ManagerDeveloperFeedback;
import net.jcores.shared.managers.ManagerLogging;
import net.jcores.shared.options.MessageType;
import net.jcores.shared.utils.internal.Reporter;
import net.jcores.shared.utils.internal.system.ProfileInformation;
import net.jcores.shared.utils.map.ConcurrentMapUtil;
import net.jcores.shared.utils.map.MapUtil;

/**
 * The common core is a singleton object shared by (thus common to) all created {@link Core} instances. It mainly
 * contains helper and utility methods and takes care of the {@link Manager} objects. For example, to but the 
 * current thread to sleep (without the ugly try/catch), you would write:<br/>
 * <br/>
 * 
 * <code>$.sys.sleep(1000);</code> <br/>
 * <br/>
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

    /** Stores error reports */
    private final Reporter reporter = new Reporter();

    /** All managers we have */
    private final ConcurrentMap<Class<? extends Manager>, Manager> managers = new ConcurrentHashMap<Class<? extends Manager>, Manager>();

    /** Random variable */
    private final Random random = new Random();

    /** Keeps the profile information */
    private final ProfileInformation profileInformation;

    /** Method to clone objects */
    private Method cloneMethod;

    /** The number of free CPUs */
    private AtomicInteger freeCPUs = new AtomicInteger();
    
    /** Common system utilities */
    public final CommonSys sys = new CommonSys(this); 

    /** Common algorithmic utilities */
    public final CommonAlgorithmic alg = new CommonAlgorithmic(this); 
    
    /**
     * Constructs the common core.
     */
    protected CommonCore() {
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
        manager(ManagerDeveloperFeedback.class, new ManagerDeveloperFeedback());
        manager(ManagerDebugGUI.class, new ManagerDebugGUI());
        manager(ManagerLogging.class, new ManagerLogging());

        try {
            this.cloneMethod = Object.class.getDeclaredMethod("clone");
            this.cloneMethod.setAccessible(true);
        } catch (Exception e) {
            report(MessageType.EXCEPTION, "Unable to get cloning method for objects. $.clone() will not work: " + e.getMessage());
        } 

        
        // Test how long it takes to execute a thread in the background
        this.profileInformation = profile();
        this.freeCPUs.set(this.profileInformation.numCPUs);
    }

    /**
     * Benchmark the VM. Dirty, but should give us some rough estimates
     * 
     * @return
     */
    private ProfileInformation profile() {
        final ProfileInformation p = new ProfileInformation();
        final int RUNS = 10;
        final int N = 5;

        // Measure how long it takes to fork a thread and to wait for it again. We
        // test 10 times and take the average of the last 5 runs.
        long times[] = new long[RUNS];
        for (int i = 0; i < RUNS; i++) {
            times[i] = measure(new F0() {
                @Override
                public void f() {
                    final CyclicBarrier barrier = new CyclicBarrier(2);

                    execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                barrier.await();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (BrokenBarrierException e) {
                                e.printStackTrace();
                            }
                        }
                    }, 1);

                    try {
                        barrier.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (BrokenBarrierException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        // Now take the average
        for (int i = RUNS - N; i < times.length; i++) {
            p.forkTime += times[i];
        }

        p.forkTime /= N;
        p.numCPUs = Runtime.getRuntime().availableProcessors();

        return p;
    }

    /**
     * Wraps number of ints and returns an Integer array.
     * 
     * @since 1.0
     * @param object The numbers to wrap.
     * @return An Integer array.
     */
    @SuppressWarnings("boxing")
    public Integer[] box(int... object) {
        int i = 0;

        final Integer[] myIntegers = new Integer[object.length];
        for (int val : object)
            myIntegers[i++] = val;

        return myIntegers;
    }


    /**
     * Clones the given object if it is cloneable. 
     * 
     * @since 1.0
     * @param <T> 
     * @param object The object to clone
     * @return A clone of the object, or null if the object could not be cloned.
     */
    @SuppressWarnings("unchecked")
    public <T> T clone(T object) {
        if(!(object instanceof Cloneable)) return null;
        
        try {
            return (T) this.cloneMethod.invoke(object);
        } catch (Exception e) {
            report(MessageType.EXCEPTION, "Unable to execute clone() on " + object);
        } 
        
        return null;
    }


    /**
     * Clones the given array and returns a <b>shallow</b> copy (i.e., the elements themselves 
     * are the same in both arrays).
     *  
     * @since 1.0
     * @param <T> 
     * @param object The array to clone
     * @return A cloned (copied) array.
     */
    public <T> T[] clone(T[] object) {
        if(object == null) return null;
        return Arrays.copyOf(object, object.length);
    }

    
    
    /**
     * Returns a new and empty {@link ConcurrentMapUtil}.  
     * 
     * @param <K> The type of the key.
     * @param <V> The type of the value.
     * @since 1.0
     * @return Returns a new map.
     */
    public <K, V> ConcurrentMapUtil<K, V> concurrentMap() {
        return new ConcurrentMapUtil<K,V>(new ConcurrentHashMap<K, V>());
    }
    
    /**
     * Wraps a given map into out {@link ConcurrentMapUtil}.  
     * 
     * @param <K> The type of the key.
     * @param <V> The type of the value.
     * @param map The map to wrap.
     * @return Returns a wrapped map.
     */
    public <K, V> ConcurrentMapUtil<K, V> concurrentMap(ConcurrentMap<K, V> map) {
        return new ConcurrentMapUtil<K,V>(map);
    }

    
    /**
     * Returns a core consisting of <code>n</code> times the given object.
     * 
     * @param object The object to fill the core with. 
     * @param n The number of times we put the object into the core.
     * @param <T> The type of the object.
     * @since 1.0 
     * @return A core of size <code>n</code> filled with the given object.
     * 
     */
    public <T> CoreObject<T> create(T object, int n) {
        final ArrayList<T> list = new ArrayList<T>(n);
        for (int i = 0; i < n; i++) {
            list.add(object);
        }
        return new CoreObject<T>(this, new ListAdapter<T>(list));
    }



    /**
     * Clones the given collection and returns a <b>shallow</b> copy (i.e., the elements themselves 
     * are the same in both arrays).
     *  
     * @since 1.0
     * @param <T> 
     * @param collection The collection to clone
     * @return A cloned (copied) collection.
     */
    public <T> Collection<T> clone(Collection<T> collection) {
        if(collection == null) return null;
        return new ArrayList<T>(collection);
    }
    
    /**
     * Clones the given array and returns a <b>deep</b> copy (i.e., the elements themselves 
     * are the cloned in both arrays).
     *  
     * @since 1.0
     * @param <T> 
     * @param object The array to clone
     * @return A cloned array.
     */
    public <T> T[] deepclone(T[] object) {
        if(object == null) return null;
        
        final T[] copyOf = Arrays.copyOf(object, object.length);
        for (int i = 0; i < copyOf.length; i++) {
            copyOf[i] = clone(copyOf[i]);
        }
        
        return copyOf;
    }

    
    /**
     * Executes the given runnable count times. Call this method with your
     * given workers and a number of threads (usually number of CPUs).
     *
     * @since 1.0
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
     * @return Return the manager that was already in the list, if there was one, or the current manager which was also
     * set.
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
     * Returns a new and empty (hash) {@link MapUtil}.  
     * 
     * @param <K> The type of the key.
     * @param <V> The type of the value.
     * @since 1.0
     * @return Returns a new map.
     */
    public <K, V> MapUtil<K, V> map() {
        return new MapUtil<K,V>(new HashMap<K, V>());
    }
    
    /**
     * Wraps a given map into out {@link MapUtil}.  
     * 
     * @param <K> The type of the key.
     * @param <V> The type of the value.
     * @param map The map to wrap.
     * @return Returns a wrapped map.
     */
    public <K, V> MapUtil<K, V> map(Map<K, V> map) {
        return new MapUtil<K,V>(map);
    }

    
    /**
     * Returns a new (linked) {@link List} when the number of elements to 
     * store is not known.
     * 
     * @since 1.0
     * @param <T> The type of the list. 
     * @return The new list.
     */
    public <T> List<T> list() {
        return new LinkedList<T>();
    }

    /**
     * Returns a new (array) {@link List} when the number of elements to store is 
     * approximately known.
     * 
     * @since 1.0
     * @param <T> The type of the list. 
     * @param n The approximate number of elements to store.
     * @return The new list.
     */
    public <T> List<T> list(int n) {
        return new ArrayList<T>(n);
    }


    /**
     * Logs the given string. This method might, but is not required, to use the official Java logging
     * facilities.
     * 
     * @param string The string to log.
     * @param level Log level to use.
     */
    public void log(String string, Level level) {
        this.manager(ManagerLogging.class).handler().log(string, level);
    }

    /**
     * Measures how long the execution of the given function took. The result will be returned in nanoseconds.
     * 
     * @param f The function to execute.
     * @return The elapsed time in nanoseconds.
     */
    public long measure(F0 f) {
        final long start = System.nanoTime();
        f.f();
        final long end = System.nanoTime();
        return end - start;
    }

    
    /**
     * Returns the profiling information gathered at startup. Only required internally.
     * 
     * @return The current profile information.
     */
    public ProfileInformation profileInformation() {
        return this.profileInformation;
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
     * Creates a CoreNumber object with the given <code>start</code> (inclusive) and <code>end</code> (exclusive) and a
     * stepping of +-1 (depending on whether start is smaller or larger than end).
     * 
     * @param from The first number (inclusive)
     * @param end The last number (exclusive).
     * @return A core number object.
     */
    public CoreNumber range(int from, int end) {
        return range(from, end, from <= end ? 1 : -1);
    }

    /**
     * Creates a CoreNumber object with the given <code>start</code> (inclusive), <code>end</code> (exclusive) and
     * stepping.
     * 
     * @param from The first number (inclusive)
     * @param end The last number (exclusive).
     * @param stepping The stepping
     * 
     * @return A core number object.
     */
    public CoreNumber range(int from, int end, int stepping) {
        // FIXME: Stepping problems
        final int rval[] = new int[Math.abs((end - from) / stepping)];
        int ptr = 0;

        if (from <= end) {
            for (int i = from; i < end; i += stepping) {
                rval[ptr++] = i;
            }
        } else {
            for (int i = from; i > end; i += stepping) {
                rval[ptr++] = i;
            }
        }

        return new CoreNumber(this, box(rval));
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
     * Unboxes a number of Integers.
     * 
     * @param object The numbers to unbox.
     * @return An int array.
     */
    @SuppressWarnings("boxing")
    public int[] unbox(Integer... object) {
        int i = 0;

        final int[] myIntegers = new int[object.length];
        for (int val : object)
            myIntegers[i++] = val;

        return myIntegers;
    }
}