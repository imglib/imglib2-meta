package net.imglib2.meta.calibration;

import net.imglib2.Localizable;
import net.imglib2.RandomAccessible;
import net.imglib2.meta.Axis;
import net.imglib2.meta.MetadataStore;
import net.imglib2.position.FunctionRandomAccessible;
import net.imglib2.type.numeric.real.DoubleType;

import java.util.RandomAccess;
import java.util.function.BiConsumer;

public class DefaultCalibration implements Calibration {
	private MetadataStore metaData;
	private static final String AXIS_DATA = "axis_data";
	private static final String AXIS_TYPE = "axis_type";

	@Override
	public void setStore(MetadataStore store) {
		this.metaData = store;
	}

	// FIXME? This is never translated/scaled.
	@Override
	public Axis axis(final int d) {
		long[] coords = new long[metaData.numDimensions()];
		return new Axis() {

			@Override
			public double calibrated(double raw) {
				coords[d] = (long) raw;
				return metaData.get(AXIS_DATA, d, DoubleType.class).get().getAt(coords).get();
			}
			@Override
			public RandomAccessible<DoubleType> data() {
				return metaData.get(AXIS_DATA, d, DoubleType.class).get().get();
			}

			@Override
			public AxisType type() {
				return metaData.get(AXIS_TYPE, d).get();
			}
		};
	}


	@Override
	public void setAxis(final Axis axis, final int d) {
		metaData.add(AXIS_DATA, axis.data(), d);
		metaData.add(AXIS_TYPE, axis.type(), d);
	}
}
