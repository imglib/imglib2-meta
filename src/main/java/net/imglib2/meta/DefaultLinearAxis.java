package net.imglib2.meta;

import net.imglib2.RandomAccessible;
import net.imglib2.meta.calibration.AxisType;
import net.imglib2.position.FunctionRandomAccessible;
import net.imglib2.type.numeric.real.DoubleType;

import java.util.function.Function;

public class DefaultLinearAxis implements Axis{

    private final double scale;
    private final double offset;
    private final AxisType type;
    private final Function<Double, Double> func;

    public DefaultLinearAxis(final AxisType type, final double scale, final double offset) {
        this.type = type;
        this.offset = offset;
        this.scale = scale;
        func = raw -> raw * this.scale + this.offset;
    }

    @Override
    public double calibrated(final double raw) {
        return func.apply(raw);
    }

    @Override
    public RandomAccessible<DoubleType> data() {
        return new FunctionRandomAccessible<>(
            1,
            () -> ((pos, out) -> out.set(func.apply(pos.getDoublePosition(0)))),
            DoubleType::new
        );
    }


    @Override
    public AxisType type() {
        return type;
    }
}
