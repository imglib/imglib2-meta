package net.imglib2.meta.calibration;

import net.imglib2.meta.LinearAxis;
import net.imglib2.meta.HasMetadataStore;

public interface Calibration extends HasMetadataStore {
	String AXIS_KEY = "axis";
	LinearAxis axis(int d);
	double calibrated(int d, double raw);
	void setAxis(LinearAxis axis, int d);
}
