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
import net.imglib2.meta.attribution.Attribution;
import net.imglib2.meta.calibration.Calibration;
import net.imglib2.meta.channels.Channels;
import net.imglib2.meta.general.General;
import net.imglib2.realtransform.RealTransform;
import net.imglib2.realtransform.RealTransformRealRandomAccessible;
import net.imglib2.transform.integer.Mixed;
import net.imglib2.transform.integer.MixedTransform;
import net.imglib2.util.ConstantUtils;
import net.imglib2.view.MixedTransformView;

import java.util.Arrays;

public final class Metadata {

	private Metadata() { }

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
	 * @param data
	 * @param numDims the number of dimensions in which this item lives; or, the number of dimensions of the dataset this {@link MetadataItem} attaches to.
	 * @param dims the dimension indices to which this item pertains.
	 * @return a {@link MetadataItem}
	 * @param <T>
	 */
	public static <T> MetadataItem<T> item(String key, T data, int numDims, int... dims) {
		boolean[] axes = makeAxisAttachmentArray(numDims, dims);
		return new SimpleItem<>(key, data, axes);
	}

	public static <T, U extends RandomAccessible<T>> MetadataItem<T> item(String name, U data, int numDims, int... dims) {
		boolean[] axes = makeAxisAttachmentArray(numDims, dims);
		return new VaryingItem<>(name, data, axes);
	}

	public static <T, U extends RealRandomAccessible<T>> MetadataItem<T> item(String name, U data, int numDims, int... dims) {
		boolean[] axes = makeAxisAttachmentArray(numDims, dims);
		// TODO: What if varying axes and attached axes are not the same?
		return new VaryingRealItem<>(name, data, axes, axes);
	}

	public static MetadataStore view(MetadataStore source, MixedTransformView<?> view) {
		return view(source, view.getTransformToSource());
	}

	public static MetadataStore view(MetadataStore source, MixedTransform transform) {
		return new MetadataStoreView(source, transform);
	}

	public static MetadataStore view(MetadataStore source, RealTransformRealRandomAccessible<?,?> view) {
		return view(source, view.getTransformToSource());
	}

	public static MetadataStore view(MetadataStore source, RealTransform transform) {
		return new MetadataStoreRealView(source, transform);
	}

	private static boolean[] makeAxisAttachmentArray(int numDims, int... dims) {
		boolean[] attachedToAxes = new boolean[numDims];
		for (int d=0; d<dims.length; d++) attachedToAxes[dims[d]] = true;
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

	private static class SimpleItem<T> implements MetadataItem<T> {
		final String name;

		final T data;

		final boolean[] attachedToAxes;

		public SimpleItem(final String name, final T data, final boolean[] attachedToAxes) {
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
		public T getAt(RealLocalizable pos) {
			return data;
		}

		@Override
		public RandomAccess<T> randomAccess() {
			return ConstantUtils.constantRandomAccessible(data, numDimensions()).randomAccess();
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
					sb.append(", " + axes[i]);
				sb.append("}; ");
			} else
				sb.append("not attached to any axis; ");

			sb.append("value = " + data);

			return sb.toString();
		}
	}

	private static Mixed transformFromAttachedAxes(boolean[] attachedToAxes) {
		int srcDims = attachedToAxes.length, targetDims = 0;
		for (boolean b: attachedToAxes) if (b) targetDims++;

		int[] componentMapping = new int[targetDims];
		int mapIdx = 0;
		for (int i = 0; i < attachedToAxes.length; i++ ){
			if (attachedToAxes[i]){
				componentMapping[mapIdx++] = i;
			}
		}

		MixedTransform transform = new MixedTransform(srcDims, targetDims);
		transform.setComponentMapping(componentMapping);
		return transform;
	}

	private static class VaryingItem<T, F extends RandomAccessible<T>> extends MixedTransformView<T> implements MetadataItem<T> {
		final String name;

		final F data;

		final boolean[] attachedToAxes;

		public VaryingItem(final String name, final F data, final boolean[] attachedToAxes) {
			this(name, data, transformFromAttachedAxes(attachedToAxes), attachedToAxes);
		}

		private VaryingItem(final String name, final F data, final Mixed tform, final boolean[] attachedToAxes) {
			super(data, tform);
			this.name = name;
			this.data = data;
			this.attachedToAxes = attachedToAxes;
		}

		@Override
		public T getAt(final RealLocalizable pos) {
			if (pos.numDimensions() != attachedToAxes.length) {
				throw new IllegalArgumentException("Point must be of " + pos.numDimensions() + " dimensions");
			}
			// Convert pos to an m-dimensional point
			final RandomAccess<T> access = data.randomAccess();
			for (int d = 0, i = 0; d < attachedToAxes.length && d < pos.numDimensions(); ++d)
				if (attachedToAxes[d])
					access.setPosition((long) pos.getDoublePosition(d), i++);
			return access.get();
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

	private static class VaryingRealItem<T, U extends RealRandomAccessible<T>> extends RealTransformRealRandomAccessible<T, RealTransform> implements RealMetadataItem<T> {
		final String name;

		final U data;

		final boolean[] variesWithAxes;

		final boolean[] attachedToAxes;

		public VaryingRealItem(final String name, final U data, final boolean[] variesWithAxes, final boolean[] attachedToAxes) {
			super(data, realTransformFromAttachedToAxes(attachedToAxes));
			this.name = name;
			this.data = data;
			this.variesWithAxes = variesWithAxes;
			this.attachedToAxes = attachedToAxes;
		}

		private static RealTransform realTransformFromAttachedToAxes(boolean[] attachedToAxes) {
			int attachedDims = 0;
			for (boolean b: attachedToAxes) if (b) attachedDims++;
			final int targetDims = attachedDims;

			final int[] componentMapping = new int[targetDims];
			int mapIdx = 0;
			for (int i = 0; i < attachedToAxes.length; i++ ){
				if (attachedToAxes[i]){
					componentMapping[mapIdx++] = i;
				}
			}

			return new RealTransform(){

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
					for(int i = 0; i < targetDims; i++) {
						doubles1[i] = doubles[componentMapping[i]];
					}
				}

				@Override
				public void apply(RealLocalizable realLocalizable, RealPositionable realPositionable) {
					for(int i = 0; i < targetDims; i++) {
						realPositionable.setPosition(realLocalizable.getDoublePosition(componentMapping[i]), i);
					}
				}

				@Override
				public RealTransform copy() {
					return realTransformFromAttachedToAxes(attachedToAxes);
				}
			};
		}

		public boolean isAttachedToAxes() {
			return attachedToAxes != null;
		}

		@Override
		public T getAt(final RealLocalizable pos) {
			final RealRandomAccess<T> access = data.realRandomAccess();
			for (int d = 0, i = 0; d < variesWithAxes.length; ++d)
				if (variesWithAxes[d])
					access.setPosition(pos.getDoublePosition(d), i++);
			return access.get();
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
		public boolean isAttachedTo(int... dims) {
			if (attachedToAxes == null) {
				return false;
			}
			for (int i: dims) {
				if (attachedToAxes.length <= i || !attachedToAxes[i]) {
					return false;
				}
			}
			return true;
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder("VaryingRealItem \"");
			sb.append(name);
			sb.append("\"; ");

			if (isAttachedToAxes()) {
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
