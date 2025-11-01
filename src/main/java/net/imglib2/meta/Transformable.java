package net.imglib2.meta;

import net.imglib2.transform.integer.Mixed;

public interface Transformable<T> {

    T view(Mixed transform, int... srcAxes);

}
