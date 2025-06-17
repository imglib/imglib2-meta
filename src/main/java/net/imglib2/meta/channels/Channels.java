package net.imglib2.meta.channels;

import net.imglib2.display.ColorTable;
import net.imglib2.meta.HasMetadataStore;

public interface Channels extends HasMetadataStore {
	String AXIS_KEY = "channel";
	ColorTable lut(int axis, int c);
	void setLut(int axis, int c, ColorTable lut);
}
