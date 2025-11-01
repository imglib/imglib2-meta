package net.imglib2.meta.n5;

import net.imglib2.meta.Metadata;
import net.imglib2.meta.MetadataItem;
import net.imglib2.meta.MetadataStore;
import net.imglib2.meta.calibration.*;
import net.imglib2.meta.general.General;
import org.janelia.saalfeldlab.n5.N5Reader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * An example implementation of {@link MetadataStore} that wraps metadata stored in an N5 dataset.
 * <p>
 * This implementation is written for eager metadata retrieval. This isn't too bad since the metadata store
 * will not be edited.
 * </p>
 * @author Gabriel Selzer
 */
public class N5MetadataStore implements MetadataStore {

    private final N5Reader reader;
    private final String group;
    private final String dataset;

    private final List<MetadataItem<?>> items;

    public N5MetadataStore(N5Reader reader, String group, String dataset) {
        this.reader = reader;
        this.group = group;
        this.dataset = dataset;
        this.items = new ArrayList<>();

        generateMetadataItems();
    }

    private void generateMetadataItems() {
        items.add(nameItem());
        for(int i = 0; i < numDimensions(); i++) {
            items.add(axisItem(i));
        }
    }

    @Override
    public int numDimensions() {
        return reader.getAttribute(dataset, "dimensions", long[].class).length;
    }

    @Override
    public Collection<? extends MetadataItem<?>> items() {
        return items;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> MetadataItem<T> item(String key, Class<T> ofType, int... dims) {
        //noinspection unchecked
        return items.stream() //
                .filter(item -> item.name().equals(key))
                .filter(item -> item.isAttachedTo(dims)) //
                .filter(item -> ofType == null || ofType.isInstance(item.getType()))
                .map(item -> (MetadataItem<T>) item)
                .findFirst().orElseGet(() -> Metadata.absent(key, numDimensions(), dims));
    }

    @SuppressWarnings("unchecked")
    private MetadataItem<Axis> axisItem(int i) {
        Map<String, Object> axisMap = (Map<String, Object>) reader.getAttribute(dataset, "transform", Map.class);
        List<Double> scale = (List<Double>) axisMap.get("scale");
        List<Double> translate = (List<Double>) axisMap.get("translate");
        AxisType at = axisType(i);
        String units = axisUnits(i);

        return Metadata.constant(
            Calibration.AXIS,
            new DefaultLinearAxis(at, scale.get(i), translate.get(i), units),
            numDimensions(),
            i
        );
    }

    private MetadataItem<String> nameItem() {
        String name = reader.getAttribute(dataset, "name", String.class);
        return Metadata.constant(General.NAME, name, numDimensions());
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
