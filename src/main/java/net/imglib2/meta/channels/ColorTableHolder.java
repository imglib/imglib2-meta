package net.imglib2.meta.channels;

import net.imglib2.display.ColorTable;

import java.util.Map;
import java.util.NoSuchElementException;

/**
 * TODO: Remove this class
 * This will require some metadata setter, or a MutableMetadataItem class or something...
 */
public class ColorTableHolder {
    private final Map<Integer, ColorTable> map;
    private int idx;

    public ColorTableHolder(final Map<Integer, ColorTable> map, final int idx) {
        this.map = map;
        this.idx = idx;
    }

    public ColorTable get() {
        if (!map.containsKey(this.idx)) {
            throw new NoSuchElementException();
        }
        return map.get(this.idx);
    }

    void setIdx(int idx) {
        this.idx = idx;
    }

    public void set(ColorTable table) {
        map.put(this.idx, table);

    }
}
