package net.imglib2.meta;

import net.imglib2.*;
import net.imglib2.transform.integer.Mixed;
import net.imglib2.transform.integer.MixedTransform;
import net.imglib2.view.MixedTransformView;

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
		for ( int i = this.transform.numTargetDimensions(); i < this.transform.numSourceDimensions(); i++) {
			this.dim_map[i] = i;
		}
	}

	@Override
	public <T> Optional<MetadataItem<T>> get(String key, Class<T> ofType) {
		return source.get(key, ofType);
	}

	@Override
	public <T> Optional<MetadataItem<T>> get(String key, Class<T> ofType, int... d) {
		final int[] dd = new int[d.length];
		for(int i = 0; i < dd.length; i++) {
			if (dim_map.length <= d[i]) {
				return Optional.empty();
			}
			dd[i] = dim_map[d[i]];
		}
		return source.get(key, ofType, dd).map(this::itemView);
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
		return transform.numSourceDimensions();
	}

	@Override
	public MixedTransform transform() {
		return transform.concatenate(source.transform());
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
		public boolean[] attachedAxes() {
			return new boolean[0];
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
