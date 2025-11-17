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
package net.imglib2.meta;

import net.imglib2.RandomAccessible;
import net.imglib2.img.list.ListImg;
import net.imglib2.meta.attribution.Attribution;
import net.imglib2.meta.calibration.Axes;
import net.imglib2.meta.calibration.AxisType;
import net.imglib2.meta.calibration.Calibration;
import net.imglib2.meta.calibration.DefaultLinearAxis;
import net.imglib2.meta.view.MetadataStoreView;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.MixedTransformView;
import net.imglib2.view.Views;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/** Tests {@link SimpleMetadataStore} functionality. */
public class SimpleMetadataStoreTest {

    @Test
    public void testInteger() {
        RandomAccessible<DoubleType> image = Data.image();
        assertEquals(12345.0, image.getAt(1,2,3,4,5).get(), 0.0);
        // rotate
        MixedTransformView<DoubleType> v = Views.rotate(image, 3, 2);
        assertEquals(12435.0, v.getAt(1,2,3,4,5).get(), 0.0);
        // translate
//        v = Views.permute(image, 0, 1);
//        System.out.println(v.getAt(1, 2, 3, 4, 5));

        // Populate some metadata.

        MetadataStore store = new SimpleMetadataStore(5);
        store.add("author", "foo Selzer");

        Calibration calibration = Metadata.calibration(store);
        calibration.setAxis(new DefaultLinearAxis(Axes.X, 1, 0), 0);
        calibration.setAxis(new DefaultLinearAxis(Axes.Y, 1, 0), 1);
        calibration.setAxis(new DefaultLinearAxis(Axes.Z, 1, 0), 2);
        calibration.setAxis(new DefaultLinearAxis(Axes.CHANNEL, 1, 0), 3);
        calibration.setAxis(new DefaultLinearAxis(Axes.TIME, 1, 0), 4);

        ListImg<String> tables = new ListImg<>(Arrays.asList("red", "green", "blue"), 3);
        store.add("lut", tables, new int[]{3}, new int[] {});

        // Query metadata type-unsafely (using key strings).
        Object authorObject = store.item("author").value();
        assertEquals("foo Selzer", authorObject);

        // Query metadata type-safely (but still using a key string).
        String authorString = store.item("author", String.class).value();
        assertEquals("foo Selzer", authorString);

        // An actually nice window into groups of metadata.
        Attribution attribution = Metadata.attribution(store);
        String author = attribution.author();
        String citation = attribution.citation();
        assertEquals("foo Selzer", author);
        assertNull(citation);

        AxisType axis0Type = calibration.axis(0).type();
        AxisType axis2Type = calibration.axis(2).type();
        assertSame(Axes.X, axis0Type);
        assertSame(Axes.Z, axis2Type);

        MetadataItem<?> lutItem = store.item("lut");
        assertArrayEquals(new int[] {3}, lutItem.varyingAxes());
        assertEquals("red", lutItem.getAt(0, 0, 0, 0, 0));
        assertEquals("green", lutItem.getAt(0, 0, 0, 1, 0));
        assertEquals("blue", lutItem.getAt(0, 0, 0, 2, 0));

        // Test viewing metadata based on a View of the data
        MetadataStore storeView = new MetadataStoreView(store, v.getTransformToSource());
        lutItem = storeView.item("lut");
        assertArrayEquals(new int[] {2}, lutItem.varyingAxes());
        assertEquals("red", lutItem.getAt(0, 0, 0, 0, 0));
        assertEquals("green", lutItem.getAt(0, 0, 1, 0, 0));
        assertEquals("blue", lutItem.getAt(0, 0, 2, 0, 0));
    }

}
