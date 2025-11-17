package net.imglib2.meta;

import net.imglib2.RandomAccessible;
import net.imglib2.position.FunctionRandomAccessible;
import net.imglib2.type.numeric.real.DoubleType;
import org.junit.Test;

import java.util.NoSuchElementException;

import static org.junit.Assert.*;

/**
 * Tests for {@link Metadata} functionality.
 *
 * @author Gabriel Selzer
 */
public class MetadataTest {

    @Test
    public void testSimpleMetadataItem() {
        MetadataItem<String> item = Metadata.constant("testKey", "testValue", 2);
        assertEquals("testKey", item.name());
        assertEquals("testValue", item.value());
        assertArrayEquals(new int[] {}, item.attachedAxes());
        assertArrayEquals(new int[] {}, item.varyingAxes());
        assertTrue(item.isAttachedTo());
        assertFalse(item.isAttachedTo(0));
        assertFalse(item.isAttachedTo(1));
        assertFalse(item.isAttachedTo(0, 1));
        // Also test some of the defaulting behavior
        assertEquals("testValue", item.valueOr("testValue"));
        MetadataItem<String> defaultItem = Metadata.constant("testKey", "defaultValue", 2);
        assertEquals(item, item.or(defaultItem));
    }

    @Test
    public void testSimpleMetadataItemOnAxis() {
        MetadataItem<String> item = Metadata.constant("testKey", "testValue", 2, 0);
        assertEquals("testKey", item.name());
        assertEquals("testValue", item.value());
        assertArrayEquals(new int[] {0}, item.attachedAxes());
        assertArrayEquals(new int[] {}, item.varyingAxes());
        assertFalse(item.isAttachedTo());
        assertTrue(item.isAttachedTo(0));
        assertFalse(item.isAttachedTo(1));
        assertFalse(item.isAttachedTo(0, 1));
    }

    @Test
    public void testVaryingMetadataItem() {
        RandomAccessible<DoubleType> data = new FunctionRandomAccessible<>(1,
            (loc, out) -> out.set(loc.getLongPosition(0)),
            DoubleType::new
        );
        MetadataItem<DoubleType> item = Metadata.varying("testKey", data, 2, new int[] {0});
        assertEquals("testKey", item.name());
        assertArrayEquals(new int[] {0}, item.varyingAxes());

        // Assert the item varies as we go along the x axis
        for (long i = 0; i < 10; i++) {
            assert item.getAt(i, 0).equals(data.getAt(i));
        }
        // Assert the item does not vary as we go along the y axis
        for (long i = 0; i < 10; i++) {
            assert item.getAt(0, i).equals(data.getAt(0));
        }

        item = Metadata.varying("testKey", data, 2, new int[] {1});
        assertEquals("testKey", item.name());
        assertArrayEquals(new int[] {1}, item.varyingAxes());

        // Assert the item varies as we go along the y axis
        for (long i = 0; i < 10; i++) {
            assertEquals(item.getAt(0, i), data.getAt(i));
        }
        // Assert the item does not vary as we go along the x axis
        for (long i = 0; i < 10; i++) {
             assertEquals(item.getAt(i, 0), data.getAt(0));
        }

    }

    @Test
    public void testAbsentMetadataItem() {
        MetadataItem<String> item = Metadata.absent("testKey", 2);
        assertEquals("testKey", item.name());
        assertArrayEquals(new int[] {}, item.attachedAxes());
        assertArrayEquals(new int[] {}, item.varyingAxes());
        assertTrue(item.isAttachedTo());
        assertFalse(item.isAttachedTo(0));
        assertFalse(item.isAttachedTo(1));
        assertFalse(item.isAttachedTo(0, 1));
        assertThrows(NoSuchElementException.class, item::value);
        String defaultValue = "defaultValue";
        assertEquals(defaultValue, item.valueOr(defaultValue));
        MetadataItem<String> defaultItem = Metadata.constant("testKey", defaultValue, 2);
        assertEquals(defaultItem, item.or(defaultItem));
    }
}
