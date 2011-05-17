/*
 * Core.java
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

import java.io.Serializable;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

import net.jcores.CommonCore;
import net.jcores.options.Option;
import net.jcores.utils.internal.Folder;
import net.jcores.utils.internal.Mapper;

/**
 * The abstract base class of all cores. Contains commonly used methods and variables. In
 * general
 * you should not need to bother with this class, as in most cases you will extend
 * CoreObject for
 * your own cores, not this class.
 * 
 * @since 1.0
 * @author Ralf Biedert
 */
public abstract class Core implements Serializable {

    /** Used for serialization */
    private static final long serialVersionUID = 2195880634253143587L;

    /** Our 'parent' core. */
    transient protected CommonCore commonCore;

    /**
     * Creates the core object for the given collection.
     * 
     * @param core
     */
    protected Core(CommonCore core) {
        this.commonCore = core;
    }

    /**
     * Returns the size of enclosed elements, counting null elements.
     * 
     * @return The number of element slots this core encloses.
     */
    public abstract int size();

    /**
     * Starts a new parallel mapping process.
     * 
     * @param mapper The mapper to use.
     * @param options Relevant options: <code>OptionMapType</code>.
     */
    @SuppressWarnings("rawtypes")
    protected void map(final Mapper mapper, final Option... options) {
        final int size = mapper.core().size();

        // Quick pass for the probably most common events
        if (size <= 0) return;
        if (size == 1) {
            mapper.handle(0);
            return;
        }

        // TODO: Test-convert the first item and measure time. If time and size are above
        // a certain threshold, parallelize, otherwise map sequentially

        // Now find the first element and measure how long the conversion takes
        // for (int i = 0; i < size; i++) { }

        // TODO: Also measure how long thread creation takes (once)
        // TODO: Then decide if we should parallelize or execute directly.
        // TODO: Get proper value for step size (same problem, see below)
        final int STEP_SIZE = Math.max(size() / 10, 1);
        final int NUM_THREADS = Runtime.getRuntime().availableProcessors();

        // TODO: Check size, if small, don't do all this setup in here ...
        // NAH, even for two objects we can have a speed gain if the calls
        // are very slow ...

        final AtomicInteger baseCount = new AtomicInteger();
        final CyclicBarrier barrier = new CyclicBarrier(NUM_THREADS + 1);

        final Runnable runner = new Runnable() {
            public void run() {
                int lower = baseCount.getAndIncrement() * STEP_SIZE;

                // Get new basecount for every pass ...
                while (lower < size) {
                    final int max = Math.min(lower + STEP_SIZE, size);

                    // Pass over all elements
                    for (int i = lower; i < max; i++) {
                        mapper.handle(i);
                    }

                    lower = baseCount.getAndIncrement() * STEP_SIZE;
                }

                // Signal finish
                try {
                    barrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }
        };

        this.commonCore.execute(runner, NUM_THREADS);

        // Wait for all threads to finish ...
        try {
            barrier.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts a parallel folding process.
     * 
     * @param folder The folder to use.
     * @param options Relevant options: <code>OptionMapType</code>.
     */
    @SuppressWarnings("rawtypes")
    protected void fold(final Folder folder, final Option... options) {
        final int size = folder.core().size();

        // Quick pass for the probably most common events
        if (size <= 1) return;
        if (size == 2) {
            folder.handle(0, 1, 0);
            return;
        }

        final int NUM_THREADS = Runtime.getRuntime().availableProcessors();

        // Indicates which level (in the folding hierarchy) we are and where the next
        // thread should proceed. The base count indicates where which element should be 
        // selected next by the thread, the level indicates how many times we already passed
        // through the whole array.
        final AtomicInteger baseCount = new AtomicInteger();
        final AtomicInteger level = new AtomicInteger();

        // Synchronizes threads. Each thread waits at the level-barrier when it finished the last level,
        // and waits the the global barrier when it is completely done. The main thread will also
        // wait at the global barrier (thus +1) for all spawned threads. 
        final CyclicBarrier levelbarrier = new CyclicBarrier(NUM_THREADS);
        final CyclicBarrier barrier = new CyclicBarrier(NUM_THREADS + 1);

        final Runnable runner = new Runnable() {
            public void run() {
                int lvl = level.get();
                int dist = (int) Math.pow(2, lvl);

                // Process as long as the jump size exceeds the length of our array
                // (each level the jump size will be increased)
                while (dist < size) {
                    final int upperBound = size - dist;

                    int i = baseCount.getAndAdd(2) * dist;
                    int j = i + dist;

                    // For each level; proceed until we reach the righter bound
                    while (j <= upperBound) {
                        folder.handle(i, j, i);

                        i = baseCount.getAndAdd(2) * dist;
                        j = i + dist;
                    }

                    // Check if we were the node processing the last element and if there
                    // was a single element left over. In that case, process both these elements again
                    if (i <= upperBound && i > 0) {
                        int left = i - (int) Math.pow(2, lvl + 1);
                        folder.handle(left, i, left);
                    }

                    // Signal finish
                    try {
                        levelbarrier.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (BrokenBarrierException e) {
                        e.printStackTrace();
                    }

                    // Increase the level afterwards. If we were the one who changed the
                    // level, we also change the baseCount back to 0
                    if (level.compareAndSet(lvl, lvl + 1)) {
                        baseCount.set(0);
                    }

                    // And get new parameters
                    lvl = level.get();
                    dist = (int) Math.pow(2, lvl);
                }

                try {
                    barrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }
        };

        this.commonCore.execute(runner, NUM_THREADS);

        // Wait for all threads to finish ...
        try {
            barrier.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
    }
}
