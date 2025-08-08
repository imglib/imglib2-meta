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

import net.imglib2.RandomAccessible;
import net.imglib2.RealRandomAccessible;
import net.imglib2.realtransform.RealTransformRealRandomAccessible;
import net.imglib2.view.MixedTransformView;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

public class SimpleMetadataStore implements MetadataStore {

	private final List<MetadataItem<?>> items;
	private final RealTransformRealRandomAccessible<?,?> realView;
	private final int numDims;

	public SimpleMetadataStore(int n) {
		this.items = new ArrayList<>();
		this.realView = null;
		this.numDims = n;
	}

	public SimpleMetadataStore(List<MetadataItem<?>> items, MixedTransformView<?> view, int n) {
		this.items = items;
		this.realView = null;
		this.numDims = n;
	}

	public SimpleMetadataStore(List<MetadataItem<?>> items, RealTransformRealRandomAccessible<?, ?> realView, int n) {
		this.items = items;
		this.realView = realView;
		this.numDims = n;
	}

	@Override
	public <T> Optional<MetadataItem<T>> get(String key, Class<T> ofType) {
		//noinspection unchecked
		return items.stream() //
			.filter(item -> item.name().equals(key))
			.filter(item -> !item.isAttachedToAnyAxis())
			.filter(item -> ofType == null || ofType.isInstance(item.getType()))
			.map(item -> (MetadataItem<T>) item)
			.findFirst();
	}

	@Override
	public <T> Optional<MetadataItem<T>> get(String name, Class<T> ofType, int... d) {
		//noinspection unchecked
		return items.stream() //
			.filter(item -> item.name().equals(name))
			.filter(item -> item.isAttachedTo(d)) //
			.filter(item -> ofType == null || ofType.isInstance(item.getType()))
			.map(item -> (MetadataItem<T>) item)
			.findFirst();
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
		items.add(Metadata.item(name, data, numDims, dims));
	}

	@Override
	public <T, U extends RandomAccessible<T>> void add(String name, U data, int... dims) {
		items.add(Metadata.item(name, data, numDims, dims));
	}

	@Override
	public <T, U extends RealRandomAccessible<T>> void add(String name, U data, int... dims) {
		items.add(Metadata.item(name, data, numDims, dims));
	}

	@Override
	public int numDimensions() {
		return numDims;
	}

}
