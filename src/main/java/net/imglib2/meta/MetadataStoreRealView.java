package net.imglib2.meta;

import net.imglib2.RandomAccessible;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.RealRandomAccessible;
import net.imglib2.realtransform.RealTransform;
import net.imglib2.realtransform.RealTransformRealRandomAccessible;

import java.util.Optional;

public class MetadataStoreRealView implements MetadataStore {

	private final MetadataStore source;
	private final RealTransform transform;

	public MetadataStoreRealView(MetadataStore source, RealTransformRealRandomAccessible<?,?> view) {
		this(source, view.getTransformToSource());
	}

	public MetadataStoreRealView(MetadataStore source, RealTransform transform) {
		this.source = source;
		this.transform = transform;
	}

	@Override
	public <T> Optional<MetadataItem<T>> get(String key, Class<T> ofType) {
		Optional<MetadataItem<T>> result = source.get(key, ofType);
		return itemView(result);
	}

	@Override
	public <T> Optional<MetadataItem<T>> get(String key, int d, Class<T> ofType) {
		//throw new UnsupportedOperationException("RealView of metadata store cannot query dimension-specific metadata");
		return itemView(source.get(key, d, ofType)); // FIXME: Dimensional index might have shifted meaning here.
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
	public <T> void add(String name, RandomAccessible<T> data, int... dims) {
		throw new UnsupportedOperationException("RealView of metadata store is read-only");
	}

	@Override
	public <T> void add(String name, RealRandomAccessible<T> data, int... dims) {
		throw new UnsupportedOperationException("RealView of metadata store is read-only");
	}

	@Override
	public int numDimensions() {
		return source.numDimensions();
	}

	private <T> Optional<MetadataItem<T>> itemView(
		@SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<MetadataItem<T>> result
	) {
		if (!result.isPresent()) return result;
		MetadataItem<T> sourceItem = result.get();
		if (!sourceItem.isAttachedToAxes()) return result;
		return Optional.of(new MetadataItemRealView<>(sourceItem, transform));
	}

	private static class MetadataItemRealView<T> implements MetadataItem<T> {
		private final MetadataItem<T> source;
		private final RealTransform transform;

		public MetadataItemRealView(MetadataItem<T> source, RealTransform transform) {
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
		public T get() {
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
