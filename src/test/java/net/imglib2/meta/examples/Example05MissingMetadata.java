package net.imglib2.meta.examples;

import net.imglib2.RandomAccessible;
import net.imglib2.meta.*;
import net.imglib2.position.FunctionRandomAccessible;
import net.imglib2.type.numeric.real.DoubleType;

/**
 * Metadata is non-uniform, optional, and generally unstructured.
 * This means it's tough to guarantee that metadata will exist.
 * This example introduces how imglib2-meta handles missing metadata.
 *
 * @author Gabriel Selzer
 */
public class Example05MissingMetadata {

    private static MetadataStore exampleStore() {
        // This is a simple read/write metadata store.
        MetadataStore store = new SimpleMetadataStore(2);
        // You can add metadata items to it as you like.
        store.add("foo", "Some foo value defined by the metadata");
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

        /*
         * Datasets will ALWAYS return a MetadataItem for any request.
         * This means that it is up to the metadata user to check whether the result is meaningful.
         */
        // FIXME: Datasets "should ALWAYS"?
        MetadataItem<String> foo = dataset.store().item("foo", String.class);

        /*
         * The magic happens when the user tries to ACCESS the value within the MetadataItem.
         * Sometimes the user wants to provide a default value instead of erroring out.
         *
         * In that case, use valueOr() to provide a default value.
         *
         * You can comment out the store.add() call in exampleStore() to see how this (and the other) examples fail.
         */
        System.out.println(foo.valueOr("Default foo value"));

        /*
         * For full control, metadata users may want to SWAP the MetadataItem itself out for a default one.
         *
         * For example, this is one way to provide a default VARYING MetadataItem.
         *
         * The syntax here is just or():
         */
        MetadataItem<String> defaultFoo = Metadata.item("foo", "Default foo MetadataItem", 2);
        System.out.println(foo.or(defaultFoo));

        /*
         * However, if the user's use case really relies on the metadata being there,
         * erroring out in the case of absence may be the right call.
         *
         * In that case, a simple call to value() will do.
         */
        System.out.println(foo.value());
    }
}
