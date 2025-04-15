package net.imglib2.meta;


import ij.ImagePlus;
import net.imagej.axis.Axes;
import net.imagej.axis.DefaultLinearAxis;
import net.imglib2.EuclideanSpace;
import net.imglib2.RandomAccessible;
import net.imglib2.RealRandomAccessible;
import net.imglib2.meta.calibration.Calibration;

import java.util.Optional;
import java.util.ServiceLoader;

public class ImagePlusMetadataStore implements MetadataStore {

    private final ImagePlus imp;

    public ImagePlusMetadataStore(ImagePlus imp) {
        this.imp = imp;
    }

    @Override
    public <T> Optional<MetadataItem<T>> get(String key, Class<T> ofType) {
        if (key.equals("name") && is(ofType, String.class)) {
            return constructMetadataItem(this, key, imp.getTitle(), ofType);

        }
        return Optional.empty();
    }

    @Override
    public <T> Optional<MetadataItem<T>> get(String key, int d, Class<T> ofType) {
        if (key.equals(Calibration.AXIS_KEY) && is(ofType, DefaultLinearAxis.class)) {
            net.imagej.axis.AxisType type = null;
            if (d == 0) {
                type = Axes.X;
            }
            else if (d == 1) {
                type = Axes.Y;
            }
            else if (d == 2) {
                type = imp.getNChannels() > 1 ? Axes.CHANNEL :
                        imp.getNSlices() > 1 ? Axes.Z :
                        imp.getNFrames() > 1 ? Axes.TIME :
                        null;
            }
            else if (d == 3) {
                type = imp.getNSlices() > 1 ? Axes.Z :
                        imp.getNFrames() > 1 ? Axes.TIME :
                        null;
            }
            else if (d == 4) {
                type = imp.getNFrames() > 1 ? Axes.TIME :
                                null;
            }

            if (type != null) {
                return constructMetadataItem(this, key, new DefaultLinearAxis(type), ofType, d);
            }
        }
        return Optional.empty();
    }

    private static <T> Optional<MetadataItem<T>> constructMetadataItem(
            EuclideanSpace space,
            final String name,
            final Object data,
            final Class<T> cls,
            final int... d
    ){
        if (!cls.isInstance(data)) {
            throw new IllegalArgumentException(data + "was not an instance of class" + cls);
        }
        //noinspection unchecked
        return Optional.of(Metadata.item(name, (T) data, space.numDimensions(), d));
    }

    @Override
    public <T extends HasMetadataStore> T info(Class<T> infoClass) {
        ServiceLoader<T> loader = ServiceLoader.load(infoClass);
        T instance = loader.iterator().next();
        instance.setStore(this);
        return instance;
    }

    @Override
    public <T> void add(String name, T data, int... dims) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public <T> void add(String name, RandomAccessible<T> data, int... dims) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public <T> void add(String name, RealRandomAccessible<T> data, int... dims) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public int numDimensions() {
        int axes = 2;
        if (imp.getNSlices() > 1) axes++;
        if (imp.getNChannels() > 1) axes++;
        if (imp.getNFrames() > 1) axes++;
        return axes;
    }

    private static <T, U> boolean is(Class<T> src, Class<U> tgt) {
        return src == null || src.isAssignableFrom(tgt);
    }

}
