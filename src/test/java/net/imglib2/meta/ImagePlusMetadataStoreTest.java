/*-
 * #%L
 * Metadata for ImgLib2
 * %%
 * Copyright (C) 2016 - 2025 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
 * John Bogovic, Albert Cardona, Barry DeZonia, Christian Dietz, Jan Funke,
 * Aivar Grislis, Jonathan Hale, Grant Harris, Stefan Helfrich, Mark Hiner,
 * Martin Horn, Steffen Jaensch, Lee Kamentsky, Larry Lindsey, Melissa Linkert,
 * Mark Longair, Brian Northan, Nick Perry, Curtis Rueden, Johannes Schindelin,
 * Gabriel Selzer, Jean-Yves Tinevez and Michael Zinsmaier.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package net.imglib2.meta;

import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.process.LUT;
import net.imglib2.display.ColorTable;
import net.imglib2.imagej.ImagePlusToImg;
import net.imglib2.img.Img;
import net.imglib2.meta.calibration.Axes;
import net.imglib2.meta.calibration.Calibration;
import net.imglib2.meta.channels.Channels;
import net.imglib2.meta.general.General;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import org.junit.Assert;
import org.junit.Test;

import java.awt.*;
import java.util.NoSuchElementException;
import java.util.Random;

import static org.junit.Assert.*;

public class ImagePlusMetadataStoreTest {

    @Test
    public void testName() {
        String title = "Fooooooo";
        ImagePlus imp = IJ.createImage(title, 10, 10, 1, 8);
        MetadataStore metadata = new ImagePlusMetadataStore(imp);

        General gen = Metadata.general(metadata);
        Assert.assertEquals(title, gen.name());
    }

    @Test
    public void test5dAxes() {
        ImagePlus imp = IJ.createImage("image", 10, 10, 8, 8);
        imp.setDimensions(2, 2,2);
        MetadataStore metadata = new ImagePlusMetadataStore(imp);

        Calibration cal = Metadata.calibration(metadata);
        Assert.assertEquals(Axes.X, cal.axis(0).type());
        Assert.assertEquals(Axes.Y, cal.axis(1).type());
        Assert.assertEquals(Axes.CHANNEL, cal.axis(2).type());
        Assert.assertEquals(Axes.Z, cal.axis(3).type());
        Assert.assertEquals(Axes.TIME, cal.axis(4).type());
    }

    @Test
    public void testAxes() {
        ImagePlus imp = IJ.createImage("image", 10, 10, 8, 8);
        MetadataStore metadata = new ImagePlusMetadataStore(imp);
        Calibration cal = Metadata.calibration(metadata);

        imp.setDimensions(1, 8, 1);
        Assert.assertEquals(Axes.X, cal.axis(0).type());
        Assert.assertEquals(Axes.Y, cal.axis(1).type());
        Assert.assertEquals(Axes.Z, cal.axis(2).type());

        imp.setDimensions(8, 1, 1);
        Assert.assertEquals(Axes.X, cal.axis(0).type());
        Assert.assertEquals(Axes.Y, cal.axis(1).type());
        Assert.assertEquals(Axes.CHANNEL, cal.axis(2).type());

        imp.setDimensions(1, 1, 8);
        Assert.assertEquals(Axes.X, cal.axis(0).type());
        Assert.assertEquals(Axes.Y, cal.axis(1).type());
        Assert.assertEquals(Axes.TIME, cal.axis(2).type());

        imp.setDimensions(2, 4, 1);
        Assert.assertEquals(Axes.X, cal.axis(0).type());
        Assert.assertEquals(Axes.Y, cal.axis(1).type());
        Assert.assertEquals(Axes.CHANNEL, cal.axis(2).type());
        Assert.assertEquals(Axes.Z, cal.axis(3).type());

        imp.setDimensions(2, 1, 4);
        Assert.assertEquals(Axes.X, cal.axis(0).type());
        Assert.assertEquals(Axes.Y, cal.axis(1).type());
        Assert.assertEquals(Axes.CHANNEL, cal.axis(2).type());
        Assert.assertEquals(Axes.TIME, cal.axis(3).type());

        imp.setDimensions(1, 2, 4);
        Assert.assertEquals(Axes.X, cal.axis(0).type());
        Assert.assertEquals(Axes.Y, cal.axis(1).type());
        Assert.assertEquals(Axes.Z, cal.axis(2).type());
        Assert.assertEquals(Axes.TIME, cal.axis(3).type());

        imp.setDimensions(2, 2, 2);
        Assert.assertEquals(Axes.X, cal.axis(0).type());
        Assert.assertEquals(Axes.Y, cal.axis(1).type());
        Assert.assertEquals(Axes.CHANNEL, cal.axis(2).type());
        Assert.assertEquals(Axes.Z, cal.axis(3).type());
        Assert.assertEquals(Axes.TIME, cal.axis(4).type());
    }

    @Test
    public void testSingleLUT() {
        // Create random LUT
        LUT lut = randomLUT(0xdeadbeefL);

        // Assign it to an image
        ImagePlus imp = IJ.createImage("1 Channel", 10, 10, 10, 8);
        imp.setDimensions(1, 8, 1);
        imp.setLut(lut);
        MetadataStore metadata = new ImagePlusMetadataStore(imp);

        // Assert metadata equals source
        Channels chan = Metadata.channels(metadata);
        ColorTable table = chan.lut(0);
        assertLUTEquals(lut, table);
    }

    @Test
    public void testRGBLUT() {
        ImagePlus imp = IJ.createImage("RGB", 10, 10, 1, 24);
        assertTrue(imp.isRGB());
        MetadataStore metadata = new ImagePlusMetadataStore(imp);

        // RGB images should have no LUT, as imglib2-imagej converts them to ARGBType
        Channels chan = Metadata.channels(metadata);
        assertThrows(NoSuchElementException.class, () -> chan.lut(0));
    }

    @Test
    public void testMultiLUT() {
        // Create a composite (multi-channel) image
        ImagePlus imp = IJ.createImage("Multiple LUTs", 10, 10, 3, 8);
        imp.setDimensions(3, 1, 1);
        CompositeImage comp = new CompositeImage(imp);
        // And a imglib2-meta wrapper around it
        MetadataStore metadata = new ImagePlusMetadataStore(comp);

        Channels chan = Metadata.channels(metadata);
        for(int i = 0; i < comp.getNChannels(); i++) {
            assertLUTEquals(comp.getLuts()[i], chan.lut(i));
        }
        assertThrows(IndexOutOfBoundsException.class, () -> chan.lut(imp.getNChannels()));
    }

    private LUT randomLUT(final long seed) {
        Random r = new Random(seed);
        byte[] reds = new byte[256];
        r.nextBytes(reds);
        byte[] greens = new byte[256];
        r.nextBytes(greens);
        byte[] blues = new byte[256];
        r.nextBytes(blues);
        return new LUT(reds, greens, blues);
    }

    private void assertLUTEquals(final LUT lut, final ColorTable table) {
        byte[] reds = new byte[256];
        lut.getReds(reds);
        byte[] greens = new byte[256];
        lut.getGreens(greens);
        byte[] blues = new byte[256];
        lut.getBlues(blues);
        for(int i = 0; i < 256; i++) {
            // Note ColorTable.get unsigned bytes as ints
            assertEquals(reds[i], (byte) table.get(ColorTable.RED, i));
            assertEquals(greens[i], (byte) table.get(ColorTable.GREEN, i));
            assertEquals(blues[i], (byte) table.get(ColorTable.BLUE, i));
            assertEquals((byte) 255, (byte) table.get(ColorTable.ALPHA, i));
        }
    }
}
