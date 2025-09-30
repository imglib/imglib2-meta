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
import net.imglib2.Localizable;
import net.imglib2.RandomAccessible;
import net.imglib2.display.ColorTable;
import net.imglib2.imagej.LUTToColorTable;
import net.imglib2.meta.calibration.Axes;
import net.imglib2.meta.calibration.AxisType;
import net.imglib2.meta.channels.ColorTableRAI;
import net.imglib2.position.FunctionRandomAccessible;
import net.imglib2.type.numeric.real.DoubleType;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class ImagePlusMetadataStore implements MetadataStore {

    private final ImagePlus imp;

    public ImagePlusMetadataStore(ImagePlus imp) {
        this.imp = imp;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> MetadataItem<T> item(String key, Class<T> ofType, int... dims) {
        if (key.equals("name") && is(ofType, String.class)) {
            return Metadata.item(key, (T) imp.getTitle(), numDimensions());
        }
        if (key.equals("channel") && is(ofType, ColorTable.class)) {
            List<ColorTable> tables = Arrays.stream(imp.getLuts()) //
                    .map(LUTToColorTable::wrap) //
                    .collect(Collectors.toList());
            if (tables.isEmpty()) {
                throw new NoSuchElementException("RGB images have no LUTs");
            }
            if (dims.length == 0) {
                if (tables.size() != 1) {
                    throw new IllegalArgumentException("Color Tables must be associated with exactly one axis");
                }
                return Metadata.item(key, (T) tables.get(0), numDimensions());
            }
            if (dims.length == 1 && axisType(dims[0]) != Axes.CHANNEL) {
                throw new IllegalArgumentException("Axis " + dims[0] + " is not the channel axis!");
            }
            if (tables.isEmpty()) {
                throw new NoSuchElementException("No LUTs found");
            }
            ColorTableRAI ctable = new ColorTableRAI(tables);
            BiConsumer<Localizable, ColorTable> setter = (pos, table) -> ctable.setLut(pos.getIntPosition(0), table);
            return (MetadataItem<T>) Metadata.item(
                key,
                ctable,
                numDimensions(),
                setter,
                dims
            );
        }
        if (key.equals("axis_data") && is(ofType, DoubleType.class)) {
            // FIXME
            int d = dims[0];
            AxisType type = axisType(d);
            if (type != null) {
                FunctionRandomAccessible<DoubleType> data = new FunctionRandomAccessible<>(
                        1,
                        () -> (pos, out) -> out.set(pos.getDoublePosition(0)),
                        DoubleType:: new
                );
                return Metadata.item(
                    key,
                    (RandomAccessible<T>) data,
                    numDimensions(),
                    d
                );
            }
        }
        if (key.equals("axis_type") && is(ofType, AxisType.class)) {
            // FIXME
            int d = dims[0];
            AxisType type = axisType(d);
            if (type != null) {
                return Metadata.item(
                        key,
                        (T) type,
                        numDimensions(),
                        d
                );
            }
        }
        throw new NoSuchElementException();
    }

    @Override
    public <T> void add(String key, T data, int... dims) {
        throw new UnsupportedOperationException("Read-Only");
    }

    @Override
    public <T> void add(String key, RandomAccessible<T> data, int... dims) {
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
