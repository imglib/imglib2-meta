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

import net.imglib2.display.ColorTable;
import net.imglib2.meta.HasMetadataStore;

/**
 * Metadata describing channel information.
 *
 * @author Curtis Rueden
 * @author Gabriel Selzer
 */
public interface Channels extends HasMetadataStore {
    /** canonical imglib2-meta key for {@link ColorTable} items */
	String CHANNEL = "channel";
    /** canonical imglib2-meta key for (A)RGB indication */
	String RGB_KEY = "is_rgb";

    /**
     * Retrieves the lookup table associated with a given channel.
     *
     * @param c an index along the channel dimension
     * @return the {@link ColorTable} associated with channel {@code c}
     */
	ColorTable lut(int c);

    /**
     * Sets the lookup table associated with a given channel.
     *
     * @param c an index along the channel dimension
     * @param lut the {@link ColorTable} to set for channel {@code c}
     */
	void setLut(int c, ColorTable lut);

    /**
     * Describes whether this image is to be interpreted as RGB(A).
     *
     * @return {@code true} iff the image is RGB(A)
     */
	boolean isRGB();

    /**
     * Prescribes whether this image is to be interpreted as RGB(A).
     *
     * @param isRGB {@code true} iff the image is RGB(A)
     */
	void setRGB(boolean isRGB);
}
