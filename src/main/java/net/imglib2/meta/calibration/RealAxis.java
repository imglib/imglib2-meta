package net.imglib2.meta.calibration;

import net.imglib2.RealRandomAccessible;
import net.imglib2.type.numeric.real.DoubleType;

/**
 * An {@link Axis} that can provide real-valued calibration data.
 *
 * @author Gabriel Selzer
 */
public interface RealAxis extends Axis<DoubleType>{

    DoubleType calibrated(double raw);

    @Override
    default DoubleType calibrated(int raw) {
        return calibrated((double) raw);
    }

    RealRandomAccessible<DoubleType> data();
}
