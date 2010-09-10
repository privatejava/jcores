/*
 * SimpleTest.java
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
package benchmarks.benchmarks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import net.jcores.interfaces.functions.F1;
import benchmarks.benchmarker.Benchmark;
import benchmarks.model.TaskData;
import benchmarks.model.TaskSolver;

/**
 * @author rb
 *
 */
public class SimpleTest extends Benchmark<Void> {

    @Override
    public TaskData<Void> data() {
        return null;
    }

    @Override
    public Collection<TaskSolver<Void>> solver() {
        final Collection<TaskSolver<Void>> rval = new ArrayList<TaskSolver<Void>>();

        rval.add(new TaskSolver<Void>("m1", new F1<Void, Void>() {
            @Override
            public Void f(Void x) {
                return null;
            }
        }));
        rval.add(new TaskSolver<Void>("m2", new F1<Void, Void>() {
            @Override
            public Void f(Void x) {
                new Random().nextInt(10);
                return null;
            }
        }));

        return rval;
    }

    @Override
    public String name() {
        return "simple";
    }
}
