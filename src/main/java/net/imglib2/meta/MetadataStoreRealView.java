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
	public <T> Optional<MetadataItem<T>> get(String key, Class<T> ofType) {
		return source.get(key, ofType);
	}

	@Override
	public <T> Optional<MetadataItem<T>> get(String key, Class<T> ofType, int... d) {
		//throw new UnsupportedOperationException("RealView of metadata store cannot query dimension-specific metadata");
		return source.get(key, ofType, d).map(this::itemView); // FIXME: Dimensional index might have shifted meaning here.
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
			return super.getAt(pos);
		}

		@Override
		public T getAt(float... position) {
			return super.getAt(position);
		}

		@Override
		public T getAt(double... position) {
			return super.getAt(position);
		}

		@Override
		public T getAt(RealLocalizable position) {
			return super.getAt(position);
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
