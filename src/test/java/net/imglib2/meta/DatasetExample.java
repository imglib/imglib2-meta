package net.imglib2.meta;

import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.meta.calibration.Axes;
import net.imglib2.meta.calibration.Axis;
import net.imglib2.meta.calibration.Calibration;
import net.imglib2.meta.calibration.DefaultLinearAxis;
import net.imglib2.meta.interval.DatasetInterval;
import net.imglib2.position.FunctionRandomAccessible;
import net.imglib2.position.FunctionRealRandomAccessible;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.type.numeric.real.DoubleType;

import java.util.Random;

public class DatasetExample {

    private static DatasetInterval<DoubleType> loadDataset() {
        // Datasets require a RandomAccessible data source. This is usually the easy part.
        RandomAccessible<DoubleType> data = new FunctionRandomAccessible<>(
            2,
            (loc, out) -> out.set(loc.getLongPosition(0) + loc.getLongPosition(1)),
            DoubleType::new
        );

        // Datasets can wrap arbitrary RandomAccessible data. If no existing MetadataStore is provided,
        // a default empty one is created. Datasets wrap a RandomAccessible and a MetadataStore.
        Dataset<DoubleType> dataset = Dataset.wrap(data);

        // Metadata itself is a set of key-value pairs.
        // Each pair is defined by:
        // - a key tuple, consisting of:
        //   - a String key
        //   - a set of associated axes
        // - a RandomAccessible value (MetadataItem)
        MetadataStore store = dataset.store();

        // You can define any metadata you want under this paradigm:
        Axis x = new DefaultLinearAxis(Axes.X, 1, 0);
        store.add("axis_data", x.data(), 0);
        store.add("axis_type", x.type(), 0);

        // However imglib2-meta provides utilities for common metadata types.
        Axis y = new DefaultLinearAxis(Axes.Y, 1, 0);
        Calibration cal = Metadata.calibration(store);
        cal.setAxis(y, 1);

        Random rng = new Random(0xdeadbeefL);
        FunctionRealRandomAccessible<IntType> labels = new FunctionRealRandomAccessible<>(
            2,
            (pos, out) -> out.set(rng.nextInt(10)),
            IntType::new
        );
        store.add("labels", labels, 0, 1);

        return dataset.view().interval(new FinalInterval(100, 100));
    }

    public static void main(String[] args) {
        // TODO: How do we feel about the wildcard here?
        //      It's very difficult to avoid it while retaining interface inheritance.
        //      For example, DatasetInterval extending Dataset
        //      I don't think developers will be more hinered than before.
        //      I think most users are scripting anyways and won't encounter the type vars.
        DatasetInterval<DoubleType> data = loadDataset();

        // Perk 1 of design: Datasets are TYPE-SAFE and INTEROPERABLE with existing imglib2 code written for RAs
        System.out.println(someExistingFunctionality(data));
        // System.out.println(someExistingUnusableFunctionality(data)); // This line would not compile

        // Perk 2 of design: Datasets can be fluently viewed, and their metadata goes with.
        System.out.println("Axis 0 on the dataset is " + Metadata.calibration(data.store()).axis(0).type());
        Dataset<DoubleType> view = data.view().permute(0, 1);
        System.out.println("Axis 0 on the view is " + Metadata.calibration(view.store()).axis(0).type());

        // Perk 3 of design: MetadataItems are RAs and can be looped over along with their data.
        MetadataItem<IntType> labels = data.store().item("labels", IntType.class, 0, 1);
        LoopBuilder.setImages(data, labels.view().interval(data)).forEachPixel((d, l) -> {
            if (l.get() == 1) {
                System.out.println("Found a 1-valued label at data value " + d);
            }
            // You could do something really cool here...
        });

        String author = data.store().item("author", String.class).valueOr("unknown");
        System.out.println("Author is " + author);
    }

    private static <T> T someExistingFunctionality(RandomAccessible<T> rai) {
        if (rai.numDimensions() < 2)
            throw new IllegalArgumentException("Requires at 2 dimensions");
        return rai.getAt(1, 1);
    }

    @SuppressWarnings("unused")
    private static LongType someExistingUnusableFunctionality(RandomAccessible<LongType> rai) {
        if (rai.numDimensions() < 2)
            throw new IllegalArgumentException("Requires at 2 dimensions");
        return rai.getAt(1, 1);
    }
}
