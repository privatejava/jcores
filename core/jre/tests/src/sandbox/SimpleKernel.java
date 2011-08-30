/*
 * SimpleScript.java
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

import static net.jcores.jre.CoreKeeper.$;

import java.io.IOException;

import net.jcores.jre.interfaces.functions.F1;
import net.jcores.jre.interfaces.functions.F2ReduceObjects;

/**
 * @author rb
 * 
 */
public class SimpleKernel {

    /**
     * @param args
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws IOException {
        /*
        
        // Standalone mode
        final DefaultKernel kernel = new DefaultKernel();
        final Locator locator = new InternalLocator();

        final Processor processor = new AnnotationProcessor(kernel);
        final Collection<Service> locate = locator.locate();
        processor.process(locate);
        kernel.register(locate);

        
        
        // Usage mode
        Processor p = kernel.get(Processor.class);

        
        
        // jCores Mode (standalone facilities like locator and processors can and should
        // be created a 2nd time).
        $.kernel().get(AnnotationProcessor.class);
        $.kernel().get(AnnotationProcessor.class, Kernel.Get.ALL);
        $.kernel().get(AnnotationProcessor.class, Kernel.Get.TAGS("a", "b", "c"));

        final Locator myLocator = new JARLocator();


         */
        
        Object objects = new Object();
        F1 f = null;
        F2ReduceObjects g = null;
        
        
        
        $(objects).map(f).reduce(g).get(0);
        
    }
}
