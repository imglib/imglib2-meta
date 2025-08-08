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
	default Optional<MetadataItem<?>> item(String key) {
		return (Optional) item(key, Object.class);
	}

	/**
	 * Find a {@link MetadataItem} associated with key {@code key} of {@link Class} {@code ofType}.
	 *
	 * @param key the identifier of the {@link MetadataItem}
	 * @param ofType the type of the {@link MetadataItem}
	 * @return a {@link MetadataItem} matching {@code key} of type {@code ofType}
	 */
	<T> Optional<MetadataItem<T>> item(String key, Class<T> ofType);

	/**
	 * Find a {@link MetadataItem} associated with key {@code key} and axes {@code d}
	 * @param key the identifier of the {@link MetadataItem}
	 * @param d the axes associated with the {@link MetadataItem}
	 * @return a {@link MetadataItem} matching {@code key} of type {@code ofType}
	 */
	default <T> Optional<MetadataItem<T>> item(String key, int... d) {
		return (Optional) item(key, Object.class, d);
	}

	/**
	 * Find a {@link MetadataItem} associated with key {@code key} and axes {@code d} of {@link Class} {@code ofType}.
	 *
	 * @param key the identifier of the {@link MetadataItem}
	 * @param ofType the type of the {@link MetadataItem}
	 * @param d the axes associated with the {@link MetadataItem}
	 * @return a {@link MetadataItem} matching {@code key} of type {@code ofType}
	 */
	<T> Optional<MetadataItem<T>> item(String key, Class<T> ofType, int... d);

	/** Get a window into a bundle of metadata, in a nice type-safe way, according to the specified interface. */
	<T extends HasMetadataStore> T info(Class<T> infoClass);

	/** TODO Simple */
	<T> void add(String name, T data, int... dims);

	/** TODO Varying in integer space */
	<T, U extends RandomAccessible<T>> void add(String name, U data, int... dims);

	/** TODO Varying in real space */
	<T, U extends RealRandomAccessible<T>> void add(String name, U data, int... dims);
}
