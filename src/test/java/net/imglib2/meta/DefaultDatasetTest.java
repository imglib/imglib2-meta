package net.imglib2.meta;

import net.imglib2.RandomAccessible;
import net.imglib2.meta.calibration.Axes;
import net.imglib2.meta.calibration.AxisType;
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
	public void testFluentPermutation() {
		Dataset<DoubleType> sliced = dataset().view().permute(3, 2);

		Calibration calView = sliced.store().info(Calibration.class);
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

	@Test
	public void testFluentConcatenation() {
		// Take a slice after rotating
		Dataset<DoubleType> sliced = dataset().view().rotate(4, 2).slice(2, 9);

		Calibration calView = sliced.store().info(Calibration.class);
		Assert.assertEquals(Axes.X, calView.axis(0).type());
		Assert.assertEquals(Axes.Y, calView.axis(1).type());
		Assert.assertEquals(Axes.CHANNEL, calView.axis(2).type());
		Assert.assertEquals(Axes.Z, calView.axis(3).type());
		Assert.assertThrows(NoSuchElementException.class, () -> calView.axis(4));
	}

	@Test
	public void testFluentTranslation() {
		// Take a slice after rotating
		Dataset<DoubleType> sliced = dataset().view().translate(1, 2, 3, 4, 5);

		Calibration calView = sliced.store().info(Calibration.class);
		LinearAxisView axis = calView.axis(0);
		Assert.assertEquals(1.0, axis.getAt(0));
//		calView.axis(0).type();
//		Assert.assertEquals(Axes.X, calView.axis(0).type());
//		Assert.assertEquals(Axes.Y, calView.axis(1).type());
//		Assert.assertEquals(Axes.CHANNEL, calView.axis(2).type());
//		Assert.assertEquals(Axes.Z, calView.axis(3).type());
//		Assert.assertThrows(NoSuchElementException.class, () -> calView.axis(4));
	}

	@Test
	public void testFluentSubsampling() {
		// Take a slice after rotating
		Dataset<DoubleType> sliced = dataset().view().subsample(2, 1, 1, 1, 1);

		Calibration calView = sliced.store().info(Calibration.class);
		LinearAxisView axis = calView.axis(0);
		Assert.assertEquals(0.0, axis.getAt(0));
		Assert.assertEquals(2.0, axis.getAt(1));
	}

	private LinearAxis axis(AxisType axisType) {
		LinearAxis ax = new LinearAxis(axisType);
//		ax.setUnit(axisType.getLabel());
		return ax;
	}
}
