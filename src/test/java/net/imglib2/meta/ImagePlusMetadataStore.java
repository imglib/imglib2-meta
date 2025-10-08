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
import net.imglib2.img.list.ListImg;
import net.imglib2.img.list.ListRandomAccess;
import net.imglib2.meta.attribution.Attribution;
import net.imglib2.meta.calibration.Axes;
import net.imglib2.meta.calibration.AxisType;
import net.imglib2.meta.calibration.Calibration;
import net.imglib2.meta.channels.Channels;
import net.imglib2.meta.channels.ColorTableRAI;
import net.imglib2.meta.general.General;
import net.imglib2.position.FunctionRandomAccessible;
import net.imglib2.type.numeric.real.DoubleType;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * A {@link MetadataStore} wrapping metadata pulled from an {@link ImagePlus}.
 *
 * @author Gabriel Selzer
 */
public class ImagePlusMetadataStore implements MetadataStore {

    private final ImagePlus imp;

    public ImagePlusMetadataStore(ImagePlus imp) {
        this.imp = imp;
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
        // X & Y
        int axes = 2;
        // Z
        if (imp.getNSlices() > 1) axes++;
        // C
        if (imp.getNChannels() > 1) axes++;
        // T
        if (imp.getNFrames() > 1) axes++;
        return axes;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> MetadataItem<T> item(String key, Class<T> ofType, int... dims) {
        // For clean code, delegate each key to its own handler function
        switch (key) {
            case General.NAME:
                return (MetadataItem<T>) handleName(ofType);
            case Calibration.AXIS_TYPE:
                return (MetadataItem<T>) handleAxisType(ofType, dims);
            case Calibration.AXIS_DATA:
                return (MetadataItem<T>) handleAxisData(ofType, dims);
            case Calibration.AXIS_UNITS:
                return (MetadataItem<T>) handleAxisUnits(ofType, dims);
            case Channels.CHANNEL:
                return (MetadataItem<T>) handleChannel(ofType, dims);
            default:
                throw new NoSuchElementException("No metadata item with key " + key);
        }
    }

    private <T> MetadataItem<ColorTable> handleChannel(Class<T> ofType, int[] dims) {
        if (isNot(ofType, ColorTable.class)) {
            throw new IllegalArgumentException("name must be of type String");
        }

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
            return Metadata.item("channel", tables.get(0), numDimensions());
        }
        if (dims.length == 1 && axisType(dims[0]) != Axes.CHANNEL) {
            throw new IllegalArgumentException("Axis " + dims[0] + " is not the channel axis!");
        }
        ListImg<ColorTable> ctable = new ListImg<>(tables, tables.size());
        return Metadata.item(
                Channels.CHANNEL,
                ctable,
                numDimensions(),
                dims
        );
    }

    private <T> MetadataItem<DoubleType> handleAxisData(Class<T> ofType, int[] dims) {
        if (isNot(ofType, DoubleType.class)) {
            throw new IllegalArgumentException("axis_data must be of doubles!");
        }
        if (dims == null || dims.length != 1) {
            throw new IllegalArgumentException("axis_data must be associated with exactly one axis (got " + (dims == null ? 0 : dims.length) + ")");
        }
        int d = dims[0];
        AxisType type = axisType(d);
        if (type == null) {
            throw new IllegalArgumentException("Cannot determine AxisType for axis " + d);
        }
        FunctionRandomAccessible<DoubleType> data = new FunctionRandomAccessible<>(
                1,
                () -> (pos, out) -> out.set(calibration(type) * pos.getDoublePosition(0)),
                DoubleType:: new
        );
        return Metadata.item(
            Calibration.AXIS_DATA,
            data,
            numDimensions(),
            d
        );
    }

    private <T> MetadataItem<String> handleAxisUnits(Class<T> ofType, int[] dims) {
        if (isNot(ofType, String.class)) {
            throw new IllegalArgumentException("axis_units must be strings!");
        }
        if (dims == null || dims.length != 1) {
            throw new IllegalArgumentException("axis_units must be associated with exactly one axis (got " + (dims == null ? 0 : dims.length) + ")");
        }
        int d = dims[0];
        AxisType type = axisType(d);
        if (type == null) {
            throw new IllegalArgumentException("Cannot determine AxisType for axis " + d);
        }
        return Metadata.item(
                Calibration.AXIS_UNITS,
                units(type),
                numDimensions(),
                d
        );
    }

    private <T> MetadataItem<AxisType> handleAxisType(Class<T> ofType, int[] dims) {
        if (isNot(ofType, AxisType.class)) {
            throw new IllegalArgumentException("axis_type must be of type AxisType");
        }
        if (dims == null || dims.length != 1) {
            throw new IllegalArgumentException("axis_type must be associated with exactly one axis (got " + (dims == null ? 0 : dims.length) + ")");
        }
        int axisIndex = dims[0];
        return Metadata.item(
            Calibration.AXIS_TYPE,
            axisType(axisIndex),
            numDimensions(),
            axisIndex
        );
    }

    private <T> MetadataItem<String> handleName(Class<T> ofType) {
        if (isNot(ofType, String.class)) {
            throw new IllegalArgumentException("name must be of type String");
        }
        return Metadata.item(General.NAME, imp.getTitle(), numDimensions());
    }

    private static <T, U> boolean isNot(Class<T> src, Class<U> tgt) {
        return src != null && !src.isAssignableFrom(tgt);
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

    private double calibration(final AxisType type) {
        switch (type.getLabel()) {
            case "X":
                return imp.getCalibration().pixelWidth;
            case "Y":
                return imp.getCalibration().pixelHeight;
            case "Z":
                return imp.getCalibration().pixelDepth;
            case "Time":
                return imp.getCalibration().frameInterval;
            default:
                return 1.0;
        }
    }

    private String units(final AxisType type) {
        switch (type.getLabel()) {
            case "X":
                return imp.getCalibration().getXUnit();
            case "Y":
                return imp.getCalibration().getYUnit();
            case "Z":
                return imp.getCalibration().getZUnit();
            case "Time":
                return imp.getCalibration().getTimeUnit();
            default:
                return "";
        }
    }

}
