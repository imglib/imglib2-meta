package net.imglib2.meta.examples;

import net.imagej.display.ColorTables;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.display.ColorTable;
import net.imglib2.meta.*;
import net.imglib2.meta.calibration.Axes;
import net.imglib2.meta.calibration.Calibration;
import net.imglib2.meta.calibration.DefaultLinearAxis;
import net.imglib2.meta.channels.Channels;
import net.imglib2.meta.DatasetInterval;
import net.imglib2.position.FunctionRandomAccessible;
import net.imglib2.type.numeric.real.DoubleType;

/**
 * This example introduces how imglib2-meta enables writing metadata
 * <p>
 * FIXME: Writing metadata has not been thoroughly thought out. Much of this example
 *  is describing open questions. Feedback on solutions to these problems is welcome!
 * </p>
 * @author Gabriel Selzer
 */
public class Example06MutatingMetadata {

    private static MetadataStore exampleStore() {
        // This is a simple read/write metadata store.
        // We'll add to it through the course of the example.
        return new SimpleMetadataStore(3);
    }

    public static void main(String[] args) {
        /*
         * All Datasets have a MetadataStore, which contains MetadataItems.
         */
        RandomAccessible<DoubleType> someData = new FunctionRandomAccessible<>(
                3, //
                (loc, out) -> out.set(loc.getLongPosition(0) + loc.getLongPosition(1)), //
                DoubleType::new //
        );
        Dataset<DoubleType> dataset = Dataset.wrap(someData, exampleStore());

        /*
         * To add metadata, the MetadataStore interface provides an add() method.
         * By default, this method will throw an UnsupportedOperationException,
         * unless the backing metadata store has been designed to allow writing.
         *
         * Here, we attached metadata "foo" to axis 0 of the dataset. Then, we can
         * request it.
         */
        dataset.store().add("foo", "Some foo value defined by the metadata", 0);
        printFoo(dataset);

        /*
         * FIXME: Adding a key that is present should probably overwrite the existing value.
         */
        dataset.store().add("foo", "Some new foo value", 0);
        printFoo(dataset);

        /*
         * If a value is already present, you can mutate it using MetadataItem.setAt().
         * By default, this method will throw an UnsupportedOperationException,
         * unless the backing metadata store has been designed to allow writing.
         *
         * TODO: Consider a MetadataItem.setValue() convenience
         * TODO: Consider a MetadataItem.mutable() function or something similar to ask if writing allowed before trying it.
         */
        MetadataItem<String> fooItem = dataset.store().item("foo", String.class, 0);
        fooItem.setAt("A different foo value", 0, 0, 0);
        printFoo(dataset);

        /*
         * Views on datasets are immutable.
         * This prevents accidental modification of metadata that may be shared.
         * TODO: Consider the ramifications of this decision
         */
        DatasetInterval<DoubleType> view = dataset.view().interval(new FinalInterval(10, 10, 3));
        // This line will throw an error when uncommented
        // view.store().add("foo", "Some new foo value", 0);
        MetadataItem<String> fooItemView = view.store().item("foo", String.class, 0);
        // This line will also throw an error when uncommented
        // fooItemView.setAt("A different foo value", 0, 0, 0);

        /*
         * TODO: Setting metadata on a VARYING view is especially difficult.
         *  See MetadataItem.add(String, T, BiConsumer, int...) for the current (yucky) solution.
         *
         * Suppose we wanted to set the color tables for a XYC dataset. How might we do that?
         */
        Calibration cal = Metadata.calibration(dataset.store());
        cal.setAxis(new DefaultLinearAxis(Axes.CHANNEL, 1, 0), 2);
        Channels c = Metadata.channels(dataset.store());
        ColorTable expected = ColorTables.RED;
        c.setLut(0, expected);
        assert expected == c.lut(0);
    }

    private static void printFoo(Dataset<DoubleType> dataset) {
        System.out.println(dataset.store().item("foo", String.class, 0).value());
    }
}
