package net.imglib2.meta.calibration;

import net.imglib2.RandomAccessible;
import net.imglib2.RealLocalizable;
import net.imglib2.meta.Axis;
import net.imglib2.meta.MetadataItem;
import net.imglib2.meta.MetadataStore;
import net.imglib2.meta.VaryingMetadataItem;
import net.imglib2.type.numeric.real.DoubleType;

import java.util.NoSuchElementException;
import java.util.Optional;

public class DefaultCalibration implements Calibration {
	private MetadataStore metaData;
	private static final String AXIS_DATA = "axis_data";
	private static final String AXIS_TYPE = "axis_type";

	private static final MetadataItem<AxisType> UNKNOWN_TYPE = new MetadataItem<AxisType>() {
		@Override
		public String name() {
			return AXIS_TYPE;
		}

		@Override
		public AxisType get() {
			return Axes.unknown();
		}
	};

	private static final VaryingMetadataItem<DoubleType, RandomAccessible<DoubleType>> UNKNOWN_DATA = new VaryingMetadataItem<DoubleType, RandomAccessible<DoubleType>>() {
		@Override
		public String name() {
			return AXIS_DATA;
		}

		@Override
		public RandomAccessible<DoubleType> get() {
			throw new IllegalArgumentException("No data associated with unknown axes!");
		}

		@Override
		public DoubleType getAt(RealLocalizable pos) {
			throw new IllegalArgumentException("Cannot query positions on unknown axes!");
		}
	};



	@Override
	public void setStore(MetadataStore store) {
		this.metaData = store;
	}

	@Override
	public Axis axis(final int d) {
		if (d >= metaData.numDimensions()) {
			throw new NoSuchElementException("Metadata is only " + metaData.numDimensions() + "-dimensional!");
		}
		// Search for Axis components
		VaryingMetadataItem<DoubleType, RandomAccessible<DoubleType>> data = metaData.getVarying(AXIS_DATA, d, DoubleType.class).orElse(UNKNOWN_DATA);

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
				return metaData.get(AXIS_TYPE, d, AxisType.class).orElse(UNKNOWN_TYPE).get();
			}
		};
	}


	@Override
	public void setAxis(final Axis axis, final int d) {
		metaData.add(AXIS_DATA, axis.data(), d);
		metaData.add(AXIS_TYPE, axis.type(), d);
	}

	@Override
	public Optional<Integer> dimension(AxisType type) {
		for (int i = 0; i < metaData.numDimensions(); i++) {
			if(axis(i).type() == type) {
				return Optional.of(i);
			}
		}
		return Optional.empty();
	}
}
