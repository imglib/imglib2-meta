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

import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccessible;
import net.imglib2.transform.integer.Mixed;
import net.imglib2.transform.integer.MixedTransform;
import net.imglib2.view.MixedTransformView;

import java.util.Arrays;
import java.util.function.Supplier;

public class MetadataStoreView implements MetadataStore {

	protected final MetadataStore source;
    protected final MixedTransform transform;
	// We want the inverse of transform.component for slicing
	private final int[] dim_map;

	public MetadataStoreView(MetadataStore source, Mixed transform) {
		if (source instanceof MetadataStoreView) {
			MetadataStoreView msv = (MetadataStoreView) source;
			this.source = msv.source;
			this.transform = msv.transform.concatenate(transform);
		}
		else {
			this.source = source;
			this.transform = new MixedTransform(transform.numSourceDimensions(), transform.numTargetDimensions());
			this.transform.set(transform);
		}

		this.dim_map = new int[ this.transform.numSourceDimensions() ];
		for ( int d = 0; d < this.transform.numTargetDimensions(); ++d )
		{
			if (!this.transform.getComponentZero(d) )
			{
				final int e = this.transform.getComponentMapping(d);
				this.dim_map[ e ] = d;
			}
		}
		for ( int i = this.transform.numTargetDimensions(); i < this.transform.numSourceDimensions(); i++) {
			this.dim_map[i] = i;
		}
	}

	@Override
	public <T> MetadataItem<T> item(String key, Class<T> ofType, int... dims) {
		final int[] dd = new int[dims.length];
		for(int i = 0; i < dd.length; i++) {
			if (dim_map.length <= dims[i]) {
				throw new IllegalArgumentException("Dimensions " + Arrays.toString(dims) + " is not present in the source metadata.");
			}
			dd[i] = dim_map[dims[i]];
		}
		return itemView(source.item(key, ofType, dd));
	}

	@Override
	public <T extends HasMetadataStore> T info(Class<T> infoClass) {
		T srcStore = source.info(infoClass);
		srcStore.setStore(this);
		return srcStore;
	}

	@Override
	public <T> void add(String key, T data, int... dims) {
        // This theoretically would work...but it could have unintended consequences
        // if the caller does not know it is a view. It's probably best to keep it read-only.
        // If it is known to be a view, it's probably feasible to add the metadata to the source directly.
        throw new UnsupportedOperationException("Views on metadata are read-only");
        // TODO: But if we did want to implement it, it would look something like this:
//        int[] targetDims = new int[dims.length];
//        for (int i = 0; i < targetDims.length; i++) {
//            if (dims[i] < 0 || dims[i] >= transform.numTargetDimensions()) {
//                throw new IllegalArgumentException("Dimension " + dims[i] + " out of bounds [0," + transform.numTargetDimensions() + ")");
//            }
//            else {
//                targetDims[i] = transform.getComponentMapping(dims[i]);
//            }
//        }
//        source.add(key, data, targetDims);
	}

	@Override
	public <T> void add(String key, RandomAccessible<T> data, int... dims) {
        // This theoretically would work...but it could have unintended consequences
        // if the caller does not know it is a view. It's probably best to keep it read-only.
        // If it is known to be a view, it's probably feasible to add the metadata to the source directly.
        throw new UnsupportedOperationException("Views on metadata are read-only");
	}

	@Override
	public int numDimensions() {
		return transform.numSourceDimensions();
	}

	private <T> MetadataItem<T> itemView(MetadataItem<T> result ) {
		return new MetadataItemView<>(result, transform);
	}

	private static class MetadataItemView<T> extends MixedTransformView<T> implements MetadataItem<T> {
		private final MetadataItem<T> source;
		private final Mixed transform;

		public MetadataItemView(MetadataItem<T> source, Mixed transform) {
			super(source, transform);
			this.source = source;
			this.transform = transform;
		}

		@Override
		public String name() {
			return source.name();
		}

		@Override
		public boolean isAttachedTo(int... dims) {
			int[] dd = new int[dims.length];
			for(int i = 0; i < dims.length; i++) {
				dd[i] = transform.getComponentMapping(dims[i]);
			}
			return source.isAttachedTo(dd);
		}

		@Override
		public T getAt(Localizable pos) {
			final Point p = new Point(transform.numSourceDimensions());
			transform.apply(pos, p);
			return source.getAt(p);
		}

        @Override
        public T valueOr(T defaultValue) {
            return source.valueOr(defaultValue);
        }

        @Override
        public MetadataItem<T> or(Supplier<MetadataItem<T>> defaultSupplier) {
            // If the source is present, sourceOr will just return itself
            MetadataItem<T> sourceOr = source.or(defaultSupplier);
            if (sourceOr == source) {
                // The source is not absent
                return this;
            }
            else {
                // The source is absent
                return sourceOr;
            }
        }
	}
}
