package net.imglib2.meta;

import net.imglib2.Localizable;
import net.imglib2.img.list.ListImg;
import net.imglib2.position.FunctionRandomAccessible;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.MixedTransformView;
import net.imglib2.view.Views;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class Experiment {

    public static void main(String[] args) {
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

        MetaData d = new MetaData();
        d.items().add(new SimpleItem<>("author", "foo Selzer"));
        d.items().add(new SimpleItem<>("type", "x", new boolean[] {true, false, false, false, false}));
        d.items().add(new SimpleItem<>("type", "y", new boolean[] {false, true, false, false, false}));
        d.items().add(new SimpleItem<>("type", "z", new boolean[] {false, false, true, false, false}));
        d.items().add(new SimpleItem<>("type", "c", new boolean[] {false, false, false, true, false}));
        d.items().add(new SimpleItem<>("type", "t", new boolean[] {false, false, false, false, true}));

        ListImg<String> tables = new ListImg<>(Arrays.asList("red", "green", "blue"), 3);
        d.items().add(new VaryingItem<>(
                "lut",
                tables,
                new boolean[] {false, false, false, true, false},
                new boolean[] {false, false, false, true, false}
        ));

        System.out.println(d.get("author").get().get());
        System.out.println(d.get("type", 0).get().getAt());
        System.out.println(d.get("type", 2).get().getAt());

        MetaDataItem<?> lutItem = d.get("lut", 3).get();
        System.out.println(lutItem.getAt(0, 0, 0, 0, 0));
        System.out.println(lutItem.getAt(0, 0, 0, 1, 0));
        System.out.println(lutItem.getAt(0, 0, 0, 2, 0));

        // Test viewing metadata based on a View of the data
        MetaData dView = d.view(v);
        lutItem = dView.get("lut", 2).get();
        System.out.println(lutItem.getAt(0, 0, 0, 0, 0));
        System.out.println(lutItem.getAt(0, 0, 1, 0, 0));
        System.out.println(lutItem.getAt(0, 0, 2, 0, 0));
    }
}
