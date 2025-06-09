package net.imglib2.meta;

import net.imglib2.RandomAccessible;
import net.imglib2.view.fluent.RandomAccessibleView;

public class DefaultDataset<T> implements Dataset<T, DefaultDataset<T>> {

	private final RandomAccessible<T> data;
	private final MetadataStore store;

	public DefaultDataset(RandomAccessible<T> data, MetadataStore store) {
		this.data = data;
		this.store = store;
	}
	@Override
	public RandomAccessible<T> data() {
		return data;
	}

	@Override
	public MetadataStore store() {
		return store;
	}
}
