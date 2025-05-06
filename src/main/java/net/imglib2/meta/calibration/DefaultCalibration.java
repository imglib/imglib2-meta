package net.imglib2.meta.calibration;

import net.imglib2.meta.LinearAxis;
import net.imglib2.meta.MetadataStore;

public class DefaultCalibration implements Calibration {
	private MetadataStore metaData;

	@Override
	public void setStore(MetadataStore store) {
		this.metaData = store;
	}

	// FIXME? This is never translated/scaled.
	@Override
	public LinearAxis axis(int d) {
		return metaData.get(AXIS_KEY, d, LinearAxis.class).get().get();
	}

	@Override
	public double calibrated(int d, double raw) {
		LinearAxis axis = axis(d);
		return axis.getAt((int) raw - metaData.transform().getTranslation(d)).get();
	}


	@Override
	public void setAxis(LinearAxis axis, int d) {
		metaData.add(AXIS_KEY, axis, d);
	}
}
