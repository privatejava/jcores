/*
 * X1.java
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
package benchmarks;

import java.util.ArrayList;
import java.util.Collection;

import net.jcores.interfaces.functions.F0;
import net.jcores.interfaces.functions.F1;
import benchmarks.benchmarker.Benchmark;
import benchmarks.benchmarker.BenchmarkResults;
import benchmarks.benchmarker.Benchmarker;
import benchmarks.benchmarks.SimpleTest;
import benchmarks.model.TaskData;
import benchmarks.model.TaskSolver;

/**
 * @author rb
 *
 */
public class BenchmarkMain {
    /**
     * @param args
     */
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        final Collection<Class<? extends Benchmark<?>>> classes = new ArrayList<Class<? extends Benchmark<?>>>();
        classes.add(SimpleTest.class);

        for (Class<?> class1 : classes) {
            try {
                final Benchmark<?> benchmark = (Benchmark<?>) class1.newInstance();
                final TaskData<?> data = benchmark.data();
                final Collection<?> solvers = benchmark.solver();

                System.out.println(benchmark.name());

                for (Object object : solvers) {
                    final TaskSolver<Object> solver = (TaskSolver<Object>) object;
                    final Object d = data != null ? data.getData() : null;
                    final F1<Object, Object> f = solver.f();

                    System.out.print("    " + solver.name() + ": ");

                    final BenchmarkResults results = Benchmarker.benchmark(new F0() {
                        @Override
                        public void f() {
                            f.f(d);
                        }
                    }, 5);

                    System.out.print(results.average(4) + "µs (");
                    long[] values = results.values();
                    for (int i = 0; i < values.length; i++) {
                        System.out.print(values[i] + "µs");
                        if(i < values.length - 1) System.out.print(" ");
                    }
                    System.out.println(")");
                }
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
