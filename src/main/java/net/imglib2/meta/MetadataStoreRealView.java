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

import net.imglib2.*;
import net.imglib2.interpolation.randomaccess.NearestNeighborInterpolatorFactory;
import net.imglib2.realtransform.RealTransform;
import net.imglib2.realtransform.RealTransformRealRandomAccessible;
import net.imglib2.view.Views;

import java.util.Optional;

class MetadataStoreRealView implements MetadataStore {

	private final MetadataStore source;
	private final RealTransform transform;

	public MetadataStoreRealView(MetadataStore source, RealTransform transform) {
		this.source = source;
		this.transform = transform;
	}

	@Override
	public <T> Optional<MetadataItem<T>> item(String key, Class<T> ofType) {
		return source.item(key, ofType);
	}

	@Override
	public <T> Optional<MetadataItem<T>> item(String key, Class<T> ofType, int... d) {
		//throw new UnsupportedOperationException("RealView of metadata store cannot query dimension-specific metadata");
		return source.item(key, ofType, d).map(this::itemView); // FIXME: Dimensional index might have shifted meaning here.
	}

	@Override
	public <T extends HasMetadataStore> T info(Class<T> infoClass) {
		return source.info(infoClass);
	}

	@Override
	public <T> void add(String name, T data, int... dims) {
		throw new UnsupportedOperationException("RealView of metadata store is read-only");
	}

	@Override
	public <T, U extends RandomAccessible<T>> void add(String name, U data, int... dims) {
		throw new UnsupportedOperationException("RealView of metadata store is read-only");
	}

	@Override
	public <T, U extends RealRandomAccessible<T>> void add(String name, U data, int... dims) {
		throw new UnsupportedOperationException("RealView of metadata store is read-only");
	}

	@Override
	public int numDimensions() {
		return source.numDimensions();
	}

	private <T> MetadataItem<T> itemView(MetadataItem<T> result) {
		if (!result.isAttachedToAnyAxis()) return result;
		return new MetadataItemRealView<>(result, transform);
	}

	private static class MetadataItemRealView<T, R extends RealTransform> extends RealTransformRealRandomAccessible<T, R> implements MetadataItem<T> {
		private final MetadataItem<T> source;
		private final R transform;

		public MetadataItemRealView(MetadataItem<T> source, R transform) {
			// FIXME: Poor RRA construction
			super(Views.interpolate(source, new NearestNeighborInterpolatorFactory<>()), transform);
			this.source = source;
			this.transform = transform;
		}

		@Override
		public String name() {
			return source.name();
		}

		@Override
		public boolean[] attachedAxes() {
			throw new UnsupportedOperationException("TODO Lol");
		}

		@Override
		public boolean isAttachedTo(int... dims) {
			throw new UnsupportedOperationException("RealView of metadata store does not know dimensional axis attachments");
		}

		@Override
		public T getAt(long... pos) {
			return getAt(pos);
		}

		@Override
		public T getAt(float... position) {
			return super.getAt(position);
		}

		@Override
		public T getAt(double... position) {
			return getAt(position);
		}

		@Override
		public T getAt(RealLocalizable position) {
			return getAt(position);
		}

		@Override
		public RandomAccess<T> randomAccess() {
			return new RealTransformRandomAccess();
		}

		@Override
		public RandomAccess<T> randomAccess(Interval interval) {
			return new RealTransformRandomAccess();
		}

		public class RealTransformRandomAccess extends Point implements RandomAccess<T> {
			protected final RandomAccess<T> sourceAccess;
			protected final RealTransform transformCopy;

			protected RealTransformRandomAccess() {
				super(MetadataItemRealView.this.transformToSource.numSourceDimensions());
				this.sourceAccess = MetadataItemRealView.this.source.randomAccess();
				this.transformCopy = MetadataItemRealView.this.transformToSource.copy();
			}

			private RealTransformRandomAccess(MetadataItemRealView<T, R>.RealTransformRandomAccess a) {
				super(a);
				this.sourceAccess = a.sourceAccess.copy();
				this.transformCopy = a.transformCopy.copy();
			}

			@Override
			public T get() {
				// FIXME: Reuse points?
				RealPoint point = new RealPoint(this);
				this.transformCopy.apply(this, point);
				return MetadataItemRealView.this.source.getAt(point);
			}

			public T getType() {
				return MetadataItemRealView.this.source.getType();
			}

			public MetadataItemRealView<T, R>.RealTransformRandomAccess copy() {
				return MetadataItemRealView.this.new RealTransformRandomAccess(this);
			}

			@Override
			public long getLongPosition(int d) {
				return 0;
			}
		}
	}
}
