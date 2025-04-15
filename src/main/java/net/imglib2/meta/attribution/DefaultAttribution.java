package net.imglib2.meta.attribution;

import net.imglib2.meta.MetadataItem;
import net.imglib2.meta.MetadataStore;

public class DefaultAttribution implements Attribution {
	private MetadataStore metaData;

	@Override
	public void setMetaData(MetadataStore metaData) {
		this.metaData = metaData;
	}

	@Override
	public String author() {
		MetadataItem<String> item = metaData.get("author", String.class).orElse(null);
		return item == null ? null : item.get();
	}

	@Override
	public String citation() {
		MetadataItem<String> item = metaData.get("citation", String.class).orElse(null);
		return item == null ? null : item.get();
	}
}
