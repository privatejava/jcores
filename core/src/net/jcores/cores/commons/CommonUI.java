/*
 * CommonFile.java
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
package net.jcores.cores.commons;

import static net.jcores.CoreKeeper.$;

import java.awt.Color;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import net.jcores.CommonCore;
import net.jcores.interfaces.functions.F0;

/**
 * Contains common ui utilities.
 * 
 * @author Ralf Biedert
 * @since 1.0
 * 
 */
public class CommonUI extends CommonNamespace {
    /** The colors we need for heat maps */
    private Color heatmapColors[] = new Color[256];
    
    /**
     * Creates a common ui object.
     * 
     * @param commonCore
     */
    public CommonUI(CommonCore commonCore) {
        super(commonCore);
        
        for (int i = 0; i < this.heatmapColors.length; i++) {
            this.heatmapColors[i] = Color.getHSBColor((float) i / (float) this.heatmapColors.length, 0.85f, 1.0f);
        }
    }

    /**
     * Executes the given function in the Event Dispatch Thread (EDT) at some
     * point in the future.
     * 
     * @since 1.0
     * @param f0 The function to execute.
     */
    public void edt(final F0 f0) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                f0.f();
            }
        });
    }

    /**
     * Executes the given function in the Event Dispatch Thread (EDT) now, waiting until
     * the function was executed.
     * 
     * @since 1.0
     * @param f0 The function to execute.
     */
    public void edtnow(final F0 f0) {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    f0.f();
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a heat-color for the given relative value. A value of <code>0</code> means cold, a value
     * of <code>1</code> means hot. Values outside the scope are limited to these values.
     * 
     * @param rel A value between <code>0</code> and <code>1</code>.
     * @return A heatmap color.
     */
    public Color heat(double rel) {
        final double d = $.alg.limit(0, rel, 1);
        return this.heatmapColors[(int) (d * this.heatmapColors.length)];
    }
}
