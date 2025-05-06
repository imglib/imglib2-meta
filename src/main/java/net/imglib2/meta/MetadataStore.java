package net.imglib2.meta;

import net.imglib2.EuclideanSpace;
import net.imglib2.RandomAccessible;
import net.imglib2.RealRandomAccessible;
import net.imglib2.transform.integer.Mixed;
import net.imglib2.transform.integer.MixedTransform;

import java.util.Optional;

public interface MetadataStore extends EuclideanSpace {

	default MixedTransform transform() {
		//Identity transform
		return new MixedTransform(numDimensions(), numDimensions());
	}

	/**
	 * Find a {@link MetadataItem} matching {@code key}
	 * @param name
	 * @return a {@link MetadataItem} matching {@code key}
	 */
	@SuppressWarnings({"raw", "unchecked"})
	default Optional<MetadataItem<?, ?>> get(String name) {
		return (Optional) get(name, null);
	}

	<T> Optional<MetadataItem<T, T>> get(String name, Class<T> ofType);

	@SuppressWarnings({"raw", "unchecked"})
	default Optional<MetadataItem<?, ?>> get(String name, int d) {
		return (Optional) get(name, d, null);
	}

	<T> Optional<MetadataItem<T, RandomAccessible<T>>> get(String name, int d, Class<T> ofType);

	/** Get a window into a bundle of metadata, in a nice type-safe way, according to the specified interface. */
	<T extends HasMetadataStore> T info(Class<T> infoClass);

	/** TODO Simple */
	<T> void add(String name, T data, int... dims);

	/** TODO Varying in integer space */
	<T, U extends RandomAccessible<T>> void add(String name, U data, int... dims);

	/** TODO Varying in real space */
	<T, U extends RealRandomAccessible<T>> void add(String name, U data, int... dims);
}
