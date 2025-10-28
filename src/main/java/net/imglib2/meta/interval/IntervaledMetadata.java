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
package net.imglib2.meta.interval;

import net.imglib2.*;
import net.imglib2.meta.MetadataItem;
import net.imglib2.meta.MetadataStore;
import net.imglib2.meta.MetadataStoreView;
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

public final class IntervaledMetadata {

	private IntervaledMetadata() { }

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
	 * @param data the metadata value associated with the item. Constant across the metadata sapce.
	 * @param numDims the number of dimensions in which this item lives; or, the number of dimensions of the dataset this {@link MetadataItem} attaches to.
	 * @param dims the dimension indices to which this item pertains.
	 * @return a {@link MetadataItem}
	 * @param <T> the type of {@code data}
	 */
	public static <T> IntervaledMetadataItem<T> item(String key, T data, Interval interval, int numDims, int... dims) {
		return new SimpleItem<>(key, data, interval, numDims, dims);
	}

    /**
     * Creates a {@code n}-dimensional {@link MetadataItem} from an {@code m}-dimensional {@link RandomAccessible}. {@code n}&geq;{@code m}. Associated with.
     * <p>
     * TODO: This API could have problems for creating a SimpleItem from a RandomAccessible.
     * One use for this is a thumbnail image.
     * </p>
     * @param key the {@link String} key associated with the item
     * @param data the metadata value associated with the item. May vary across the metadata space.
     * @param numDims the number of dimensions in which this item lives; or, the number of dimensions of the dataset this {@link MetadataItem} attaches to.
     * @param dims the dimension indices to which this item pertains.
     * @return a {@link MetadataItem}
     * @param <T> the type of {@code data}
     */
	public static <T, U extends RandomAccessible<T>> IntervaledMetadataItem<T> item(String key, U data, Interval interval, int numDims, int... dims) {
		return new VaryingItem<>(key, data, interval, numDims, dims);
	}

    /**
     * Creates a {@code n}-dimensional {@link MetadataItem} from an {@code m}-dimensional {@link RandomAccessible}. {@code n}&geq;{@code m}. Associated with.
     *
     * @param key the {@link String} key associated with the item
     * @param data the metadata value associated with the item. May vary across the metadata space.
     * @param numDims the number of dimensions in which this item lives; or, the number of dimensions of the dataset this {@link MetadataItem} attaches to.
     * @param dims the dimension indices to which this item pertains.
     * @return a {@link MetadataItem}
     * @param <T> the type of {@code data}
     */
    public static <T, U extends RandomAccessible<T>> IntervaledMetadataItem<T> item(String key, U data, int numDims, Interval interval, BiConsumer<Localizable, T> setter, int... dims) {
        return new VaryingItem<>(key, data, interval, setter, numDims, dims);
    }

	public static MetadataStore view(MetadataStore source, MixedTransformView<?> view) {
		return view(source, view.getTransformToSource());
	}

	public static MetadataStore view(MetadataStore source, MixedTransform transform) {
		return new MetadataStoreView(source, transform);
	}

    public static <T> IntervaledMetadataItem<T> absent(String name, Interval interval, int numDimensions, int... attachedAxes) {
        return new AbsentMetadataItem<>(name, interval, numDimensions, attachedAxes);
    }

    private static class AbsentMetadataItem<T> implements IntervaledMetadataItem<T> {

        final String name;
        final int numDimensions;
        final Interval interval;
        final int[] attachedAxes;

        public AbsentMetadataItem(String name, Interval interval, int numDimensions, int... attachedAxes) {
            this.name = name;
            this.interval = interval;
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
        public boolean isAttachedTo(int... dims) {
            for (int d: dims) {
                boolean found = false;
                for (int attached: attachedAxes) {
                    if (d == attached) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return false;
                }
            }
            return true;
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

        @Override
        public long min(int d) {
            return interval.min(d);
        }

        @Override
        public long max(int d) {
            return interval.max(d);
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


    private static class SimpleItem<T> implements IntervaledMetadataItem<T> {
		final String name;
        T data;
        final Interval interval;

        final int numDimensions;
        final int[] attachedAxes;

		public SimpleItem(final String name, final T data, final Interval interval, final int numDimensions, final int... attachedAxes) {
			this.name = name;
			this.data = data;
            this.interval = interval;
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
        public boolean isAttachedTo(int... dims) {
            for (int dim : dims) {
                boolean found = false;
                for (int attachedAx : attachedAxes) {
                    if (dim == attachedAx) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public long min(int d) {
            return interval.min(d);
        }

        @Override
        public long max(int d) {
            return interval.max(d);
        }
    }

	private static Mixed transformFromAttachedAxes(int numDimensions, int[] attachedAxes) {
        MixedTransform transform = new MixedTransform(numDimensions, attachedAxes.length);
		transform.setComponentMapping(attachedAxes);
		return transform;
	}

	private static class VaryingItem<T, F extends RandomAccessible<T>> extends MixedTransformView<T> implements IntervaledMetadataItem<T> {
		final String name;

		final F data;

        final Interval interval;

        final ThreadLocal<Point> pointCache = ThreadLocal.withInitial(() -> new Point(numDimensions()));

        final BiConsumer<Localizable, T> setter;

		public VaryingItem(final String name, final F data, final Interval interval, final int numDimensions, final int... dims) {
			this(name, data, interval, transformFromAttachedAxes(numDimensions, dims));
		}

        private VaryingItem(final String name, final F data, final Interval interval, BiConsumer<Localizable, T> setter, final int numDimensions, final int... dims) {
            this(name, data, interval, transformFromAttachedAxes(numDimensions, dims), setter);
        }

		private VaryingItem(final String name, final F data, final Interval interval, final Mixed tform) {
            this(name, data, interval, tform, (pos, val) -> {});
		}

        private VaryingItem(final String name, final F data, final Interval interval, final Mixed tform, BiConsumer<Localizable, T> setter) {
            super(data, tform);
            this.name = name;
            this.data = data;
            this.interval = interval;
            this.setter = setter;
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
        public boolean isAttachedTo(int... dims) {
            for (int dim : dims) {
                boolean found = false;
                for (int j = 0; j < getTransformToSource().numTargetDimensions(); j++) {
                    if (dim == getTransformToSource().getComponentMapping(j)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public long min(int d) {
            return interval.min(d);
        }

        @Override
        public long max(int d) {
            return interval.max(d);
        }
    }

}
