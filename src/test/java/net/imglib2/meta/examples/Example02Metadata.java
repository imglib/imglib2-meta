package net.imglib2.meta.examples;

import net.imglib2.RandomAccessible;
import net.imglib2.meta.Dataset;
import net.imglib2.meta.MetadataItem;
import net.imglib2.meta.MetadataStore;
import net.imglib2.meta.SimpleMetadataStore;
import net.imglib2.meta.calibration.Axes;
import net.imglib2.meta.calibration.AxisType;
import net.imglib2.meta.calibration.Calibration;
import net.imglib2.meta.general.General;
import net.imglib2.position.FunctionRandomAccessible;
import net.imglib2.type.numeric.real.DoubleType;

/**
 * This example introduces how metadata can be pulled from a Dataset.
 *
 * @author Gabriel Selzer
 */
public class Example02Metadata {

    private static MetadataStore exampleStore() {
        // This is a simple read/write metadata store.
        MetadataStore store = new SimpleMetadataStore(2);
        // You can add metadata items to it as you like.
        store.add("name", "An Example Dataset");
        store.add("author", "John Doe");
        store.add("axis_type", Axes.X, 0);
        store.add("axis_type", Axes.Y, 1);
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
        Dataset<DoubleType, ?> dataset = Dataset.wrap(someData, exampleStore());
        MetadataStore store = dataset.store();

        /*
         * You can query MetadataStores for MetadataItems using a key, and optionally axes.
         */
        MetadataItem<?> name = store.item("name");
        System.out.println("Dataset name: " + name.value());

        /*
         * You can also provide a class to get a type-safe MetadataItem.
         */
        MetadataItem<String> author = store.item("author", String.class);
        System.out.println("Dataset author: " + author.value());

        /*
         * Some metadata attributes only make sense in the context of certain axes.
         * These MetadataItems can be queried by providing axes indices.
         */
        MetadataItem<AxisType> axis0Type = store.item("axis_type", AxisType.class, 0);
        MetadataItem<AxisType> axis1Type = store.item("axis_type", AxisType.class, 1);
        System.out.println("Axis 0 type: " + axis0Type.value() + ", Axis 1 type: " + axis1Type.value());

        /*
         * Of course, using strings and integers as keys is error-prone.
         * imglib2-meta provides type-safe accessors for common metadata types
         */
        General generalData = store.info(General.class);
        System.out.println("Dataset name (again): " + generalData.name());
        Calibration calData = store.info(Calibration.class);
        System.out.println("Axis 0 type (again): " + calData.axis(0).type());
    }
}
