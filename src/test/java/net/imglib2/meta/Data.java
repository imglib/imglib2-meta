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

import net.imglib2.*;
import net.imglib2.position.FunctionRandomAccessible;
import net.imglib2.position.FunctionRealRandomAccessible;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public final class Data {

	private Data() { }

	public static RandomAccessible<DoubleType> image() {
		Supplier<DoubleType> s = DoubleType::new;
		BiConsumer<Localizable, ? super DoubleType> f = (l, t) -> {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < l.numDimensions(); i++) {
				sb.append(Math.abs(l.getLongPosition(i)));
			}
			t.set(Long.parseLong(sb.toString()));
		};
		return new FunctionRandomAccessible<>(5, f, s);
	}

	public static RandomAccessibleInterval<DoubleType> intervalImage() {
		Supplier<DoubleType> s = DoubleType::new;
		BiConsumer<Localizable, ? super DoubleType> f = (l, t) -> {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < l.numDimensions(); i++) {
				sb.append(Math.abs(l.getLongPosition(i)));
			}
			t.set(Long.parseLong(sb.toString()));
		};
		return Views.interval( //
			new FunctionRandomAccessible<>(5, f, s), //
			new FinalInterval(10, 20, 30, 40, 50) //
		);
	}

	public static RealRandomAccessible<DoubleType> realImage() {
		// create an image on the fly
		Supplier<DoubleType> s = DoubleType::new;
		BiConsumer<RealLocalizable, ? super DoubleType> f = (l, t) -> {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < l.numDimensions(); i++) {
				sb.append(Math.abs(l.getDoublePosition(i)));
			}
			String digits = sb.toString().replaceAll("\\D", "");
			if (digits.length() > 18) digits = digits.substring(0, 18);
			t.set(Long.parseLong(digits));
		};
		FunctionRealRandomAccessible<DoubleType> image = new FunctionRealRandomAccessible<>(5, f, s);
		return image;
	}
}
