package net.imglib2.meta.calibration;

import net.imglib2.RealRandomAccessible;
import net.imglib2.type.numeric.real.DoubleType;

/**
 * An {@link Axis} that can provide real-valued calibration data.
 *
 * @author Gabriel Selzer
 */
public interface RealAxis extends Axis{

    RealRandomAccessible<DoubleType> data();
}
