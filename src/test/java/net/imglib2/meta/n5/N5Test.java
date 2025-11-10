package net.imglib2.meta.n5;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.meta.Metadata;
import net.imglib2.meta.MetadataStore;
import net.imglib2.meta.calibration.Axis;
import net.imglib2.meta.calibration.Calibration;
import net.imglib2.meta.general.General;
import net.imglib2.meta.DatasetInterval;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
import org.janelia.saalfeldlab.n5.universe.N5Factory;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class N5Test {

    /* make an N5 reader, we start with a public container on AWS S3 */
    public static final String n5Url = "https://janelia-cosem.s3.amazonaws.com/jrc_hela-2/jrc_hela-2.n5";
    public static final String n5Group = "/em/fibsem-uint16";
    public static final String n5Dataset = n5Group + "/s4";

    public static N5Reader n5;
    public static DatasetInterval<UnsignedShortType> data;
    public static MetadataStore store;

    @Before
    public void setUp() {
        n5 = new N5Factory().openReader(n5Url);
        store = new N5MetadataStore(n5, n5Group, n5Dataset);
        RandomAccessibleInterval<UnsignedShortType> n5RAI = N5Utils.open(n5, n5Dataset);
        data = DatasetInterval.wrap(n5RAI, store);
    }

    @Test
    public void testName() {
        General g = Metadata.general(store);
        assertEquals(n5.getAttribute(n5Dataset, "name", String.class), g.name());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAxes() {
        Calibration c = Metadata.calibration(store);
        Map<String, Object> axisMap = (Map<String, Object>) n5.getAttribute(n5Dataset, "transform", Map.class);
        List<String> axes =  (List<String>) axisMap.get("axes");
        List<Double> translate =  (List<Double>) axisMap.get("translate");
        List<Double> scale =  (List<Double>) axisMap.get("scale");
        List<String> units =  (List<String>) axisMap.get("units");
        for(int i = 0; i < data.numDimensions(); i++) {
            Axis metaAxis = c.axis(i);
            assertEquals( //
                    axes.get(i).toUpperCase(),
                    metaAxis.type().toString() //
            );
            assertEquals( //
                    scale.get(i) * 1.0 + translate.get(i), //
                    metaAxis.calibrated(1.0), //
                    1e-6 //
            );
            assertEquals(units.get(i), metaAxis.unit());
        }
    }

}
