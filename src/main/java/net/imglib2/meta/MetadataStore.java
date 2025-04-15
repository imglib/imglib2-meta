package net.imglib2.meta;

import net.imglib2.EuclideanSpace;
import net.imglib2.RandomAccessible;
import net.imglib2.RealRandomAccessible;
import net.imglib2.meta.attribution.Attribution;
import net.imglib2.meta.calibration.Calibration;
import net.imglib2.realtransform.AffineGet;
import net.imglib2.realtransform.AffineRealRandomAccessible;
import net.imglib2.realtransform.RealTransformRealRandomAccessible;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.MixedTransformView;

import java.util.Optional;

public interface MetadataStore extends EuclideanSpace {

	@SuppressWarnings({"raw", "unchecked"})
	default Optional<MetadataItem<?>> get(String name) {
		return (Optional) get(name, null);
	}

	<T> Optional<MetadataItem<T>> get(String name, Class<T> ofType);

	@SuppressWarnings({"raw", "unchecked"})
	default Optional<MetadataItem<?>> get(String name, int d) {
		return (Optional) get(name, d, null);
	}

	<T> Optional<MetadataItem<T>> get(String name, int d, Class<T> ofType);

	/** Get a window into a bundle of metadata, in a nice type-safe way, according to the specified interface. */
	<T extends HasMetadataStore> T info(Class<T> infoClass);

	/** TODO Simple */
	<T> void add(String name, T data, int... dims);

	/** TODO Varying in integer space */
	<T> void add(String name, RandomAccessible<T> data, int... dims);

	/** TODO Varying in real space */
	<T> void add(String name, RealRandomAccessible<T> data, int... dims);
}
