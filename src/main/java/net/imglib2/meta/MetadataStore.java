package net.imglib2.meta;

import net.imglib2.EuclideanSpace;
import net.imglib2.RandomAccessible;
import net.imglib2.meta.attribution.Attribution;
import net.imglib2.meta.calibration.Calibration;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.MixedTransformView;

import java.util.Optional;

public interface MetadataStore extends EuclideanSpace {

	/**
	 * @param key the metadata key
	 */
	@SuppressWarnings({"raw", "unchecked"})
	default Optional<MetadataItem<?>> get(String key) {
		return (Optional) get(key, null);
	}

	<T> Optional<MetadataItem<T>> get(String key, Class<T> ofType);

	/**
	 * @param key the metadata key
	 * @param d   the axis
	 */
	@SuppressWarnings({"raw", "unchecked"})
	default Optional<MetadataItem<?>> get(String key, int d) {
		return (Optional) get(key, d, null);
	}

	<T> Optional<MetadataItem<T>> get(String key, int d, Class<T> ofType);

	MetadataStore view(MixedTransformView<?> v);

	/** Get a window into a bundle of metadata, in a nice type-safe way, according to the specified interface. */
	<T extends HasMetadataStore> T info(Class<T> infoClass);

	/** TODO Simple */
	<T> void add(String name, T data, int... dims);

	/** TODO Varying */
	<T> void add(String name, RandomAccessible<T> data, int... dims);
}
