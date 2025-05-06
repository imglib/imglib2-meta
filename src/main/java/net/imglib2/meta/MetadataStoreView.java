package net.imglib2.meta;

import net.imglib2.*;
import net.imglib2.transform.integer.Mixed;

import java.util.Optional;

class MetadataStoreView implements MetadataStore {

	private final MetadataStore source;
	private final Mixed transform;
	// We want the inverse of transform.component for slicing
	private final int[] dim_map;

	public MetadataStoreView(MetadataStore source, Mixed transform) {
		this.source = source;
		this.transform = transform;
		this.dim_map = new int[ transform.numSourceDimensions() ];
		for ( int d = 0; d < transform.numTargetDimensions(); ++d )
		{
			if (!transform.getComponentZero(d) )
			{
				final int e = transform.getComponentMapping(d);
				this.dim_map[ e ] = d;
			}
		}
	}

	@Override
	public <T> Optional<MetadataItem<T, T>> get(String key, Class<T> ofType) {
		return itemView(source.get(key, ofType));
	}

	@Override
	public <T> Optional<MetadataItem<T, RandomAccessible<T>>> get(String key, int d, Class<T> ofType) {
		if (dim_map.length <= d) {
			return Optional.empty();
		}
		final int dd = dim_map[d];
		return itemView(source.get(key, dd, ofType));
	}

	@Override
	public <T extends HasMetadataStore> T info(Class<T> infoClass) {
		T srcStore = source.info(infoClass);
		srcStore.setStore(this);
		return srcStore;
	}

	@Override
	public <T> void add(String name, T data, int... dims) {
		throw new UnsupportedOperationException("View of metadata store is read-only");
	}

	@Override
	public <T, U extends RandomAccessible<T>> void add(String name, U data, int... dims) {
		throw new UnsupportedOperationException("View of metadata store is read-only");
	}

	@Override
	public <T, U extends RealRandomAccessible<T>> void add(String name, U data, int... dims) {
		throw new UnsupportedOperationException("View of metadata store is read-only");
	}

	@Override
	public int numDimensions() {
		return source.numDimensions();
	}

	private <T, U> Optional<MetadataItem<T, U>> itemView(
		@SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<MetadataItem<T, U>> result
	) {
		if (!result.isPresent()) return result;
		MetadataItem<T, U> sourceItem = result.get();
		if (!sourceItem.isAttachedToAxes()) return result;
		return Optional.of(new MetadataItemView<>(sourceItem, transform));
	}

	private static class MetadataItemView<T, U> implements MetadataItem<T, U> {
		private final MetadataItem<T, U> source;
		private final Mixed transform;

		public MetadataItemView(MetadataItem<T, U> source, Mixed transform) {
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
		public U getAt(RealLocalizable pos) {
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
