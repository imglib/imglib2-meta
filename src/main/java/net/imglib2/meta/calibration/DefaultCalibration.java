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

import net.imglib2.meta.MetadataStore;

import java.util.NoSuchElementException;
import java.util.Optional;

public class DefaultCalibration implements Calibration {
	private MetadataStore metaData;

    @Override
	public void setStore(MetadataStore store) {
		this.metaData = store;
	}

	@Override
	public <F, T extends Axis<F>> T axis(final int d, final Class<T> type) {
		if (d >= metaData.numDimensions()) {
			throw new NoSuchElementException("Metadata is only " + metaData.numDimensions() + "-dimensional!");
		}
		return metaData.item(AXIS, type, d).valueOr(() -> defaultAxis(type));
	}

    @SuppressWarnings("unchecked")
    private <T, F extends Axis<T>> F defaultAxis(final Class<F> axisType) {
        if (axisType.isAssignableFrom(DefaultLinearAxis.class)) {
            return (F) new DefaultLinearAxis(Axes.unknown(), 1, 0);
        }
        if (axisType.isAssignableFrom(DefaultEnumeratedAxis.class)) {
            return (F) new DefaultEnumeratedAxis<>(Axes.unknown(), "");
        }
        throw new UnsupportedOperationException("Do not know how to make an unknown axis of type " + axisType);
    }


	@Override
	public void setAxis(final Axis<?> axis, final int d) {
        metaData.add(AXIS, axis, d);
        metaData.add(AXIS_DATA, axis.data(), new int[] {d}, d);
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
