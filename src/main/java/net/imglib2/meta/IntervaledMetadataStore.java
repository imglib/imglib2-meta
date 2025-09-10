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

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;

public interface IntervaledMetadataStore extends MetadataStore {


	/**
	 * Find a {@link Object} associated with key {@code key}.
	 *
	 * @param key the identifier of the metadata item
	 * @return a metadata item matching {@code key}
	 */
	default IntervaledMetadataItem<?> item(String key) {
		return item(key, Object.class);
	}

	/**
	 * Find a metadata item associated with key {@code key} of {@link Class} {@code ofType}.
	 *
	 * @param key the identifier of the metadata item
	 * @param ofType the type of the metadata item
	 * @return a metadata item matching {@code key} of type {@code ofType}
	 */
	<T> IntervaledMetadataItem<T> item(String key, Class<T> ofType);

	/**
	 * Find a metadata item associated with key {@code key} and axes {@code d}
	 * @param key the identifier of the metadata item
	 * @param d the axes associated with the metadata item
	 * @return a metadata item matching {@code key}
	 */
	default IntervaledMetadataItem<?> item(String key, int... d) {
		return item(key, Object.class, d);
	}

	/**
	 * Find a metadata item associated with key {@code key} and axes {@code d} of {@link Class} {@code ofType}.
	 *
	 * @param key the identifier of the metadata item
	 * @param ofType the type of the metadata item
	 * @param d the axes associated with the metadata item
	 * @return a metadata item matching {@code key} of type {@code ofType}
	 */
	<T> IntervaledMetadataItem<T> item(String key, Class<T> ofType, int... d);

	/** Get a window into a bundle of metadata, in a nice type-safe way, according to the specified interface. */
	<T extends HasMetadataStore> T info(Class<T> infoClass);

	/** Add simple metadata */
	<T> void add(String name, T data, int... dims);

	default <T> void add(String name, RandomAccessible<T> data, int... d) {
        throw new UnsupportedOperationException("IntervaledMetadataStore does not support RandomAccessible metadata");
    }

    /** Add simple metadata */
    <T> void add(String name, RandomAccessibleInterval<T> data, int... dims);
}
