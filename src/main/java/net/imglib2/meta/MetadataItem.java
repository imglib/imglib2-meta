package net.imglib2.meta;

import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.view.MixedTransformView;

public interface MetadataItem<T, U> {

	String name();

	default boolean isAttachedToAxes() { return false; }

	default boolean isAttachedTo(final int d) { return false; }

	T get();

	U getAt(RealLocalizable pos);

	default U getAt(long... pos) {
		return getAt(new Point(pos));
	}

	default U getAt(double... pos) {
		return getAt(new RealPoint(pos));
	}

}
