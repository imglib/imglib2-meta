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
import net.imglib2.transform.integer.Mixed;
import net.imglib2.transform.integer.MixedTransform;
import net.imglib2.util.ConstantUtils;
import net.imglib2.view.MixedTransformView;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Utility class for working with {@link MetadataStore} and {@link MetadataItem}.
 *
 * @author Gabriel Selzer
 * @author Curtis Rueden
 */
public final class Metadata {

    /*
     * Prevent instantiation of utility class.
     */
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
	 * Creates a {@link MetadataItem} that is constant across the metadata space.
     * <p>
     * In other words, {@code data} will be returned at all queried positions.
     * </p>
	 *
	 * @param key the {@link String} key associated with the item
	 * @param data the metadata value associated with the item. Constant across the metadata sapce.
	 * @param numDims the number of dimensions in which this item lives; or, the number of dimensions of the dataset this {@link MetadataItem} attaches to.
	 * @param attachedAxes the dimension indices to which this item is attached.
	 * @return a {@link MetadataItem} wrapping {@code data}
	 * @param <T> the type of {@code data}
	 */
	public static <T> MetadataItem<T> constant(String key, T data, int numDims, int... attachedAxes) {
		return new ConstantItem<>(key, data, numDims, attachedAxes);
	}

    /**
     * Creates an {@code n}-dimensional {@link MetadataItem} from an {@code m}-dimensional {@link RandomAccessible},
     * where the metadata varies along specific axes of the dataset ({@code n} &ge; {@code m}).
     * <p>
     * This method handles metadata that changes along some dimensions but needs to be accessible from the full
     * n-dimensional space of the dataset. The {@code varyingAxes} parameter maps which dataset axes should be
     * used to index into the m-dimensional metadata.
     * </p>
     * <p>
     * <b>Example:</b> Consider an XYC image (3D) where you want to store a different color lookup table (LUT)
     * for each channel. The LUTs themselves are 1-dimensional (one per channel), but the dataset is 3-dimensional.
     * </p>
     * <pre>
     * // data: 1D RandomAccessible of LUTs (one for each of 3 channels)
     * // varyingAxes: [2] means "use the C axis (index 2) to look up LUTs"
     * // attachedAxes: 2 means "this metadata pertains to the C axis"
     * MetadataItem&lt;ColorTable&gt; luts = Metadata.varying("luts", data, 3, new int[]{2}, 2);
     * 
     * // When accessing luts at position (x=10, y=20, c=1):
     * // The system ignores x and y, and uses c=1 to get data[1]
     * </pre>
     *
     * @param key the {@link String} key identifying this metadata
     * @param data an {@code m}-dimensional {@link RandomAccessible} containing the metadata values
     * @param n the dimensionality of the dataset (and returned {@link MetadataItem})
     * @param varyingAxes indices of dataset axes that map to {@code data}'s dimensions (length = {@code m})
     * @param attachedAxes indices of dataset axes this metadata pertains to (0 &le; attachedAxes[i] &lt; {@code n})
     * @param <T> the type of metadata values
     * @param <U> the type of the data {@link RandomAccessible}
     * @return an {@code n}-dimensional {@link MetadataItem} that varies along the specified axes
     */
	public static <T, U extends RandomAccessible<T>> MetadataItem<T> varying(String key, U data, int n, int[] varyingAxes, int... attachedAxes) {
		return new VaryingItem<>(key, data, n, varyingAxes, attachedAxes);
	}

    /**
     * Creates an {@code n}-dimensional {@link MetadataItem} from an {@code m}-dimensional {@link RandomAccessible}
     * with support for updating metadata values ({@code n} &ge; {@code m}).
     * <p>
     * This is an advanced version of {@link #varying(String, RandomAccessible, int, int[], int...)} that allows
     * callers to provide a custom setter function for modifying metadata values. Most users should use the simpler
     * version unless they need to update metadata after creation.
     * </p>
     *
     * @param key the {@link String} key identifying this metadata
     * @param data an {@code m}-dimensional {@link RandomAccessible} containing the metadata values
     * @param n the dimensionality of the dataset (and returned {@link MetadataItem})
     * @param setter a {@link BiConsumer} that updates values in {@code data} at specific positions
     * @param varyingAxes indices of dataset axes that map to {@code data}'s dimensions (length = {@code m})
     * @param attachedAxes indices of dataset axes this metadata pertains to (0 &le; attachedAxes[i] &lt; {@code n})
     * @param <T> the type of metadata values
     * @param <U> the type of the data {@link RandomAccessible}
     * @return an {@code n}-dimensional {@link MetadataItem} that varies along the specified axes
     * @see #varying(String, RandomAccessible, int, int[], int...)
     */
    public static <T, U extends RandomAccessible<T>> MetadataItem<T> varying(String key, U data, int n, BiConsumer<Localizable, T> setter, int[] varyingAxes, int... attachedAxes) {
        return new VaryingItem<>(key, data, setter, n, varyingAxes, attachedAxes);
    }

    /**
     * Creates a {@link MetadataItem} to return in the absence of the requested {@link MetadataItem}.
     * <p>
     * To signify that no metadata exists for the given key and axes, the returned {@link MetadataItem}'s
     * {@code get()} methods will throw a {@link NoSuchElementException} when called, and their {@code or()}
     * methods will return the provided default value.
     * </p>
     * @param name the {@link String} key requested
     * @param numDimensions the number of dimensions in which this item lives
     * @param attachedAxes the attached axes requested
     * @return an {@code n}-dimensional {@link MetadataItem} that varies along the specified axes
     * @param <T> the type of metadata values
     */
    public static <T> MetadataItem<T> absent(String name, int numDimensions, int... attachedAxes) {
        return new AbsentMetadataItem<>(name, numDimensions, attachedAxes);
    }

    // -- Internal classes -- //

    private static class AbsentMetadataItem<T> implements MetadataItem<T> {

        final String name;
        final int numDimensions;
        final int[] attachedAxes;

        public AbsentMetadataItem(String name, int numDimensions, int... attachedAxes) {
            this.name = name;
            this.numDimensions = numDimensions;
            this.attachedAxes = attachedAxes;
        }


        @Override
        public int numDimensions() {
            return numDimensions;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public int[] attachedAxes() {
            return attachedAxes;
        }

        @Override
        public int[] varyingAxes() {
            // Absent metadata has no varying axes
            return new int[0];
        }

        @Override
        public RandomAccess<T> randomAccess() {
            return new AbsentRandomAccess();
        }

        @Override
        public RandomAccess<T> randomAccess(Interval interval) {
            return randomAccess();
        }

        @Override
        public T getAt(int... pos) {
            return new AbsentRandomAccess().get();
        }

        @Override
        public T getAt(long... pos) {
            return new AbsentRandomAccess().get();
        }

        @Override
        public T getAt(Localizable pos) {
            return new AbsentRandomAccess().get();
        }

        @Override
        public T valueOr(T defaultItem) {
            return defaultItem;
        }

        @Override
        public MetadataItem<T> or(Supplier<MetadataItem<T>> defaultSupplier) {
            return defaultSupplier.get();
        }

        class AbsentRandomAccess extends Point implements RandomAccess<T> {
            @Override
            public T get() {
                throw new NoSuchElementException("No metadata exists of key " + name + " attached to axes " + Arrays.toString(attachedAxes) + "!");
            }

            @Override
            public RandomAccess<T> copy() {
                return new AbsentRandomAccess();
            }
        }
    }


    private static class ConstantItem<T> implements MetadataItem<T> {
		final String name;
        T data;

        final int numDimensions;
        final int[] attachedAxes;

		public ConstantItem(final String name, final T data, final int numDimensions, final int... attachedAxes) {
			this.name = name;
			this.data = data;
            this.numDimensions = numDimensions;
			this.attachedAxes = attachedAxes;
		}

		@Override
		public String name() {
			return name;
		}

		@Override
		public T getAt(Localizable pos) {
			return data;
		}

		@Override
		public RandomAccess<T> randomAccess() {
			return ConstantUtils.constantRandomAccessible(data, numDimensions()).randomAccess();
		}

		@Override
		public RandomAccess<T> randomAccess(Interval interval) {
			return ConstantUtils.constantRandomAccessible(data, numDimensions()).randomAccess(interval);
		}

		@Override
		public int numDimensions() {
			return numDimensions;
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder("SimpleItem \"");
			sb.append(name);
			sb.append("\"; ");

			if (attachedAxes.length > 0) {
				sb.append("attached to axes {");
				sb.append(attachedAxes[0]);
				for (int i = 1; i < attachedAxes.length; ++i)
					sb.append(", ").append(attachedAxes[i]);
				sb.append("}; ");
			} else
				sb.append("not attached to any axis; ");

			sb.append("value = ").append(data);

			return sb.toString();
		}

        @Override
        public void setAt(T value, int... position) {
            data = value;
        }

        @Override
        public void setAt(T value, long... position) {
            data = value;
        }

        @Override
        public void setAt(T value, Localizable position) {
            data = value;
        }

        @Override
        public int[] attachedAxes() {
            return attachedAxes;
        }

        @Override
        public int[] varyingAxes() {
            // Simple metadata is constant, so has no varying axes
            return new int[0];
        }
    }

	private static Mixed transformFromAttachedAxes(int numDimensions, int[] varyingAxes) {
        MixedTransform transform = new MixedTransform(numDimensions, varyingAxes.length);
		transform.setComponentMapping(varyingAxes);
		return transform;
	}

	private static class VaryingItem<T, F extends RandomAccessible<T>> extends MixedTransformView<T> implements MetadataItem<T> {
		final String name;
		final F data;
        final ThreadLocal<Point> pointCache = ThreadLocal.withInitial(() -> new Point(numDimensions()));
        final BiConsumer<Localizable, T> setter;
        final int[] attachedAxes;

		public VaryingItem(final String name, final F data, final int numDimensions, final int[] varyingAxes, final int... attachedAxes) {
			this(name, data, transformFromAttachedAxes(numDimensions, varyingAxes), attachedAxes);
		}

        private VaryingItem(final String name, final F data, BiConsumer<Localizable, T> setter, final int numDimensions, final int[] varyingAxes, final int... attachedAxes) {
            this(name, data, transformFromAttachedAxes(numDimensions, varyingAxes), setter, attachedAxes);
        }

		private VaryingItem(final String name, final F data, final Mixed tform, final int... attachedAxes) {
            this(name, data, tform, (pos, val) -> {}, attachedAxes);
		}

        private VaryingItem(final String name, final F data, final Mixed tform, BiConsumer<Localizable, T> setter, final int... attachedAxes) {
            super(data, tform);
            this.name = name;
            this.data = data;
            this.setter = setter;
            this.attachedAxes = attachedAxes;
        }

		@Override
		public String name() {
			return name;
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder("VaryingItem \"");
			sb.append(name);
			sb.append("\"; ");

            sb.append("attached to axes {");
            for(int i = 0; i < getTransformToSource().numTargetDimensions(); i++) {
                int axis = getTransformToSource().getComponentMapping(i);
                sb.append(axis);
                if (i < getTransformToSource().numTargetDimensions() - 1) {
                    sb.append(", ");
                }
            }
            sb.append("}");

			return sb.toString();
		}

        @Override
        public void setAt(T value, int... position) {
            Point p = pointCache.get();
            int[] dest = new int[getTransformToSource().numTargetDimensions()];
            getTransformToSource().apply(position, dest);
            p.setPosition(dest);
            setter.accept(p, value);
        }

        @Override
        public void setAt(T value, long... position) {
            Point p = pointCache.get();
            long[] dest = new long[getTransformToSource().numTargetDimensions()];
            getTransformToSource().apply(position, dest);
            p.setPosition(dest);
            setter.accept(p, value);
        }

        @Override
        public void setAt(T value, Localizable position) {
            Point p = pointCache.get();
            getTransformToSource().apply(position, p);
            setter.accept(position, value);
        }

        @Override
        public int[] attachedAxes() {
            return attachedAxes;
        }
        @Override
        public int[] varyingAxes() {
            int[] varyingAxes = new int[getTransformToSource().numTargetDimensions()];
            getTransformToSource().getComponentMapping(varyingAxes);
            return varyingAxes;
        }
    }

}
