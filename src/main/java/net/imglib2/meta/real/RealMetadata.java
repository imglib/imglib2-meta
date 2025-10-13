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

import net.imglib2.*;
import net.imglib2.meta.MetadataItem;
import net.imglib2.meta.MetadataStore;
import net.imglib2.meta.MetadataStoreView;
import net.imglib2.meta.attribution.Attribution;
import net.imglib2.meta.calibration.Calibration;
import net.imglib2.meta.channels.Channels;
import net.imglib2.meta.general.General;
import net.imglib2.realtransform.RealTransform;
import net.imglib2.realtransform.RealTransformRealRandomAccessible;
import net.imglib2.transform.integer.MixedTransform;
import net.imglib2.util.ConstantUtils;
import net.imglib2.view.MixedTransformView;

import java.util.Arrays;

public final class RealMetadata {

	private RealMetadata() { }

	public static Attribution attribution(MetadataStore store) {
		return store.info(Attribution.class);
	}

	public static Calibration calibration(MetadataStore store) {
		return store.info(Calibration.class);
	}

	public static General general(MetadataStore store) {
		return store.info(General.class);
	}

	public static Channels channels(MetadataStore store) {
		return store.info(Channels.class);
	}

	/**
	 * Creates a {@link MetadataItem}.
	 *
	 * @param key the {@link String} key associated with the item
	 * @param data the metadata
	 * @param numDims the number of dimensions in which this item lives; or, the number of dimensions of the dataset this {@link MetadataItem} attaches to.
	 * @param dims the dimension indices to which this item pertains.
	 * @return a {@link MetadataItem}
	 * @param <T> the type of the metadata
	 */
	public static <T> RealMetadataItem<T> item(String key, T data, int numDims, int... dims) {
		boolean[] axes = makeAxisAttachmentArray(numDims, dims);
		return new SimpleRealItem<>(key, data, axes);
	}

	public static <T, U extends RealRandomAccessible<T>> RealMetadataItem<T> item(String name, U data, int numDims, int... dims) {
		boolean[] axes = makeAxisAttachmentArray(numDims, dims);
		// TODO: What if varying axes and attached axes are not the same?
		return new VaryingRealItem<>(name, data, axes);
	}

	public static MetadataStore view(MetadataStore source, MixedTransformView<?> view) {
		return view(source, view.getTransformToSource());
	}

	public static MetadataStore view(MetadataStore source, MixedTransform transform) {
		return new MetadataStoreView(source, transform);
	}

//	public static MetadataStore view(MetadataStore source, RealTransformRealRandomAccessible<?,?> view) {
//		return view(source, view.getTransformToSource());
//	}
//
//	public static MetadataStore view(MetadataStore source, RealTransform transform) {
//		return new MetadataStoreRealView(source, transform);
//	}
//
	private static boolean[] makeAxisAttachmentArray(int numDims, int... dims) {
		boolean[] attachedToAxes = new boolean[numDims];
        for (int dim : dims) attachedToAxes[dim] = true;
		return attachedToAxes;
	}

	private static int[] flagsToAxisList( final boolean[] flags ) {
		final int[] tmp = new int[ flags.length ];
		int i = 0;
		for ( int d = 0; d < flags.length; ++d )
			if ( flags[ d ] )
				tmp[ i++ ] = d;
		return Arrays.copyOfRange( tmp, 0, i );
	}

	/**
	 * A {@link RealMetadataItem} that constructs
	 * @param <T>
	 */
	private static class SimpleRealItem<T> implements RealMetadataItem<T> {
		final String name;

		final T data;

		final boolean[] attachedToAxes;

		public SimpleRealItem(final String name, final T data, final boolean[] attachedToAxes) {
			this.name = name;
			this.data = data;
			this.attachedToAxes = attachedToAxes;
		}

		public boolean[] attachedAxes() {
			return attachedToAxes;
		}

		@Override
		public String name() {
			return name;
		}

		@Override
		public RealRandomAccess<T> realRandomAccess() {
			return ConstantUtils.constantRealRandomAccessible(data, numDimensions()).realRandomAccess();
		}

		@Override
		public RealRandomAccess<T> realRandomAccess(RealInterval interval) {
			return ConstantUtils.constantRealRandomAccessible(data, numDimensions()).realRandomAccess(interval);
		}

		@Override
		public int numDimensions() {
			return attachedToAxes.length;
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder("SimpleItem \"");
			sb.append(name);
			sb.append("\"; ");

			if (isAttachedToAnyAxis()) {
				sb.append("attached to axes {");
				final int[] axes = flagsToAxisList(attachedToAxes);
				sb.append(axes[0]);
				for (int i = 1; i < axes.length; ++i)
					sb.append(", ").append(axes[i]);
				sb.append("}; ");
			} else
				sb.append("not attached to any axis; ");

			sb.append("value = ").append(data);

			return sb.toString();
		}
	}

	private static RealTransform transformFromAttachedAxes(boolean[] attachedToAxes) {
		int numAttachedAxes = 0;
		for (boolean b: attachedToAxes) if (b) numAttachedAxes++;
		final int targetDims = numAttachedAxes;



		int[] componentMapping = new int[numAttachedAxes];
		int mapIdx = 0;
		for (int i = 0; i < attachedToAxes.length; i++ ){
			if (attachedToAxes[i]){
				componentMapping[mapIdx++] = i;
			}
		}

		return new RealTransform() {
			@Override
			public int numSourceDimensions() {
				return attachedToAxes.length;
			}

			@Override
			public int numTargetDimensions() {
				return targetDims;
			}

			@Override
			public void apply(double[] doubles, double[] doubles1) {
				for(int i = 0; i < doubles.length; i++) {
					if (attachedToAxes[i]) doubles1[i] = doubles[componentMapping[i]];
				}
			}

			@Override
			public void apply(RealLocalizable realLocalizable, RealPositionable realPositionable) {
				for(int i = 0; i < realLocalizable.numDimensions(); i++) {
					if (attachedToAxes[i]){
						realPositionable.setPosition(realLocalizable.getDoublePosition(componentMapping[i]),i);
					}
				}

			}

			@Override
			public RealTransform copy() {
				return transformFromAttachedAxes(attachedToAxes);
			}
		};
	}

	private static class VaryingRealItem<T, F extends RealRandomAccessible<T>> extends RealTransformRealRandomAccessible<T, RealTransform> implements RealMetadataItem<T> {
		final String name;

		final F data;

		final boolean[] attachedToAxes;

		public VaryingRealItem(final String name, final F data, final boolean[] attachedToAxes) {
			super(data, transformFromAttachedAxes(attachedToAxes));
			this.name = name;
			this.data = data;
			this.attachedToAxes = attachedToAxes;
		}

		@Override
		public String name() {
			return name;
		}

		@Override
		public boolean[] attachedAxes() {
			return attachedToAxes;
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder("VaryingItem \"");
			sb.append(name);
			sb.append("\"; ");

			if (isAttachedToAnyAxis()) {
				sb.append("attached to axes {");
				final int[] axes = flagsToAxisList(attachedToAxes);
				sb.append(axes[0]);
				for (int i = 1; i < axes.length; ++i)
					sb.append(", ").append(axes[i]);
				sb.append("}; ");
			} else
				sb.append("not attached to any axis; ");

			return sb.toString();
		}

	}

}
