package net.imglib2.meta.n5;

import net.imglib2.RandomAccessible;
import net.imglib2.meta.Metadata;
import net.imglib2.meta.MetadataItem;
import net.imglib2.meta.MetadataStore;
import net.imglib2.meta.calibration.Axes;
import net.imglib2.meta.calibration.AxisType;
import net.imglib2.meta.calibration.Calibration;
import net.imglib2.meta.general.General;
import net.imglib2.position.FunctionRandomAccessible;
import net.imglib2.type.numeric.real.DoubleType;
import org.janelia.saalfeldlab.n5.N5Reader;

import java.util.List;
import java.util.Map;

public class N5MetadataStore implements MetadataStore {

    private final N5Reader reader;
    private final String group;
    private final String dataset;

    public N5MetadataStore(N5Reader reader, String group, String dataset) {
        this.reader = reader;
        this.group = group;
        this.dataset = dataset;
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
        return reader.getAttribute(dataset, "dimensions", long[].class).length;
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
                return (MetadataItem<T>) handleAxisUnit(ofType, dims);
//            case Channels.CHANNEL:
//                return (MetadataItem<T>) handleChannel(ofType, dims);
            default:
                return Metadata.absent(key, numDimensions(), dims);
        }
    }

    private <T> MetadataItem<String> handleAxisUnit(Class<T> ofType, int[] dims) {
        if (isNot(ofType, String.class)) {
            throw new IllegalArgumentException("axis_units must be of type String");
        }
        if (dims == null || dims.length != 1) {
            throw new IllegalArgumentException("axis_units must be associated with exactly one axis (got " + (dims == null ? 0 : dims.length) + ")");
        }
        int axisIndex = dims[0];
        return Metadata.item(
                Calibration.AXIS_TYPE,
                axisUnits(axisIndex),
                numDimensions(),
                axisIndex
        );
    }

    @SuppressWarnings("unchecked")
    private <T> MetadataItem<DoubleType> handleAxisData(Class<T> ofType, int[] dims) {
        if (isNot(ofType, DoubleType.class)) {
            throw new IllegalArgumentException("axis_data must be of doubles!");
        }
        if (dims == null || dims.length != 1) {
            throw new IllegalArgumentException("axis_data must be associated with exactly one axis (got " + (dims == null ? 0 : dims.length) + ")");
        }
        int axisIndex = dims[0];

        Map<String, Object> axisMap = (Map<String, Object>) reader.getAttribute(dataset, "transform", Map.class);
        List<Double> scale = (List<Double>) axisMap.get("scale");
        List<Double> translate = (List<Double>) axisMap.get("translate");

        FunctionRandomAccessible<DoubleType> data = new FunctionRandomAccessible<>(
                1,
                () -> (pos, out) -> out.set(scale.get(axisIndex) * pos.getLongPosition(0) + translate.get(axisIndex)),
                DoubleType::new
        );
        return Metadata.item(Calibration.AXIS_DATA, data, numDimensions(), dims);
    }

    private <T> MetadataItem<AxisType> handleAxisType(Class<T> ofType, int[] dims) {
        if (isNot(ofType, AxisType.class)) {
            throw new IllegalArgumentException("axis_type must be of type AxisType");
        }
        if (dims == null || dims.length != 1) {
            throw new IllegalArgumentException("axis_type must be associated with exactly one axis (got " + (dims == null ? 0 : dims.length) + ")");
        }
        int axisIndex = dims[0];
        return Metadata.item(
            Calibration.AXIS_TYPE,
            axisType(axisIndex),
            numDimensions(),
            axisIndex
        );
    }

    private <T> MetadataItem<String> handleName(Class<T> ofType) {
        if (isNot(ofType, String.class)) {
            throw new IllegalArgumentException("name must be of type String");
        }
        String name = reader.getAttribute(dataset, "name", String.class);
        return Metadata.item(General.NAME, name, numDimensions());
    }


    private static <T, U> boolean isNot(Class<T> src, Class<U> tgt) {
        return src != null && !src.isAssignableFrom(tgt);
    }

    @SuppressWarnings("unchecked")
    private AxisType axisType(int axisIndex) {
        Map<String, Object> axisMap = (Map<String, Object>) reader.getAttribute(dataset, "transform", Map.class);
        List<String> axes = (List<String>) axisMap.get("axes");
        switch (axes.get(axisIndex)) {
            case "x":
                return Axes.X;
            case "y":
                return Axes.Y;
            case "c":
                return Axes.CHANNEL;
            case "t":
                return Axes.TIME;
            case "z":
                return Axes.Z;
            default:
                return Axes.unknown();
        }
    }

    @SuppressWarnings("unchecked")
    private String axisUnits(int axisIndex) {
        Map<String, Object> axisMap = (Map<String, Object>) reader.getAttribute(dataset, "transform", Map.class);
        List<String> axes = (List<String>) axisMap.get("units");
        return axes.get(axisIndex);
    }

}
