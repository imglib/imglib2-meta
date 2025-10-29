/*-
 * #%L
 * Metadata for ImgLib2
 * %%
 * Copyright (C) 2016 - 2025 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
 * John Bogovic, Albert Cardona, Barry DeZonia, Christian Dietz, Jan Funke,
 * Aivar Grislis, Jonathan Hale, Grant Harris, Stefan Helfrich, Mark Hiner,
 * Martin Horn, Steffen Jaensch, Lee Kamentsky, Larry Lindsey, Melissa Linkert,
 * Mark Longair, Brian Northan, Nick Perry, Curtis Rueden, Johannes Schindelin,
 * Gabriel Selzer, Jean-Yves Tinevez and Michael Zinsmaier.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package net.imglib2.meta;

import net.imglib2.EuclideanSpace;
import net.imglib2.Localizable;
import net.imglib2.RandomAccessible;

import java.util.Collection;
import java.util.ServiceLoader;
import java.util.function.BiConsumer;

public interface MetadataStore extends EuclideanSpace {

    /**
     * Get all metadata items stored in this {@link MetadataStore}.
     * @return all metadata items
     */
    Collection<? extends MetadataItem<?>> items();

	/**
	 * Find a metadata item associated with key {@code key} and axes {@code dims}
	 * @param key the identifier of the metadata item
	 * @param dims the axes associated with the metadata item
	 * @return a metadata item matching {@code key}
	 */
	default MetadataItem<?> item(String key, int... dims) {
		return item(key, Object.class, dims);
	}

	/**
	 * Find a metadata item associated with key {@code key} and axes {@code dims} of {@link Class} {@code ofType}.
	 *
	 * @param key the identifier of the metadata item
	 * @param ofType the type of the metadata item
	 * @param dims the axes associated with the metadata item
	 * @return a metadata item matching {@code key} of type {@code ofType}
	 */
	<T> MetadataItem<T> item(String key, Class<T> ofType, int... dims);

	/** Get a window into a bundle of metadata, in a nice type-safe way, according to the specified interface. */
    default <T extends HasMetadataStore> T info(Class<T> infoClass) {
        ServiceLoader<T> loader = ServiceLoader.load(infoClass);
        T instance = loader.iterator().next();
        instance.setStore(this);
        return instance;
    }

	/**
     * Adds metadata {@code data} associated with key {@code key} and axes {@code dims}.
     * <p>
     * Some {@link MetadataStore}s are <b>read-only</b> and do not support adding new items. Calling this method on
     * such a {@link MetadataStore} should throw an {@link UnsupportedOperationException}.
     * </p>
     * @param key the identifier of the metadata item
     * @param data the metadata
     * @param dims the axes associated with the metadata item
     */
	default <T> void add(String key, T data, int... dims) {
        // Implementations may override to implement metadata writes
        throw new UnsupportedOperationException(getClass() + " is Read-only!");
    }

    /**
     * Adds metadata {@code data} associated with key {@code key} and axes {@code dims}.
     * <p>
     * Some {@link MetadataStore}s are <b>read-only</b> and do not support adding new items. Calling this method on
     * such a {@link MetadataStore} should throw an {@link UnsupportedOperationException}.
     * </p>
     * @param key the identifier of the metadata item
     * @param data the metadata
     * @param dims the axes associated with the metadata item
     */
	default <T> void add(String key, RandomAccessible<T> data, int... dims) {
        // Implementations may override to implement metadata writes
        throw new UnsupportedOperationException(getClass() + " is Read-only!");
    }

    /**
     * Adds <em>mutable</em> metadata {@code data} associated with key {@code key} and axes {@code dims}.
     * <p>
     * Some {@link MetadataStore}s are <b>read-only</b> and do not support adding new items. Calling this method on
     * such a {@link MetadataStore} should throw an {@link UnsupportedOperationException}.
     * </p>
     * @param key the identifier of the metadata item
     * @param data the metadata
     * @param setter a function able to update the metadata value at a given position
     * TODO: This parameter is <b>REALLY</b> unfortunate. Very interested in better designs.
     * @param dims the axes associated with the metadata item
     */
    default <T> void add(String key, RandomAccessible<T> data, BiConsumer<Localizable, T> setter, int... dims) {
        throw new UnsupportedOperationException("This MetadataStore is read-only!");
    }
}
