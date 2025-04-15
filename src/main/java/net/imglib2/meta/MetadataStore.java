package net.imglib2.meta;

import net.imglib2.EuclideanSpace;
import net.imglib2.RandomAccessible;
import net.imglib2.meta.attribution.Attribution;
import net.imglib2.meta.calibration.Calibration;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.MixedTransformView;

import java.util.Optional;

public interface MetadataStore extends EuclideanSpace {

	Optional<MetadataItem<?>> get(String key);

	<T> Optional<MetadataItem<T>> get(String key, Class<T> ofType);

	Optional<MetadataItem<?>> get(String key, int d);

	<T> Optional<MetadataItem<T>> get(String key, int d, Class<T> ofType);

	MetadataStore view(MixedTransformView<DoubleType> v);

	/** Get a window into a bundle of metadata, in a nice type-safe way, according to the specified interface. */
	<T extends HasMetaData> T info(Class<T> infoClass);

	// Type-safe convenience accessors for specific metadata bundles.

	default Attribution attribution() {
		return info(Attribution.class);
	}

	default Calibration calibration() {
		return info(Calibration.class);
	}

	/** TODO Simple */
	<T> void add(String name, T data, int... dims);

	/** TODO Varying */
	<T> void add(String name, RandomAccessible<T> data, int... dims);
}
