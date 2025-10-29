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
package net.imglib2.meta.real;

import net.imglib2.RealRandomAccessible;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A <b>real</b>ly simple implementation of {@link RealMetadataStore} that keeps all metadata
 * items in a list...
 *
 * @author Gabriel Selzer
 */
public class RealSimpleMetadataStore implements RealMetadataStore {

	private final List<RealMetadataItem<?>> items;
	private final int numDims;

	public RealSimpleMetadataStore(int n) {
		this.items = new ArrayList<>();
		this.numDims = n;
	}

    @Override
    public Collection<? extends RealMetadataItem<?>> items() {
        // Wrap all items from source MetadataStore as RealMetadataItems
        return items;
    }

	@Override
	public <T> RealMetadataItem<T> item(String name, Class<T> ofType, int... dims) {
		//noinspection unchecked
		return items.stream() //
			.filter(item -> item.name().equals(name))
			.filter(item -> item.isAttachedTo(dims)) //
			.filter(item -> ofType == null || ofType.isInstance(item.getType()))
			.map(item -> (RealMetadataItem<T>) item)
            .findFirst().orElseGet(() -> RealMetadata.absent(name, numDims, dims));
	}

	@Override
	public <T> void add(String key, T data, int... dims) {
		items.add(RealMetadata.item(key, data, numDims, dims));
	}

	@Override
	public <T> void add(String name, RealRandomAccessible<T> data, int... d) {
		items.add(RealMetadata.item(name, data, numDims, d));
	}

	@Override
	public int numDimensions() {
		return numDims;
	}

}
