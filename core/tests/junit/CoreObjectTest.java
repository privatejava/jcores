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

import static net.jcores.CoreKeeper.$;
import junit.data.Data;
import net.jcores.cores.CoreString;
import net.jcores.interfaces.functions.F1;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Ralf Biedert
 */
public class CoreObjectTest {

    /** */
    @Test
    public void testIntersect() {
        Assert.assertEquals("world", $("hello", "world").intersect($("world", "goodbye")).get(0));
        Assert.assertEquals(1, $("world", "hello").intersect($("world", "goodbye")).compact().size());
    }

    /** */
    @Test
    public void testForEach() {
        Assert.assertEquals("hw", $("hello", "world").forEach(new F1<String, String>() {
            @Override
            public String f(String x) {
                return x.substring(0, 1);
            }

        }).string().join());
    }

    /** */
    @Test
    public void testSlice() {
        Assert.assertEquals("goodbyecruelworld", $("goodbye", "cruel", "world").slice(0, 3).string().join());
    }

    /** */
    @Test
    public void testSerialize() {
        $("hello", "world").serialize("test.jcores");
        final CoreString converted = $("test.jcores").file().deserialize(String.class).string();
        Assert.assertEquals("helloworld", converted.join());
        
        $(Data.strings(10000)).serialize("big.file");
    }
    

    /** */
    @Test
    public void testRandom() {
        Assert.assertEquals(4, $("a", "b", "c", "d", "e", "f", "g").random(4).size());
        Assert.assertEquals($("a", "b", "c", "d", "e", "f", "g"), $("a", "b", "c", "d", "e", "f", "g").random(1.0).sort());
        Assert.assertEquals(0, $("a", "b", "c", "d", "e", "f", "g").random(0.0).size());
    }
}
