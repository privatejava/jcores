/*
 * CommonCore.java
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
package net.jcores.jre;

import net.jcores.jre.cores.commons.JRECommonUI;
import net.jcores.shared.CommonCore;

public class JRECommonCore extends CommonCore {

    /** Common ui utilities */
    public final JRECommonUI ui = new JRECommonUI(this); 

    /** */
    JRECommonCore() {
        super();
    }

    
    /**
     * Requests a number of CPUs. The system will check how many CPUs are available 
     * and allocate up to <code>request</code> units. The number of allocated CPUs is 
     * returned.<br/><br/>
     * 
     * This function is only used internally. Also note that it is essential to call 
     * <code>releaseCPUs</code> after the application stopped using them.
     *  
     * @param request The number of CPUs to request. 
     * 
     * @return The actual number of CPUs available.
     */
    public int requestCPUs(int request) {
        // When looking at our benchmarks, it seems this does not speed up things, see Issue #12.
        return Math.min(profileInformation().numCPUs, request);
        
        /*
        synchronized (this.freeCPUs) {
            final int free = this.freeCPUs.get();
            
            // No free CPUs means to party
            if(free == 0) return 0;
            
            // More requested than free, return what we have
            if(request > free) {
                this.freeCPUs.set(0);
                return free;
            } 

            // In other cases, subtract what we have
            this.freeCPUs.set(free - request);
            return request;
        }
        */
    }
    
    
    /**
     * Releases a number of CPUSs previously allocated.<br/><br/>
     * 
     * This function is only used internally. Also note that it is essential to call 
     * <code>releaseCPUs</code> after the application stopped using them.
     *  
     * @param toRelease The number of CPUs to release. 
     */
    public void releaseCPUs(int toRelease) {
        // When looking at our benchmarks, it seems this does not speed up things, see Issue #12.
        /*
        synchronized (this.freeCPUs) {
            final int free = this.freeCPUs.get();
            this.freeCPUs.set(Math.min(this.profileInformation.numCPUs, free + toRelease));
        }
        */
    }

}
