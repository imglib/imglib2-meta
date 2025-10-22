package net.imglib2.meta.scifio;

import io.scif.ImageMetadata;
import io.scif.img.SCIFIOImgPlus;
import net.imglib2.*;
import net.imglib2.display.ColorTable;
import net.imglib2.meta.Metadata;
import net.imglib2.meta.MetadataItem;
import net.imglib2.meta.MetadataStore;
import net.imglib2.meta.calibration.Axes;
import net.imglib2.meta.calibration.AxisType;
import net.imglib2.meta.calibration.Calibration;
import net.imglib2.meta.channels.Channels;
import net.imglib2.meta.general.General;
import net.imglib2.position.FunctionRandomAccessible;
import net.imglib2.type.numeric.real.DoubleType;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Wraps a SCIFIO {@link io.scif.Metadata} and {@link ImageMetadata} to provide
 * a read-only {@link MetadataStore} implementation.
 *
 * @author Gabriel Selzer
 */
public class SCIFIOMetadataStore implements MetadataStore{
    private final SCIFIOImgPlus<?> img;

    public SCIFIOMetadataStore(SCIFIOImgPlus<?> img) {
        this.img = img;
    }

    @Override
    public <T> void add(String key, T data, int... dims) {
        throw new UnsupportedOperationException("Read-Only");
    }

    @Override
    public <T> void add(String key, RandomAccessible<T> data, int... dims) {
        throw new UnsupportedOperationException("Read-Only");
    }

    @Override
    public int numDimensions() {
        return img.getImageMetadata().getAxes().size();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> MetadataItem<T> item(String key, Class<T> ofType, int... dims) {
        switch (key) {
            case General.NAME:
                return (MetadataItem<T>) handleName(ofType);
            case Calibration.AXIS_TYPE:
                return (MetadataItem<T>) handleAxisType(ofType, dims);
            case Calibration.AXIS_DATA:
                return (MetadataItem<T>) handleAxisData(ofType, dims);
            case Calibration.AXIS_UNITS:
                return (MetadataItem<T>) handleAxisUnits(ofType, dims);
            case Channels.CHANNEL:
                return (MetadataItem<T>) handleChannel(ofType, dims);
            default:
                return MetadataItem.absent(key, numDimensions(), dims);
        }
    }

    private <T> MetadataItem<ColorTable> handleChannel(Class<T> ofType, int... dims) {
        if (isNot(ofType, ColorTable.class)) {
            throw new IllegalArgumentException("name must be of type String");
        }

        if (dims.length == 0) {
            return Metadata.item("channel", img.getColorTable(0), numDimensions());
        }
        if (dims.length == 1 && metaAxis(dims[0]) != Axes.CHANNEL) {
            throw new IllegalArgumentException("Axis " + dims[0] + " is not the channel axis!");
        }
        ViewRandomAccessible<ColorTable> imgView = new ViewRandomAccessible<>(
            1,
            pos -> img.getColorTable(pos.getIntPosition(0))
        );
        BiConsumer<Localizable, ColorTable> setter = (pos, table) -> img.setColorTable(table, pos.getIntPosition(dims[0]));
        return Metadata.item(
            Channels.CHANNEL,
            imgView,
            numDimensions(),
            setter,
            dims
        );
    }

    private <T> MetadataItem<String> handleName(Class<T> ofType) {
        if (isNot(ofType, String.class)) {
            throw new IllegalArgumentException("name must be of type String");
        }
        return Metadata.item(General.NAME, img.getMetadata().getDatasetName(), numDimensions());
    }

    private <T> MetadataItem<AxisType> handleAxisType(Class<T> ofType, int... d) {
        if (isNot(ofType, AxisType.class)) {
            throw new IllegalArgumentException("axis_type must be of type AxisType");
        }
        if (d == null || d.length != 1) {
            throw new IllegalArgumentException("axis_type must be associated with exactly one axis (got " + (d == null ? 0 : d.length) + ")");
        }
        int axisIndex = d[0];
        return Metadata.item(
            Calibration.AXIS_TYPE,
            metaAxis(axisIndex),
            numDimensions(),
            axisIndex
        );
    }

    private <T> MetadataItem<DoubleType> handleAxisData(Class<T> ofType, int... d) {
        if (isNot(ofType, DoubleType.class)) {
            throw new IllegalArgumentException("axis_data must be of doubles!");
        }
        if (d == null || d.length != 1) {
            throw new IllegalArgumentException("axis_data must be associated with exactly one axis (got " + (d == null ? 0 : d.length) + ")");
        }
        int axisIndex = d[0];
        net.imagej.axis.CalibratedAxis ax = img.getImageMetadata().getAxis(axisIndex);
        FunctionRandomAccessible<DoubleType> data = new FunctionRandomAccessible<>(
            1,
            () -> (pos, out) -> out.set(ax.calibratedValue(pos.getIntPosition(0))),
            DoubleType::new
        );
        return Metadata.item(Calibration.AXIS_DATA, data, numDimensions(), d);
    }

    private <T> MetadataItem<String> handleAxisUnits(Class<T> ofType, int... d) {
        if (isNot(ofType, String.class)) {
            throw new IllegalArgumentException("axis_units must be strings!");
        }
        if (d == null || d.length != 1) {
            throw new IllegalArgumentException("axis_units must be associated with exactly one axis (got " + (d == null ? 0 : d.length) + ")");
        }
        int axisIndex = d[0];
        net.imagej.axis.CalibratedAxis ax = img.getImageMetadata().getAxis(axisIndex);
        return Metadata.item(
                Calibration.AXIS_UNITS,
                ax.unit(),
                numDimensions(),
                d
        );
    }

    private AxisType metaAxis(int dim) {
        net.imagej.axis.AxisType ij2type = img.getImageMetadata().getAxes().get(dim).type();
        if (ij2type == net.imagej.axis.Axes.CHANNEL) {
            return Axes.CHANNEL;
        } else if (ij2type == net.imagej.axis.Axes.TIME) {
            return Axes.TIME;
        } else if (ij2type == net.imagej.axis.Axes.Z) {
            return Axes.Z;
        } else if (ij2type == net.imagej.axis.Axes.X) {
            return Axes.X;
        } else if (ij2type == net.imagej.axis.Axes.Y) {
            return Axes.Y;
        } else {
            return Axes.unknown();
        }
    }

    private static <T, U> boolean isNot(Class<T> src, Class<U> tgt) {
        return src != null && !src.isAssignableFrom(tgt);
    }

}

/**
 * A {@link RandomAccessible} that generates a function value for each
 * position in discrete coordinate space by side-effect using a
 * {@link BiConsumer}.
 *
 * @author Stephan Saalfeld
 */
class ViewRandomAccessible< T > implements RandomAccessible< T >
{
    private final int numDimensions;

    private final Supplier<Function<Localizable, T>> functionSupplier;

    public ViewRandomAccessible(
            final int n,
            final Function< Localizable, T > function
    )
    {
        this.numDimensions = n;
        this.functionSupplier = () -> function;
    }

    @Override
    public int numDimensions() {
        return numDimensions;
    }


    public class FunctionRandomAccess extends Point implements RandomAccess< T >
    {
        public FunctionRandomAccess()
        {
            super( ViewRandomAccessible.this.numDimensions() );
        }

        @Override
        public T get()
        {
            return functionSupplier.get().apply(this);
        }

        @Override
        public T getType()
        {
            return functionSupplier.get().apply(new Point());
        }

        @Override
        public FunctionRandomAccess copy()
        {
            return new FunctionRandomAccess();
        }
    }

    @Override
    public FunctionRandomAccess randomAccess()
    {
        return new FunctionRandomAccess();
    }

    @Override
    public FunctionRandomAccess randomAccess(final Interval interval )
    {
        return randomAccess();
    }

    @Override
    public T getType() {
        return functionSupplier.get().apply(new Point());
    }
}
