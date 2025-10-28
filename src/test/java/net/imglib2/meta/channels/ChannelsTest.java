/*-
 * #%L
 * Metadata for ImgLib2
 * %%
 * Copyright (C) 2016 - 2025 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
 * John Bogovic, Albert Cardona, Barry DeZonia, Christian Dietz, Jan Funke,
 * Aivar Grislis, Jonathan Hale, Grant Harris, Stefan Helfrich, Mark Hiner,
 * Martin Horn, Steffen Jaensch, Lee Kamentsky, Larry Lindsey, Melissa Linkert,
 * Mark Longair, Brian Northan, Nick Perry, Curtis Rueden, Johannes Schindelin,
 * Gabriel Selzer, Jean-Yves Tinevez and Michael Zinsmaier.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package net.imglib2.meta.channels;

import net.imagej.display.ColorTables;
import net.imglib2.Localizable;
import net.imglib2.meta.Metadata;
import net.imglib2.meta.MetadataStore;
import net.imglib2.meta.SimpleMetadataStore;
import net.imglib2.meta.calibration.Axes;
import net.imglib2.meta.calibration.Calibration;
import net.imglib2.meta.calibration.DefaultLinearAxis;
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

        Calibration cal = Metadata.calibration(store);
        cal.setAxis(new DefaultLinearAxis(Axes.CHANNEL, 1, 0), 2);

        Channels channels = Metadata.channels(store);
        channels.setLut(0, ColorTables.RED);
        channels.setLut(1, ColorTables.GREEN);
        channels.setLut(2, ColorTables.BLUE);

        assertEquals(ColorTables.RED, channels.lut(0));
        assertEquals(ColorTables.GREEN, channels.lut(1));
        assertEquals(ColorTables.BLUE, channels.lut(2));
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

        Calibration cal = Metadata.calibration(store);
        cal.setAxis(new DefaultLinearAxis(Axes.CHANNEL, 1, 0), 2);

        Channels channels = Metadata.channels(store);
        channels.setLut(0, ColorTables.RED);
        channels.setLut(1, ColorTables.GREEN);
        channels.setLut(2, ColorTables.BLUE);

        // Permute the channel axis and ensure the channels also change
        MixedTransformView<DoubleType> v = Views.invertAxis(image, 2);
        MetadataStore storeView = Metadata.view(store, v);
        Channels channelsView = Metadata.channels(storeView);

        assertEquals(ColorTables.RED, channelsView.lut(0));
        assertEquals(ColorTables.GREEN, channelsView.lut(-1));
        assertEquals(ColorTables.BLUE, channelsView.lut(-2));
    }
}
