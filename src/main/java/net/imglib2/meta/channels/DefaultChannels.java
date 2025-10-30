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
package net.imglib2.meta.channels;

import net.imglib2.Point;
import net.imglib2.display.ColorTable;
import net.imglib2.meta.Metadata;
import net.imglib2.meta.MetadataItem;
import net.imglib2.meta.MetadataStore;
import net.imglib2.meta.calibration.Axes;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class DefaultChannels implements Channels {

	private static final Supplier<RuntimeException> NO_CHANNEL_AXIS_YET = //
			() -> new RuntimeException("The channel axis has not yet been set!");

	private MetadataStore metaData;

	private final Supplier<Point> pointSupplier = //
			() -> new Point(metaData.numDimensions());

	private final ThreadLocal<Point> pointCache = ThreadLocal.withInitial(pointSupplier);

	@Override
	public void setStore(MetadataStore store) {
		this.metaData = store;
	}

	@Override
	public ColorTable lut(int c) {
        MetadataItem<ColorTable> item = metaData.item(CHANNEL, ColorTable.class);
        int[] varyingAxes = item.varyingAxes();
        if (varyingAxes.length == 1) {
            int lutAxis = varyingAxes[0];
            Point point = pointCache.get();
            for (int i = 0; i < point.numDimensions(); i++) {
                point.setPosition(lutAxis == i ? c : 0, i);
            }
            return item.getAt(point);
        } else if (varyingAxes.length == 0){
            // One global LUT
            return item.value();
        }
        else {
            throw new IllegalStateException("LUTs vary along multiple axes, cannot get by channel index alone.");
        }
	}

	@Override
	public void setLut(int c, ColorTable lut) {
		int axis = Metadata.calibration(this.metaData)
				.indexOf(Axes.CHANNEL)
				.orElseThrow(NO_CHANNEL_AXIS_YET);
		MetadataItem<ColorTable> item = metaData.item(CHANNEL, ColorTable.class).or(() -> {
            // Create the item if it doesn't exist yet, and returnthat.
            // FIXME: This should really be a ListImg, but we don't know the number of channels (yet)
            ColorTableRAI newLut = new ColorTableRAI();
            // FIXME: Ideally we wouldn't do this here, but ColorTableRAI needs getType to work.
            // getType requires an element already in the map for that to work.
            newLut.setLut(c, lut);
            // FIXME: This is kind of a HACK :)
            metaData.add(
                CHANNEL,
                newLut,
                (pos, table) -> newLut.setLut(pos.getIntPosition(axis), table),
                new int[] {axis}
            );
            return metaData.item(CHANNEL, ColorTable.class);
        });
        Point point = pointCache.get();
        for (int i = 0; i < point.numDimensions(); i++) {
            point.setPosition(axis == i ? c : 0, i);
        }
        item.setAt(lut, point);
	}

	@Override
	public boolean isRGB() {
        return metaData.item(RGB_KEY, Boolean.class).valueOr(Boolean.FALSE);
	}

	@Override
	public void setRGB(boolean isRGB) {
		metaData.add(RGB_KEY, isRGB);
	}

}
