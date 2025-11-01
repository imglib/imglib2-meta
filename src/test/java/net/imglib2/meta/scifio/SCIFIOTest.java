package net.imglib2.meta.scifio;

import io.scif.DefaultImageMetadata;
import io.scif.DefaultMetadata;
import io.scif.img.SCIFIOImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.DefaultLinearAxis;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.meta.Metadata;
import net.imglib2.meta.MetadataStore;
import net.imglib2.meta.calibration.Axis;
import net.imglib2.meta.calibration.Calibration;
import net.imglib2.meta.general.General;
import net.imglib2.type.numeric.real.DoubleType;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests the ability to wrap a {@link SCIFIOImgPlus} into a {@link MetadataStore}.
 *
 * @author Gabriel Selzer
 */
public class SCIFIOTest {

    static SCIFIOImgPlus<DoubleType> img;
    static MetadataStore store;

    @BeforeClass
    public static void setUp() {
        img = new SCIFIOImgPlus<>(ArrayImgs.doubles(5, 5));
        img.setMetadata(new DefaultMetadata());
        img.setImageMetadata(new DefaultImageMetadata());
        img.getImageMetadata().setAxes( //
            new DefaultLinearAxis(Axes.X), //
            new DefaultLinearAxis(Axes.Y) //
        );
        img.getImageMetadata().setName("Test Image");
        store = new SCIFIOMetadataStore(img);
    }

    @Test
    public void testName() {
        General g = Metadata.general(store);
        assertEquals(img.getName(), g.name());
    }

    @Test
    public void testAxes() {
        Calibration c = Metadata.calibration(store);
        for(int i = 0; i < img.numDimensions(); i++) {
            net.imagej.axis.CalibratedAxis ijAxis = img.axis(i);
            Axis metaAxis = c.axis(i);
            assertEquals( //
                ijAxis.type().toString(), //
                metaAxis.type().toString() //
            );
            assertEquals( //
                ijAxis.calibratedValue(1.0), //
                metaAxis.calibrated(1.0), //
                1e-6 //
            );
            assertEquals(ijAxis.unit(), metaAxis.unit());
        }
    }
}
