package net.imglib2.meta;

import net.imglib2.RandomAccessible;
import net.imglib2.meta.calibration.AxisType;
import net.imglib2.type.numeric.real.DoubleType;

public interface Axis {

    default double calibrated(final double raw){
        return data().getAt((int) raw).get();
    }

    RandomAccessible<DoubleType> data();

    AxisType type();

}
