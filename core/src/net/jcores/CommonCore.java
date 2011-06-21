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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;

import javax.swing.SwingUtilities;

import net.jcores.cores.Core;
import net.jcores.cores.CoreNumber;
import net.jcores.interfaces.functions.F0;
import net.jcores.managers.Manager;
import net.jcores.managers.ManagerClass;
import net.jcores.managers.ManagerDebugGUI;
import net.jcores.managers.ManagerDeveloperFeedback;
import net.jcores.managers.ManagerLogging;
import net.jcores.options.MessageType;
import net.jcores.options.Option;
import net.jcores.utils.internal.Reporter;
import net.jcores.utils.internal.system.ProfileInformation;
import net.jcores.utils.map.ConcurrentMapUtil;
import net.jcores.utils.map.MapUtil;

/**
 * The common core is a singleton object shared by (thus common to) all created {@link Core} instances. It mainly
 * contains helper and utility methods and takes care
 * of the {@link Manager} objects. For example, to but the current thread to sleep (without the
 * ugly try/catch), you would write:<br/>
 * <br/>
 * 
 * <code>$.sleep(1000);</code> <br/>
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
     * Executes the given function in the Event Dispatch Thread (EDT) at some
     * point in the future.
     * 
     * @since 1.0
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
     * @since 1.0
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
     * Ensures the value <code>x</code> is between a and b, so that
     * <code>a <= x <= b</code>. If x is larger or smaller, it will be 
     * limited to the given  bounds.
     *  
     * @param a The lower bound. 
     * @param x The value.
     * @param b The upper bound.
     * 
     * @since 1.0
     * @return The new list.
     */
    public double limit(double a, double x, double b) {
        if(x < a) return a;
        if(x > b) return b;
        return x;
    }
    
    
    /**
     * Ensures the value <code>x</code> is between a and b, so that
     * <code>a <= x <= b</code>. If x is larger or smaller, it will be 
     * limited to the given  bounds.
     *  
     * @param a The lower bound. 
     * @param x The value.
     * @param b The upper bound.
     * 
     * @since 1.0
     * @return The new list.
     */
    public int limit(int a, int x, int b) {
        if(x < a) return a;
        if(x > b) return b;
        return x;
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
     * Permutes the given <b>sorted</b> list of objects. With each invocation the next
     * possible permutation will be constructed. You can call this method multiple times
     * on the same array, which iteratively creates the next permutation until <code>false</code> is being returned. In
     * that case, the array was not permuted
     * and no other permutations exist.
     * 
     * @since 1.0
     * @param <T> The type of the array.
     * @param objects The array to permute.
     * @return True if the array was successfully permuted, false if not. Once this method
     * returns false, subsequent calls on the same array will always return false.
     */
    public <T extends Comparable<T>> boolean permute(T objects[]) {
        // Pseudocode from Wikipedia
        // Find the largest index k such that a[k] < a[k + 1]. If no such index exists, the permutation is the last
        // permutation.
        int kk = -1, ll = -1, n = objects.length;
        for (int k = 0; k < n - 1; k++) {
            if (objects[k].compareTo(objects[k + 1]) < 0) kk = k;
        }
        if (kk < 0) return false;

        // Find the largest index l such that a[k] < a[l]. Since k + 1 is such an index, l is well defined and satisfies
        // k < l.
        for (int l = 0; l < n; l++) {
            if (objects[kk].compareTo(objects[l]) < 0) ll = l;
        }

        // Swap a[k] with a[l].
        swap(objects, kk, ll);

        // Reverse the sequence from a[k + 1] up to and including the final element a[n].
        int c = 1;
        for (int i = kk + 1; i < n; i++) {
            if (i >= n - c) break;
            swap(objects, i, n - c++);
        }

        return true;
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
     * Swaps two elements in an array.
     * 
     * @since 1.0
     * @param <T> The type of the object array.
     * @param objects The array to swap the elements in.
     * @param i The index i to swap with j.
     * @param j The index j to swap with i.
     */
    @SuppressWarnings("unchecked")
    public <T> void swap(T objects[], int i, int j) {
        Object tmp = objects[i];
        objects[i] = objects[j];
        objects[j] = (T) tmp;
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
        if (!file.mkdirs()) {
            report(MessageType.EXCEPTION, "Unable to create directory " + file);
        }
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
        for (int val : object)
            myIntegers[i++] = val;

        return myIntegers;
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
