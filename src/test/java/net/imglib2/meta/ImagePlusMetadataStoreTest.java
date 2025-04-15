package net.imglib2.meta;

import ij.IJ;
import ij.ImagePlus;
import net.imglib2.imagej.ImagePlusToImg;
import net.imglib2.img.Img;
import net.imglib2.meta.calibration.Axes;
import net.imglib2.meta.calibration.Calibration;
import net.imglib2.meta.general.General;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import org.junit.Assert;
import org.junit.Test;

public class ImagePlusMetadataStoreTest {

    @Test
    public void testName() {
        ImagePlus imp = IJ.openImage("https://imagej.net/ij/images/mitosis.tif");
        Img<UnsignedShortType> img = ImagePlusToImg.wrapShortDirect(imp);
        MetadataStore metadata = new ImagePlusMetadataStore(imp);

        General gen = Metadata.general(metadata);
        Assert.assertEquals("mitosis.tif", gen.name());
    }

    @Test
    public void test5dAxes() {
        ImagePlus imp = IJ.openImage("https://imagej.net/ij/images/mitosis.tif");
        Img<UnsignedShortType> img = ImagePlusToImg.wrapShortDirect(imp);
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
        ImagePlus imp = IJ.openImage("https://imagej.net/ij/images/xrays.zip");
        Img<UnsignedByteType> img = ImagePlusToImg.wrapByteDirect(imp);
        MetadataStore metadata = new ImagePlusMetadataStore(imp);

        Calibration cal = Metadata.calibration(metadata);
        Assert.assertEquals(Axes.X, cal.axis(0).type());
        Assert.assertEquals(Axes.Y, cal.axis(1).type());
        Assert.assertEquals(Axes.Z, cal.axis(2).type());

    }
}
