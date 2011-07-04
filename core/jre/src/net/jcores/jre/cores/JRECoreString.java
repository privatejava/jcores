/*
 * JRECoreString.java
 * 
 * Copyright (c) 2011, Ralf Biedert All rights reserved.
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
package net.jcores.jre.cores;

import net.jcores.shared.CommonCore;
import net.jcores.shared.cores.CoreString;
import net.jcores.shared.cores.adapter.AbstractAdapter;

public class JRECoreString extends CoreString {

    /** */
    private static final long serialVersionUID = 1050943834374663676L;

    /**
     * @param supercore
     * @param adapter
     */
    public JRECoreString(CommonCore supercore, AbstractAdapter<String> adapter) {
        super(supercore, adapter);
    }
    

    /**
     * Creates an string core.
     * 
     * @param supercore The common core.
     * @param objects The strings to wrap.
     */
    public JRECoreString(CommonCore supercore, String... objects) {
        super(supercore, objects);
    }

    
    /* (non-Javadoc)
     * @see net.jcores.shared.cores.CoreString#uri()
     */
    @Override
    public JRECoreURI uri() {
        return new JRECoreURI(this.commonCore, super.uri().unsafeadapter());
    }
}
