package net.imglib2.meta.calibration;

import net.imagej.axis.CalibratedAxis;
import net.imglib2.meta.HasMetadataStore;

public interface Calibration extends HasMetadataStore {
	String AXIS_KEY = "axis";
	CalibratedAxis axis(int d);
	void setAxis(CalibratedAxis axis, int d);
}
