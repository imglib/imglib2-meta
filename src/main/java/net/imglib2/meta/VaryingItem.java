package net.imglib2.meta;

import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.transform.integer.MixedTransform;
import net.imglib2.view.MixedTransformView;

public class VaryingItem<T> implements MetaDataItem<T> {
	final String name;

	final RandomAccessible<T> data;
	final MixedTransformView<?> view;

	final boolean[] variesWithAxes;

	final boolean[] attachedToAxes;

	public VaryingItem(final String name, final RandomAccessible<T> data, final boolean[] variesWithAxes) {
		this(name, data, variesWithAxes, null);
	}

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
	public MetaDataItem<?> view(MixedTransformView<?> view) {
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
			final int[] axes = MetaViews.flagsToAxisList(attachedToAxes);
			sb.append(axes[0]);
			for (int i = 1; i < axes.length; ++i)
				sb.append(", " + axes[i]);
			sb.append("}; ");
		} else
			sb.append("not attached to any axis; ");

		return sb.toString();
	}
}
