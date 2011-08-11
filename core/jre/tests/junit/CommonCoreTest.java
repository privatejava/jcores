/*
 * CoreStringTest.java
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
package junit;

import static net.jcores.shared.CoreKeeper.$;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import net.jcores.shared.interfaces.functions.F0;
import net.jcores.shared.utils.map.MapUtil;
import net.jcores.shared.utils.map.generators.NewUnsafeInstance;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Ralf Biedert
 */
public class CommonCoreTest {

    /** */
    @Test
    public void testTimer() {
        final AtomicInteger i = new AtomicInteger(333);

        $.sys.oneTime(new F0() {
            public void f() {
                i.set(666);
            }
        }, 250);

        $.sys.sleep(400);

        Assert.assertEquals(666, i.get());

        $.sys.oneTime(new F0() {
            public void f() {
                i.set(667);
            }
        }, 400);

        $.sys.sleep(200);

        Assert.assertEquals(666, i.get());
    }

    /** */
    @Test
    public void testPermute() {
        final String x[] = $("a", "b", "c", "d", "e").array(String.class);

        int i = 0;
        while ($.alg.permute(x))
            i++;

        Assert.assertEquals(120 - 1, i);
    }

    /** */
    @SuppressWarnings("boxing")
    @Test
    public void testMap() {
        final MapUtil<String, List<Integer>> m1 = $.map();
        m1.generator(new NewUnsafeInstance<String, List<Integer>>(ArrayList.class));

        m1.get("a").add(1);
        Assert.assertEquals(m1.get("a").get(0), Integer.valueOf(1));
    }

}
