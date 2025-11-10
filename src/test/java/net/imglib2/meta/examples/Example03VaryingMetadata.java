package net.imglib2.meta.examples;

import net.imglib2.Point;
import net.imglib2.RandomAccessible;
import net.imglib2.meta.Dataset;
import net.imglib2.meta.MetadataItem;
import net.imglib2.meta.MetadataStore;
import net.imglib2.meta.SimpleMetadataStore;
import net.imglib2.meta.calibration.Axes;
import net.imglib2.meta.calibration.Axis;
import net.imglib2.meta.calibration.Calibration;
import net.imglib2.position.FunctionRandomAccessible;
import net.imglib2.type.numeric.real.DoubleType;

/**
 * This example introduces how to access metadata items that vary as a function of position.
 *
 * @author Gabriel Selzer
 */
public class Example03VaryingMetadata {

    private static MetadataStore exampleStore() {
        // This is a simple read/write metadata store.
        MetadataStore store = new SimpleMetadataStore(2);
        // You can add metadata items to it as you like.
        store.add("name", "An Example Dataset");
        store.add("axis_type", Axes.X, 0);
        store.add("axis_unit", "nm", 0);
        FunctionRandomAccessible<DoubleType> axisData = new FunctionRandomAccessible<>(
            1,
            (loc, out) -> out.set(loc.getLongPosition(0) * 5.4),
            DoubleType::new
        );
        store.add("axis_data", axisData, new int[] {0}, 0);
        return store;
    }

    public static void main(String[] args) {
        /*
         * All Datasets have a MetadataStore, which contains MetadataItems.
         */
        RandomAccessible<DoubleType> someData = new FunctionRandomAccessible<>(
            2, //
            (loc, out) -> out.set(loc.getLongPosition(0) + loc.getLongPosition(1)), //
            DoubleType::new //
        );
        Dataset<DoubleType> dataset = Dataset.wrap(someData, exampleStore());
        MetadataStore store = dataset.store();

        /*
         * In imglib2-meta, all MetadataItems can be viewed as n-dimensional RandomAccessibles.
         *
         * Note how the generic type of the MetadataItem matches the type of data stored within it.
         * For MetadataItems that are position-invariant, the generic type is the type of that singular value.
         * For MetadataItems that vary with position, the generic type is the type of the elements within the space.
         *
         * i.e. a MetadataItem wrapping a RandomAccessible<DoubleType> will be a MetadataItem<DoubleType>.
         * BUT a MetadataItem wrapping a String will be a MetadataItem<String>.
         */
        MetadataItem<DoubleType> xAxisCalibration = store.item("axis_data", DoubleType.class, 0);
        System.out.println("Calibration value at x=0: " + xAxisCalibration.getAt(0, 0, 0));
        System.out.println("Calibration value at x=0: " + xAxisCalibration.getAt(1, 0, 0));

        /*
         * This is very convenient for metadata-aware processing, because you can use one Positionable to access both data and metadata.
         */
        Point p = new Point(1, 0);
        DoubleType dataValue = dataset.getAt(p);
        DoubleType calValue = xAxisCalibration.getAt(p);
        System.out.println("At x= " + calValue + ", y=0, data value = " + dataValue);

        /*
         * Of course, if you don't want to deal with getAt() calls, you can always use the convenience accessors.
         */
        Calibration calibration = store.info(Calibration.class);
        Axis axis = calibration.axis(0);
        System.out.println("Position 1 along axis 0 is at " + axis.calibrated(1) + " " + axis.unit());

        /*
         * Note that ALL MetadataItems get projected up to n dimensions (on demand) for uniformity.
         * Previously, we used MetadataItem.value(), which is just a shorthand for getAt() at the origin.
         */
        MetadataItem<String> nameItem = store.item("name", String.class);
        System.out.println("Dataset name at (0,0): " + nameItem.getAt(0, 0));

    }
}
