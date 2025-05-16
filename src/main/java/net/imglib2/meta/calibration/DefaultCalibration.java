package net.imglib2.meta.calibration;

import net.imglib2.Localizable;
import net.imglib2.RandomAccessible;
import net.imglib2.meta.Axis;
import net.imglib2.meta.MetadataStore;
import net.imglib2.meta.VaryingMetadataItem;
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

	@Override
	public Axis axis(final int d) {
		// Search for Axis components
		VaryingMetadataItem<DoubleType, RandomAccessible<DoubleType>> data = metaData.getVarying(AXIS_DATA, d, DoubleType.class).get();
		AxisType type = metaData.get(AXIS_TYPE, d, AxisType.class).get().get();

		// Construct a
		ThreadLocal<long[]> cs = ThreadLocal.withInitial(() -> new long[metaData.numDimensions()]);
		return new Axis() {

			@Override
			public double calibrated(double raw) {
				long[] c = cs.get();
				c[d] = (long) raw;
				return data.getAt(c).get();
			}
			@Override
			public RandomAccessible<DoubleType> data() {
				return data.get();
			}

			@Override
			public AxisType type() {
				return type;
			}
		};
	}


	@Override
	public void setAxis(final Axis axis, final int d) {
		metaData.add(AXIS_DATA, axis.data(), d);
		metaData.add(AXIS_TYPE, axis.type(), d);
	}
}
