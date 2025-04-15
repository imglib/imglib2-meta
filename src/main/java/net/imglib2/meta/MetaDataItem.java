package net.imglib2.meta;

import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.view.MixedTransformView;

public interface MetaDataItem<T> {

	String name();

	boolean isAttachedToAxes();

	boolean isAttachedTo(final int d);

	T get();

	T getAt(Localizable pos);

	default T getAt(long... pos) {
		return getAt(new Point(pos));
	}

	MetaDataItem<?> view(MixedTransformView<?> view);

}
