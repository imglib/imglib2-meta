package net.imglib2.meta;

import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.transform.integer.MixedTransform;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.MixedTransformView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

public class SimpleMetadataStore implements MetadataStore {

	private final List<MetadataItem<?>> items;
	private final MixedTransformView<?> view;
	private final int numDims;

	public SimpleMetadataStore(int n) {
		this(new ArrayList<>(), null, n);
	}

	public SimpleMetadataStore(List<MetadataItem<?>> items, MixedTransformView<?> view, int n) {
		this.items = items;
		this.view = view;
		this.numDims = n;
	}

	/**
	 * @param key the metadata key
	 */
	@Override
	@SuppressWarnings({"raw", "unchecked"})
	public Optional<MetadataItem<?>> get(String key) {
		return (Optional) get(key, null);
	}

	/**
	 * @param key the metadata key
	 */
	@Override
	public <T> Optional<MetadataItem<T>> get(String key, Class<T> ofType) {
		//noinspection unchecked
		return items.stream() //
			.filter(item -> item.name().equals(key))
			.filter(item -> !item.isAttachedToAxes())
			.filter(item -> ofType == null || ofType.isInstance(item.get()))
			.map(item -> (MetadataItem<T>) item)
			.findFirst();
	}

	/**
	 * @param key the metadata key
	 * @param d   the axis
	 */
	@Override
	@SuppressWarnings({"raw", "unchecked"})
	public Optional<MetadataItem<?>> get(String key, int d) {
		return (Optional) get(key, d, null);
	}

	@Override
	public <T> Optional<MetadataItem<T>> get(String key, int d, Class<T> ofType) {
		final int dd = view == null ? d : view.getTransformToSource().getComponentMapping(d);
		//noinspection unchecked
		return items.stream() //
			.filter(item -> item.name().equals(key))
			.filter(item -> item.isAttachedTo(dd)) //
			.filter(item -> ofType == null || ofType.isInstance(item.get()))
			.map(item -> (MetadataItem<T>) item)
			.map(item -> view == null ? item : item.view(view))
			.findFirst();
	}

	@Override
	public MetadataStore view(MixedTransformView<DoubleType> v) {
		// TODO: Can we chain them? That'd be a cool trick
		if (view != null)
			throw new UnsupportedOperationException("You must call view() on the original MetaData");
		return new SimpleMetadataStore(items, v, numDims);
	}

	@Override
	public <T extends HasMetaData> T info(Class<T> infoClass) {
		ServiceLoader<T> loader = ServiceLoader.load(infoClass);
		T instance = loader.iterator().next();
		instance.setMetaData(this);
		return instance;
	}

	@Override
	public <T> void add(String name, T data, int... dims) {
		if (dims.length == 1 && dims[0] == 2) {
			System.out.println("We should stop here");
		}
		boolean[] axes = makeAxisAttachmentArray(dims);
		items.add(new SimpleItem<>(name, data, axes));
	}

	@Override
	public <T> void add(String name, RandomAccessible<T> data, int... dims) {
		boolean[] axes = makeAxisAttachmentArray(dims);
		// TODO: What if varying axes and attached axes are not the same?
		items.add(new VaryingItem<>(name, data, axes, axes));
	}

	@Override
	public int numDimensions() {
		return numDims;
	}

	private boolean[] makeAxisAttachmentArray(int... dims) {
		if (dims.length == 0) return null;
		boolean[] attachedToAxes = new boolean[numDims];
		for (int d=0; d<dims.length; d++) attachedToAxes[dims[d]] = true;
		return attachedToAxes;
	}

	private static class SimpleItem<T> implements MetadataItem<T> {
		final String name;

		final T data;

		final boolean[] attachedToAxes;

		public SimpleItem(final String name, final T data) {
			this(name, data, null);
		}

		public SimpleItem(final String name, final T data, final boolean[] attachedToAxes) {
			this.name = name;
			this.data = data;
			this.attachedToAxes = attachedToAxes;
		}

		public boolean isAttachedToAxes() {
			return attachedToAxes != null;
		}

		@Override
		public String name() {
			return name;
		}

		@Override
		public boolean isAttachedTo(int d) {
			return attachedToAxes != null && attachedToAxes[d];
		}

		@Override
		public T get() {
			return data;
		}

		@Override
		public T getAt(final Localizable pos) {
			return get();
		}

		@Override
		public MetadataItem<T> view(MixedTransformView<?> view) {
			return this;
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder("SimpleItem \"");
			sb.append(name);
			sb.append("\"; ");

			if (isAttachedToAxes()) {
				sb.append("attached to axes {");
				final int[] axes = flagsToAxisList(attachedToAxes);
				sb.append(axes[0]);
				for (int i = 1; i < axes.length; ++i)
					sb.append(", " + axes[i]);
				sb.append("}; ");
			} else
				sb.append("not attached to any axis; ");

			sb.append("value = " + data);

			return sb.toString();
		}
	}

	private static class VaryingItem<T> implements MetadataItem<T> {
		final String name;

		final RandomAccessible<T> data;
		final MixedTransformView<?> view;

		final boolean[] variesWithAxes;

		final boolean[] attachedToAxes;

		public VaryingItem(final String name, final RandomAccessible<T> data, final boolean[] variesWithAxes, final boolean[] attachedToAxes) {
			this(name, data, null, variesWithAxes, attachedToAxes);
		}

		private VaryingItem(final String name, final RandomAccessible<T> data, final MixedTransformView<?> view, final boolean[] variesWithAxes, final boolean[] attachedToAxes) {
			this.name = name;
			this.data = data;
			this.view = view;
			this.variesWithAxes = variesWithAxes;
			this.attachedToAxes = attachedToAxes;
		}

		public boolean isAttachedToAxes() {
			return attachedToAxes != null;
		}

		@Override
		public T getAt(final Localizable pos) {
			Localizable src;
			if (view != null) {
				// FIXME: Yuckkkkkkk
				final MixedTransform tform = view.getTransformToSource();
				final Point dest = new Point(tform.numSourceDimensions());
				view.getTransformToSource().apply(pos, dest);
				src = dest;
			} else {
				src = pos;
			}
			final RandomAccess<T> access = data.randomAccess();
			for (int d = 0, i = 0; d < variesWithAxes.length; ++d)
				if (variesWithAxes[d])
					access.setPosition(src.getLongPosition(d), i++);
			return access.get();
		}

		@Override
		public MetadataItem<T> view(MixedTransformView<?> view) {
			return new VaryingItem<>(
				this.name,
				this.data,
				view,
				this.variesWithAxes,
				this.attachedToAxes
			);
		}

		@Override
		public String name() {
			return name;
		}

		@Override
		public boolean isAttachedTo(int d) {
			return attachedToAxes != null && attachedToAxes[d];
		}

		@Override
		public T get() {
			throw new UnsupportedOperationException("Varying item does not support get()");
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder("VaryingItem \"");
			sb.append(name);
			sb.append("\"; ");

			if (isAttachedToAxes()) {
				sb.append("attached to axes {");
				final int[] axes = flagsToAxisList(attachedToAxes);
				sb.append(axes[0]);
				for (int i = 1; i < axes.length; ++i)
					sb.append(", " + axes[i]);
				sb.append("}; ");
			} else
				sb.append("not attached to any axis; ");

			return sb.toString();
		}
	}

	private static int[] flagsToAxisList( final boolean[] flags ) {
		final int[] tmp = new int[ flags.length ];
		int i = 0;
		for ( int d = 0; d < flags.length; ++d )
			if ( flags[ d ] )
				tmp[ i++ ] = d;
		return Arrays.copyOfRange( tmp, 0, i );
	}
}
