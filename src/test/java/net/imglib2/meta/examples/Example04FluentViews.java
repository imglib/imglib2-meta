package net.imglib2.meta.examples;

import net.imglib2.RandomAccessible;
import net.imglib2.meta.Dataset;
import net.imglib2.meta.Metadata;
import net.imglib2.meta.MetadataStore;
import net.imglib2.meta.SimpleMetadataStore;
import net.imglib2.meta.calibration.Axes;
import net.imglib2.meta.calibration.Axis;
import net.imglib2.meta.calibration.Calibration;
import net.imglib2.meta.calibration.DefaultLinearAxis;
import net.imglib2.position.FunctionRandomAccessible;
import net.imglib2.type.numeric.real.DoubleType;

/**
 * This example introduces how metadata items persist through data views.
 *
 * @author Gabriel Selzer
 */
public class Example04FluentViews {

    private static MetadataStore exampleStore() {
        // This is a simple read/write metadata store.
        MetadataStore store = new SimpleMetadataStore(2);
        // You can add metadata items to it as you like.
        // This is identical to example 3, however let's use the convenience Calibration API.
        Calibration cal = Metadata.calibration(store);
        cal.setAxis(new DefaultLinearAxis(Axes.X, 5.4, 0, "nm"), 0);
        cal.setAxis(new DefaultLinearAxis(Axes.Y, 2.7, 7, "nm"), 1);
        return store;
    }

    public static void main(String[] args) {
        /*
         * All Datasets have a MetadataStore, which contains MetadataItems.
         */
        RandomAccessible<DoubleType> someData = new FunctionRandomAccessible<>(
            2, //
            (loc, out) -> out.set(loc.getLongPosition(0) + loc.getLongPosition(1)), //
            DoubleType::new //
        );
        Dataset<DoubleType> dataset = Dataset.wrap(someData, exampleStore());

        /*
         * Datasets also implement the RandomAccessibleView interface, meaning a host of zero-copy transforms
         * are available and also preserve metadata.
         */
        Calibration untransformed = Metadata.calibration(dataset.store());
        System.out.println("Axis 0 is the " + untransformed.axis(0).type() + " axis");
        // Switch axes 0 and 1
        Dataset<DoubleType> permutedData = dataset.view().permute(0, 1);
        Calibration permuted = Metadata.calibration(permutedData.store());
        System.out.println("Axis 0 (permuted) is the " + permuted.axis(0).type() + " axis");

        /*
         * Varying metadata are also affected by the fluent view operations.
         * (This is one potent benefit of expressing all metadata as n-dimensional RandomAccessibles.)
         */
        Calibration calibration = Metadata.calibration(dataset.store());
        Axis axis0 = calibration.axis(0);
        System.out.println("Position 1 along axis 0 is at " + axis0.calibrated(1) + " " + axis0.unit());
        Dataset<DoubleType> subsampled = dataset.view().subsample(2, 2);
        Calibration subsampledCalibration = Metadata.calibration(subsampled.store());
        Axis subsampledAxis0 = subsampledCalibration.axis(0);
        System.out.println("Position 1 along subsampled (step=2) axis 0 is at " + subsampledAxis0.calibrated(1) + " " + subsampledAxis0.unit());
    }
}
