package net.imglib2.meta.examples;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.meta.Dataset;
import net.imglib2.meta.Metadata;
import net.imglib2.meta.MetadataItem;
import net.imglib2.meta.MetadataStore;
import net.imglib2.meta.general.General;
import net.imglib2.meta.interval.DatasetInterval;
import net.imglib2.meta.n5.N5MetadataStore;
import net.imglib2.position.FunctionRandomAccessible;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.DoubleType;
import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
import org.janelia.saalfeldlab.n5.universe.N5Factory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This example introduces how MetadataStores can wrap arbitrary sources of metadata.
 *
 * @author Gabriel Selzer
 */
public class Example07Interfaces {

    /* make an N5 reader, we start with a public container on AWS S3 */
    public static final String n5Url = "https://janelia-cosem.s3.amazonaws.com/jrc_hela-2/jrc_hela-2.n5";
    public static final String n5Group = "/em/fibsem-uint16";
    public static final String n5Dataset = n5Group + "/s4";

    public static void main(String[] args) {
        /*
         * Since MetadataStore is an interface, you can implement it to wrap any source of metadata.
         * There is actually pretty boring example for users, but more interesting for developers!
         */
        RandomAccessible<DoubleType> someData = new FunctionRandomAccessible<>(
            2, //
            (loc, out) -> out.set(loc.getLongPosition(0) + loc.getLongPosition(1)), //
            DoubleType::new //
        );
        Dataset<DoubleType> dataset = Dataset.wrap(someData, new MyMetadataStore());

        /*
         * Now we can access our custom metadata via the standard Metadata APIs
         */
        General general = Metadata.general(dataset.store());
        System.out.println(general.name() + ": " + general.description());

        /*
         * For a more practical example, see N5MetadataStore, which wraps metadata stored in an N5 file.
         */
        N5Reader n5 = new N5Factory().openReader(n5Url);
        MetadataStore store = new N5MetadataStore(n5, n5Group, n5Dataset);
        RandomAccessibleInterval<UnsignedShortType> n5RAI = N5Utils.open(n5, n5Dataset);
        DatasetInterval<UnsignedShortType> n5Dataset = DatasetInterval.wrap(n5RAI, store);
        General n5General = Metadata.general(n5Dataset.store());
        // FIXME: This name might actually be wrong :)
        System.out.println("N5 Dataset Name: " + n5General.name());
    }

    /**
     * A simple {@link MetadataStore} implementation
     */
    private static class MyMetadataStore implements MetadataStore {

        private final List<MetadataItem<?>> items;

        public MyMetadataStore() {
            items = new ArrayList<>();
            items.add(Metadata.constant(General.NAME, "A great name from my custom metadata", numDimensions()));
            items.add(Metadata.constant(General.DESCRIPTION, "An awesome description from my custom metadata", numDimensions()));
        }

        @Override
        public Collection<? extends MetadataItem<?>> items() {
            return items;
        }

        /**
         * Converts user requests for metadata items into actual {@link MetadataItem}s.
         * <p>
         * Developers can put whatever logic they want here to source metadata from an arbitrary location.
         * </p>
         * @param key the identifier of the metadata item
         * @param ofType the type of the metadata item
         * @param dims the axes associated with the metadata item
         * @return a {@link MetadataItem} satisfying the request, or an absent item if no such item exists
         * @param <T> the type of the metadata item elements
         */
        @Override
        @SuppressWarnings("unchecked")
        public <T> MetadataItem<T> item(String key, Class<T> ofType, int... dims) {
            return items.stream() //
                    .filter(item -> item.name().equals(key))
                    .filter(item -> item.isAttachedTo(dims)) //
                    .filter(item -> ofType == null || ofType.isInstance(item.getType()))
                    .map(item -> (MetadataItem<T>) item)
                    .findFirst().orElseGet(() -> Metadata.absent(key, numDimensions(), dims));
        }

        @Override
        public int numDimensions() {
            return 2;
        }
    }
}
