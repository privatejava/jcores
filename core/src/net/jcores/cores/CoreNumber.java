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

import net.jcores.CommonCore;

/**
 * Wraps a number of Numbers and exposes some convenience functions. For example, 
 * to calulate the variance of a number of numbers, write:<br/><br/>
 * 
 * <code>$(5, 0, 8, 6, 6, 7).variance()</code>
 * 
 * @author Ralf Biedert
 * @since 1.0
 */
public class CoreNumber extends CoreObject<Number> {

    /** Used for serialization */
    private static final long serialVersionUID = -8437925527295825364L;

    /**
     * Wraps a number of numbers.
     * 
     * @param supercore The shared CommonCore.
     * @param objects The numbers to wrap.
     */
    public CoreNumber(CommonCore supercore, Number... objects) {
        super(supercore, objects);
    }

    /**
     * Returns the average of all enclosed numbers.<br/>
     * <br/>
     * 
     * Single-threaded.<br/>
     * <br/>
     * 
     * @return The average of all enclosed numbers. If no numbers are enclosed,
     * <code>0</code> is returned.
     */
    public double average() {
        final int size = size();
        int cnt = 0;
        double sum = 0.0;

        // Compute the average of all values
        for (int i = 0; i < size; i++) {
            final Number number = get(i);
            if (number == null) continue;

            sum += number.doubleValue();
            cnt++;
        }

        // If we haven't had any element, return 0
        if (cnt == 0) return 0;

        return sum / cnt;
    }

    /**
     * Returns the variance of all enclosed numbers, assuming a uniform distribution.<br/>
     * <br/>
     * 
     * Single-threaded.<br/>>
     * <br/>
     * 
     * @return The variance of all enclosed numbers. If no numbers are enclosed,
     * <code>0</code> is returned.
     */
    public double variance() {
        final double average = average();
        final int size = size();

        int cnt = 0;
        double rval = 0;

        // Compute the variance
        for (int i = 0; i < size; i++) {
            final Number number = get(i);
            if (number == null) continue;

            rval += (average - number.doubleValue()) * (average - number.doubleValue());
            cnt++;
        }

        // If we haven't had any element, return 0
        if (cnt == 0) return 0;

        return rval / cnt;
    }

    /**
     * Returns the standard deviation of all enclosed numbers.<br/>
     * <br/>
     * 
     * Single-threaded.<br/>>
     * <br/>
     * 
     * @return The standard deviation of all enclosed numbers. If no numbers are
     * enclosed, <code>0</code> is returned.
     */
    public double standarddeviation() {
        return Math.sqrt(variance());
    }
    

    /**
     * Returns the sum of all enclosed numbers.<br/>
     * <br/>
     * 
     * Single-threaded.<br/>>
     * <br/>
     * 
     * @return The sum of all enclosed numbers. If no numbers are
     * enclosed, <code>0</code> is returned.
     */
    public double sum() {
        final int size = size();
        double sum = 0.0;

        // Compute the average of all values
        for (int i = 0; i < size; i++) {
            final Number number = get(i);
            if (number == null) continue;

            sum += number.doubleValue();
        }

        return sum;
    }
}
