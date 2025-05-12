package net.imglib2.meta;

import net.imglib2.Localizable;
import net.imglib2.RandomAccessible;
import net.imglib2.meta.calibration.AxisType;
import net.imglib2.position.FunctionRandomAccessible;
import net.imglib2.type.numeric.real.DoubleType;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class DefaultLinearAxis implements Axis{

    private double scale;
    private double offset;
    private AxisType type;
    private final Supplier<BiConsumer<Localizable, ? super DoubleType>> funcSupplier;

    public DefaultLinearAxis(final AxisType type, final double scale, final double offset) {
        this.type = type;
        this.offset = offset;
        this.scale = scale;
        funcSupplier = () -> (pos, out) -> out.set(pos.getDoublePosition(0) * this.scale + this.offset);
    }

    @Override
    public RandomAccessible<DoubleType> data() {
        return new FunctionRandomAccessible<>(1, funcSupplier, DoubleType::new);
    }

    @Override
    public AxisType type() {
        return type;
    }
}
