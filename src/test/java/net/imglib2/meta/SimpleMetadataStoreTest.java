package net.imglib2.meta;

import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.axis.CalibratedAxis;
import net.imagej.axis.DefaultLinearAxis;
import net.imagej.axis.TypedAxis;
import net.imglib2.Localizable;
import net.imglib2.RealLocalizable;
import net.imglib2.RealRandomAccessible;
import net.imglib2.img.list.ListImg;
import net.imglib2.meta.attribution.Attribution;
import net.imglib2.meta.calibration.Calibration;
import net.imglib2.position.FunctionRandomAccessible;
import net.imglib2.position.FunctionRealRandomAccessible;
import net.imglib2.realtransform.AffineGet;
import net.imglib2.realtransform.AffineRealRandomAccessible;
import net.imglib2.realtransform.RealViews;
import net.imglib2.realtransform.ScaleAndTranslation;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.MixedTransformView;
import net.imglib2.view.Views;
import org.junit.Test;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static org.junit.Assert.*;

/** Tests {@link SimpleMetadataStore} functionality. */
public class SimpleMetadataStoreTest {

    @Test
    public void testInteger() {
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
        assertEquals(12345.0, image.getAt(1,2,3,4,5).get(), 0.0);
        // rotate
        MixedTransformView<DoubleType> v = Views.rotate(image, 3, 2);
        assertEquals(12435.0, v.getAt(1,2,3,4,5).get(), 0.0);
        // translate
//        v = Views.permute(image, 0, 1);
//        System.out.println(v.getAt(1, 2, 3, 4, 5));

        // Populate some metadata.

        MetadataStore store = new SimpleMetadataStore(5);
        store.add("author", "foo Selzer");

        Calibration calibration = Metadata.calibration(store);
        calibration.setAxis(new DefaultLinearAxis(Axes.X), 0);
        calibration.setAxis(new DefaultLinearAxis(Axes.Y), 1);
        calibration.setAxis(new DefaultLinearAxis(Axes.Z), 2);
        calibration.setAxis(new DefaultLinearAxis(Axes.CHANNEL), 3);
        calibration.setAxis(new DefaultLinearAxis(Axes.TIME), 4);

        ListImg<String> tables = new ListImg<>(Arrays.asList("red", "green", "blue"), 3);
        store.add("lut", tables, 3);

        // Query metadata type-unsafely (using key strings).
        Object authorObject = store.get("author").get().get();
        assertEquals("foo Selzer", authorObject);

        // Query metadata type-safely (but still using a key string).
        String authorString = store.get("author", String.class).get().get();
        assertEquals("foo Selzer", authorString);

        // An actually nice window into groups of metadata.
        Attribution attribution = Metadata.attribution(store);
        String author = attribution.author();
        String citation = attribution.citation();
        assertEquals("foo Selzer", author);
        assertNull(citation);

        AxisType axis0Type = calibration.axis(0).type();
        AxisType axis2Type = calibration.axis(2).type();
        assertSame(Axes.X, axis0Type);
        assertSame(Axes.Z, axis2Type);

        CalibratedAxis axis0 = store.get("axis", 0, CalibratedAxis.class).get().get();
        CalibratedAxis axis2 = store.get("axis", 2, CalibratedAxis.class).get().get();
        assertSame(Axes.X, axis0.type());
        assertSame(Axes.Z, axis2.type());

        MetadataItem<?> lutItem = store.get("lut", 3).get();
        assertEquals("red", lutItem.getAt(0, 0, 0, 0, 0));
        assertEquals("green", lutItem.getAt(0, 0, 0, 1, 0));
        assertEquals("blue", lutItem.getAt(0, 0, 0, 2, 0));

        // Test viewing metadata based on a View of the data
        MetadataStore storeView = Metadata.view(store, v);
        lutItem = storeView.get("lut", 2).get();
        assertEquals("red", lutItem.getAt(0, 0, 0, 0, 0));
        assertEquals("green", lutItem.getAt(0, 0, 1, 0, 0));
        assertEquals("blue", lutItem.getAt(0, 0, 2, 0, 0));
    }

    @Test
    public void testReal() {
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
        assertEquals(1.02030405E9, image.getAt(1,2,3,4,5).get(), 0.0);
        // rotate
        double[] scales = {1.1, 2.2, 3.3, 4.4, 5.5};
        double[] translations = {9.9, 8.8, 7.7, 6.6, 5.5};
        AffineGet transform = new ScaleAndTranslation(scales, translations);
        AffineRealRandomAccessible<DoubleType, AffineGet> v = RealViews.affineReal(image, transform);
        assertEquals(8.0909090909090918E17, v.getAt(1,2,3,4,5).get(), 0.0);

        // Populate some metadata.

        MetadataStore store = new SimpleMetadataStore(5);

        RealRandomAccessible<StringBuilder> coordStrings = makeCoordStrings();
        store.add("coords", coordStrings, 0, 1, 2);

        MetadataItem<?> coordsItem = store.get("coords", 0).get();
        assertEquals("(0.3, 0.6, 0.9)", coordsItem.getAt(0.3, 0.6, 0.9, 1.2, 1.5).toString());

        // Test viewing metadata based on a View of the data
        MetadataStore storeRealView = Metadata.view(store, v);
        coordsItem = storeRealView.get("coords", 2).get();
        assertEquals("(-8.090909090909092, -3.090909090909091, -1.4242424242424243)", coordsItem.getAt(1, 2, 3, 4, 5).toString());
        assertEquals("(-4.454545454545455, -1.272727272727273, -0.21212121212121238)", coordsItem.getAt(5, 6, 7, 8, 9).toString());
        assertEquals("(-18.09090909090909, -9.0, -5.969696969696971)", coordsItem.getAt(-10, -11, -12, -13, -14).toString());
    }

    private RealRandomAccessible<StringBuilder> makeCoordStrings() {
        Supplier<StringBuilder> s = StringBuilder::new;
        BiConsumer<RealLocalizable, ? super StringBuilder> f = (l, t) -> {
            t.append("(").append(l.getDoublePosition(0))
              .append(", ").append(l.getDoublePosition(1))
              .append(", ").append(l.getDoublePosition(2))
              .append(")");
        };
        return new FunctionRealRandomAccessible<>(5, f, s);
    }
}
