package net.imglib2.meta.calibration;

import net.imagej.axis.CalibratedAxis;
import net.imglib2.meta.MetadataStore;

public class DefaultCalibration implements Calibration {
	private MetadataStore metaData;

	@Override
	public void setMetaData(MetadataStore store) {
		this.metaData = store;
	}

	@Override
	public CalibratedAxis axis(int d) {
		return metaData.get(AXIS_KEY, d, CalibratedAxis.class).get().get();
	}

	@Override
	public void setAxis(CalibratedAxis axis, int d) {
		metaData.add(AXIS_KEY, axis, d);
	}
}
