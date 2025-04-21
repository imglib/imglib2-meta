package net.imglib2.meta;

import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.interpolation.InterpolatorFactory;
import net.imglib2.interpolation.randomaccess.ClampingNLinearInterpolatorFactory;
import net.imglib2.interpolation.randomaccess.LanczosInterpolatorFactory;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.interpolation.randomaccess.NearestNeighborInterpolatorFactory;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;
import net.imglib2.view.fluent.RandomAccessibleIntervalView;
import net.imglib2.view.fluent.RandomAccessibleView;
import net.imglib2.view.fluent.RealRandomAccessibleView;

import java.util.function.Function;
import java.util.function.Supplier;

public interface Dataset<T, V extends RandomAccessibleView<T, V>> extends RandomAccessibleView<T, V> {
	RandomAccessible<T> data();
	MetadataStore store();

	@Override
	default RandomAccessible<T> delegate() {
		return data();
	}

	static <T, V extends RandomAccessibleView<T, V>> RandomAccessibleView<T, V> wrap(RandomAccessible<T> delegate) {
		return () -> {
			return delegate;
		};
	}

	default RandomAccessibleIntervalView<T> interval(Interval interval) {
		return RandomAccessibleIntervalView.wrap(Views.interval(this.delegate(), interval));
	}

	default RandomAccessibleView<T, ?> slice(int d, long pos) {
		return wrap(Views.hyperSlice(this.delegate(), d, pos));
	}

	default RandomAccessibleView<T, ?> addDimension() {
		return wrap(Views.addDimension(this.delegate()));
	}

	default RandomAccessibleView<T, ?> translate(long... translation) {
		return wrap(Views.translate(this.delegate(), translation));
	}

	default RandomAccessibleView<T, ?> translateInverse(long... translation) {
		return wrap(Views.translateInverse(this.delegate(), translation));
	}

	default RandomAccessibleView<T, ?> subsample(long... steps) {
		return wrap(Views.subsample(this.delegate(), Util.expandArray(steps, this.numDimensions())));
	}

	default RandomAccessibleView<T, ?> rotate(int fromAxis, int toAxis) {
		return wrap(Views.rotate(this.delegate(), fromAxis, toAxis));
	}

	default RandomAccessibleView<T, ?> permute(int fromAxis, int toAxis) {
		return wrap(Views.permute(this.delegate(), fromAxis, toAxis));
	}

	default RandomAccessibleView<T, ?> moveAxis(int fromAxis, int toAxis) {
		return wrap(Views.moveAxis(this.delegate(), fromAxis, toAxis));
	}

	default RandomAccessibleView<T, ?> invertAxis(int axis) {
		return wrap(Views.invertAxis(this.delegate(), axis));
	}

	default RealRandomAccessibleView<T> interpolate(RandomAccessibleView.Interpolation<T> interpolation) {
		return RealRandomAccessibleView.wrap(Views.interpolate(this.delegate(), interpolation.factory));
	}

	default <U> RandomAccessibleView<U, ?> convert(Supplier<U> targetSupplier, Converter<? super T, ? super U> converter) {
		return wrap(Converters.convert2(this.delegate(), converter, targetSupplier));
	}

	default <U> RandomAccessibleView<U, ?> convert(Supplier<U> targetSupplier, Supplier<Converter<? super T, ? super U>> converterSupplier) {
		return wrap(Converters.convert2(this.delegate(), converterSupplier, targetSupplier));
	}

	default <U> U use(Function<? super V, U> function) {
		return function.apply(this);
	}

	default RandomAccessibleView<T, ?> view() {
		return this;
	}

	default T getType() {
		return this.delegate().getType();
	}

	default int numDimensions() {
		return this.delegate().numDimensions();
	}

	default RandomAccess<T> randomAccess() {
		return this.delegate().randomAccess();
	}

	default RandomAccess<T> randomAccess(Interval interval) {
		return this.delegate().randomAccess(interval);
	}

	public static class Interpolation<T> {
		final InterpolatorFactory<T, ? super RandomAccessible<T>> factory;

		private Interpolation(InterpolatorFactory<T, ? super RandomAccessible<T>> factory) {
			this.factory = factory;
		}

		public static <T> RandomAccessibleView.Interpolation<T> nearestNeighbor() {
			return new RandomAccessibleView.Interpolation(new NearestNeighborInterpolatorFactory());
		}

		public static <T extends NumericType<T>> RandomAccessibleView.Interpolation<T> nLinear() {
			return new RandomAccessibleView.Interpolation(new NLinearInterpolatorFactory());
		}

		public static <T extends NumericType<T>> RandomAccessibleView.Interpolation<T> clampingNLinear() {
			return new RandomAccessibleView.Interpolation(new ClampingNLinearInterpolatorFactory());
		}

		public static <T extends RealType<T>> RandomAccessibleView.Interpolation<T> lanczos() {
			return new RandomAccessibleView.Interpolation(new LanczosInterpolatorFactory());
		}
	}
}
