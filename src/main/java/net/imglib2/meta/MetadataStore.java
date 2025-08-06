package net.imglib2.meta;

import net.imglib2.EuclideanSpace;
import net.imglib2.RandomAccessible;
import net.imglib2.RealRandomAccessible;
import net.imglib2.transform.integer.MixedTransform;

import java.util.Optional;

public interface MetadataStore extends EuclideanSpace {

	default MixedTransform transform() {
		//Identity transform
		return new MixedTransform(numDimensions(), numDimensions());
	}

	/**
	 * Find a {@link MetadataItem} associated with key {@code key}.
	 *
	 * @param key the identifier of the {@link MetadataItem}
	 * @return a {@link MetadataItem} matching {@code key}
	 */
	@SuppressWarnings({"raw", "unchecked"})
	default Optional<MetadataItem<?>> get(String key) {
		return (Optional) get(key, Object.class);
	}

	/**
	 * Find a {@link MetadataItem} associated with key {@code key} of {@link Class} {@code ofType}.
	 *
	 * @param key the identifier of the {@link MetadataItem}
	 * @param ofType the type of the {@link MetadataItem}
	 * @return a {@link MetadataItem} matching {@code key} of type {@code ofType}
	 */
	<T> Optional<MetadataItem<T>> get(String key, Class<T> ofType);

	/**
	 * Find a {@link MetadataItem} associated with key {@code key} and axes {@code d}
	 * @param key the identifier of the {@link MetadataItem}
	 * @param d the axes associated with the {@link MetadataItem}
	 * @return a {@link MetadataItem} matching {@code key} of type {@code ofType}
	 */
	default <T> Optional<MetadataItem<T>> get(String key, int... d) {
		return (Optional) get(key, Object.class, d);
	}

	/**
	 * Find a {@link MetadataItem} associated with key {@code key} and axes {@code d} of {@link Class} {@code ofType}.
	 *
	 * @param key the identifier of the {@link MetadataItem}
	 * @param ofType the type of the {@link MetadataItem}
	 * @param d the axes associated with the {@link MetadataItem}
	 * @return a {@link MetadataItem} matching {@code key} of type {@code ofType}
	 */
	<T> Optional<MetadataItem<T>> get(String key, Class<T> ofType, int... d);

	/** Get a window into a bundle of metadata, in a nice type-safe way, according to the specified interface. */
	<T extends HasMetadataStore> T info(Class<T> infoClass);

	/** TODO Simple */
	<T> void add(String name, T data, int... dims);

	/** TODO Varying in integer space */
	<T, U extends RandomAccessible<T>> void add(String name, U data, int... dims);

	/** TODO Varying in real space */
	<T, U extends RealRandomAccessible<T>> void add(String name, U data, int... dims);
}
