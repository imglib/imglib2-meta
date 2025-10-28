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
package net.imglib2.meta.interval;

import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.meta.Dataset;
import net.imglib2.meta.MetadataStore;
import net.imglib2.transform.integer.MixedTransform;
import net.imglib2.view.RandomAccessibleIntervalCursor;

public interface DatasetInterval<T> extends Dataset<T>, RandomAccessibleInterval<T> {
	RandomAccessibleInterval<T> data();
	IntervaledMetadataStore store();

	static <T> DatasetInterval<T> wrap(RandomAccessibleInterval<T> delegate, IntervaledMetadataStore store) {
		return new DatasetInterval<T>() {
			@Override
			public RandomAccessibleInterval<T> data() {
				return delegate;
			}

			@Override
			public IntervaledMetadataStore store() {
				return store;
			}
		};
	}

    static <T> DatasetInterval<T> wrap(RandomAccessibleInterval<T> delegate) {
        return wrap(delegate, new SimpleIntervaledMetadataStore(delegate));
    }

    static <T> DatasetInterval<T> wrap(RandomAccessibleInterval<T> delegate, MetadataStore store) {
        return wrap(delegate, new MetadataStoreIntervalView(store, delegate));
    }

	/** RandomAccessibleInterval Overrides */


    @Override
    default DatasetIntervalView<T, ?> view() {
        return DatasetIntervalView.wrap(this, new MixedTransform(numDimensions(), numDimensions()), this);
    }

	@Override
	default Cursor< T > cursor()
	{
		return new RandomAccessibleIntervalCursor<>( this.data() );
	}

	@Override
	default Cursor< T > localizingCursor()
	{
		return cursor();
	}

    @Override
    default T getType() {
        return Dataset.super.getType();
    }

    @Override
    default long min( final int d )
    {
        return data().min( d );
    }

    @Override
    default long max( final int d )
    {
        return data().max( d );
    }

}
