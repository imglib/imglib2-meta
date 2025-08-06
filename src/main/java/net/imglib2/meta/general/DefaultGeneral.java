package net.imglib2.meta.general;

import net.imglib2.meta.MetadataItem;
import net.imglib2.meta.MetadataStore;

public class DefaultGeneral implements General{
    private MetadataStore metaData;

    @Override
    public String name() {
        MetadataItem<String> item = metaData.get("name", String.class).orElse(null);
        return item == null ? null : item.getType();
    }

    @Override
    public void setStore(MetadataStore metaData) {this.metaData = metaData;}
}
