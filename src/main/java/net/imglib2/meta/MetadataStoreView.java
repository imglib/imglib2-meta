package net.imglib2.meta;

import net.imglib2.*;
import net.imglib2.transform.integer.Mixed;
import net.imglib2.transform.integer.MixedTransform;

import java.util.Optional;

class MetadataStoreView implements MetadataStore {

	private final MetadataStore source;
	private final MixedTransform transform;
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
	}

	@Override
	public <T> Optional<MetadataItem<T>> get(String key, Class<T> ofType) {
		return source.get(key, ofType);
	}

	@Override
	public <T> Optional<MetadataItem<T>> get(String key, int d, Class<T> ofType) {
		if (dim_map.length <= d) {
			return Optional.empty();
		}
		final int dd = dim_map[d];
		return source.get(key, dd, ofType);
	}

	@Override
	public <T> Optional<VaryingMetadataItem<T, RandomAccessible<T>>> getVarying(String key, int d, Class<T> ofType) {
		if (dim_map.length <= d) {
			return Optional.empty();
		}
		final int dd = dim_map[d];
		return itemView(source.getVarying(key, dd, ofType));
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

	@Override
	public MixedTransform transform() {
		return transform.concatenate(source.transform());
	}

	private <T, U> Optional<VaryingMetadataItem<T, U>> itemView(
		@SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<VaryingMetadataItem<T, U>> result
	) {
		return result.map(item -> new VaryingMetadataItemView<>(item, transform));
	}

	private static class VaryingMetadataItemView<T, U> implements VaryingMetadataItem<T, U> {
		private final VaryingMetadataItem<T, U> source;
		private final Mixed transform;

		public VaryingMetadataItemView(VaryingMetadataItem<T, U> source, Mixed transform) {
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
		public U get() {
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
