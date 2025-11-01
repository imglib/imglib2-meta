package net.imglib2.meta.scifio;

import io.scif.ImageMetadata;
import io.scif.img.SCIFIOImgPlus;
import net.imglib2.*;
import net.imglib2.display.ColorTable;
import net.imglib2.meta.Metadata;
import net.imglib2.meta.MetadataItem;
import net.imglib2.meta.MetadataStore;
import net.imglib2.meta.calibration.*;
import net.imglib2.meta.calibration.Axis;
import net.imglib2.meta.channels.Channels;
import net.imglib2.meta.general.General;
import net.imglib2.position.FunctionRealRandomAccessible;
import net.imglib2.transform.integer.Mixed;
import net.imglib2.type.numeric.real.DoubleType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Wraps a SCIFIO {@link io.scif.Metadata} and {@link ImageMetadata} to provide
 * a read-only {@link MetadataStore} implementation.
 * <p>
 * This implementation is written for lazy, read-only metadata retrieval.
 * It's good to evaluate the pros and cons of this approach.
 * <h4>Pros:</h4>
 * <ul>
 *     <li>minimal memory footprint</li>
 *     <li>Always up-to-date with the wrapped ImagePlus (e.g. if axes change)</li>
 * </ul>
 * <h4>Cons:</h4>
 * <ul>
 *     <li>Adding new metadata is WET - you need to report it in {@link #items()} and in {@link #item(String, Class, int...)}</li>
 * </p>
 * @author Gabriel Selzer
 */
public class SCIFIOMetadataStore implements MetadataStore{
    private final SCIFIOImgPlus<?> img;

    public SCIFIOMetadataStore(SCIFIOImgPlus<?> img) {
        this.img = img;
    }

    @Override
    public int numDimensions() {
        return img.getImageMetadata().getAxes().size();
    }

    @Override
    public Collection<? extends MetadataItem<?>> items() {
        List<MetadataItem<?>> items = new ArrayList<>();
        items.add(item(General.NAME, String.class));
        int channelAxis = -1;
        for(int i = 0; i < numDimensions(); i++) {
            MetadataItem<Axis> axisTypeItem = item(Calibration.AXIS, Axis.class, i);
            if (axisTypeItem.value() == Axes.CHANNEL) {
                channelAxis = i;
            }
            items.add(axisTypeItem);
        }
        if (channelAxis != -1) {
            items.add(item(Channels.CHANNEL, ColorTable.class, channelAxis));
        }
        return items;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> MetadataItem<T> item(String key, Class<T> ofType, int... dims) {
        switch (key) {
            case General.NAME:
                return (MetadataItem<T>) handleName(ofType);
            case Calibration.AXIS:
                return (MetadataItem<T>) handleAxis(ofType, dims);
            case Channels.CHANNEL:
                return (MetadataItem<T>) handleChannel(ofType, dims);
            default:
                return Metadata.absent(key, numDimensions(), dims);
        }
    }

    private <T> MetadataItem<ColorTable> handleChannel(Class<T> ofType, int... dims) {
        if (isNot(ofType, ColorTable.class)) {
            throw new IllegalArgumentException("name must be of type String");
        }

        if (dims.length == 0) {
            return Metadata.constant("channel", img.getColorTable(0), numDimensions());
        }
        if (dims.length == 1 && metaAxis(dims[0]) != Axes.CHANNEL) {
            throw new IllegalArgumentException("Axis " + dims[0] + " is not the channel axis!");
        }
        ViewRandomAccessible<ColorTable> imgView = new ViewRandomAccessible<>(
            1,
            pos -> img.getColorTable(pos.getIntPosition(0))
        );
        BiConsumer<Localizable, ColorTable> setter = (pos, table) -> img.setColorTable(table, pos.getIntPosition(dims[0]));
        return Metadata.variant(
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
        return Metadata.constant(General.NAME, img.getMetadata().getDatasetName(), numDimensions());
    }

    private <T> MetadataItem<Axis<?>> handleAxis(Class<T> ofType, int... d) {
        if (isNot(ofType, Axis.class)) {
            throw new IllegalArgumentException("axis must be of type Axis");
        }
        if (d == null || d.length != 1) {
            throw new IllegalArgumentException("axis must be associated with exactly one axis (got " + (d == null ? 0 : d.length) + ")");
        }
        int axisIndex = d[0];
        net.imagej.axis.CalibratedAxis ax = img.getImageMetadata().getAxis(axisIndex);
        RealAxis axis = new RealAxis() {
            @Override
            public DoubleType calibrated(double raw) {
                return data().getAt(raw);
            }

            @Override
            public RealRandomAccessible<DoubleType> data() {
                return new FunctionRealRandomAccessible<>(1,
                    () -> (pos, out) -> out.set(ax.calibratedValue(pos.getDoublePosition(0))),
                    DoubleType::new
                );
            }

            @Override
            public String unit() {
                return ax.unit();
            }

            @Override
            public AxisType type() {
                return metaAxis(axisIndex);
            }

            @Override
            public RealAxis view(long[] steps, int... srcAxes) {
                throw new UnsupportedOperationException("TODO");
            }

            @Override
            public RealAxis view(Mixed transform, int... srcAxes) {
                throw new UnsupportedOperationException("TODO");
            }
        };

        return Metadata.constant(
            Calibration.AXIS,
            axis,
            numDimensions(),
            axisIndex
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
