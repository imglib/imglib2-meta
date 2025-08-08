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

import net.imglib2.*;
import net.imglib2.transform.integer.MixedTransform;
import net.imglib2.view.SubsampleView;

import java.util.Optional;

public class MetadataStoreSubsampleView implements MetadataStore {

	private final MetadataStore source;
	private final long[] steps;

	public MetadataStoreSubsampleView(MetadataStore store, long[] steps) {
		if (store.numDimensions() != steps.length) throw new IllegalArgumentException("BAD");
		this.source = store;
		this.steps = steps;
	}

	@Override
	public <T> Optional<MetadataItem<T>> get(String key, Class<T> ofType) {
		return source.get(key, ofType);
	}

	@Override
	public <T> Optional<MetadataItem<T>> get(String key, Class<T> ofType, int... d) {
		return itemView(source.get(key, ofType, d));
	}

	@Override
	public <T extends HasMetadataStore> T info(Class<T> infoClass) {
		T srcStore = source.info(infoClass);
		srcStore.setStore(this);
		return srcStore;
	}

	@Override
	public <T> void add(String name, T data, int... dims) {
		throw new UnsupportedOperationException("View of metadata store is read-only");
	}

	@Override
	public <T, U extends RandomAccessible<T>> void add(String name, U data, int... dims) {
		throw new UnsupportedOperationException("View of metadata store is read-only");
	}

	@Override
	public <T, U extends RealRandomAccessible<T>> void add(String name, U data, int... dims) {
		throw new UnsupportedOperationException("View of metadata store is read-only");
	}

	@Override
	public int numDimensions() {
		return source.numDimensions();
	}

	@Override
	public MixedTransform transform() {
		throw new UnsupportedOperationException("FIXME");
	}

	private <T, U extends RandomAccessible<T>> Optional<MetadataItem<T>> itemView(
		@SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<MetadataItem<T>> result
	) {
		return result.map(item -> new MetadataStoreSubsampleView.MetadataItemSubsampleView<>(item, steps));
	}

	private static class MetadataItemSubsampleView<T> extends SubsampleView<T> implements MetadataItem<T> {
		private final MetadataItem<T> source;
		private final long[] steps;

		public MetadataItemSubsampleView(MetadataItem<T> source, long[] steps) {
			super(source, steps);
			this.source = source;
			// FIXME: The RA here is only <=N-dimensional not necessarily N-dimensional.
			this.steps = steps;
		}

		@Override
		public String name() {
			return source.name();
		}

		@Override
		public boolean[] attachedAxes() {
			return source.attachedAxes();
		}

		@Override
		public T getAt(RealLocalizable pos) {
			if (!(pos instanceof Localizable)) {
				throw new UnsupportedOperationException("Cannot use non-Localizable with metadata store view. YET!");
			}
			Localizable l = (Localizable) pos;

			// TODO: Don't create a new Point every time. ThreadLocal?
			final Point p = new Point(steps.length);
			for(int i = 0; i < l.numDimensions(); i++) {
				p.setPosition(l.getLongPosition(i) * steps[i], i);
			}
			return source.getAt(p);
		}
	}
}
