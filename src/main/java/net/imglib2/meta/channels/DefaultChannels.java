package net.imglib2.meta.channels;

import net.imglib2.*;
import net.imglib2.display.ColorTable;
import net.imglib2.meta.Metadata;
import net.imglib2.meta.MetadataItem;
import net.imglib2.meta.MetadataStore;
import net.imglib2.meta.calibration.Axes;
import net.imglib2.meta.calibration.AxisType;
import net.imglib2.meta.calibration.Calibration;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class DefaultChannels implements Channels {

	private static final Supplier<RuntimeException> NO_CHANNEL_AXIS_YET = //
			() -> new RuntimeException("The channel axis has not yet been set!");
	private final Map<Integer, ColorTable> savedLUTs = new HashMap<>();

	private MetadataStore metaData;

	private final Supplier<Point> pointSupplier = //
			() -> new Point(metaData.numDimensions());

	private final ThreadLocal<Point> pointCache = ThreadLocal.withInitial(pointSupplier);

	@Override
	public void setStore(MetadataStore store) {
		this.metaData = store;
	}

	@Override
	public ColorTable lut(int c) {
		Optional<Integer> axis = Metadata.calibration(this.metaData).indexOf(Axes.CHANNEL);
		if (axis.isPresent()) {
			Point point = pointCache.get();
			for (int i = 0; i < point.numDimensions(); i++) {
				point.setPosition(axis.get() == i ? c : 0, i);
			}
			return metaData.get(AXIS_KEY, ColorTableHolder.class, axis.get()).get().getAt(point).get();
		}
		else {
			// One LUT for the whole image
			return metaData.get(AXIS_KEY, ColorTable.class).get().getAt();
		}
	}

	@Override
	public void setLut(int c, ColorTable lut) {
		int axis = Metadata.calibration(this.metaData)
				.indexOf(Axes.CHANNEL)
				.orElseThrow(NO_CHANNEL_AXIS_YET);
		metaData.get(AXIS_KEY, ColorTableHolder.class, axis).orElseGet(() -> {
			ColorTableRAI newLut = new ColorTableRAI();
			metaData.add(AXIS_KEY, newLut, axis);
			return metaData.get(AXIS_KEY, ColorTableHolder.class, axis).get();
		}).getAt(0, 0, c).set(lut);
	}

	@Override
	public boolean isRGB() {
		Optional<MetadataItem<Boolean>> item = metaData.get(RGB_KEY, Boolean.class);
		return item.isPresent() ? item.get().get() : false;
	}

	@Override
	public void setRGB(boolean isRGB) {
		metaData.add(RGB_KEY, isRGB);
	}

}
