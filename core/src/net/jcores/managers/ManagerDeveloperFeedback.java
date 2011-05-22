/*
 * ManagerDeveloperFeedback.java
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
package net.jcores.managers;

import static net.jcores.CoreKeeper.$;

import java.awt.Desktop;
import java.net.URI;

import net.jcores.cores.CoreObject;

/**
 * Manager for developer feedback and statistics.
 * 
 * @since 1.0
 * @author Ralf Biedert
 */
public class ManagerDeveloperFeedback extends Manager {
    /** The last time we had an outbound message */
    long lastoutbound = 0;
    
    /**
     * Handles a feature request from a {@link CoreObject}.
     * 
     * @param request The given request.
     * @param fingerprint The fingerprint of the core.
     */
    public void featurerequest(String request, String fingerprint) {
        if(!checkOutbound()) return;
        
        String r = $(request + "\n\n" + fingerprint).encode().get(0);
        
        try {
            Desktop.getDesktop().mail(URI.create("mailto:info@jcores.net?SUBJECT=Feature%20Request&BODY="+ r.replaceAll("\\+", "%20")));
        } catch (Exception e) {
            e.printStackTrace();
        }

        updateOutbound();
    }

    /**
     * Updates the timestamp of our last outgoing operation.
     */
    private void updateOutbound() {
        this.lastoutbound = System.currentTimeMillis();
    }

    
    /**
     * Checks if we are allowed to perform the next outgoing operation.
     * 
     * @return True if we are, false if not.
     */
    private boolean checkOutbound() {
        if(this.lastoutbound + 2000 > System.currentTimeMillis()) {
            System.err.println("You are not allowed to send outgoing messages at this rate. Try again in a few seconds.");
            return false;
        }
        
        return true;
    }
}
