package net.imglib2.meta.calibration;

import net.imglib2.meta.LinearAxis;
import net.imglib2.meta.LinearAxisView;
import net.imglib2.meta.MetadataStore;

public class DefaultCalibration implements Calibration {
	private MetadataStore metaData;

	@Override
	public void setStore(MetadataStore store) {
		this.metaData = store;
	}

	@Override
	public LinearAxisView axis(int d) {
		return LinearAxisView.from(metaData, d);
	}

	@Override
	public void setAxis(LinearAxis axis, int d) {
		metaData.add(AXIS_KEY, axis, d);
	}
}
