package net.imglib2.meta;

import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.DoubleType;
import org.junit.Assert;
import org.junit.Test;


public class MetadataItemViewTest {

	@Test
	public void testPermuteAttachedAxes() {
		// Create a constant metadata item attached to axis 0 in a 3D space
		MetadataItem<String> item = Metadata.constant("x_meta", "x", 3, 0);

		// Permute: swap X and Y
		MetadataItemView<String> permuted = item.view().permute(0, 1);
		Assert.assertArrayEquals(new int[]{1}, permuted.attachedAxes());
	}

	@Test
	public void testRotateAttachedAxes() {
		MetadataItem<String> item = Metadata.constant("y_meta", "y", 3, 0);

		// Rotate axis 0 into 2 (like a move); attached axes should update accordingly
		MetadataItemView<String> rotated = item.view().rotate(0, 2);
		Assert.assertArrayEquals(new int[]{2}, rotated.attachedAxes());
	}

	@Test
	public void testSliceRemovesAttachedAxis() {
		MetadataItem<String> item = Metadata.constant("z_meta", "z", 3, 2);

		// Slice out axis 2
		MetadataItemView<String> sliced = item.view().slice(2, 0);
		// Attached axis (2) is removed in the sliced view
		Assert.assertArrayEquals(new int[]{}, sliced.attachedAxes());
	}

    @Test
    public void testAddDimensionAttachedAxis() {
        MetadataItem<String> item = Metadata.constant("z_meta", "z", 3, 2);

        // Add a new dimension - attached axes should remain unchanged
        MetadataItemView<String> sliced = item.view().addDimension();
        Assert.assertArrayEquals(new int[]{2}, sliced.attachedAxes());
    }

    @Test
    public void testInvertAttachedAxis() {
        MetadataItem<String> item = Metadata.constant("z_meta", "z", 3, 2);

        // Invert the attached axis - should remain unchanged
        MetadataItemView<String> sliced = item.view().invertAxis(2);
        Assert.assertArrayEquals(new int[]{2}, sliced.attachedAxes());
    }

    @Test
    public void testTranslateAttachedAxis() {
        MetadataItem<String> item = Metadata.constant("z_meta", "z", 3, 2);

        // Translate the attached axis - should remain unchanged
        MetadataItemView<String> sliced = item.view().translate(1, 2, 3);
        Assert.assertArrayEquals(new int[]{2}, sliced.attachedAxes());
    }

    @Test
    public void testTranslateInverseAttachedAxis() {
        MetadataItem<String> item = Metadata.constant("z_meta", "z", 3, 2);

        // Translate the attached axis - should remain unchanged
        MetadataItemView<String> sliced = item.view().translateInverse(1, 2, 3);
        Assert.assertArrayEquals(new int[]{2}, sliced.attachedAxes());
    }

    @Test
    public void testPermuteVaryingAxes() {
        // Create a varying metadata item attached to axis 0 in a 3D space
        MetadataItem<DoubleType> item = Metadata.variant("test", ArrayImgs.doubles(10), 3, new int[] {0});

        // Permute: swap X and Y
        MetadataItemView<DoubleType> permuted = item.view().permute(0, 1);
        Assert.assertArrayEquals(new int[]{1}, permuted.varyingAxes());
    }

    @Test
    public void testRotateVaryingAxes() {
        // Create a varying metadata item attached to axis 0 in a 3D space
        MetadataItem<DoubleType> item = Metadata.variant("test", ArrayImgs.doubles(10), 3, new int[] {0});

        // Rotate: X and Y (X -> Y, Y -> -X)
        MetadataItemView<DoubleType> permuted = item.view().rotate(0, 1);
        Assert.assertArrayEquals(new int[]{1}, permuted.varyingAxes());
    }

    @Test
    public void testSliceRemovesVaryingAxis() {
        // Create a varying metadata item attached to axis 0 in a 3D space
        MetadataItem<DoubleType> item = Metadata.variant("test", ArrayImgs.doubles(10), 3, new int[] {0});

        // Slice: Axis 0 (varying)
        MetadataItemView<DoubleType> permuted = item.view().slice(0, 0);
        Assert.assertArrayEquals(new int[]{}, permuted.varyingAxes());
    }

    @Test
    public void testAddDimensionVaryingAxis() {
        // Create a varying metadata item attached to axis 0 in a 3D space
        MetadataItem<DoubleType> item = Metadata.variant("test", ArrayImgs.doubles(10), 3, new int[] {0});

        // Add a new dimension - varying axes should remain unchanged
        MetadataItemView<DoubleType> sliced = item.view().addDimension();
        Assert.assertArrayEquals(new int[]{0}, sliced.varyingAxes());
    }

    @Test
    public void testInvertVaryingAxis() {
        // Create a varying metadata item attached to axis 0 in a 3D space
        MetadataItem<DoubleType> item = Metadata.variant("test", ArrayImgs.doubles(10), 3, new int[] {0});

        // Invert the varying axis - should remain unchanged
        MetadataItemView<DoubleType> sliced = item.view().invertAxis(2);
        Assert.assertArrayEquals(new int[]{0}, sliced.varyingAxes());
    }

    @Test
    public void testTranslateVaryingAxis() {
        // Create a varying metadata item attached to axis 0 in a 3D space
        MetadataItem<DoubleType> item = Metadata.variant("test", ArrayImgs.doubles(10), 3, new int[] {0});

        // Translate the varying axis - should remain unchanged
        MetadataItemView<DoubleType> sliced = item.view().translate(1, 2, 3);
        Assert.assertArrayEquals(new int[]{0}, sliced.varyingAxes());
    }

    @Test
    public void testTranslateInverseVaryingAxis() {
        // Create a varying metadata item attached to axis 0 in a 3D space
        MetadataItem<DoubleType> item = Metadata.variant("test", ArrayImgs.doubles(10), 3, new int[] {0});

        // Translate the varying axis - should remain unchanged
        MetadataItemView<DoubleType> sliced = item.view().translateInverse(1, 2, 3);
        Assert.assertArrayEquals(new int[]{0}, sliced.varyingAxes());
    }
}
