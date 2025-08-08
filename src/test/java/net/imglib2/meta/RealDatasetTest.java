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

import net.imglib2.RealRandomAccessible;
import net.imglib2.meta.calibration.Axes;
import net.imglib2.meta.calibration.AxisType;
import net.imglib2.meta.calibration.Calibration;
import net.imglib2.type.numeric.real.DoubleType;
import org.junit.Assert;
import org.junit.Test;

/** Tests {@link RealDataset}. */
public class RealDatasetTest {

    /**
     * Get a fully calibrated {@link RealDataset}
     *
     * @return a fully calibrated {@link DatasetInterval}
     */
    private RealDataset<DoubleType, ?> dataset() {
        RealRandomAccessible<DoubleType> data = Data.realImage();

        MetadataStore store = new SimpleMetadataStore(data.numDimensions());
        Calibration calibration = store.info(Calibration.class);
        calibration.setAxis(axis(Axes.X), 0);
        calibration.setAxis(axis(Axes.Y), 1);
        calibration.setAxis(axis(Axes.Z), 2);
        calibration.setAxis(axis(Axes.CHANNEL), 3);
        calibration.setAxis(axis(Axes.TIME), 4);
        return RealDataset.wrap(data, store);
    }

    @Test
    public void testRaster() {
        Dataset<DoubleType, ?> rasterized = dataset().raster();

        Calibration calView = rasterized.store().info(Calibration.class);
        Assert.assertEquals(Axes.X, calView.axis(0).type());
        Assert.assertEquals(Axes.Y, calView.axis(1).type());
        Assert.assertEquals(Axes.Z, calView.axis(2).type());
        Assert.assertEquals(Axes.CHANNEL, calView.axis(3).type());
        Assert.assertEquals(Axes.TIME, calView.axis(4).type());
    }

    private Axis axis(AxisType axisType) {
        return new DefaultLinearAxis(axisType, 1, 0);
    }
}
