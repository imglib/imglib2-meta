package net.imglib2.meta;

import net.imglib2.EuclideanSpace;

import java.util.Optional;

/**
 * @param <M> A base {@link MetadataItem} type
 */
public interface BaseMetadataStore<M extends MetadataItem<?>> extends EuclideanSpace {

    /**
     * Find a {@link M} associated with key {@code key}.
     *
     * @param key the identifier of the metadata item
     * @return a metadata item matching {@code key}
     */
    @SuppressWarnings({"raw", "unchecked"})
    default Optional<M> item(String key) {
        return (Optional<M>) item(key, Object.class);
    }

    /**
     * Find a metadata item associated with key {@code key} of {@link Class} {@code ofType}.
     *
     * @param key the identifier of the metadata item
     * @param ofType the type of the metadata item
     * @return a metadata item matching {@code key} of type {@code ofType}
     */
    <T> Optional<? extends M> item(String key, Class<T> ofType);

    /**
     * Find a metadata item associated with key {@code key} and axes {@code d}
     * @param key the identifier of the metadata item
     * @param d the axes associated with the metadata item
     * @return a metadata item matching {@code key}
     */
    default Optional<M> item(String key, int... d) {
        return (Optional<M>) item(key, Object.class, d);
    }

    /**
     * Find a metadata item associated with key {@code key} and axes {@code d} of {@link Class} {@code ofType}.
     *
     * @param key the identifier of the metadata item
     * @param ofType the type of the metadata item
     * @param d the axes associated with the metadata item
     * @return a metadata item matching {@code key} of type {@code ofType}
     */
    <T> Optional<? extends M> item(String key, Class<T> ofType, int... d);

    /** Get a window into a bundle of metadata, in a nice type-safe way, according to the specified interface. */
    <T extends HasMetadataStore> T info(Class<T> infoClass);

    /** Add simple metadata */
    <T> void add(String name, T data, int... dims);

}