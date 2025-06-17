package net.imglib2.meta.calibration;

import net.imglib2.meta.Axis;
import net.imglib2.meta.HasMetadataStore;

import java.util.Optional;

public interface Calibration extends HasMetadataStore {
	String AXIS_KEY = "axis";
	Axis axis(int d);
	void setAxis(Axis axis, int d);

	/**
	 * Retrieves the position of the dimension associated with an axis type.
	 * @param type the {@link AxisType} to search for.
	 * @return the position of {@code type}, if it is present.
	 */
	Optional<Integer> dimension(AxisType type);
}
