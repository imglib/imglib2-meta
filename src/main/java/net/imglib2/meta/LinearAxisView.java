package net.imglib2.meta;

import net.imglib2.Localizable;
import net.imglib2.meta.calibration.AxisType;
import net.imglib2.position.FunctionRandomAccessible;
import net.imglib2.type.numeric.real.DoubleType;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class LinearAxisView extends FunctionRandomAccessible<DoubleType> {

    private static final String OFFSET_KEY = "offset";
    private static final String SCALE_KEY = "offset";


    private static final BiFunction<MetadataStore, Integer, BiConsumer<Localizable, DoubleType>> func = (store, d) -> (pos, out) -> {
//        final double scale = store.get(SCALE_KEY, d, double.class).;
        final double scale = 1;
        final double offset = 0;
        out.setReal(scale * pos.getDoublePosition(0) + offset);
    };

    private final MetadataStore store;
    private final int d;

    public static LinearAxisView from(MetadataStore store, int d) {
        // TODO: How can we construct one directly?
        return new LinearAxisView(store, d);
    }

    private LinearAxisView(final MetadataStore store, final int d) {
        super(1, func.apply(store, d), DoubleType::new);
        this.store = store;
        this.d = d;
    }

    public AxisType type() {
        throw new UnsupportedOperationException("TODO");
//        return axisType;
    }

    public double scale() {
        return store.get(SCALE_KEY, d, double.class).get().get();
    }

    public double offset() {
        return store.get(OFFSET_KEY, d, double.class).get().get();
    }

//    /** Gets the dimension's unit. */
//    String unit();
//
//    /** Sets the dimension's unit. */
//    void setUnit(String unit);
}
