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

import net.imglib2.Localizable;
import net.imglib2.RandomAccessible;
import net.imglib2.meta.view.MetadataItemView;

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

	/**
	 * Returns the key identifying this piece of metadata
	 * @return the key
	 */
	String name();

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
     * Describes the axes this metadata is attached to.
     * <p>
     * There is a subtle difference between "attached to" and "varying along".
     * Axis attachment specifies that metadata pertains directly to these axes. If a slice is taken along each of the
     * attached axes, the metadata would no longer have meaning.
     * </p>
     * <p>
     * The classic case of axis-attached metadata is axis calibration, where each axis has its own
     * calibration values, label, and unit.
     * </p>
     *
     * @return an {@code int[]}, listing the axes this metadata is attached to.
     */
    int[] attachedAxes();

    /**
     * Tests whether this metadata is attached to <b>exactly</b> the given axes.
     *
     * @param dims the axes to test for attachment.
     * @return true if this metadata is attached to the given axes.
     */
    default boolean isAttachedTo(int... dims) {
        int[] attached = attachedAxes();
        if (attached.length != dims.length) {
            return false;
        }
        outer:
        for (int dim : dims) {
            for (int a : attached) {
                if (a == dim) {
                    continue outer;
                }
            }
            return false;
        }
        return true;
    }

    /**
     * Describes the axes this metadata varies along.
     * <p>
     * There is a subtle difference between "attached to" and "varying along".
     * Axis variance specifies that metadata values change along these axes. If a slice is taken along each of the
     * varying axes, the metadata still has meaning, but is constant across the slice.
     * </p>
     * <p>
     * One case of axis-varying metadata is Look Up Tables (LUTs). Each index along a channel axis may have a different
     * LUT. If so, slicing along one index on the channel axis will result in a constant LUT across all remaining axes.
     * That LUT still have meaning for the sliced data.
     * </p>
     *
     * @return an {@code int[]}, listing the axes this metadata varies along.
     */
    int[] varyingAxes();

	/**
	 * Returns the value of the metadata at an <em>arbitrary</em> position.
	 * Convenient for constant metadata.
	 *
	 * @return the value of the metadata at an arbitrary position.
	 */
	default T value() {
		return getAt(new long[numDimensions()]);
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
     * @see Metadata#absent(String, int, int[])
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
     * @see Metadata#absent(String, int, int[])
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
        setAt(value, new long[numDimensions()]);
    }

	// -- RandomAccessible Overrides -- //
    @Override
    default MetadataItemView<T> view() {
        return new MetadataItemView<>(this);
    }

    default T getType() {
        return value();
	}
}
