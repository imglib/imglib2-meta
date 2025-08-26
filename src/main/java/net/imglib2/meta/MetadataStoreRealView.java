///*-
// * #%L
// * Metadata for ImgLib2
// * %%
// * Copyright (C) 2016 - 2025 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
// * John Bogovic, Albert Cardona, Barry DeZonia, Christian Dietz, Jan Funke,
// * Aivar Grislis, Jonathan Hale, Grant Harris, Stefan Helfrich, Mark Hiner,
// * Martin Horn, Steffen Jaensch, Lee Kamentsky, Larry Lindsey, Melissa Linkert,
// * Mark Longair, Brian Northan, Nick Perry, Curtis Rueden, Johannes Schindelin,
// * Gabriel Selzer, Jean-Yves Tinevez and Michael Zinsmaier.
// * %%
// * Redistribution and use in source and binary forms, with or without
// * modification, are permitted provided that the following conditions are met:
// *
// * 1. Redistributions of source code must retain the above copyright notice,
// *    this list of conditions and the following disclaimer.
// * 2. Redistributions in binary form must reproduce the above copyright notice,
// *    this list of conditions and the following disclaimer in the documentation
// *    and/or other materials provided with the distribution.
// *
// * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
// * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// * POSSIBILITY OF SUCH DAMAGE.
// * #L%
// */
//package net.imglib2.meta;
//
//import net.imglib2.*;
//import net.imglib2.interpolation.randomaccess.NearestNeighborInterpolatorFactory;
//import net.imglib2.realtransform.RealTransform;
//import net.imglib2.realtransform.RealTransformRealRandomAccessible;
//import net.imglib2.view.RandomAccessibleOnRealRandomAccessible;
//import net.imglib2.view.Views;
//import net.imglib2.view.fluent.RandomAccessibleView;
//
//import java.util.Optional;
//
///**
// * A {@link RealMetadataStore} wrapping a {@link MetadataStore}
// */
//class MetadataStoreRealView implements RealMetadataStore {
//
//	private final MetadataStore source;
//	private final RealTransform transform;
//
//	public MetadataStoreRealView(MetadataStore source, RealTransform transform) {
//		this.source = source;
//		this.transform = transform;
//	}
//
//	@Override
//	public <T> RealMetadataItem<T> item(String key, Class<T> ofType) {
//		return new MetadataItemRealView<>(source.item(key, ofType), transform);
//	}
//
//	@Override
//	public <T> RealMetadataItem<T> item(String key, Class<T> ofType, int... d) {
//		throw new UnsupportedOperationException("RealView of metadata store cannot query dimension-specific metadata");
////		return new MetadataItemRealView<>(source.item(key, ofType, d), transform);
//	}
//
//	@Override
//	public <T extends HasMetadataStore> T info(Class<T> infoClass) {
//		return source.info(infoClass);
//	}
//
//	@Override
//	public <T> void add(String name, T data, int... dims) {
//		throw new UnsupportedOperationException("RealView of metadata store is read-only");
//	}
//
//	@Override
//	public <T> void add(String name, RealRandomAccessible<T> data, int... dims) {
//		throw new UnsupportedOperationException("RealView of metadata store is read-only");
//	}
//
//	@Override
//	public int numDimensions() {
//		return source.numDimensions();
//	}
//
//	private static class MetadataItemRealView<T, R extends RealTransform> extends RealTransformRealRandomAccessible<T, R> implements RealMetadataItem<T> {
//		private final MetadataItem<T> source;
//		private final R transform;
//
//		public MetadataItemRealView(MetadataItem<T> source, R transform) {
//			// FIXME: Poor RRA construction
//			super(Views.interpolate(source, new NearestNeighborInterpolatorFactory<>()), transform);
//			this.source = source;
//			this.transform = transform;
//		}
//
//		@Override
//		public String name() {
//			return source.name();
//		}
//
//		@Override
//		public boolean[] attachedAxes() {
//			throw new UnsupportedOperationException("TODO Lol");
//		}
//
//		@Override
//		public boolean isAttachedTo(int... dims) {
//			throw new UnsupportedOperationException("RealView of metadata store does not know dimensional axis attachments");
//		}
//		@Override
//		public RandomAccess<T> randomAccess() {
//			// FIXME: Wrong
//			return RandomAccessibleOnRealRandomAccessible
//		}
//
//		@Override
//		public RandomAccess<T> randomAccess(Interval interval) {
//			return new RealTransformRandomAccess();
//		}
//
//		public class RealTransformRandomAccess extends Point implements RandomAccess<T> {
//			protected final RandomAccess<T> sourceAccess;
//			protected final RealTransform transformCopy;
//
//			protected RealTransformRandomAccess() {
//				super(MetadataItemRealView.this.transformToSource.numSourceDimensions());
//				this.sourceAccess = MetadataItemRealView.this.source.randomAccess();
//				this.transformCopy = MetadataItemRealView.this.transformToSource.copy();
//			}
//
//			private RealTransformRandomAccess(MetadataItemRealView<T, R>.RealTransformRandomAccess a) {
//				super(a);
//				this.sourceAccess = a.sourceAccess.copy();
//				this.transformCopy = a.transformCopy.copy();
//			}
//
//			@Override
//			public T get() {
//				// FIXME: Rasterization strategy
//				// FIXME: Reuse points?
////				return
////				Point point = new Point(this);
////                for (int i = 0; i < this.numDimensions(); i++) {
////                    long
////                    point.setPosition
////                }
////				this.transformCopy.apply(this, point);
////				return MetadataItemRealView.this.source.getAt(point.);
//			}
//
//			public T getType() {
//				return MetadataItemRealView.this.source.getType();
//			}
//
//			public MetadataItemRealView<T, R>.RealTransformRandomAccess copy() {
//				return MetadataItemRealView.this.new RealTransformRandomAccess(this);
//			}
//
//			@Override
//			public long getLongPosition(int d) {
//				return 0;
//			}
//		}
//	}
//}
