package net.imglib2.meta.calibration;

import net.imglib2.meta.Axis;
import net.imglib2.meta.HasMetadataStore;

public interface Calibration extends HasMetadataStore {
	String AXIS_KEY = "axis";
	Axis axis(int d);
	void setAxis(Axis axis, int d);
}
