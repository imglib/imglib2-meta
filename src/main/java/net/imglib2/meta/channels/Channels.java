package net.imglib2.meta.channels;

import net.imglib2.display.ColorTable;
import net.imglib2.meta.HasMetadataStore;

public interface Channels extends HasMetadataStore {
	String AXIS_KEY = "channel";
	String RGB_KEY = "is_rgb";
	ColorTable lut(int c);
	void setLut(int c, ColorTable lut);
	boolean isRGB();
	void setRGB(boolean isRGB);
}
