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

import java.util.Map;

import junit.data.Data;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Ralf Biedert
 */
public class CoreStringTest {

    /** */
    @Test
    public void testFile() {
        Assert.assertEquals("ranges.zip", $(Data.DATA_PATH + "/ranges.zip").file().get(0).getName());
    }

    /** */
    @Test
    public void testFilter() {
        Assert.assertEquals(1, $(Data.sn).filter("667").size());
    }

    /** */
    @Test
    public void testJoin() {
        Assert.assertEquals("Hello$World", $("Hello", null, "World").join("$"));
    }

    /** */
    @Test
    public void testSplit() {
        Assert.assertEquals("rld", $("Hello World").split("o").get(2));
    }

    /** */
    @Test
    public void testHashMap() {
        Map<String, String> map = $("x:5", "y=7", "z:=13", "alpha=:31").hashmap();
        Assert.assertEquals("5", map.get("x"));
        Assert.assertEquals("7", map.get("y"));
        Assert.assertEquals("13", map.get("z"));
        Assert.assertEquals(":31", map.get("alpha"));
    }
    
    /** */
    @Test
    public void testNCoding() {
        Assert.assertEquals("!§$%&/()?", $("!§$%&/()?").encode().decode().get(0));
    }

    /** */
    @Test
    public void testReplace() {
        Assert.assertEquals("Hello World", $("Hello$World").replace("\\$", " ").get(0));
        Assert.assertEquals("Hello World", $("Hellx", "Wxrld").replace("x", "o").join(" "));

        
    }
}
