/*
 * CoreFile.java
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
package net.jcores.cores;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import net.jcores.CommonCore;
import net.jcores.interfaces.functions.F1;
import net.jcores.options.MessageType;

/**
 * Wraps a number of BufferedImages and exposes some convenience functions.
 * 
 * @author Ralf Biedert
 * 
 * @since 1.0
 */
public class CoreBufferedImage extends CoreObject<BufferedImage> {

    /** Used for serialization */
    private static final long serialVersionUID = -9069210371713045372L;

    /**
     * Creates an BufferedImage core.
     * 
     * @param supercore The common core.
     * @param objects The BufferedImage to wrap.
     */
    public CoreBufferedImage(CommonCore supercore, BufferedImage... objects) {
        super(supercore, objects);
    }

    /**
     * Copies the buffered images, creating deep clones of the given image data. Altering
     * a copy
     * will not alter the source image.<br/>
     * <br/>
     * 
     * Multi-threaded.<br/>
     * <br/>
     * 
     * @return A CoreBufferedImage containing the copies.
     */
    public CoreBufferedImage copy() {
        return new CoreBufferedImage(this.commonCore, map(new F1<BufferedImage, BufferedImage>() {
            public BufferedImage f(final BufferedImage bi) {
                // Code shamelessly stolen from stackoverflow.com
                ColorModel cm = bi.getColorModel();
                boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
                WritableRaster raster = bi.copyData(null);
                return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
            }
        }).array(BufferedImage.class));
    }

    /**
     * Scales all contained images by the given factor.<br/>
     * <br/>
     * 
     * Multi-threaded.<br/>
     * <br/>
     * 
     * @param factor The factor by which to scale the image.
     * 
     * @return A CoreBufferedImage containing the scaled images.
     */
    public CoreBufferedImage scale(final float factor) {
        return new CoreBufferedImage(this.commonCore, map(new F1<BufferedImage, BufferedImage>() {
            public BufferedImage f(final BufferedImage bi) {
                // Code shamelessly stolen from stackoverflow.com
                final AffineTransform af = new AffineTransform();
                af.scale(factor, factor);
                final AffineTransformOp operation = new AffineTransformOp(af, AffineTransformOp.TYPE_BILINEAR);
                return operation.filter(bi, null);
            }
        }).array(BufferedImage.class));
    }

    /**
     * Writes the enclosed image at position <code>0</code> to the given file. The file type will be
     * recognized by the suffix.<br/>
     * <br/>
     * Single-threaded, size-of-one.<br/>
     * <br/>
     * 
     * @param file The file to write the image get(0) to.
     * @return This CoreBufferedImage object.
     */
    public CoreBufferedImage write(String file) {
        if (file == null) return this;

        if (size() != 1) {
            this.commonCore.report(MessageType.MISUSE, "CoreBufferedImage.write() needs exactly one image to write, at the moment.");
        }

        String type = "png";
        type = file.toLowerCase().endsWith(".gif") ? "GIF" : type;
        type = file.toLowerCase().endsWith(".jpg") ? "JPG" : type;
        type = file.toLowerCase().endsWith(".jpeg") ? "JPG" : type;
        type = file.toLowerCase().endsWith(".bmp") ? "BMP" : type;
        type = file.toLowerCase().endsWith(".wbmp") ? "WBMP" : type;

        try {
            ImageIO.write(get(0), type, new File(file));
        } catch (IOException e) {
            this.commonCore.report(MessageType.EXCEPTION, "Error writing image to file " + file);
        }

        return this;
    }

}
