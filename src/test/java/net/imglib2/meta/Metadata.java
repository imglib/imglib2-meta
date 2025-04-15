package net.imglib2.meta;

import net.imglib2.meta.attribution.Attribution;
import net.imglib2.meta.calibration.Calibration;

/** Utility class for common metadata access. */
public final class Metadata {
	private Metadata() { }

	public static Attribution attribution(MetadataStore store) {
		return store.info(Attribution.class);
	}

	public static Calibration calibration(MetadataStore store) {
		return store.info(Calibration.class);
	}
}
