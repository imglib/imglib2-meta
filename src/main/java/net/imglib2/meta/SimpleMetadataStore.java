package net.imglib2.meta;

import net.imglib2.RandomAccessible;
import net.imglib2.RealRandomAccessible;
import net.imglib2.realtransform.RealTransformRealRandomAccessible;
import net.imglib2.view.MixedTransformView;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

public class SimpleMetadataStore implements MetadataStore {

	private final List<MetadataItem<?>> items;
	private final RealTransformRealRandomAccessible<?,?> realView;
	private final int numDims;

	public SimpleMetadataStore(int n) {
		this.items = new ArrayList<>();
		this.realView = null;
		this.numDims = n;
	}

	public SimpleMetadataStore(List<MetadataItem<?>> items, MixedTransformView<?> view, int n) {
		this.items = items;
		this.realView = null;
		this.numDims = n;
	}

	public SimpleMetadataStore(List<MetadataItem<?>> items, RealTransformRealRandomAccessible<?, ?> realView, int n) {
		this.items = items;
		this.realView = realView;
		this.numDims = n;
	}

	@Override
	public <T> Optional<MetadataItem<T>> get(String name, Class<T> ofType) {
		//noinspection unchecked
		return items.stream() //
			.filter(item -> item.name().equals(name))
			.filter(item -> !item.isAttachedToAxes())
			.filter(item -> ofType == null || ofType.isInstance(item.get()))
			.map(item -> (MetadataItem<T>) item)
			.findFirst();
	}

	@Override
	public <T> Optional<MetadataItem<T>> get(String name, int d, Class<T> ofType) {
		//noinspection unchecked
		return items.stream() //
			.filter(item -> item.name().equals(name))
			.filter(item -> item.isAttachedTo(d)) //
			.filter(item -> ofType == null || ofType.isInstance(item.get()))
			.map(item -> (MetadataItem<T>) item)
			.findFirst();
	}

	@Override
	public <T extends HasMetadataStore> T info(Class<T> infoClass) {
		ServiceLoader<T> loader = ServiceLoader.load(infoClass);
		T instance = loader.iterator().next();
		instance.setStore(this);
		return instance;
	}

	@Override
	public <T> void add(String name, T data, int... dims) {
		items.add(Metadata.item(name, data, numDims, dims));
	}

	@Override
	public <T> void add(String name, RandomAccessible<T> data, int... dims) {
		items.add(Metadata.item(name, data, numDims, dims));
	}

	@Override
	public <T> void add(String name, RealRandomAccessible<T> data, int... dims) {
		items.add(Metadata.item(name, data, numDims, dims));
	}

	@Override
	public int numDimensions() {
		return numDims;
	}

}
