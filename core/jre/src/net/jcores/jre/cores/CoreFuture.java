/*
 * CoreFuture
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
package net.jcores.jre.cores;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.jcores.jre.CommonCore;
import net.jcores.jre.interfaces.functions.F1;
import net.jcores.jre.options.MessageType;

/**
 * Wraps a number of {@link Future} objects and exposes some convenience functions. For example,
 * to execute a callback when a future has some result, write:<br/>
 * <br/>
 * 
 * <code>$(future).finishedOne(f)</code><br/>
 * <br/>
 * 
 * @author Ralf Biedert
 * @param <T> The type of future to wait for.
 * @since 1.0
 */
public class CoreFuture<T> extends CoreObject<Future<T>> {

    /** Used for serialization */
    private static final long serialVersionUID = -7643964446329787050L;

    /**
     * Creates an AudioInputStream core.
     * 
     * @param supercore The common core.
     * @param objects The strings to wrap.
     */
    public CoreFuture(CommonCore supercore, Future<T>... objects) {
        super(supercore, objects);
    }

    

    /**
     * Registers a listener that is being called when one of the futures finished.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$(future).finishedOne(f)</code> - Calls <code>f</code> when <code>future</code> finished.</li>
     * </ul>
     * 
     * Multi-threaded.<br/>
     * <br/>
     * 
     * @param listener The listener that is being called when one finished. 
     * @return This core again.
     */
    public CoreFuture<T> oneFinished(final F1<T, Void> listener) {
        // For each future ...
        for (final Future<T> future : this) {
            // ... execute a thread which waits for the future to finish
            this.commonCore.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        // When we get some result, call the listener
                        T t = future.get();
                        listener.f(t);
                    } catch (InterruptedException e) {
                        CoreFuture.this.commonCore.report(MessageType.EXCEPTION, "InterruptedException when waiting for future");
                        return;
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }, 1);
        }

        return this;
    }

    
    /**
     * Waits until all futures finish and returns a {@link CoreObject} with the results.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$(future).await().get(0)</code> - Retrieves the result of <code>future</code> once it finished.</li>
     * </ul>
     * 
     * Single-threaded.<br/>
     * <br/>
     * 
     * @return This core with all results.
     */
    public CoreObject<T> await() {
        return await(Long.MAX_VALUE, TimeUnit.DAYS);
    }

    
    
    /**
     * Waits for some time until all futures finish and returns a {@link CoreObject} with 
     * the results of the future objects that returned in time.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$(future).await(3, TimeUnit.SECONDS).get(0)</code> - Retrieves the result 
     * of <code>future</code> once it finished, or an empty CoreObject if no result was available
     * in three seconds.</li>
     * </ul>
     * 
     * Single-threaded.<br/>
     * <br/>
     * 
     * @param wait The amount of {@link TimeUnit} to wait.
     * @param unit The actual unit of time to wait.
     * 
     * @return This core with all results.
     */
    public CoreObject<T> await(long wait, TimeUnit unit) {
        throw new IllegalStateException("Not implemented yet");
    }

    
    
    /**
     * Tries to retrieve the result of a {@link Future} object, or null, if 
     * it was not available yet.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$(future).obtain(0)</code> - Retrieves the result 
     * of <code>future</code> or return <code>null</code> it is hasn't 
     * finished yet.</li>
     * </ul>
     * 
     * Single-threaded.<br/>
     * <br/>
     * 
     * @param i The index to retrieve. 
     * @return The result or <code>null</code> if the future has not finished yet.
     */
    public T obtain(int i) {
        return obtain(i, 1, TimeUnit.NANOSECONDS);
    }

    
    
    /**
     * Tries to retrieve the result of a {@link Future} object, or null, if 
     * it is not available after a certain time.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$(future).obtain(0, 3, TimeUnit.MINUTES)</code> - Retrieves the result 
     * of <code>future</code> or return <code>null</code> it is hasn't 
     * finished after 3 minutes.</li>
     * </ul>
     * 
     * Single-threaded.<br/>
     * <br/>
     * 
     * @param i The index to retrieve. 
     * @param wait The amount of {@link TimeUnit} to wait.
     * @param unit The actual unit of time to wait.
     * @return The result or <code>null</code> if the future has not finished after 
     * the given time.
     */
    public T obtain(int i, long wait, TimeUnit unit) {
        // Get the proper future object
        final Future<T> future = get(i);
        if (future == null) return null;

        // Try to get the results ...
        try {
            return future.get(wait, unit);
        } catch (InterruptedException e) {} catch (ExecutionException e) {} catch (TimeoutException e) {}

        // ... or return null if nothing was there.
        return null;
    }

}
