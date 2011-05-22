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
import net.jcores.interfaces.functions.F2ReduceObjects;

/**
 * Wraps a number of Numbers and exposes some convenience functions. For example,
 * to calulate the variance of a number of numbers, write:<br/>
 * <br/>
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
     * Examples:
     * <ul>
     * <li><code>$(1, 3).average()</code> - Returns the average of 1 and 3, which is 2.</li>
     * </ul> 
     * 
     * Single-threaded.<br/>
     * <br/>
     * 
     * @return The average of all enclosed numbers. If no numbers are enclosed, <code>0</code> is returned.
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
     * Returns the number at the given position as a double, or
     * returns <code>Double.NaN</code> if the object was null.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$(100, 200).d(0)</code> - Returns the first value (100) in this core as a double value (100.0).</li>
     * </ul> 
     * 
     * Single-threaded.<br/>
     * <br/>
     * 
     * @param index The index to get the number for.
     * @return The double value of the number or <code>NaN</code> if it was null.
     */
    public double d(int index) {
        if (get(index) == null) return Double.NaN;
        return this.t[index].doubleValue();
    }

    /**
     * Returns the number at the given position as an integer, or
     * returns <code>Integer.MIN_VALUE</code> if the object was null.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$(100.26, 200.33).i(1)</code> - Returns the second value (200.33) in this core as an int value (200).</li>
     * </ul> 
     * 
     * Single-threaded.<br/>
     * <br/>
     * 
     * @param index The index to get the number for.
     * @return The integer value of the number or <code>Integer.MIN_VALUE</code> if it was null.
     */
    public int i(int index) {
        if (get(index) == null) return Integer.MIN_VALUE;
        return this.t[index].intValue();
    }

    /**
     * Returns the maximum value.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$(3, 1, 2).max()</code> - Returns 3.0.</li>
     * </ul> 
     * 
     * Single-threaded.<br/>
     * <br/>
     * 
     * @return The maximum value enclosed in this core.
     */
    public double max() {
        return reduce(new F2ReduceObjects<Number>() {
            @SuppressWarnings("boxing")
            @Override
            public Number f(Number left, Number right) {
                return Math.max(left.doubleValue(), right.doubleValue());
            }
        }).get(Double.NaN).doubleValue();
    }

    /**
     * Returns the maximum value.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$(3, 1, -2).max()</code> - Returns -2.0.</li>
     * </ul> 
     * 
     * Single-threaded.<br/>
     * <br/>
     * 
     * @return The maximum value enclosed in this core.
     */
    public double min() {
        return reduce(new F2ReduceObjects<Number>() {
            @SuppressWarnings("boxing")
            @Override
            public Number f(Number left, Number right) {
                return Math.min(left.doubleValue(), right.doubleValue());
            }
        }).get(Double.NaN).doubleValue();
    }

    /**
     * Returns the standard deviation of all enclosed numbers.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$(2, -2, 2, -2).standarddeviation()</code> - Returns 2.0.</li>
     * </ul> 
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
     * Examples:
     * <ul>
     * <li><code>$(1, 2, 3).sum()</code> - Returns 6.0.</li>
     * </ul>  
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

    /**
     * Returns the variance of all enclosed numbers, assuming a uniform distribution.<br/>
     * <br/>
     * 
     * Examples:
     * <ul>
     * <li><code>$(2, -2, 2, -2).variance()</code> - Returns 4.0.</li>
     * </ul> 
     * 
     * Single-threaded.<br/>>
     * <br/>
     * 
     * @return The variance of all enclosed numbers. If no numbers are enclosed, <code>0</code> is returned.
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
}
