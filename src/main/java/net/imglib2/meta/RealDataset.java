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
import net.imglib2.RealRandomAccessible;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.transform.integer.Mixed;
import net.imglib2.view.fluent.RealRandomAccessibleView;

import java.util.function.Function;
import java.util.function.Supplier;

public interface RealDataset<T, V extends RealDataset<T, V>> extends Dataset<T, V>, RealRandomAccessibleView<T, V> {
    RealRandomAccessible<T> data();
    RealMetadataStore store();

    @Override
    default RealRandomAccessible<T> delegate() {
        return data();
    }

    static <T, V extends RealDataset<T, V>> RealDataset<T, ?> wrap(RealRandomAccessible<T> delegate, RealMetadataStore store) {
        return new RealDataset<T, V>() {
            @Override
            public RealRandomAccessible<T> data() {
                return delegate;
            }

            @Override
            public RealMetadataStore store() {
                return store;
            }
        };
    }

    static <T, V extends RealDataset<T, V>> RealDataset<T, ?> wrap(RealRandomAccessible<T> delegate, MetadataStore store) {
        return wrap(delegate, new MetadataStoreRealView(store));
    }

    static <T> RealDataset<T, ?> wrap(RealDataset<T, ?> dataset, Mixed tform) {
        throw new UnsupportedOperationException("TODO");
        // TODO: Wrap up the MetadataStore in a RealMetadataStore, somehow??
//        return wrap(
//                dataset.delegate(),
//                new MetadataStoreRealView(dataset.store(), tform)
//        );
    }

    @Override
    default Dataset< T, ? > raster()
    {
        return this;
    }

    @Override
    default < U > RealDataset< U, ? > convert(
            final Supplier< U > targetSupplier ,
            final Converter< ? super T, ? super U > converter)
    {
        return wrap( Converters.convert2( delegate(), converter, targetSupplier ), store() );
    }

    @Override
    default < U > RealDataset< U, ? > convert(
            final Supplier< U > targetSupplier,
            final Supplier< Converter< ? super T, ? super U > > converterSupplier)
    {
        return wrap( Converters.convert2( delegate(), converterSupplier, targetSupplier ) , store() );
    }

    default T getType() {
        return this.delegate().getType();
    }

    @Override
    default int numDimensions() {
        return this.delegate().numDimensions();
    }

    @Override
    default RandomAccess<T> randomAccess() {
        return this.delegate().randomAccess();
    }

    @Override
    default RandomAccess<T> randomAccess(Interval interval) {
        return this.delegate().randomAccess(interval);
    }

    @Override
    default < U > U use( Function< ? super V, U > function )
    {
        return function.apply( (V) this );
    }


}
