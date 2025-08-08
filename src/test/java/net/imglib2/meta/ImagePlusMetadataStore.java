/*-
 * #%L
 * Metadata for ImgLib2
 * %%
 * Copyright (C) 2016 - 2025 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
 * John Bogovic, Albert Cardona, Barry DeZonia, Christian Dietz, Jan Funke,
 * Aivar Grislis, Jonathan Hale, Grant Harris, Stefan Helfrich, Mark Hiner,
 * Martin Horn, Steffen Jaensch, Lee Kamentsky, Larry Lindsey, Melissa Linkert,
 * Mark Longair, Brian Northan, Nick Perry, Curtis Rueden, Johannes Schindelin,
 * Gabriel Selzer, Jean-Yves Tinevez and Michael Zinsmaier.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package net.imglib2.meta;


import ij.ImagePlus;
import ij.process.LUT;
import net.imglib2.RandomAccessible;
import net.imglib2.RealRandomAccessible;
import net.imglib2.display.ColorTable;
import net.imglib2.imagej.LUTToColorTable;
import net.imglib2.img.list.ListImg;
import net.imglib2.meta.calibration.Axes;
import net.imglib2.meta.calibration.AxisType;
import net.imglib2.meta.calibration.Calibration;
import net.imglib2.meta.calibration.DefaultCalibration;
import net.imglib2.meta.channels.ColorTableHolder;
import net.imglib2.meta.channels.ColorTableRAI;
import net.imglib2.position.FunctionRandomAccessible;
import net.imglib2.type.numeric.real.DoubleType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

public class ImagePlusMetadataStore implements MetadataStore {

    private final ImagePlus imp;

    public ImagePlusMetadataStore(ImagePlus imp) {
        this.imp = imp;
    }

    @Override
    public <T> Optional<MetadataItem<T>> get(String key, Class<T> ofType) {
        if (key.equals("name") && is(ofType, String.class)) {
            return Optional.of(Metadata.item(key, (T) imp.getTitle(), numDimensions()));
        }
        if (key.equals("channel") && is(ofType, ColorTable.class)) {
            LUT[] luts = imp.getLuts();
            if (luts.length == 0) {
                return Optional.empty();
            }
            return Optional.of(Metadata.item(
                key,
                (T) LUTToColorTable.wrap(luts[0]),
                numDimensions()
            ));
        }
        return Optional.empty();
    }

    @Override
    public <T> Optional<MetadataItem<T>> get(String key, Class<T> ofType, int... dims) {
        // FIXME
        int d = dims[0];
        if (key.equals("channel") && is(ofType, ColorTableHolder.class)) {
            if (axisType(d) == Axes.CHANNEL) {
                List<ColorTable> tables = Arrays.stream(imp.getLuts()) //
                    .map(LUTToColorTable::wrap) //
                    .collect(Collectors.toList());
                return Optional.of(Metadata.item(
                    key,
                    (RandomAccessible<T>) new ColorTableRAI(tables),
                    numDimensions(),
                    d
                ));
            }
//            else if (imp.isRGB()) {
//                ColorTable.
//            }
        }
        if (key.equals("axis_data") && is(ofType, DoubleType.class)) {
            AxisType type = axisType(d);
            if (type != null) {
                FunctionRandomAccessible<DoubleType> data = new FunctionRandomAccessible<>(
                        1,
                        () -> (pos, out) -> out.set(pos.getDoublePosition(0)),
                        DoubleType:: new
                );
                return Optional.of(Metadata.item(
                    key,
                    (RandomAccessible<T>) data,
                    numDimensions(),
                    d
                ));
            }
        }
        if (key.equals("axis_type") && is(ofType, AxisType.class)) {
            AxisType type = axisType(d);
            if (type != null) {
                return Optional.of(Metadata.item(
                        key,
                        (T) type,
                        numDimensions(),
                        d
                ));
            }
        }
        return Optional.empty();
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
        throw new UnsupportedOperationException("Read-Only");
    }

    @Override
    public <T, U extends RandomAccessible<T>> void add(String name, U data, int... dims) {
        throw new UnsupportedOperationException("Read-Only");
    }

    @Override
    public <T, U extends RealRandomAccessible<T>> void add(String name, U data, int... dims) {
        throw new UnsupportedOperationException("Read-Only");
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

    private AxisType axisType(final int d) {
        if (d == 0) {
            return Axes.X;
        }
        else if (d == 1) {
            return Axes.Y;
        }
        else if (d == 2) {
            return imp.getNChannels() > 1 ? Axes.CHANNEL :
                    imp.getNSlices() > 1 ? Axes.Z :
                            imp.getNFrames() > 1 ? Axes.TIME :
                                    null;
        }
        else if (d == 3) {
            return imp.getNSlices() > 1 && imp.getNChannels() > 1 ? Axes.Z :
                    imp.getNFrames() > 1 ? Axes.TIME :
                            null;
        }
        else if (d == 4) {
            return imp.getNFrames() > 1 ? Axes.TIME : null;
        }
        return null;
    }

}
