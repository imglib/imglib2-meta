package net.imglib2.meta.channels;

import net.imagej.display.ColorTables;
import net.imglib2.Interval;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.display.ColorTable;

import java.util.HashMap;
import java.util.Map;

class ColorTableRAI implements RandomAccessible<ColorTable> {

    private final Map<Integer, ColorTable> luts = new HashMap<>();

    private class ColorTableRandomAccess extends Point implements RandomAccess<ColorTable> {

        public ColorTableRandomAccess()
        {
            super(1);
        }

        @Override
        public ColorTable get()
        {
            return luts.getOrDefault(getIntPosition(0), null);
        }

        @Override
        public ColorTable getType()
        {
            return ColorTables.BLUE;
        }

        @Override
        public ColorTableRandomAccess copy()
        {
            return new ColorTableRandomAccess();
        }
    }

    @Override
    public RandomAccess<ColorTable> randomAccess() {
        return new ColorTableRandomAccess();
    }

    @Override
    public RandomAccess<ColorTable> randomAccess(Interval interval) {
        return randomAccess();
    }

    @Override
    public int numDimensions() {
        return 1;
    }

    @Override
    public ColorTable getType() {
        return randomAccess().getType();
    }

    public void setLut(int channel, ColorTable lut) {
        luts.put(channel, lut);
    }
}
