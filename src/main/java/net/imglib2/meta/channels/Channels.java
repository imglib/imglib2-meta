package net.imglib2.meta.channels;

import net.imglib2.display.ColorTable;
import net.imglib2.meta.HasMetadataStore;

public interface Channels extends HasMetadataStore {
	String AXIS_KEY = "channel";
	String RGB_KEY = "is_rgb";
	ColorTable lut(int axis, int c);
	void setLut(int axis, int c, ColorTable lut);
	boolean isRGB();
	void setRGB(boolean isRGB);
}
