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
