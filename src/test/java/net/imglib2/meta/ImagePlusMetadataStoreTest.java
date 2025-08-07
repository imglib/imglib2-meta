package net.imglib2.meta;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class ImagePlusMetadataStoreTest {

    @Test
    public void testName() {
        ImagePlus imp = IJ.openImage("https://imagej.net/ij/images/mitosis.tif");
        MetadataStore metadata = new ImagePlusMetadataStore(imp);

        General gen = Metadata.general(metadata);
        Assert.assertEquals("mitosis.tif", gen.name());
    }

    @Test
    public void test5dAxes() {
        ImagePlus imp = IJ.openImage("https://imagej.net/ij/images/mitosis.tif");
        MetadataStore metadata = new ImagePlusMetadataStore(imp);

        Calibration cal = Metadata.calibration(metadata);
        Assert.assertEquals(Axes.X, cal.axis(0).type());
        Assert.assertEquals(Axes.Y, cal.axis(1).type());
        Assert.assertEquals(Axes.CHANNEL, cal.axis(2).type());
        Assert.assertEquals(Axes.Z, cal.axis(3).type());
        Assert.assertEquals(Axes.TIME, cal.axis(4).type());
    }

    @Test
    public void test3dAxes() {
        // Test an image with Z-Slices
        ImagePlus imp = IJ.openImage("https://imagej.net/ij/images/xrays.zip");
        MetadataStore metadata = new ImagePlusMetadataStore(imp);

        Calibration cal = Metadata.calibration(metadata);
        Assert.assertEquals(Axes.X, cal.axis(0).type());
        Assert.assertEquals(Axes.Y, cal.axis(1).type());
        Assert.assertEquals(Axes.Z, cal.axis(2).type());

        imp = IJ.openImage("https://imagej.net/ij/images/mitosis.tif");
        metadata = new ImagePlusMetadataStore(imp);

        cal = Metadata.calibration(metadata);
        Assert.assertEquals(Axes.X, cal.axis(0).type());
        Assert.assertEquals(Axes.Y, cal.axis(1).type());
        Assert.assertEquals(Axes.CHANNEL, cal.axis(2).type());
    }

    @Test
    public void testSingleLUT() {
        // Create random LUT
        LUT lut = randomLUT(0xdeadbeefL);

        // Assign it to an image
        ImagePlus imp = IJ.openImage("https://imagej.net/ij/images/xrays.zip");
        imp.setLut(lut);
        MetadataStore metadata = new ImagePlusMetadataStore(imp);

        // Assert metadata equals source
        Channels chan = Metadata.channels(metadata);
        ColorTable table = chan.lut(0);
        assertLUTEquals(lut, table);
    }

    @Test
    public void testRGBLUT() {
        // Create random LUT
        LUT lut = randomLUT(0xdeadbeefL);

        // Assign it to an image
        ImagePlus imp = IJ.openImage("https://imagej.net/ij/images/clown.png");
        MetadataStore metadata = new ImagePlusMetadataStore(imp);

        // RGB images should have no LUT, as imglib2-imagej converts them to ARGBType
        Channels chan = Metadata.channels(metadata);
        assertThrows(NoSuchElementException.class, () -> chan.lut(0));
    }

    @Test
    public void testMultiLUT() {
        // Assign it to an image
        ImagePlus imp = IJ.openImage("https://imagej.net/ij/images/3_channel_inverted_luts.tif");
        MetadataStore metadata = new ImagePlusMetadataStore(imp);

        Channels chan = Metadata.channels(metadata);
        for(int i = 0; i < imp.getNChannels(); i++) {
            assertLUTEquals(imp.getLuts()[i], chan.lut(i));
        }
        assertThrows(NoSuchElementException.class, () -> chan.lut(imp.getNChannels()));
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
