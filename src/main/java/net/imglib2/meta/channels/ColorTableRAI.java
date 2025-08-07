package net.imglib2.meta.channels;

import net.imglib2.Interval;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.display.ColorTable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ColorTableRAI implements RandomAccessible<ColorTableHolder> {

    private final Map<Integer, ColorTable> luts = new HashMap<>();

    public ColorTableRAI() {}

    public ColorTableRAI(List<ColorTable> tables) {
        for(int i = 0; i < tables.size(); i++) {
            luts.put(i, tables.get(i));
        }
    }

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
