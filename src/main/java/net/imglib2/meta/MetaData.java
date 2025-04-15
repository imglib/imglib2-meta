package net.imglib2.meta;

import net.imglib2.meta.attribution.Attribution;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.MixedTransformView;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// TODO: Make this an interface
public class MetaData {

	private List<MetaDataItem<?>> items;
	private MixedTransformView<?> view;

	public MetaData() {
		this(new ArrayList<>(), null);
	}

	public MetaData(List<MetaDataItem<?>> items, MixedTransformView view) {
		this.items = items;
		this.view = view;
	}

	public List<MetaDataItem<?>> items() {
		return items;
	}

	/**
	 * @param key the metadata key
	 */
	public Optional<MetaDataItem<?>> get(String key) {
		return items.stream() //
			.filter(item -> item.name().equals(key))
			.filter(item -> !item.isAttachedToAxes())
			.findFirst();
	}

	/**
	 * @param key the metadata key
	 */
	public <T> Optional<MetaDataItem<T>> get(String key, Class<T> ofType) {
		//noinspection unchecked
		return items.stream() //
			.filter(item -> item.name().equals(key))
			.filter(item -> !item.isAttachedToAxes())
			.filter(item -> ofType.isInstance(item.get()))
			.map(item -> (MetaDataItem<T>) item)
			.findFirst();
	}

	/**
	 * @param key the metadata key
	 * @param d   the axis
	 */
	public Optional<MetaDataItem<?>> get(String key, int d) {
		final int dd = view == null ? d : view.getTransformToSource().getComponentMapping(d);
		return items.stream() //
			.filter(item -> item.name().equals(key))
			.filter(item -> item.isAttachedTo(dd)) //
			.map(item -> view == null ? item : item.view(view))
			.findFirst();
	}

	public MetaData view(MixedTransformView<DoubleType> v) {
		// TODO: Can we chain them? That'd be a cool trick
		if (this.view != null)
			throw new UnsupportedOperationException("You must call view() on the original MetaData");
		return new MetaData(this.items, v);
	}

	public <T> T info(Class<T> infoClass) {
		// FIXME
		return null;
	}
}
