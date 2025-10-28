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

import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.transform.integer.MixedTransform;

/**
 * A coupled {@link RandomAccessible} and associated {@link MetadataStore}.
 *
 * @param <T> the type of pixels contained within the {@link RandomAccessible}
 * @author Gabriel Selzer
 * @author Curtis Rueden
 * @author Edward Evans
 */
public interface Dataset<T> extends RandomAccessible<T> {
	RandomAccessible<T> data();
	MetadataStore store();

    /**
     * Creates a new {@link Dataset} from a {@link RandomAccessible}. An empty {@link MetadataStore} is created.
     *
     * @param delegate the coupled {@link RandomAccessible}
     * @return a {@link Dataset} wrapping {@code delegate}
     * @param <T> the type of pixels contained within {@code delegate}
     */
    static <T> Dataset<T> wrap(RandomAccessible<T> delegate) {
        return wrap(delegate, new SimpleMetadataStore(delegate.numDimensions()));
    }

	/**
	 * Creates a new {@link Dataset} from a {@link RandomAccessible} and a {@link MetadataStore}.
	 *
	 * @param delegate the coupled {@link RandomAccessible}
	 * @param store the coupled {@link MetadataStore}
	 * @return a {@link Dataset} wrapping {@code delegate} and {@code store}
	 * @param <T> the type of pixels contained within {@code delegate}
	 */
	static <T> Dataset<T> wrap(RandomAccessible<T> delegate, MetadataStore store) {
		return new Dataset<T>() {
			@Override
			public RandomAccessible<T> data() {
				return delegate;
			}

			@Override
			public MetadataStore store() {
				return store;
			}
		};
	}

	default DatasetView<T, ?> view() {
		return DatasetView.wrap(this, new MixedTransform(numDimensions(), numDimensions()));
	}

    @Override
	default T getType() {
		return this.data().getType();
	}

    @Override
	default int numDimensions() {
		return this.data().numDimensions();
	}

    @Override
	default RandomAccess<T> randomAccess() {
		return this.data().randomAccess();
	}

    @Override
	default RandomAccess<T> randomAccess(Interval interval) {
		return this.data().randomAccess(interval);
	}

}
