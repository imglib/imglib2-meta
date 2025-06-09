package net.imglib2.meta;

import net.imglib2.*;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.transform.integer.Mixed;
import net.imglib2.transform.integer.MixedTransform;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import net.imglib2.view.*;
import net.imglib2.view.fluent.RandomAccessibleIntervalView;

import java.util.function.Supplier;

public interface DatasetInterval<T, V extends DatasetInterval<T, V>> extends Dataset<T, V>, RandomAccessibleIntervalView<T, V> {
	RandomAccessibleInterval<T> data();
	MetadataStore store();

	@Override
	default RandomAccessibleInterval<T> delegate() {
		return data();
	}

	static <T, V extends DatasetInterval<T, V>> DatasetInterval<T, ?> wrap(RandomAccessibleInterval<T> delegate, MetadataStore store) {
		return new DatasetInterval<T, V>() {
			@Override
			public RandomAccessibleInterval<T> data() {
				return delegate;
			}

			@Override
			public MetadataStore store() {
				return store;
			}
		};
	}

	static <T> DatasetInterval<T, ?> wrap(DatasetInterval<T, ?> dataset, Mixed tform, Interval interval) {
		return wrap(
			new IntervalView<>(new MixedTransformView<>(dataset.delegate(), tform), interval),
			new MetadataStoreView(dataset.store(), tform)
		);
	}

	default DatasetInterval<T, ?> interval(Interval interval) {
		// TODO: Consider an interval on the metadata?
		return wrap(
				this,
				new MixedTransform(numDimensions(), numDimensions()),
				interval
		);
	}

	@Override
	default DatasetInterval<T, ?> slice(int d, long pos) {
		return wrap(
				this,
				ViewTransforms.hyperSlice(numDimensions(), d, pos),
				Intervals.hyperSlice(this, d)
		);
	}

	@Override
	default DatasetInterval<T, ?> addDimension() {
		return wrap( //
				this, //
				ViewTransforms.addDimension(numDimensions()), //
				Intervals.addDimension(this, 0, 1) //
		);
	}

	@Override
	default DatasetInterval<T, ?> translate(long... translation) {
		return wrap(
				this,
				ViewTransforms.translate(translation),
				Intervals.translate(this, translation)
		);
	}

	@Override
	default DatasetInterval<T, ?> translateInverse(long... translation) {
		return wrap(
				this,
				ViewTransforms.translateInverse(translation),
				Intervals.translateInverse(this, translation)
		);
	}

	@Override
	default DatasetInterval<T, ?> subsample(long... steps) {
		long[] fullSteps = Util.expandArray(steps, this.numDimensions());
		SubsampleIntervalView<T> dataView = Views.subsample(this.delegate(), fullSteps);
		MetadataStore storeView = new MetadataStoreSubsampleView(
			store(),
			fullSteps
		);
		return wrap(dataView, storeView);
	}

	@Override
	default DatasetInterval<T, ?> rotate(int fromAxis, int toAxis) {
		return wrap(
				this,
				ViewTransforms.rotate(numDimensions(), fromAxis, toAxis),
				Intervals.rotate(this, fromAxis, toAxis)
		);
	}

	@Override
	default DatasetInterval<T, ?> permute(int fromAxis, int toAxis) {
		return wrap(
				this,
				ViewTransforms.permute(numDimensions(), fromAxis, toAxis),
				Intervals.permuteAxes(this, fromAxis, toAxis)
		);
	}

	@Override
	default DatasetInterval<T, ?> moveAxis(int fromAxis, int toAxis) {
		return wrap(
				this,
				ViewTransforms.moveAxis(numDimensions(), fromAxis, toAxis),
				Intervals.moveAxis(this, fromAxis, toAxis)
		);
	}

	@Override
	default DatasetInterval<T, ?> invertAxis(int axis) {
		return wrap(
				this,
				ViewTransforms.invertAxis(numDimensions(), axis),
				Intervals.invertAxis(this, axis)
		);
	}

	@Override
	default Dataset<T, ?> extend( Extension< T, V > extension )
	{
		RandomAccessible<T> delegate = RandomAccessibleIntervalView.super.extend(extension);
		return Dataset.wrap(delegate, store());
	}

	@Override
	default DatasetInterval< T, ?> expand( Extension< T, V > extension, long... border )
	{
		RandomAccessibleInterval<T> delegate = RandomAccessibleIntervalView.super.expand(extension, border);
		return DatasetInterval.wrap(delegate, store());
	}

	@Override
	default RandomAccessibleIntervalView< T, ? > zeroMin()
	{
		return wrap(this, ViewTransforms.zeroMin(this), Intervals.zeroMin(this));
	}

	// FIXME: Dataset wildcard bound
	@Override
	default <U> DatasetInterval<U, ?> convert(Supplier<U> targetSupplier, Converter<? super T, ? super U> converter) {
		return wrap(Converters.convert2(this.delegate(), converter, targetSupplier), store());
	}

	// FIXME: Dataset wildcard bound
	@Override
	default <U> DatasetInterval<U, ?> convert(Supplier<U> targetSupplier, Supplier<Converter<? super T, ? super U>> converterSupplier) {
		return wrap(Converters.convert2(this.delegate(), converterSupplier, targetSupplier), store());
	}

	default DatasetInterval<T, V> view() {
		return this;
	}

	@Override
	default T getType() {
		return Dataset.super.getType();
	}


	/** RandomAccessibleInterval Overrides */

	@Override
	default Cursor< T > cursor()
	{
		return new RandomAccessibleIntervalCursor<>( this.delegate() );
	}

	@Override
	default Cursor< T > localizingCursor()
	{
		return cursor();
	}

	@Override
	default long size()
	{
		return Intervals.numElements( this );
	}

	@Override
	default Object iterationOrder()
	{
		return new FlatIterationOrder( this );
	}


}
