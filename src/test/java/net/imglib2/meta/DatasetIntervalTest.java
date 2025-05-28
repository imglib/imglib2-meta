package net.imglib2.meta;

import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.meta.calibration.Axes;
import net.imglib2.meta.calibration.AxisType;
import net.imglib2.meta.calibration.Calibration;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;
import net.imglib2.view.fluent.RandomAccessibleIntervalView;
import net.imglib2.view.fluent.RandomAccessibleView;
import org.junit.Assert;
import org.junit.Test;

import java.util.NoSuchElementException;

/** Tests {@link DatasetInterval}. */
public class DatasetIntervalTest {

	/**
	 * Get a fully calibrated {@link DatasetInterval}
	 *
	 * @return a fully calibrated {@link DatasetInterval}
	 */
	private DatasetInterval<DoubleType> dataset() {
		RandomAccessible<DoubleType> data = Data.image();

		MetadataStore store = new SimpleMetadataStore(data.numDimensions());
		Calibration calibration = store.info(Calibration.class);
		calibration.setAxis(axis(Axes.X), 0);
		calibration.setAxis(axis(Axes.Y), 1);
		calibration.setAxis(axis(Axes.Z), 2);
		calibration.setAxis(axis(Axes.CHANNEL), 3);
		calibration.setAxis(axis(Axes.TIME), 4);
		RandomAccessibleInterval<DoubleType> intervaled = Views.interval(data, new FinalInterval(10, 20, 30, 40, 50));
		return DatasetInterval.wrap(intervaled, store);
	}

	@Test
	public void testFluentMoveAxis() {
		DatasetInterval<DoubleType> permuted = dataset().moveAxis(0, 3);

		Calibration calView = permuted.store().info(Calibration.class);
		Assert.assertEquals(Axes.Y, calView.axis(0).type());
		Assert.assertEquals(Axes.Z, calView.axis(1).type());
		Assert.assertEquals(Axes.CHANNEL, calView.axis(2).type());
		Assert.assertEquals(Axes.X, calView.axis(3).type());
		Assert.assertEquals(Axes.TIME, calView.axis(4).type());
	}

	@Test
	public void testFluentRotation() {
		DatasetInterval<DoubleType> rotated = dataset().rotate(3, 2);

		Calibration calView = rotated.store().info(Calibration.class);
		Assert.assertEquals(Axes.X, calView.axis(0).type());
		Assert.assertEquals(Axes.Y, calView.axis(1).type());
		Assert.assertEquals(Axes.CHANNEL, calView.axis(2).type());
		Assert.assertEquals(Axes.Z, calView.axis(3).type());
		Assert.assertEquals(Axes.TIME, calView.axis(4).type());
	}

	@Test
	public void testFluentPermutation() {
		DatasetInterval<DoubleType> permuted = dataset().permute(3, 2);

		Calibration calView = permuted.store().info(Calibration.class);
		Assert.assertEquals(Axes.X, calView.axis(0).type());
		Assert.assertEquals(Axes.Y, calView.axis(1).type());
		Assert.assertEquals(Axes.CHANNEL, calView.axis(2).type());
		Assert.assertEquals(Axes.Z, calView.axis(3).type());
		Assert.assertEquals(Axes.TIME, calView.axis(4).type());
	}

	@Test
	public void testFluentInterval() {
		DatasetInterval<DoubleType> intervaled = dataset().interval(new FinalInterval(10, 10, 10, 10, 10));
		// Assert the new dataset has an interval
		Assert.assertArrayEquals(new long[] {0, 0, 0, 0, 0}, intervaled.minAsLongArray());
		Assert.assertArrayEquals(new long[] {9, 9, 9, 9, 9}, intervaled.maxAsLongArray());

		Calibration calView = intervaled.store().info(Calibration.class);
		Assert.assertEquals(Axes.X, calView.axis(0).type());
		Assert.assertEquals(Axes.Y, calView.axis(1).type());
		Assert.assertEquals(Axes.Z, calView.axis(2).type());
		Assert.assertEquals(Axes.CHANNEL, calView.axis(3).type());
		Assert.assertEquals(Axes.TIME, calView.axis(4).type());
	}

	@Test
	public void testFluentSlicing() {
		// Take a Z-slice
		DatasetInterval<DoubleType> sliced = dataset().slice(2, 9);

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
		DatasetInterval<DoubleType> rotated = dataset().rotate(4, 2).slice(2, 9);

		Calibration calView = rotated.store().info(Calibration.class);
		Assert.assertEquals(Axes.X, calView.axis(0).type());
		Assert.assertEquals(Axes.Y, calView.axis(1).type());
		Assert.assertEquals(Axes.CHANNEL, calView.axis(2).type());
		Assert.assertEquals(Axes.Z, calView.axis(3).type());
		Assert.assertThrows(NoSuchElementException.class, () -> calView.axis(4));
	}

	@Test
	public void testFluentTranslation() {
		// Translate
		DatasetInterval<DoubleType> translated = dataset().translate(-1, 0, 0, 0, 0);
		Calibration calView = translated.store().info(Calibration.class);
		Assert.assertEquals(1.0, calView.axis(0).calibrated(0), 1e-6);
		// Translate & permute
		translated = dataset().translate(-1, 0, 0, 0, 0).permute(0, 2);
		calView = translated.store().info(Calibration.class);
		Assert.assertEquals(1.0, calView.axis(2).calibrated(0), 1e-6);
	}

	@Test
	public void testFluentInvertAxis() {
		// Invert Axis
		DatasetInterval<DoubleType> inverted = dataset().invertAxis(1);
		Calibration calView = inverted.store().info(Calibration.class);
		Assert.assertEquals(-1.0, calView.axis(1).calibrated(1), 1e-6);
		// Translate & permute
		inverted = dataset().invertAxis(1).permute(1, 2);
		calView = inverted.store().info(Calibration.class);
		Assert.assertEquals(-1.0, calView.axis(2).calibrated(1), 1e-6);
	}

	@Test
	public void testFluentInverseTranslation() {
		// Translate
		DatasetInterval<DoubleType> translated = dataset().translateInverse(1, 0, 0, 0, 0);
		Calibration calView = translated.store().info(Calibration.class);
		Assert.assertEquals(1.0, calView.axis(0).calibrated(0), 1e-6);
		// Translate & permute
		translated = dataset().translateInverse(1, 0, 0, 0, 0).permute(0, 2);
		calView = translated.store().info(Calibration.class);
		Assert.assertEquals(1.0, calView.axis(2).calibrated(0), 1e-6);
	}

	@Test
	public void testFluentSubsampling() {
		DatasetInterval<DoubleType> translated = dataset().subsample(2, 1, 1, 1, 1);
		Calibration calView = translated.store().info(Calibration.class);
		Assert.assertEquals(2.0, calView.axis(0).calibrated(1), 1e-6);
	}

	@Test
	public void testFluentAddDimension() {
		DatasetInterval<DoubleType> original = dataset();
		DatasetInterval<DoubleType> translated = dataset().addDimension();
		Calibration cal = original.store().info(Calibration.class);
		Calibration calView = translated.store().info(Calibration.class);
		// New axes should be default be unknown
		Assert.assertEquals(Axes.unknown(), calView.axis(original.numDimensions()).type());
		// But we can set them
		// FIXME: Can we set them??
//		cal.setAxis(axis(Axes.X), original.numDimensions());
		// And they should persist
//		Assert.assertEquals(Axes.X, calView.axis(original.numDimensions()).type());
	}

	@Test
	public void testExtension() {
		Dataset<DoubleType> permuted = dataset() //
				.extend(RandomAccessibleIntervalView.Extension.border());

		// Assert axes unchanged
		Calibration calView = permuted.store().info(Calibration.class);
		Assert.assertEquals(Axes.X, calView.axis(0).type());
		Assert.assertEquals(Axes.Y, calView.axis(1).type());
		Assert.assertEquals(Axes.Z, calView.axis(2).type());
		Assert.assertEquals(Axes.CHANNEL, calView.axis(3).type());
		Assert.assertEquals(Axes.TIME, calView.axis(4).type());
	}

	@Test
	public void testExpansion() {
		long borderSize = 2;
		DatasetInterval<DoubleType> permuted = dataset() //
				.expand(RandomAccessibleIntervalView.Extension.border(), 2, 2, 2, 2, 2);

		// Assert minimum moved by -borderSize
		for(int i = 0; i < permuted.numDimensions(); i++) {
			Assert.assertEquals(permuted.min(i), dataset().min(i) - borderSize);
		}
		// Assert maximum moved by +borderSize
		for(int i = 0; i < permuted.numDimensions(); i++) {
			Assert.assertEquals(permuted.max(i), dataset().max(i) + borderSize);
		}

		// Assert axes unchanged
		Calibration calView = permuted.store().info(Calibration.class);
		Assert.assertEquals(Axes.X, calView.axis(0).type());
		Assert.assertEquals(Axes.Y, calView.axis(1).type());
		Assert.assertEquals(Axes.Z, calView.axis(2).type());
		Assert.assertEquals(Axes.CHANNEL, calView.axis(3).type());
		Assert.assertEquals(Axes.TIME, calView.axis(4).type());
	}

	@Test
	public void testInterpolate() {
		RealDataset<DoubleType> interpolated = dataset() //
				.interpolate(RandomAccessibleView.Interpolation.nearestNeighbor()); //

		// Assert axes unchanged
		Calibration calView = interpolated.store().info(Calibration.class);
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
