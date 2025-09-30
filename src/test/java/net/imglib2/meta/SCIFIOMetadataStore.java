package net.imglib2.meta;

import io.scif.ImageMetadata;
import io.scif.img.SCIFIOImgPlus;
import net.imglib2.RandomAccessible;
import net.imglib2.display.ColorTable;
import net.imglib2.meta.calibration.Axes;
import net.imglib2.meta.calibration.AxisType;
import net.imglib2.meta.channels.ColorTableRAI;
import net.imglib2.position.FunctionRandomAccessible;
import net.imglib2.type.numeric.real.DoubleType;

import java.util.NoSuchElementException;

/**
 * Wraps a SCIFIO {@link io.scif.Metadata} and {@link ImageMetadata} to provide
 * a read-only {@link MetadataStore} implementation.
 *
 * @author Gabriel Selzer
 */
public class SCIFIOMetadataStore implements MetadataStore{
    private final io.scif.Metadata globalMetadata;
    private final ImageMetadata imageMetadata;

    public SCIFIOMetadataStore(SCIFIOImgPlus<?> img) {
        this(img.getMetadata(), img.getImageMetadata());
    }

    public SCIFIOMetadataStore(io.scif.Metadata globalMetadata, ImageMetadata imageMetadata) {
        this.globalMetadata = globalMetadata;
        this.imageMetadata = imageMetadata;
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
        return imageMetadata.getAxes().size();
    }

    @Override
    public <T> MetadataItem<T> item(String key, Class<T> ofType, int... dims) {
        switch (key) {
            case "name":
                return handleName(ofType, dims);
            case "axis_type":
                return handleAxisType(ofType, dims);
            case "axis_data":
                return handleAxisData(ofType, dims);
            case "channel":
                return handleChannel(ofType, dims);
            default:
                throw new NoSuchElementException("No metadata item with key " + key);
        }
    }

    private <T> MetadataItem<T> handleChannel(Class<T> ofType, int[] d) {
        if (!is(ofType, ColorTable.class)) {
            throw new IllegalArgumentException("name must be of type String");
        }
        throw new UnsupportedOperationException();
//        return Metadata.item(
//                "channel",
//                (RandomAccessible<T>) new ColorTableRAI(globalMetadata.getC),
//                numDimensions(),
//                d
//        );
    }

    private <T> MetadataItem<T> handleName(Class<T> ofType, int... d) {
        if (!is(ofType, String.class)) {
            throw new IllegalArgumentException("name must be of type String");
        }
        return Metadata.item("name", (T) globalMetadata.getDatasetName(), numDimensions());
    }

    // Handler for "axis_type" and AxisType.class
    private <T> MetadataItem<T> handleAxisType(Class<T> ofType, int... d) {
        if (!is(ofType, AxisType.class)) {
            throw new IllegalArgumentException("axis_type must be of type AxisType");
        }
        if (d == null || d.length != 1) {
            throw new IllegalArgumentException("axis_type must be associated with exactly one axis (got " + (d == null ? 0 : d.length) + ")");
        }
        int axisIndex = d[0];
        net.imagej.axis.AxisType ij2type = imageMetadata.getAxes().get(axisIndex).type();
        return Metadata.item(
                "axis_type",
                (T) convertAxisType(ij2type),
                numDimensions(),
                axisIndex
        );
    }

    // Handler for "axis_data" and Double.class
    @SuppressWarnings("unchecked")
    private <T> MetadataItem<T> handleAxisData(Class<T> ofType, int... d) {
        if (!is(ofType, DoubleType.class)) {
            throw new IllegalArgumentException("axis_data must be of doubles!");
        }
        if (d == null || d.length != 1) {
            throw new IllegalArgumentException("axis_data must be associated with exactly one axis (got " + (d == null ? 0 : d.length) + ")");
        }
        int axisIndex = d[0];
        net.imagej.axis.CalibratedAxis ax = imageMetadata.getAxis(axisIndex);
        FunctionRandomAccessible<DoubleType> data = new FunctionRandomAccessible<>(
                1,
                () -> (pos, out) -> out.set(ax.calibratedValue(pos.getIntPosition(0))),
                DoubleType::new
        );
        return Metadata.item(
                "axis_data",
                (RandomAccessible<T>) data,
                numDimensions(),
                d
        );
    }

    private AxisType convertAxisType(net.imagej.axis.AxisType ij2type) {
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

    private static <T, U> boolean is(Class<T> src, Class<U> tgt) {
        return src == null || src.isAssignableFrom(tgt);
    }

}
