package net.imglib2.meta;

import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RealRandomAccessible;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.transform.integer.Mixed;
import net.imglib2.view.fluent.RealRandomAccessibleView;

import java.util.function.Function;
import java.util.function.Supplier;

public interface RealDataset<T, V extends RealDataset<T, V>> extends Dataset<T, V>, RealRandomAccessibleView<T, V> {
    RealRandomAccessible<T> data();
    MetadataStore store();

    @Override
    default RealRandomAccessible<T> delegate() {
        return data();
    }

    static <T, V extends RealDataset<T, V>> RealDataset<T, ?> wrap(RealRandomAccessible<T> delegate, MetadataStore store) {
        return new RealDataset<T, V>() {
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

    static <T> RealDataset<T, ?> wrap(RealDataset<T, ?> dataset, Mixed tform) {
        return wrap(
                dataset.delegate(),
                new MetadataStoreView(dataset.store(), tform)
        );
    }

    @Override
    default Dataset< T, ? > raster()
    {
        return this;
    }

    @Override
    default < U > RealDataset< U, ? > convert(
            final Supplier< U > targetSupplier ,
            final Converter< ? super T, ? super U > converter)
    {
        return wrap( Converters.convert2( delegate(), converter, targetSupplier ), store() );
    }

    @Override
    default < U > RealDataset< U, ? > convert(
            final Supplier< U > targetSupplier,
            final Supplier< Converter< ? super T, ? super U > > converterSupplier)
    {
        return wrap( Converters.convert2( delegate(), converterSupplier, targetSupplier ) , store() );
    }

    @Override
    default < U > U use( Function< ? super V, U > function )
    {
        return function.apply( (V) this );
    }

    default T getType() {
        return this.delegate().getType();
    }

    @Override
    default int numDimensions() {
        return this.delegate().numDimensions();
    }

    @Override
    default RandomAccess<T> randomAccess() {
        return this.delegate().randomAccess();
    }

    @Override
    default RandomAccess<T> randomAccess(Interval interval) {
        return this.delegate().randomAccess(interval);
    }

}
