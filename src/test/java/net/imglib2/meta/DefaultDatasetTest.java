package net.imglib2.meta;

import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.axis.CalibratedAxis;
import net.imagej.axis.DefaultLinearAxis;
import net.imglib2.RandomAccessible;
import net.imglib2.meta.calibration.Calibration;
import net.imglib2.type.numeric.real.DoubleType;
import org.junit.Assert;
import org.junit.Test;

import java.util.NoSuchElementException;

/** Tests {@link DefaultDataset}. */
public class DefaultDatasetTest {

	/**
	 * Get a fully calibrated {@link Dataset}
	 *
	 * @return a fully calibrated {@link Dataset}
	 */
	private Dataset<DoubleType> dataset() {
		RandomAccessible<DoubleType> data = Data.image();

		MetadataStore store = new SimpleMetadataStore(data.numDimensions());
		Calibration calibration = store.info(Calibration.class);
		calibration.setAxis(axis(Axes.X), 0);
		calibration.setAxis(axis(Axes.Y), 1);
		calibration.setAxis(axis(Axes.Z), 2);
		calibration.setAxis(axis(Axes.CHANNEL), 3);
		calibration.setAxis(axis(Axes.TIME), 4);

		return new DefaultDataset<>(data, store);
	}

	@Test
	public void testFluentRotation() {
		Dataset<DoubleType> rotated = dataset().view().rotate(3, 2);

		Calibration calView = rotated.store().info(Calibration.class);
		Assert.assertEquals(Axes.X, calView.axis(0).type());
		Assert.assertEquals(Axes.Y, calView.axis(1).type());
		Assert.assertEquals(Axes.CHANNEL, calView.axis(2).type());
		Assert.assertEquals(Axes.Z, calView.axis(3).type());
		Assert.assertEquals(Axes.TIME, calView.axis(4).type());
	}

	@Test
	public void testFluentSlicing() {
		// Take a Z-slice
		Dataset<DoubleType> sliced = dataset().view().slice(2, 9);

		Calibration calView = sliced.store().info(Calibration.class);
		Assert.assertEquals(Axes.X, calView.axis(0).type());
		Assert.assertEquals(Axes.Y, calView.axis(1).type());
		Assert.assertEquals(Axes.CHANNEL, calView.axis(2).type());
		Assert.assertEquals(Axes.TIME, calView.axis(3).type());
		Assert.assertThrows(NoSuchElementException.class, () -> calView.axis(4));
	}

	private CalibratedAxis axis(AxisType axisType) {
		return new DefaultLinearAxis(axisType);
	}
}
