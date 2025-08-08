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
package net.imglib2.meta.calibration;

import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RealLocalizable;
import net.imglib2.meta.Axis;
import net.imglib2.meta.Metadata;
import net.imglib2.meta.MetadataItem;
import net.imglib2.meta.MetadataStore;
import net.imglib2.position.FunctionRandomAccessible;
import net.imglib2.type.numeric.real.DoubleType;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;

public class DefaultCalibration implements Calibration {
	private MetadataStore metaData;
	private static final String AXIS_DATA = "axis_data";
	private static final String AXIS_TYPE = "axis_type";

	private static class UnknownData implements MetadataItem<DoubleType> {
		private final boolean[] axisAttachments;

		public UnknownData(int numDimensions) {
			axisAttachments = new boolean[numDimensions];
		}

		@Override
		public RandomAccess<DoubleType> randomAccess() {
			return new FunctionRandomAccessible<>(
				axisAttachments.length,
				(loc, out) -> out.set(getAt(loc)),
				DoubleType::new
			).randomAccess();
		}

		@Override
		public String name() {
			return AXIS_DATA;
		}

		@Override
		public boolean[] attachedAxes() {
			return axisAttachments;
		}

		@Override
		public DoubleType getAt(RealLocalizable pos) {
			throw new IllegalArgumentException("Cannot query positions on unknown axes!");
		}

	};



	@Override
	public void setStore(MetadataStore store) {
		this.metaData = store;
	}

	@Override
	public Axis axis(final int d) {
		if (d >= metaData.numDimensions()) {
			throw new NoSuchElementException("Metadata is only " + metaData.numDimensions() + "-dimensional!");
		}
		// Search for Axis components
		MetadataItem<DoubleType> data = metaData.get(AXIS_DATA, DoubleType.class, d).orElseGet(() -> new UnknownData(metaData.numDimensions()));

		// Construct a
		ThreadLocal<long[]> cs = ThreadLocal.withInitial(() -> new long[metaData.numDimensions()]);
		return new Axis() {
			private final Supplier<MetadataItem<AxisType>> unknownAxisSupplier = //
					() -> Metadata.item(AXIS_TYPE, Axes.unknown(), metaData.numDimensions());

			@Override
			public double calibrated(double raw) {
				long[] c = cs.get();
				c[d] = (long) raw;
				return data.getAt(c).get();
			}
			@Override
			public RandomAccessible<DoubleType> data() {
				return new FunctionRandomAccessible<>(
						1,
						(pos, out) -> out.set(calibrated(pos.getDoublePosition(0))),
						DoubleType::new
				);
			}

			@Override
			public AxisType type() {
				return metaData.get(AXIS_TYPE, AxisType.class, d).orElseGet(unknownAxisSupplier).getType();
			}
		};
	}


	@Override
	public void setAxis(final Axis axis, final int d) {
		metaData.add(AXIS_DATA, axis.data(), d);
		metaData.add(AXIS_TYPE, axis.type(), d);
	}

	@Override
	public Optional<Integer> indexOf(AxisType type) {
		for (int i = 0; i < metaData.numDimensions(); i++) {
			if(axis(i).type() == type) {
				return Optional.of(i);
			}
		}
		return Optional.empty();
	}
}
