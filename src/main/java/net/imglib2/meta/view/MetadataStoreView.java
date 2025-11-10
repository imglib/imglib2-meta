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
import net.imglib2.transform.integer.Mixed;
import net.imglib2.transform.integer.MixedTransform;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public class MetadataStoreView implements MetadataStore {

	protected final MetadataStore source;
    protected final MixedTransform transform;
	// We want the inverse of transform.component for slicing
	private final int[] inverseComponentMapping;

	public MetadataStoreView(MetadataStore source, Mixed transform) {
		if (source instanceof MetadataStoreView) {
			MetadataStoreView msv = (MetadataStoreView) source;
			this.source = msv.source;
			this.transform = msv.transform.concatenate(transform);
		}
		else {
			this.source = source;
			this.transform = new MixedTransform(transform.numSourceDimensions(), transform.numTargetDimensions());
			this.transform.set(transform);
		}

		this.inverseComponentMapping = new int[ this.transform.numSourceDimensions() ];
		for ( int d = 0; d < this.transform.numTargetDimensions(); ++d )
		{
			if (!this.transform.getComponentZero(d) )
			{
				final int e = this.transform.getComponentMapping(d);
				this.inverseComponentMapping[ e ] = d;
			}
		}
		for ( int i = this.transform.numTargetDimensions(); i < this.transform.numSourceDimensions(); i++) {
			this.inverseComponentMapping[i] = i;
		}
	}

    @Override
    public Collection<? extends MetadataItem<?>> items() {
        return source.items().stream() //
                .filter(this::shouldIncludeItem) //
                .map(this::itemView) //
                .collect(Collectors.toList());
    }
    
    /**
     * Returns true if this metadata item should be included in the view.
     * Items attached ONLY to sliced-out axes should be excluded.
     */
    private boolean shouldIncludeItem(MetadataItem<?> item) {
        int[] attachedAxes = item.attachedAxes();
        if (attachedAxes.length == 0) {
            // Not attached to any axes, always include
            return true;
        }

        // Check if at least one attached axis is still present in the view
        for (int sourceAxis : attachedAxes) {
            if (!transform.getComponentZero(sourceAxis)) {
                return true; // This attached axis is still present
            }
        }

        // All attached axes were sliced out
        return false;
    }

	@Override
	public <T> MetadataItem<T> item(String key, Class<T> ofType, int... dims) {
		final int[] dd = new int[dims.length];
		for(int i = 0; i < dd.length; i++) {
			if (inverseComponentMapping.length <= dims[i]) {
				throw new IllegalArgumentException("Dimensions " + Arrays.toString(dims) + " is not present in the source metadata.");
			}
			dd[i] = inverseComponentMapping[dims[i]];
		}
		return itemView(source.item(key, ofType, dd));
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
        // Even if it is known to be a view, it's probably feasible to add the metadata to the source directly.
        throw new UnsupportedOperationException("Views on metadata are read-only");
	}

	@Override
	public int numDimensions() {
		return transform.numSourceDimensions();
	}

	private <T> MetadataItem<T> itemView(MetadataItem<T> result ) {
		return new MetadataItemView<>(result, transform);
	}

}
