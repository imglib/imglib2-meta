package net.imglib2.meta;

import net.imglib2.Localizable;
import net.imglib2.view.MixedTransformView;

public class SimpleItem<T> implements MetaDataItem<T> {
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
	public MetaDataItem<?> view(MixedTransformView<?> view) {
		return this;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("SimpleItem \"");
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

		sb.append("value = " + data);

		return sb.toString();
	}
}
