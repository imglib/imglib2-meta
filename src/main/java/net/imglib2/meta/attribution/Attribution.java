package net.imglib2.meta.attribution;

import net.imglib2.meta.HasMetaData;

public interface Attribution extends HasMetaData {
	String author();
	String citation();
}
