package net.imglib2.meta;

public interface MetadataItem<T> {

	String name();

	default boolean isAttachedToAxes() { return false; }

	default boolean isAttachedTo(final int d) { return false; }

	T get();

}
