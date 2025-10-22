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
import net.imglib2.realtransform.RealTransform;
import net.imglib2.realtransform.RealTransformRealRandomAccessible;
import net.imglib2.util.ConstantUtils;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

public final class RealMetadata {

	private RealMetadata() { }

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
		return new SimpleRealItem<>(key, data, numDims, dims);
	}

	public static <T, U extends RealRandomAccessible<T>> RealMetadataItem<T> item(String name, U data, int numDims, int... dims) {
		boolean[] axes = makeAxisAttachmentArray(numDims, dims);
		return new VaryingRealItem<>(name, data, axes);
	}

	private static boolean[] makeAxisAttachmentArray(int numDims, int... dims) {
		boolean[] attachedToAxes = new boolean[numDims];
        for (int dim : dims) attachedToAxes[dim] = true;
		return attachedToAxes;
	}


    public static <T> RealMetadataItem<T> absent(String name, int numDimensions, int... attachedAxes) {
        return new AbsentRealMetadataItem<>(name, numDimensions, attachedAxes);
    }

    private static class AbsentRealMetadataItem<T> implements RealMetadataItem<T> {

        final String name;
        final int numDimensions;
        final int[] attachedAxes;

        public AbsentRealMetadataItem(String name, int numDimensions, int... attachedAxes) {
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
        public boolean isAttachedTo(int... dims) {
            return false;
        }

        @Override
        public RealRandomAccess<T> realRandomAccess() {
            return new AbsentRealRandomAccess();
        }

        @Override
        public RealRandomAccess<T> realRandomAccess(RealInterval interval) {
            return new AbsentRealRandomAccess();
        }

        @Override
        public T getAt(int... pos) {
            return new AbsentRealRandomAccess().get();
        }

        @Override
        public T getAt(long... pos) {
            return new AbsentRealRandomAccess().get();
        }

        @Override
        public T getAt(Localizable pos) {
            return new AbsentRealRandomAccess().get();
        }

        @Override
        public MetadataItem<T> or(Supplier<MetadataItem<T>> defaultItem) {
            return defaultItem.get();
        }

        class AbsentRealRandomAccess extends RealPoint implements RealRandomAccess<T> {
            @Override
            public T get() {
                throw new NoSuchElementException("No metadata exists of key " + name + " attached to axes " + Arrays.toString(attachedAxes) + "!");
            }

            @Override
            public RealRandomAccess<T> copy() {
                return new AbsentRealRandomAccess();
            }
        }
    }

	/**
	 * A {@link RealMetadataItem} that constructs
	 * @param <T>
	 */
	private static class SimpleRealItem<T> implements RealMetadataItem<T> {
		final String name;

		final T data;

		final int numDimensions;

        final int[] attachedAxes;

		public SimpleRealItem(final String name, final T data, final int numDimensions, final int... attachedAxes) {
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
		public RealRandomAccess<T> realRandomAccess() {
			return ConstantUtils.constantRealRandomAccessible(data, numDimensions()).realRandomAccess();
		}

		@Override
		public RealRandomAccess<T> realRandomAccess(RealInterval interval) {
			return ConstantUtils.constantRealRandomAccessible(data, numDimensions()).realRandomAccess(interval);
		}

		@Override
		public int numDimensions() {
			return numDimensions;
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder("SimpleRealItem \"");
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
        public boolean isAttachedTo(int... dims) {
            for (int dim : dims) {
                if (!attachedToAxes[dim]) {
                    return false;
                }
            }
            return true;
        }

        @Override
		public String toString() {
            return "VaryingRealItem \"" + name + "\"; ";
		}

	}

}
