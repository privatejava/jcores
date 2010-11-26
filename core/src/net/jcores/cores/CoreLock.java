/*
 * CoreFile.java
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

import java.util.concurrent.locks.Lock;

import net.jcores.CommonCore;
import net.jcores.interfaces.functions.F0;

/**
 * Wraps a number of Locks and exposes some convenience functions.
 * 
 * @author Ralf Biedert
 * @since 1.0
 */
public class CoreLock extends CoreObject<Lock> {

    /** Used for serialization */
    private static final long serialVersionUID = -4449041324296817648L;

    /**
     * Wraps a number of locks.
     * 
     * @param supercore The shared CommonCore.
     * @param objects The locks to wrap.
     */
    public CoreLock(CommonCore supercore, Lock... objects) {
        super(supercore, objects);
    }

    /**
     * Executes the given function as soon as the lock is ready and fail-safely unlocks
     * the lock afterwards<br/>
     * <br/>
     * 
     * Single-threaded, size-of-one.<br/>
     * <br/>
     * 
     * @param f The function to execute when the lock is ready.
     */
    public void locked(F0 f) {
        Lock lock = get(0);

        try {
            f.f();
        } finally {
            lock.unlock();
        }
    }
}
