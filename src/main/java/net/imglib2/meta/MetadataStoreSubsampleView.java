package net.imglib2.meta;

import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccessible;
import net.imglib2.RealLocalizable;
import net.imglib2.RealRandomAccessible;
import net.imglib2.transform.integer.Mixed;
import net.imglib2.transform.integer.MixedTransform;
import net.imglib2.view.SubsampleView;

import java.util.Optional;

public class MetadataStoreSubsampleView implements MetadataStore {

	private final MetadataStore source;
	private final long[] steps;

	public MetadataStoreSubsampleView(MetadataStore store, long[] steps) {
		if (store.numDimensions() != steps.length) throw new IllegalArgumentException("BAD");
		this.source = store;
		this.steps = steps;
	}

	@Override
	public <T> Optional<MetadataItem<T>> get(String key, Class<T> ofType) {
		return source.get(key, ofType);
	}

	@Override
	public <T> Optional<MetadataItem<T>> get(String key, int d, Class<T> ofType) {
		return source.get(key, d, ofType);
	}

	@Override
	public <T> Optional<VaryingMetadataItem<T, RandomAccessible<T>>> getVarying(String key, int d, Class<T> ofType) {
		return itemView(source.getVarying(key, d, ofType));
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
		throw new UnsupportedOperationException("FIXME");
	}

	private <T, U extends RandomAccessible<T>> Optional<VaryingMetadataItem<T, U>> itemView(
		@SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<VaryingMetadataItem<T, U>> result
	) {
		return result.map(item -> new MetadataStoreSubsampleView.VaryingMetadataItemSubsampleView<>(item, steps));
	}

	private static class VaryingMetadataItemSubsampleView<T, U extends RandomAccessible<T>> implements VaryingMetadataItem<T, U> {
		private final VaryingMetadataItem<T, U> source;
		private final SubsampleView<T> transform;

		public VaryingMetadataItemSubsampleView(VaryingMetadataItem<T, U> source, long[] steps) {
			this.source = source;
			// FIXME: The RA here is only <=N-dimensional not necessarily N-dimensional.
			this.transform = new SubsampleView<T>(source.get(), steps);
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
			return source.isAttachedTo(d);
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

			// TODO: Don't create a new Point every time. ThreadLocal?
			final Point p = new Point(transform.getSteps().length);
			for(int i = 0; i < l.numDimensions(); i++) {
				p.setPosition(l.getLongPosition(i) * transform.getSteps()[i], i);
			}
			return source.getAt(p);
		}
	}
}
