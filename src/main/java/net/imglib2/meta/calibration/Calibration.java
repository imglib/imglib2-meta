package net.imglib2.meta.calibration;

import net.imagej.axis.CalibratedAxis;
import net.imglib2.meta.HasMetaData;

public interface Calibration extends HasMetaData {
	String AXIS_KEY = "axis";
	CalibratedAxis axis(int d);
	void setAxis(CalibratedAxis axis, int d);
}
