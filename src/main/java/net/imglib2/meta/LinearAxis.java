package net.imglib2.meta;

import net.imglib2.Localizable;
import net.imglib2.meta.calibration.AxisType;
import net.imglib2.position.FunctionRandomAccessible;
import net.imglib2.type.numeric.real.DoubleType;

import java.util.function.BiConsumer;

public class LinearAxis extends FunctionRandomAccessible<DoubleType> {

    private AxisType axisType;
    private double offset;
    private double scale;

    public LinearAxis(AxisType type) {
        this(type, 0.0, 1.0);
    }

    public LinearAxis(AxisType type, double offset, double scale) {
        super(1, (l, o) -> o.setReal(l.getDoublePosition(0) * scale + offset), DoubleType::new);

        this.axisType = type;
        this.offset = offset;
        this.scale = scale;
    }

    public AxisType type() {
        return axisType;
    }

    public double scale() {
        return scale;
    }

    public double offset() {
        return offset;
    }

//    /** Gets the dimension's unit. */
//    String unit();
//
//    /** Sets the dimension's unit. */
//    void setUnit(String unit);
}
