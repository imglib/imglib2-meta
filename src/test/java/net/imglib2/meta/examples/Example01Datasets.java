package net.imglib2.meta.examples;

import net.imglib2.*;
import net.imglib2.meta.Dataset;
import net.imglib2.meta.interval.DatasetInterval;
import net.imglib2.meta.real.RealDataset;
import net.imglib2.position.FunctionRandomAccessible;
import net.imglib2.position.FunctionRealRandomAccessible;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.type.numeric.real.DoubleType;

/**
 * Imglib2-meta exists to provide a standardized way to associate metadata with imglib2 data structures,
 * enabling <b>metadata-aware</b> and <b>metadata-preserving</b> image processing.
 *
 * <p>
 * The library goals are:
 * <ul>
 *   <li>Compatibility with existing ImgLib2 types: data+metadata should behave like
 *       standard {@link RandomAccessible} / {@link RandomAccessibleInterval} objects.</li>
 *   <li>Transform-aware metadata: views or transforms applied to data should
 *       automatically apply to associated metadata as well.</li>
 *   <li>Type-safe, interface-driven access: a small, well-typed API for
 *       querying and composing metadata without unnecessary copying.</li>
 *   <li>Convenient structured metadata access for common formats (for example, OME).</li>
 * </ul>
 * </p>
 * <p>
 * There are alternatives within the Fiji ecosystem for storing metadata alongside images, including
 * <ul>
 * <li>imagej-common's {@code Dataset}, provides none of the above goals</li>
 * <li>SCIFIO's {@code SCIFIOImgPlus}, which is not transform aware,</li>
 * </ul>
 * </p>
 *
 * <p>
 *     This example introduces the fundamental concept of a Dataset, and how it's just a {@link RandomAccessible}.
 * </p>
 *
 * @author Gabriel Selzer
 */
public class Example01Datasets {

    public static void main(String[] args) {
        /*
         * The fundamental currency of imglib2-meta is the {@link Dataset}, which wraps
         * up the dataset {@link RadomAccessible} with its associated metadata.
         */
        RandomAccessible<DoubleType> someData = new FunctionRandomAccessible<>(
            2, //
            (loc, out) -> out.set(loc.getLongPosition(0) + loc.getLongPosition(1)), //
            DoubleType::new //
        );
        // TODO: Issue #1 - that wildcard! See the Views example for more discussion.
        Dataset<DoubleType, ?> dataset = Dataset.wrap(someData);

        /*
         * Datasets are just RandomAccessible, which means that you can pass them as-is to all existing imglib functionality
         */
        Point p = new Point(1, 1);
        DoubleType resultFromOriginal = localMeanAround(someData, p);
        System.out.println("Mean at (1,1) from original: " + resultFromOriginal.getRealDouble());

        DoubleType resultFromDataset = localMeanAround(dataset, p);
        System.out.println("Mean at (1,1) from dataset:  " + resultFromDataset.getRealDouble());


        /*
         * However, because Datasets are type-safe, you cannot just pass them to ANY existing functionality.
         * Uncommenting the line below would cause a compile-time error!
         */
//        DoubleType someOtherResult = someExistingUnusableFunctionality(dataset);

        /*
         * Naturally, to complement Dataset are DatasetIntervals, which wrap RandomAccessibleIntervals...
         */
        RandomAccessibleInterval<DoubleType> someInterval = someData.view().interval(new FinalInterval(10, 10));
        DatasetInterval<DoubleType, ?> datasetInterval = DatasetInterval.wrap(someInterval);
        /*
         * ...and RealDatasets, which wrap RealRandomAccessibles.
         */
        RealRandomAccessible<DoubleType> someRealData = new FunctionRealRandomAccessible<>( //
            2, //
            (pos, out) -> out.set(pos.getDoublePosition(0) * pos.getDoublePosition(1)), //
            DoubleType::new //
        );
        RealDataset<DoubleType, ?> datasetReal = RealDataset.wrap(someRealData);
    }

    private static <T extends RealType<T>> T localMeanAround(RandomAccessible<T> rai, Point point) {
        if (rai.numDimensions() < 2)
            throw new IllegalArgumentException("Requires at least 2 dimensions");

        // Compute the mean over a 3x3 neighborhood centered at (1,1) in the
        // first two dimensions. This shows a small, concrete example of
        // accessing values via RandomAccess and producing a RealType result.
        RandomAccess<T> ra = rai.randomAccess();
        final T sum = rai.getType().createVariable();
        final T numElements = rai.getType().createVariable();
        final int radius = 1;
        ra.setPosition(point);
        ra.move(new long[] {-radius, -radius});
        for (int dx = 0; dx < 2 * radius + 1; dx++) {
            for (int dy = 0; dy < 2 * radius + 1; dy++) {
                sum.add(ra.get());
                numElements.inc();
                ra.move(1, 1);
            }
            ra.move(- (2 * radius + 1), 1);
            ra.move(1, 0);
        }

        sum.div(numElements);
        return sum;
    }

    @SuppressWarnings("unused")
    private static LongType someExistingUnusableFunctionality(RandomAccessible<LongType> rai) {
        if (rai.numDimensions() < 2)
            throw new IllegalArgumentException("Requires at 2 dimensions");
        return rai.getAt(1, 1);
    }
}
