package net.imglib2.meta.calibration;

import net.imglib2.meta.LinearAxis;
import net.imglib2.meta.HasMetadataStore;
import net.imglib2.meta.LinearAxisView;

public interface Calibration extends HasMetadataStore {
	String AXIS_KEY = "axis";
	LinearAxisView axis(int d);
	void setAxis(LinearAxis axis, int d);
}
