package net.imglib2.meta.channels;

import net.imagej.display.ColorTables;
import net.imglib2.Localizable;
import net.imglib2.meta.Metadata;
import net.imglib2.meta.MetadataStore;
import net.imglib2.meta.SimpleMetadataStore;
import net.imglib2.position.FunctionRandomAccessible;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.MixedTransformView;
import net.imglib2.view.Views;
import org.junit.Test;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;

public class ChannelsTest {

    /**
     * Asserts
     */
    @Test
    public void testPersistence() {
        Supplier<DoubleType> s = DoubleType::new;
        BiConsumer<Localizable, ? super DoubleType> f = (l, t) -> {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < l.numDimensions(); i++) {
                sb.append(Math.abs(l.getLongPosition(i)));
            }
            t.set(Long.parseLong(sb.toString()));
        };
        FunctionRandomAccessible<DoubleType> image = new FunctionRandomAccessible<>(3, f, s);

        MetadataStore store = new SimpleMetadataStore(image.numDimensions());
        Channels channels = Metadata.channels(store);

        channels.setLut(2, 0, ColorTables.RED);
        channels.setLut(2, 1, ColorTables.GREEN);
        channels.setLut(2, 2, ColorTables.BLUE);

        assertEquals(ColorTables.RED, channels.lut(2, 0));
        assertEquals(ColorTables.GREEN, channels.lut(2, 1));
        assertEquals(ColorTables.BLUE, channels.lut(2, 2));
    }

    @Test
    public void testPermutation() {
        Supplier<DoubleType> s = DoubleType::new;
        BiConsumer<Localizable, ? super DoubleType> f = (l, t) -> {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < l.numDimensions(); i++) {
                sb.append(Math.abs(l.getLongPosition(i)));
            }
            t.set(Long.parseLong(sb.toString()));
        };
        FunctionRandomAccessible<DoubleType> image = new FunctionRandomAccessible<>(3, f, s);

        MetadataStore store = new SimpleMetadataStore(image.numDimensions());
        Channels channels = Metadata.channels(store);

        channels.setLut(2, 0, ColorTables.RED);
        channels.setLut(2, 1, ColorTables.GREEN);
        channels.setLut(2, 2, ColorTables.BLUE);

        // Permute the channel axis and ensure the channels also change
        MixedTransformView<DoubleType> v = Views.invertAxis(image, 2);
        MetadataStore storeView = Metadata.view(store, v);
        Channels channelsView = Metadata.channels(storeView);

        assertEquals(ColorTables.RED, channelsView.lut(2, 0));
        assertEquals(ColorTables.GREEN, channelsView.lut(2, -1));
        assertEquals(ColorTables.BLUE, channelsView.lut(2, -2));
    }
}
