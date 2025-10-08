package net.imglib2.meta;

import io.scif.img.SCIFIOImgPlus;
import io.scif.services.DatasetIOService;
import net.imagej.Dataset;
import net.imglib2.meta.calibration.Axis;
import net.imglib2.meta.calibration.Calibration;
import net.imglib2.meta.general.General;
import org.junit.BeforeClass;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.io.location.FileLocation;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Tests the ability to wrap a {@link SCIFIOImgPlus} into a {@link MetadataStore}.
 * <p>
 * Note that this test currently requires you download <a href="https://samples.scif.io/mitosis-ics.zip">this image</a>
 * to the repository root.
 * </p>
 *
 * @author Gabriel Selzer
 */
public class SCIFIOTest {

    static SCIFIOImgPlus<?> img;
    static MetadataStore store;

    @BeforeClass
    public static void setUp() throws IOException {
        // Note that this test currently requires you download https://samples.scif.io/mitosis-ics.zip
        // to the repository root.
        Context ctx = new Context();
        DatasetIOService dio = ctx.getService(DatasetIOService.class);
        Dataset data = dio.open(new FileLocation("mitosis.ics"));
        img = (SCIFIOImgPlus<?>) data.getImgPlus();
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
