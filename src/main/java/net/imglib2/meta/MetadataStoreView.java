package net.imglib2.meta;

import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccessible;
import net.imglib2.RealLocalizable;
import net.imglib2.RealRandomAccessible;
import net.imglib2.transform.integer.MixedTransform;
import net.imglib2.view.MixedTransformView;

import java.util.Optional;

public class MetadataStoreView implements MetadataStore {

	private final MetadataStore source;
	private final MixedTransform transform;

	public MetadataStoreView(MetadataStore source, MixedTransformView<?> view) {
		this(source, view.getTransformToSource());
	}

	public MetadataStoreView(MetadataStore source, MixedTransform transform) {
		this.source = source;
		this.transform = transform;
	}

	@Override
	public <T> Optional<MetadataItem<T>> get(String key, Class<T> ofType) {
		return itemView(source.get(key, ofType));
	}

	@Override
	public <T> Optional<MetadataItem<T>> get(String key, int d, Class<T> ofType) {
		final int dd = transform.getComponentMapping(d);
		return itemView(source.get(key, dd, ofType));
	}

	@Override
	public <T extends HasMetadataStore> T info(Class<T> infoClass) {
		return source.info(infoClass);
	}

	@Override
	public <T> void add(String name, T data, int... dims) {
		throw new UnsupportedOperationException("View of metadata store is read-only");
	}

	@Override
	public <T> void add(String name, RandomAccessible<T> data, int... dims) {
		throw new UnsupportedOperationException("View of metadata store is read-only");
	}

	@Override
	public <T> void add(String name, RealRandomAccessible<T> data, int... dims) {
		throw new UnsupportedOperationException("View of metadata store is read-only");
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
		return Optional.of(new MetadataItemView<>(sourceItem, transform));
	}

	private static class MetadataItemView<T> implements MetadataItem<T> {
		private final MetadataItem<T> source;
		private final MixedTransform transform;

		public MetadataItemView(MetadataItem<T> source, MixedTransform transform) {
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
			return source.isAttachedTo(transform.getComponentMapping(d));
		}

		@Override
		public T get() {
			return source.get();
		}

		@Override
		public T getAt(RealLocalizable pos) {
			if (!(pos instanceof Localizable)) {
				throw new UnsupportedOperationException("Cannot use non-Localizable with metadata store view. YET!");
			}
			Localizable l = (Localizable) pos;
			final Point p = new Point(transform.numSourceDimensions());
			transform.apply(l, p);
			return source.getAt(p);
		}
	}
}
