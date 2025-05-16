package net.imglib2.meta;

import net.imglib2.RandomAccessible;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.RealRandomAccessible;
import net.imglib2.realtransform.RealTransform;

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
	public <T> Optional<MetadataItem<T>> get(String key, int d, Class<T> ofType) {
		//throw new UnsupportedOperationException("RealView of metadata store cannot query dimension-specific metadata");
		return source.get(key, d, ofType); // FIXME: Dimensional index might have shifted meaning here.
	}

	@Override
	public <T> Optional<VaryingMetadataItem<T, RandomAccessible<T>>> getVarying(String key, int d, Class<T> ofType) {
		//throw new UnsupportedOperationException("RealView of metadata store cannot query dimension-specific metadata");
		return itemView(source.getVarying(key, d, ofType)); // FIXME: Dimensional index might have shifted meaning here.
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

	private <T, U> Optional<VaryingMetadataItem<T, U>> itemView(
		@SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<VaryingMetadataItem<T, U>> result
	) {
		if (!result.isPresent()) return result;
		VaryingMetadataItem<T, U> sourceItem = result.get();
		if (!sourceItem.isAttachedToAxes()) return result;
		return Optional.of(new MetadataItemRealView<>(sourceItem, transform));
	}

	private static class MetadataItemRealView<T, U> implements VaryingMetadataItem<T, U> {
		private final VaryingMetadataItem<T, U> source;
		private final RealTransform transform;

		public MetadataItemRealView(VaryingMetadataItem<T, U> source, RealTransform transform) {
			this.source = source;
			this.transform = transform;
		}

		@Override
		public String name() {
			return source.name();
		}

		@Override
		public boolean isAttachedToAxes() {
			return source.isAttachedToAxes();
		}

		@Override
		public boolean isAttachedTo(int d) {
			throw new UnsupportedOperationException("RealView of metadata store does not know dimensional axis attachments");
		}

		@Override
		public U get() {
			return source.get();
		}

		@Override
		public T getAt(RealLocalizable pos) {
			final RealPoint p = new RealPoint(transform.numSourceDimensions());
			transform.apply(pos, p);
			return source.getAt(p);
		}
	}
}
