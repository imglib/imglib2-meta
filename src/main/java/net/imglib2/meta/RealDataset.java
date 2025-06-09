package net.imglib2.meta;

import net.imglib2.RealRandomAccessible;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.transform.integer.Mixed;
import net.imglib2.view.RandomAccessibleOnRealRandomAccessible;
import net.imglib2.view.fluent.RealRandomAccessibleView;

import java.util.function.Supplier;

public interface RealDataset<T, V extends RealDataset<T, V>> extends Dataset<T, V>, RealRandomAccessibleView<T, V> {
    RealRandomAccessible<T> data();
    MetadataStore store();

    @Override
    default RealRandomAccessible<T> delegate() {
        return data();
    }

    static <T> RealDataset<T> wrap(RealRandomAccessible<T> delegate, MetadataStore store) {
        return new RealDataset<T>() {
            @Override
            public RealRandomAccessible<T> data() {
                return delegate;
            }

            @Override
            public MetadataStore store() {
                return store;
            }
        };
    }

    static <T> RealDataset<T> wrap(RealDataset<T> dataset, Mixed tform) {
        return wrap(
                dataset.delegate(),
                new MetadataStoreView(dataset.store(), tform)
        );
    }

    @Override
    default Dataset< T > raster()
    {
        return Dataset.wrap( new RandomAccessibleOnRealRandomAccessible<>(delegate()), store());
    }

    @Override
    default < U > RealDataset< U > convert(
            final Converter< ? super T, ? super U > converter,
            final Supplier< U > targetSupplier )
    {
        return wrap( Converters.convert2( delegate(), converter, targetSupplier ), store() );
    }

    @Override
    default < U > RealDataset< U > convert(
            final Supplier< Converter< ? super T, ? super U > > converterSupplier,
            final Supplier< U > targetSupplier )
    {
        return wrap( Converters.convert2( delegate(), converterSupplier, targetSupplier ) , store() );
    }

}
