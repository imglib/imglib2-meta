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

import net.imglib2.Localizable;
import net.imglib2.RandomAccessible;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;

public class SimpleMetadataStore implements MetadataStore {

	private final List<MetadataItem<?>> items;
	private final int numDims;

	public SimpleMetadataStore(int n) {
		this.items = new ArrayList<>();
		this.numDims = n;
	}

	@Override
	public <T> MetadataItem<T> item(String name, Class<T> ofType, int... dims) {
		//noinspection unchecked
		return items.stream() //
			.filter(item -> item.name().equals(name))
			.filter(item -> item.isAttachedTo(dims)) //
			.filter(item -> ofType == null || ofType.isInstance(item.getType()))
			.map(item -> (MetadataItem<T>) item)
			.findFirst().orElseThrow(NoSuchElementException::new);
	}

    @Override
	public <T> void add(String key, T data, int... dims) {
		items.add(Metadata.item(key, data, numDims, dims));
	}

	@Override
	public <T> void add(String key, RandomAccessible<T> data, int... dims) {
		items.add(Metadata.item(key, data, numDims, dims));
	}

    @Override
    public <T> void add(String key, RandomAccessible<T> data, BiConsumer<Localizable, T> setter, int... dims) {
        items.add(Metadata.item(key, data, numDims, setter, dims));
    }

	@Override
	public int numDimensions() {
		return numDims;
	}

}
