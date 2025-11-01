package net.imglib2.meta;

public interface Subsampleable<T> {

    T view(long[] steps, int... attachedAxes);
}
