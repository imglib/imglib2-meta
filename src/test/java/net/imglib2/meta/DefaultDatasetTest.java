package net.imglib2.meta;

import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.axis.CalibratedAxis;
import net.imagej.axis.DefaultLinearAxis;
import net.imglib2.RandomAccessible;
import net.imglib2.meta.calibration.Calibration;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.fluent.RandomAccessibleView;
import org.junit.Test;

/** Tests {@link DefaultDataset}. */
public class DefaultDatasetTest {
	@Test
	public void testFluent() {
		RandomAccessible<DoubleType> data = Data.image();

		MetadataStore store = new SimpleMetadataStore(data.numDimensions());
		Calibration calibration = store.info(Calibration.class);
		calibration.setAxis(axis(Axes.X), 0);
		calibration.setAxis(axis(Axes.Y), 1);
		calibration.setAxis(axis(Axes.Z), 2);
		calibration.setAxis(axis(Axes.CHANNEL), 3);
		calibration.setAxis(axis(Axes.TIME), 4);

		Dataset<DoubleType, ?> dataset = new DefaultDataset<>(data, store);

		Dataset<DoubleType, ?> rotated = dataset.rotate(3, 2);

		Calibration calView = rotated.store().info(Calibration.class);
		for (int d=0; d<rotated.numDimensions(); d++) {
			System.out.println(calView.axis(d).type());
		}
	}

	private CalibratedAxis axis(AxisType axisType) {
		return new DefaultLinearAxis(axisType);
	}
}
