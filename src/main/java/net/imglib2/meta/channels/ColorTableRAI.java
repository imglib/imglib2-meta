package net.imglib2.meta.channels;

import net.imagej.display.ColorTables;
import net.imglib2.Interval;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.display.ColorTable;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

class ColorTableHolder {
    private final Map<Integer, ColorTable> map;
    private int idx;

    public ColorTableHolder(final Map<Integer, ColorTable> map, final int idx) {
        this.map = map;
        this.idx = idx;
    }

    public ColorTable get() {
        return map.getOrDefault(this.idx, null);
    }

    void setIdx(int idx) {
        this.idx = idx;
    }

    public void set(ColorTable table) {
        map.put(this.idx, table);

    }
}

class ColorTableRAI implements RandomAccessible<ColorTableHolder> {

    private final Map<Integer, ColorTable> luts = new HashMap<>();

    private class ColorTableRandomAccess extends Point implements RandomAccess<ColorTableHolder> {

        public ColorTableRandomAccess()
        {
            super(1);
        }

        @Override
        public ColorTableHolder get()
        {
            return new ColorTableHolder(luts, getIntPosition(0));
        }

        @Override
        public ColorTableHolder getType()
        {
            return get();
        }

        @Override
        public ColorTableRandomAccess copy()
        {
            return new ColorTableRandomAccess();
        }
    }

    @Override
    public RandomAccess<ColorTableHolder> randomAccess() {
        return new ColorTableRandomAccess();
    }

    @Override
    public RandomAccess<ColorTableHolder> randomAccess(Interval interval) {
        return randomAccess();
    }

    @Override
    public int numDimensions() {
        return 1;
    }

    @Override
    public ColorTableHolder getType() {
        return randomAccess().getType();
    }

    public void setLut(int channel, ColorTable lut) {
        luts.put(channel, lut);
    }
}
