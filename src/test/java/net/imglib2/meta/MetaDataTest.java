package net.imglib2.meta;

import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.axis.DefaultLinearAxis;
import net.imglib2.Localizable;
import net.imglib2.img.list.ListImg;
import net.imglib2.meta.attribution.Attribution;
import net.imglib2.meta.calibration.Calibration;
import net.imglib2.position.FunctionRandomAccessible;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.MixedTransformView;
import net.imglib2.view.Views;
import org.junit.Test;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/** Tests {@link SimpleMetadataStore} functionality. */
public class MetaDataTest {

    @Test
    public void testEverything() {
        // create an image on the fly
        Supplier<DoubleType> s = DoubleType::new;
        BiConsumer<Localizable, ? super DoubleType> f = (l, t) -> {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < l.numDimensions(); i++) {
                sb.append(Math.abs(l.getLongPosition(i)));
            }
            t.set(Long.parseLong(sb.toString()));
        };
        FunctionRandomAccessible<DoubleType> image = new FunctionRandomAccessible<>(5, f, s);
        System.out.println(image.getAt(1,2,3,4,5));
        // rotate
        MixedTransformView<DoubleType> v = Views.rotate(image, 3, 2);
        System.out.println(v.getAt(1,2,3,4,5));
        // translate
//        v = Views.permute(image, 0, 1);
//        System.out.println(v.getAt(1, 2, 3, 4, 5));

        // Populate some metadata.

        MetadataStore d = new SimpleMetadataStore(5);
        d.add("author", "foo Selzer");

        Calibration calibration = d.info(Calibration.class);
        calibration.setAxis(new DefaultLinearAxis(Axes.X), 0);
        calibration.setAxis(new DefaultLinearAxis(Axes.Y), 1);
        calibration.setAxis(new DefaultLinearAxis(Axes.Z), 2);
        calibration.setAxis(new DefaultLinearAxis(Axes.CHANNEL), 3);
        calibration.setAxis(new DefaultLinearAxis(Axes.TIME), 4);

        ListImg<String> tables = new ListImg<>(Arrays.asList("red", "green", "blue"), 3);
        d.add("lut", tables, 3);

        // Query metadata type-unsafely (using key strings).
        Object authorObject = d.get("author").get().get();
        System.out.println(authorObject);

        // Query metadata type-safely (but still using a key string).
        String authorString = d.get("author", String.class).get().get();
        System.out.println(authorString);

        // An actually nice window into groups of metadata.
        Attribution attribution = d.attribution(); // d.info(Attribution.class);
        String author = attribution.author();
        String citation = attribution.citation();
        System.out.println(author);
        System.out.println(citation);

        //Calibration calibration = d.calibration(); // d.info(Calibration.class);
        AxisType axis0Type = calibration.axis(0).type();
        AxisType axis2Type = calibration.axis(2).type();
        System.out.println("Axis 0 type: " + axis0Type);
        System.out.println("Axis 2 type: " + axis2Type);

        System.out.println(d.get("axis", 0).get().get());
        System.out.println(d.get("axis", 2).get().get());

        MetadataItem<?> lutItem = d.get("lut", 3).get();
        System.out.println(lutItem.getAt(0, 0, 0, 0, 0));
        System.out.println(lutItem.getAt(0, 0, 0, 1, 0));
        System.out.println(lutItem.getAt(0, 0, 0, 2, 0));

        // Test viewing metadata based on a View of the data
        MetadataStore dView = d.view(v);
        lutItem = dView.get("lut", 2).get();
        System.out.println(lutItem.getAt(0, 0, 0, 0, 0));
        System.out.println(lutItem.getAt(0, 0, 1, 0, 0));
        System.out.println(lutItem.getAt(0, 0, 2, 0, 0));
    }
}
