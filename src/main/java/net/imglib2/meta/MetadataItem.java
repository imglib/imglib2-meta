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

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

/**
 * A piece of metadata associated with an n-dimensional dataset.
 * All {@link MetadataItem}s have:
 * <ol>
 * <li>A {@link String} name</li>
 * <li>A {@code m}-dimensional value (m&le;n)</li>
 * <li>{@code m} axes to which they are "attached". Metadata can vary along
 * each axis it is attached to.</li>
 * </ol>
 * <p>
 * {@link MetadataItem}s are {@link RandomAccessible} across the {@code n}
 * dimensions of its associated dataset.
 * </p>
 * <p>
 * Often, metadata is a single value that may (e.g. axis type) or may not (e.g.
 * author name) be associated with an axis; in these cases,
 * {@link #getAt(Localizable)} et. al return that single value across all
 * positions. {@link #value()} provides a no-args convenience for that single value.
 * </p>
 * <p>
 * Other metadata elements (e.g. axis calibration) vary along one or more axes.
 * {@link #getAt(Localizable)} et. al are then responsible for projecting
 * positions from the metadata item's internal m-dimensional space onto the
 * external n-dimensional space of the {@link Localizable}.
 * {@link #value()} will then return a value at an arbitrary position.
 * </p>
 *
 * @param <T> the type of metadata values.
 * @author Curtis Rueden
 * @author Gabriel Selzer
 */
public interface MetadataItem<T> extends RandomAccessible<T> {

    static <T> MetadataItem<T> absent(String name, int numDimensions, int... attachedAxes) {
        boolean [] attached = new boolean[numDimensions];
        for (int d : attachedAxes) {
            if (d >= 0 && d < numDimensions) {
                attached[d] = true;
            }
        }
        return new MetadataItem<T>() {

            @Override
            public String name() {
                return name;
            }

            @Override
            public boolean[] attachedAxes() {
                return attached;
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
        };
    }

	/**
	 * Returns the key identifying this piece of metadata
	 * @return the key
	 */
	String name();

	/**
	 * Returns a boolean array [b<sub>1</sub>, b<sub>2</sub>, ..., b<sub>n</sub>].
	 * This {@link MetadataItem} is considered "attached" to dimension {@code i}
	 * iff b<sub>i</sub> is {@code true}.
     * <p>
     *     TODO: I'm not
     * </p>
	 *
	 * @return a {@code boolean[]} denoting attachment to each of the {@code n}
	 * dimensions in the data space.
	 */
	boolean[] attachedAxes();

    default void setAt(T value, int... pos) {
        throw new UnsupportedOperationException("This MetadataItem is read-only!");
    }
    default void setAt(T value, long... pos) {
        throw new UnsupportedOperationException("This MetadataItem is read-only!");
    }
    default void setAt(T value, Localizable pos) {
        throw new UnsupportedOperationException("This MetadataItem is read-only!");
    }

	// -- default utility methods -- //

	/**
	 * Describes whether this {@link MetadataItem} is attached to <em>any</em> dimensions.
	 * @return {@code true} iff this {@link MetadataItem} pertains to any dimension.
	 * TODO: Could we remove this in favor of an empty array to {@link #isAttachedTo(int...)}?
	 */
	default boolean isAttachedToAnyAxis() {
		for(boolean b: attachedAxes()) if (b) return true;
		return false;
	}

	/**
	 * Describes whether all dimensions in {@code dims} are in {d<sub>1</sub>, d<sub>2</sub>, ..., d<sub>m</sub>}.
	 * @param dims a list of dimensional indices
	 * @return {@code true} iff this {@link MetadataItem} pertains to all
	 * 		dimensional indices in {@code dims}.
	 */
	default boolean isAttachedTo(final int... dims) {
		boolean[] attachedAxes = attachedAxes();
		for(int i: dims) {
			// Cannot be attached to a dimension beyond the data space.
			if (i < 0 || attachedAxes.length <= i) {
				return false;
			}
			if (!attachedAxes[i]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns the value of the metadata at an <em>arbitrary</em> position.
	 * Convenient for constant metadata.
	 *
	 * @return the value of the metadata at an arbitrary position.
	 */
	default T value() {
		return getAt(new long[attachedAxes().length]);
	}

    /**
     * Returns the value of the metadata at an <em>arbitrary</em> position.
     * If this metadata is absent, returns {@code defaultValue}.
     * Convenient for constant metadata.
     *
     * @param defaultValue the value to return if this metadata is absent.
     * @return the value of the metadata at an arbitrary position.
     */
    default T valueOr(T defaultValue) {
        try {
            return value();
        } catch (NoSuchElementException e) {
            return defaultValue;
        }
    }

    /**
     * Returns {@code this}, unless this {@link MetadataItem} is absent (in which case {@code defaultItem} is returned).
     *
     * @param defaultItem the {@link MetadataItem} to return if this {@link MetadataItem} is absent.
     * @return the value of the metadata at an arbitrary position.
     * @see MetadataItem#absent(String, int, int[])
     */
    default MetadataItem<T> or(MetadataItem<T> defaultItem) {
        return or(() -> defaultItem);
    }

    /**
     * Returns {@code this}, unless this {@link MetadataItem} is absent (in which case {@code defaultItem} is returned).
     * <p>
     * This version is useful as it avoids creating the default item unless it is needed.
     * </p>
     *
     * @param defaultSupplier the {@link MetadataItem} to return if this {@link MetadataItem} is absent.
     * @return the value of the metadata at an arbitrary position.
     * @see MetadataItem#absent(String, int, int[])
     */
    default MetadataItem<T> or(Supplier<MetadataItem<T>> defaultSupplier) {
        return this;
    }

    /**
     * Sets the value of the metadata at an <em>arbitrary</em> position.
     * Convenient for constant metadata.
     *
     * @param value the new value of the metadata at an arbitrary position.
     */
    default void setValue(T value) {
        setAt(value, new long[attachedAxes().length]);
    }

	// -- RandomAccessible Overrides -- //

	default int numDimensions() {
		return attachedAxes().length;
	}

	default T getType() {
        return value();
	}
}
