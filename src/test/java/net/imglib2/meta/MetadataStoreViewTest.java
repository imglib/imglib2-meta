package net.imglib2.meta;

import net.imglib2.transform.integer.MixedTransform;
import net.imglib2.view.ViewTransforms;
import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

/**
 * Tests {@link MetadataStoreView} behavior.
 *
 * @author Gabriel Selzer
 */
public class MetadataStoreViewTest {


    /**
     * Tests that slicing metadata along an axis removes {@link MetadataItem}s attached <em>only</em> to that axis.
     */
    @Test
    public void testSlicingAttachedAxes() {
        // Create a 3D metadata store
        MetadataStore store = new SimpleMetadataStore(3);
        // Add metadata attached only to dimension 0
        String key = "some_key";
        store.add(key, 1.0, 0);

        // Slice along dimension 0
        MetadataStore sliced = Metadata.view(store, ViewTransforms.hyperSlice(3, 0, 0));
        // key should be filtered out (attached only to sliced axis)
        boolean hasZCal = sliced.items().stream()
                .anyMatch(item -> key.equals(item.name()));
        Assert.assertFalse("key should be removed when Z axis is sliced", hasZCal);

        // Permute, then slice
        // Our metadata item will become attached to dimension 1 after permutation (0, 1, 2) -> (1, 0, 2).
        // Then, in the slicing, the first dimension (which is now dimension 1) is sliced out. This means
        // that in the (permute-then-slice) view the first dimension (index 0) should be the axis of attachment.
        MixedTransform permutation = new MixedTransform(3, 3);
        permutation.set(ViewTransforms.permute(3, 0, 1));
        sliced = Metadata.view(store, permutation);
        sliced = Metadata.view(sliced, ViewTransforms.hyperSlice(3, 0, 0));
        // key should NOT be filtered out
        Optional<? extends MetadataItem<?>> permutedItem = sliced.items().stream().filter(item -> key.equals(item.name())).findFirst();
        Assert.assertTrue("key should be removed when Z axis is sliced", permutedItem.isPresent());
        Assert.assertArrayEquals(new int[] {0}, permutedItem.get().attachedAxes());

        // If we permute 0 and 2, the metadata should instead be attached to the first index.
        permutation = new MixedTransform(3, 3);
        permutation.set(ViewTransforms.permute(3, 0, 2));
        sliced = Metadata.view(store, permutation);
        sliced = Metadata.view(sliced, ViewTransforms.hyperSlice(3, 0, 0));
        // key should NOT be filtered out
        permutedItem = sliced.items().stream().filter(item -> key.equals(item.name())).findFirst();
        Assert.assertTrue("key should be removed when Z axis is sliced", permutedItem.isPresent());
        Assert.assertArrayEquals(new int[] {1}, permutedItem.get().attachedAxes());
    }
}
