package net.imglib2.meta;

import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RealLocalizable;
import net.imglib2.RealRandomAccess;
import net.imglib2.RealRandomAccessible;
import net.imglib2.meta.attribution.Attribution;
import net.imglib2.meta.calibration.Calibration;
import net.imglib2.realtransform.RealTransform;
import net.imglib2.realtransform.RealTransformRealRandomAccessible;
import net.imglib2.transform.integer.MixedTransform;
import net.imglib2.view.MixedTransformView;

import java.util.Arrays;

public final class Metadata {

	private Metadata() { }

	public static Attribution attribution(MetadataStore store) {
		return store.info(Attribution.class);
	}

	public static Calibration calibration(MetadataStore store) {
		return store.info(Calibration.class);
	}

	public static <T> MetadataItem<T> item(String name, T data, int numDims, int... dims) {
		boolean[] axes = makeAxisAttachmentArray(numDims, dims);
		return new SimpleItem<>(name, data, axes);
	}

	public static <T> MetadataItem<T> item(String name, RandomAccessible<T> data, int numDims, int... dims) {
		boolean[] axes = makeAxisAttachmentArray(numDims, dims);
		// TODO: What if varying axes and attached axes are not the same?
		return new VaryingItem<>(name, data, axes, axes);
	}

	public static <T> MetadataItem<T> item(String name, RealRandomAccessible<T> data, int numDims, int... dims) {
		boolean[] axes = makeAxisAttachmentArray(numDims, dims);
		// TODO: What if varying axes and attached axes are not the same?
		return new VaryingRealItem<>(name, data, axes, axes);
	}

	public static MetadataStore view(MetadataStore source, MixedTransformView<?> view) {
		return view(source, view.getTransformToSource());
	}

	public static MetadataStore view(MetadataStore source, MixedTransform transform) {
		return new MetadataStoreView(source, transform);
	}

	public static MetadataStore view(MetadataStore source, RealTransformRealRandomAccessible<?,?> view) {
		return view(source, view.getTransformToSource());
	}

	public static MetadataStore view(MetadataStore source, RealTransform transform) {
		return new MetadataStoreRealView(source, transform);
	}

	private static boolean[] makeAxisAttachmentArray(int numDims, int... dims) {
		if (dims.length == 0) return null;
		boolean[] attachedToAxes = new boolean[numDims];
		for (int d=0; d<dims.length; d++) attachedToAxes[dims[d]] = true;
		return attachedToAxes;
	}

	private static int[] flagsToAxisList( final boolean[] flags ) {
		final int[] tmp = new int[ flags.length ];
		int i = 0;
		for ( int d = 0; d < flags.length; ++d )
			if ( flags[ d ] )
				tmp[ i++ ] = d;
		return Arrays.copyOfRange( tmp, 0, i );
	}

	private static class SimpleItem<T> implements MetadataItem<T> {
		final String name;

		final T data;

		final boolean[] attachedToAxes;

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
		public T getAt(final RealLocalizable pos) {
			return get();
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
		public T getAt(final RealLocalizable pos) {
			final RandomAccess<T> access = data.randomAccess();
			for (int d = 0, i = 0; d < variesWithAxes.length; ++d)
				if (variesWithAxes[d])
					access.setPosition((long) pos.getDoublePosition(d), i++);
			return access.get();
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
					sb.append(", ").append(axes[i]);
				sb.append("}; ");
			} else
				sb.append("not attached to any axis; ");

			return sb.toString();
		}
	}

	private static class VaryingRealItem<T> implements MetadataItem<T> {
		final String name;

		final RealRandomAccessible<T> data;

		final boolean[] variesWithAxes;

		final boolean[] attachedToAxes;

		public VaryingRealItem(final String name, final RealRandomAccessible<T> data, final boolean[] variesWithAxes, final boolean[] attachedToAxes) {
			this.name = name;
			this.data = data;
			this.variesWithAxes = variesWithAxes;
			this.attachedToAxes = attachedToAxes;
		}

		public boolean isAttachedToAxes() {
			return attachedToAxes != null;
		}

		@Override
		public T getAt(final RealLocalizable pos) {
			final RealRandomAccess<T> access = data.realRandomAccess();
			for (int d = 0, i = 0; d < variesWithAxes.length; ++d)
				if (variesWithAxes[d])
					access.setPosition(pos.getDoublePosition(d), i++);
			return access.get();
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
			throw new UnsupportedOperationException("Varying real item does not support get()");
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder("VaryingRealItem \"");
			sb.append(name);
			sb.append("\"; ");

			if (isAttachedToAxes()) {
				sb.append("attached to axes {");
				final int[] axes = flagsToAxisList(attachedToAxes);
				sb.append(axes[0]);
				for (int i = 1; i < axes.length; ++i)
					sb.append(", ").append(axes[i]);
				sb.append("}; ");
			} else
				sb.append("not attached to any axis; ");

			return sb.toString();
		}
	}
}
