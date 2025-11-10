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
package net.imglib2.meta.view;

import net.imglib2.meta.HasMetadataStore;
import net.imglib2.meta.MetadataItem;
import net.imglib2.meta.MetadataStore;
import net.imglib2.view.SubsampleView;

import java.util.Collection;
import java.util.stream.Collectors;

public class MetadataStoreSubsampleView implements MetadataStore {

	private final MetadataStore source;
	private final long[] steps;

	public MetadataStoreSubsampleView(MetadataStore store, long[] steps) {
		if (store.numDimensions() != steps.length) throw new IllegalArgumentException("BAD");
		this.source = store;
		this.steps = steps;
	}

    @Override
    public Collection<? extends MetadataItem<?>> items() {
        return source.items().stream() //
                .map(this::itemView) //
                .collect(Collectors.toList());
    }

	@Override
	public <T> MetadataItem<T> item(String key, Class<T> ofType, int... dims) {
		return itemView(source.item(key, ofType, dims));
	}

	@Override
	public <T extends HasMetadataStore> T info(Class<T> infoClass) {
		T srcStore = source.info(infoClass);
		srcStore.setStore(this);
		return srcStore;
	}

	@Override
	public <T> void add(MetadataItem<T> item) {
        // This theoretically would work...but it could have unintended consequences
        // if the caller does not know it is a view. It's probably best to keep it read-only.
        // If it is known to be a view, it's probably feasible to add the metadata to the source directly.
        // Not to mention metadata backed by RAIs would require interpolation of sorts.
        throw new UnsupportedOperationException("Subsample views on metadata are read-only");
	}


	@Override
	public int numDimensions() {
		return source.numDimensions();
	}

	private <T> MetadataItem<T> itemView(MetadataItem<T> result) {
		return new MetadataStoreSubsampleView.MetadataItemSubsampleView<>(result, steps);
	}

	private static class MetadataItemSubsampleView<T> extends SubsampleView<T> implements MetadataItem<T> {
		private final MetadataItem<T> source;

		public MetadataItemSubsampleView(MetadataItem<T> source, long[] steps) {
			super(source, steps);
			this.source = source;
		}

		@Override
		public String name() {
			return source.name();
		}

        @Override
        public int[] attachedAxes() {
            return source.attachedAxes();
        }

        @Override
        public int[] varyingAxes() {
            return source.varyingAxes();
        }

        @Override
        public T valueOr(T defaultValue) {
            T srcValueOr = source.valueOr(defaultValue);
            if (srcValueOr == defaultValue) {
                return srcValueOr;
            }
            if (srcValueOr instanceof Viewable) {
                return ((Viewable<T>) srcValueOr).transform(steps, source.attachedAxes());
            }
            return srcValueOr;
        }

        @Override
        public T value() {
            T srcValue = source.value();
            if (srcValue instanceof Viewable) {
                return ((Viewable<T>) srcValue).transform(steps, source.attachedAxes());
            }
            return srcValue;
        }
    }
}

