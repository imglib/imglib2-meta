package net.imglib2.meta.channels;

import net.imglib2.*;
import net.imglib2.display.ColorTable;
import net.imglib2.meta.MetadataStore;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class DefaultChannels implements Channels {

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
	public ColorTable lut(int axis, int c) {
		Point point = pointCache.get();
		for (int i = 0; i < point.numDimensions(); i++) {
			point.setPosition(axis == i ? c : 0, i);
		}
		return metaData.getVarying(AXIS_KEY, axis, ColorTable.class).get().getAt(point);
	}

	@Override
	public void setLut(int axis, int c, ColorTable lut) {
		metaData.get(AXIS_KEY, axis, ColorTableRAI.class).orElseGet(() -> {
			ColorTableRAI newLut = new ColorTableRAI();
			metaData.add(AXIS_KEY, newLut, axis);
			return metaData.get(AXIS_KEY, axis, ColorTableRAI.class).get();
		}).get().setLut(c, lut);
	}

}
