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

import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.meta.MetadataItem;
import net.imglib2.meta.MetadataStore;
import net.imglib2.meta.MetadataStoreView;
import net.imglib2.meta.real.RealMetadataStore;
import net.imglib2.transform.integer.Mixed;
import net.imglib2.transform.integer.MixedTransform;

import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * A {@link RealMetadataStore} wrapping a {@link MetadataStore}
 */
class MetadataStoreIntervalView extends MetadataStoreView implements IntervaledMetadataStore {

    private final Interval interval;

    public MetadataStoreIntervalView(final MetadataStore source, Interval interval) {
        this(
            source, //
            new MixedTransform(source.numDimensions(), source.numDimensions()), //
            interval //
        );
    }

	public MetadataStoreIntervalView(final MetadataStore source, final Mixed transform, Interval interval) {
        super(source, transform);
        this.interval = interval;
	}

    @Override
    public Collection<? extends IntervaledMetadataItem<?>> items() {
        return source.items().stream() //
            .map(item -> new MetadataItemIntervalView<>(item, interval)) //
            .collect(Collectors.toList());
    }

    @Override
	public <T> IntervaledMetadataItem<T> item(String key, Class<T> ofType) {
        MetadataItem<T> tformed = super.item(key, ofType);
		return new MetadataItemIntervalView<>(tformed, interval);
	}

	@Override
	public <T> IntervaledMetadataItem<T> item(String key, Class<T> ofType, int... dims) {
        MetadataItem<T> tformed = super.item(key, ofType, dims);
        return new MetadataItemIntervalView<>(tformed, interval);
	}

	@Override
	public <T> void add(String key, T data, int... dims) {
        // This theoretically would work...but it could have unintended consequences
        // if the caller does not know it is a view. It's probably best to keep it read-only.
        // If it is known to be a view, it's probably feasible to add the metadata to the source directly.
        throw new UnsupportedOperationException("Views on metadata are read-only");
	}

    @Override
    public <T> void add(String name, RandomAccessibleInterval<T> data, int... dims) {
        // This theoretically would work...but it could have unintended consequences
        // if the caller does not know it is a view. It's probably best to keep it read-only.
        // If it is known to be a view, it's probably feasible to add the metadata to the source directly.
        throw new UnsupportedOperationException("Views on metadata are read-only");
    }

    @Override
	public int numDimensions() {
		return transform.numSourceDimensions();
	}

	private static class MetadataItemIntervalView<T> implements IntervaledMetadataItem<T> {
		private final MetadataItem<T> source;
        private final Interval interval;

		public MetadataItemIntervalView(MetadataItem<T> source, Interval interval) {
			this.source = source;
            this.interval = interval;
		}

		@Override
		public String name() {
			return source.name();
		}

        @Override
		public boolean isAttachedTo(int... dims) {
			throw new UnsupportedOperationException("RealView of metadata store does not know dimensional axis attachments");
		}

        @Override
        public long min(int d) {
            return interval.min(d);
        }

        @Override
        public long max(int d) {
            return interval.max(d);
        }

        @Override
        public RandomAccess<T> randomAccess() {
            return source.randomAccess();
        }

        @Override
        public RandomAccess<T> randomAccess(Interval interval) {
            return source.randomAccess(interval);
        }

        @Override
        public T valueOr(T defaultValue) {
            return source.valueOr(defaultValue);
        }

        @Override
        public MetadataItem<T> or(Supplier<MetadataItem<T>> defaultSupplier) {
            // Return this, unless what we're wrapping is absent
            MetadataItem<T> sourceOr = source.or(defaultSupplier);
            if (sourceOr == source) {
                return this;
            }
            return sourceOr;
        }

        @Override
        public int numDimensions() {
            return source.numDimensions();
        }
    }
}
