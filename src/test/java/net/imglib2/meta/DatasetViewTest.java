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

import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.meta.calibration.*;
import net.imglib2.meta.calibration.Axes;
import net.imglib2.meta.calibration.AxisType;
import net.imglib2.meta.interval.DatasetInterval;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.fluent.RandomAccessibleIntervalView;
import org.junit.Assert;
import org.junit.Test;

import java.util.NoSuchElementException;

/** Tests {@link DatasetView} */
public class DatasetViewTest {

	/**
	 * Get a fully calibrated {@link Dataset}
	 *
	 * @return a fully calibrated {@link Dataset}
	 */
	private Dataset<DoubleType> dataset() {
		RandomAccessible<DoubleType> data = Data.image();

		MetadataStore store = new SimpleMetadataStore(data.numDimensions());
		Calibration calibration = store.info(Calibration.class);
		calibration.setAxis(axis(Axes.X), 0);
		calibration.setAxis(axis(Axes.Y), 1);
		calibration.setAxis(axis(Axes.Z), 2);
		calibration.setAxis(axis(Axes.CHANNEL), 3);
		calibration.setAxis(axis(Axes.TIME), 4);

		return Dataset.wrap(data, store);
	}

	@Test
	public void testFluentMoveAxis() {
		Dataset<DoubleType> permuted = dataset().view().moveAxis(0, 3);

		Calibration calView = permuted.store().info(Calibration.class);
		Assert.assertEquals(Axes.Y, calView.axis(0).type());
		Assert.assertEquals(Axes.Z, calView.axis(1).type());
		Assert.assertEquals(Axes.CHANNEL, calView.axis(2).type());
		Assert.assertEquals(Axes.X, calView.axis(3).type());
		Assert.assertEquals(Axes.TIME, calView.axis(4).type());
	}

	@Test
	public void testFluentRotation() {
		Dataset<DoubleType> rotated = dataset().view().rotate(3, 2);

		Calibration calView = rotated.store().info(Calibration.class);
		Assert.assertEquals(Axes.X, calView.axis(0).type());
		Assert.assertEquals(Axes.Y, calView.axis(1).type());
		Assert.assertEquals(Axes.CHANNEL, calView.axis(2).type());
		Assert.assertEquals(Axes.Z, calView.axis(3).type());
		Assert.assertEquals(Axes.TIME, calView.axis(4).type());
	}

	@Test
	public void testFluentPermutation() {
		Dataset<DoubleType> permuted = dataset().view().permute(3, 2);

		Calibration calView = permuted.store().info(Calibration.class);
		Assert.assertEquals(Axes.X, calView.axis(0).type());
		Assert.assertEquals(Axes.Y, calView.axis(1).type());
		Assert.assertEquals(Axes.CHANNEL, calView.axis(2).type());
		Assert.assertEquals(Axes.Z, calView.axis(3).type());
		Assert.assertEquals(Axes.TIME, calView.axis(4).type());
	}

	@Test
	public void testFluentInterval() {
		DatasetInterval<DoubleType> intervaled = dataset().view().interval(new FinalInterval(10, 10, 10, 10, 10));
		// Assert the new dataset has an interval
		Assert.assertArrayEquals(new long[] {0, 0, 0, 0, 0}, intervaled.minAsLongArray());
		Assert.assertArrayEquals(new long[] {9, 9, 9, 9, 9}, intervaled.maxAsLongArray());

		Calibration calView = intervaled.store().info(Calibration.class);
		Assert.assertEquals(Axes.X, calView.axis(0).type());
		Assert.assertEquals(Axes.Y, calView.axis(1).type());
		Assert.assertEquals(Axes.Z, calView.axis(2).type());
		Assert.assertEquals(Axes.CHANNEL, calView.axis(3).type());
		Assert.assertEquals(Axes.TIME, calView.axis(4).type());
	}

	@Test
	public void testFluentSlicing() {
		// Take a Z-slice
		Dataset<DoubleType> sliced = dataset().view().slice(2, 9);

		Calibration calView = sliced.store().info(Calibration.class);
		Assert.assertEquals(Axes.X, calView.axis(0).type());
		Assert.assertEquals(Axes.Y, calView.axis(1).type());
		Assert.assertEquals(Axes.CHANNEL, calView.axis(2).type());
		Assert.assertEquals(Axes.TIME, calView.axis(3).type());
		Assert.assertThrows(NoSuchElementException.class, () -> calView.axis(4));
	}

	@Test
	public void testFluentConcatenation() {
		// Take a slice after rotating
		Dataset<DoubleType> rotated = dataset().view().rotate(4, 2).slice(2, 9);

		Calibration calView = rotated.store().info(Calibration.class);
		Assert.assertEquals(Axes.X, calView.axis(0).type());
		Assert.assertEquals(Axes.Y, calView.axis(1).type());
		Assert.assertEquals(Axes.CHANNEL, calView.axis(2).type());
		Assert.assertEquals(Axes.Z, calView.axis(3).type());
		Assert.assertThrows(NoSuchElementException.class, () -> calView.axis(4));
	}

	@Test
	public void testFluentTranslation() {
		// Translate
		Dataset<DoubleType> translated = dataset().view().translate(-1, 0, 0, 0, 0);
		Calibration calView = translated.store().info(Calibration.class);
		Assert.assertEquals(1.0, calView.axis(0).calibrated(0), 1e-6);
		// Translate & permute
		translated = dataset().view().translate(-1, 0, 0, 0, 0).permute(0, 2);
		calView = translated.store().info(Calibration.class);
		Assert.assertEquals(1.0, calView.axis(2).calibrated(0), 1e-6);
	}

	@Test
	public void testFluentInvertAxis() {
		// Invert Axis
		Dataset<DoubleType> inverted = dataset().view().invertAxis(1);
		Calibration calView = inverted.store().info(Calibration.class);
		Assert.assertEquals(-1.0, calView.axis(1).calibrated(1), 1e-6);
		// Translate & permute
		inverted = dataset().view().invertAxis(1).permute(1, 2);
		calView = inverted.store().info(Calibration.class);
		Assert.assertEquals(-1.0, calView.axis(2).calibrated(1), 1e-6);
	}

	@Test
	public void testFluentInverseTranslation() {
		// Translate
		Dataset<DoubleType> translated = dataset().view().translateInverse(1, 0, 0, 0, 0);
		Calibration calView = translated.store().info(Calibration.class);
		Assert.assertEquals(1.0, calView.axis(0).calibrated(0), 1e-6);
		// Translate & permute
		translated = dataset().view().translateInverse(1, 0, 0, 0, 0).permute(0, 2);
		calView = translated.store().info(Calibration.class);
		Assert.assertEquals(1.0, calView.axis(2).calibrated(0), 1e-6);
	}

	@Test
	public void testFluentSubsampling() {
		Dataset<DoubleType> translated = dataset().view().subsample(2, 1, 1, 1, 1);
		Calibration calView = translated.store().info(Calibration.class);
		Assert.assertEquals(2.0, calView.axis(0).calibrated(1), 1e-6);
	}

	@Test
	public void testFluentAddDimension() {
		Dataset<DoubleType> original = dataset();
		Dataset<DoubleType> translated = dataset().view().addDimension();
		Calibration calView = translated.store().info(Calibration.class);
		// New axes should be default be unknown
        // FIXME: Can we set them??
		Assert.assertEquals(Axes.unknown(), calView.axis(original.numDimensions()).type());
	}

	@Test
	public void testExtension() {
		Dataset<DoubleType> permuted = dataset().view() //
				.interval(new FinalInterval(10, 10, 10, 10, 10)) //
				.extend(RandomAccessibleIntervalView.Extension.border());

		// Assert axes unchanged
		Calibration calView = permuted.store().info(Calibration.class);
		Assert.assertEquals(Axes.X, calView.axis(0).type());
		Assert.assertEquals(Axes.Y, calView.axis(1).type());
		Assert.assertEquals(Axes.Z, calView.axis(2).type());
		Assert.assertEquals(Axes.CHANNEL, calView.axis(3).type());
		Assert.assertEquals(Axes.TIME, calView.axis(4).type());
	}

	@Test
	public void testExpansion() {
		long borderSize = 2;
		DatasetInterval<DoubleType> interval = dataset().view() //
				.interval(new FinalInterval(10, 10, 10, 10, 10)); //
		DatasetInterval<DoubleType> permuted = interval.view() //
				.expand(RandomAccessibleIntervalView.Extension.border(), 2, 2, 2, 2, 2);

		// Assert minimum moved by -borderSize
		for(int i = 0; i < permuted.numDimensions(); i++) {
			Assert.assertEquals(permuted.min(i), interval.min(i) - borderSize);
		}
		// Assert maximum moved by +borderSize
		for(int i = 0; i < permuted.numDimensions(); i++) {
			Assert.assertEquals(permuted.max(i), interval.max(i) + borderSize);
		}

		// Assert axes unchanged
		Calibration calView = permuted.store().info(Calibration.class);
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
