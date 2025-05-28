package net.imglib2.meta;

import net.imglib2.RealRandomAccessible;
import net.imglib2.meta.calibration.Axes;
import net.imglib2.meta.calibration.AxisType;
import net.imglib2.meta.calibration.Calibration;
import net.imglib2.type.numeric.real.DoubleType;
import org.junit.Assert;
import org.junit.Test;

/** Tests {@link RealDataset}. */
public class RealDatasetTest {

    /**
     * Get a fully calibrated {@link RealDataset}
     *
     * @return a fully calibrated {@link DatasetInterval}
     */
    private RealDataset<DoubleType> dataset() {
        RealRandomAccessible<DoubleType> data = Data.realImage();

        MetadataStore store = new SimpleMetadataStore(data.numDimensions());
        Calibration calibration = store.info(Calibration.class);
        calibration.setAxis(axis(Axes.X), 0);
        calibration.setAxis(axis(Axes.Y), 1);
        calibration.setAxis(axis(Axes.Z), 2);
        calibration.setAxis(axis(Axes.CHANNEL), 3);
        calibration.setAxis(axis(Axes.TIME), 4);
        return RealDataset.wrap(data, store);
    }

    @Test
    public void testRaster() {
        Dataset<DoubleType, ?> rasterized = dataset().raster();

        Calibration calView = rasterized.store().info(Calibration.class);
        Assert.assertEquals(Axes.X, calView.axis(0).type());
        Assert.assertEquals(Axes.Y, calView.axis(1).type());
        Assert.assertEquals(Axes.Z, calView.axis(2).type());
        Assert.assertEquals(Axes.CHANNEL, calView.axis(3).type());
        Assert.assertEquals(Axes.TIME, calView.axis(4).type());
    }

    private Axis axis(AxisType axisType) {
        return new DefaultLinearAxis(axisType, 1, 0);
    }
}
