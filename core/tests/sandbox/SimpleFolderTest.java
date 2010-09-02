/*
 * SimpleSpeedTests.java
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
package sandbox;

import static net.jcores.CoreKeeper.$;

import java.util.ArrayList;
import java.util.List;

import net.jcores.interfaces.functions.F2DeltaObjects;
import net.jcores.interfaces.functions.F2ReduceObjects;
import net.jcores.utils.Staple;

/**
 * @author rb
 */
public class SimpleFolderTest {
    /**
     * @param args
     */
    @SuppressWarnings("boxing")
    public static void main(String[] args) {
        List<Integer> ints = new ArrayList<Integer>();
        for (int i = 0; i < 10000; i++) {
            ints.add(i);
            //ints.add(1);
        }

        long t1 = System.nanoTime();
        Integer i1 = $(ints).fold(new F2ReduceObjects<Integer>() {
            @Override
            public Integer f(Integer stack, Integer next) {
                return stack + next;
            }
        }).get(0);
        long t2 = System.nanoTime();

        long t3 = System.nanoTime();
        Integer i2 = $(ints).reduce(new F2ReduceObjects<Integer>() {
            @Override
            public Integer f(Integer stack, Integer next) {
                return stack + next;
            }
        }).get(0);
        long t4 = System.nanoTime();

        System.out.println((t2 - t1) / 1000);
        System.out.println((t4 - t3) / 1000);
        System.out.println();
        System.out.println(i1);
        System.out.println(i2);

        final Staple<Integer> staple = $(ints).staple(0, new F2ReduceObjects<Integer>() {
            public Integer f(Integer left, Integer right) {
                return left + right;
            }
        });

        System.out.println(staple.staple() / staple.size());

        Integer integer = $(ints).delta(new F2DeltaObjects<Integer, Integer>() {
            public Integer f(Integer left, Integer right) {
                return right - left;
            }
        }).size();
        System.out.println(integer);

    }
}
