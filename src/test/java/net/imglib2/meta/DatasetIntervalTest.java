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

import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.meta.calibration.*;
import net.imglib2.meta.calibration.Axes;
import net.imglib2.meta.calibration.AxisType;
import net.imglib2.type.numeric.real.DoubleType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests {@link DatasetInterval}.
 *
 * @author Gabriel Selzer
 */
public class DatasetIntervalTest {

	/**
	 * Get a fully calibrated {@link DatasetInterval}
	 *
	 * @return a fully calibrated {@link DatasetInterval}
	 */
	private DatasetInterval<DoubleType> calibratedDataset() {
		RandomAccessibleInterval<DoubleType> data = ArrayImgs.doubles(2, 2, 2, 2, 2);

		MetadataStore store = new SimpleMetadataStore(data.numDimensions());
		Calibration calibration = store.info(Calibration.class);
		calibration.setAxis(axis(Axes.X), 0);
		calibration.setAxis(axis(Axes.Y), 1);
		calibration.setAxis(axis(Axes.Z), 2);
		calibration.setAxis(axis(Axes.CHANNEL), 3);
		calibration.setAxis(axis(Axes.TIME), 4);
		return DatasetInterval.wrap(data, store);
	}

    @Test
    public void testLoopBuilder() {
        // Slim down the image :)
        DatasetInterval<DoubleType> dataset = calibratedDataset();
        MetadataItem<DoubleType> x_axis = dataset.store().item(Calibration.AXIS_DATA, DoubleType.class, 0);
        // Pass the data along with our metadata to LoopBuilder
        LoopBuilder.setImages(dataset, x_axis.view().interval(dataset)).forEachPixel((data, x_cal) -> data.set(x_cal.get()));
        Cursor<DoubleType> dataCursor = dataset.data().cursor();
        // Assert that the loopBuilder iteration looped over the data.
        while (dataCursor.hasNext()) {
            dataCursor.fwd();
            assertEquals(dataCursor.get().get(), x_axis.getAt(dataCursor).get(), 0.0);
        }
    }

	private Axis axis(AxisType axisType) {
		return new DefaultLinearAxis(axisType, 2, 0);
	}
}
