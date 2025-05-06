package net.imglib2.meta;

import net.imglib2.Localizable;
import net.imglib2.meta.calibration.AxisType;
import net.imglib2.position.FunctionRandomAccessible;
import net.imglib2.type.numeric.real.DoubleType;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class LinearAxis extends FunctionRandomAccessible<DoubleType> {

    private final LinearAxisFunction function;
    private final AxisType type;

    public LinearAxis(AxisType type) {
        this(type, 0.0, 1.0);
    }

    public LinearAxis(AxisType type, double offset, double scale) {
        super(1, new LinearAxisFunction(), DoubleType::new);
        this.function = (LinearAxisFunction) this.functionSupplier.get();
        this.type = type;
    }

    public AxisType type() {
        return type;
    }

    public double scale() {
        return function.scale;
    }

    public double offset() {
        return function.offset;
    }

    public void setScale(final double scale) {
        function.scale = scale;
    }

    public void setOffset(final double offset) {
        function.offset = offset;
    }

    private static class LinearAxisFunction implements BiConsumer<Localizable, DoubleType> {

        public double scale = 1;
        public double offset = 0;

        @Override
        public void accept(Localizable localizable, DoubleType doubleType) {
            doubleType.setReal(scale * localizable.getDoublePosition(0) + offset);
        }
    }
}
