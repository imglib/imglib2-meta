package net.imglib2.meta.attribution;

import net.imglib2.meta.HasMetadataStore;

public interface Attribution extends HasMetadataStore {
	String author();
	String citation();
}
