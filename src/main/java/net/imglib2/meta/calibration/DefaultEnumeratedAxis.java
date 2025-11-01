package net.imglib2.meta.calibration;

import net.imglib2.transform.integer.Mixed;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * A default implementation of an {@link Axis} that represents enumerated
 * values.
 *
 * @author Gabriel Selzer
 */
public class DefaultEnumeratedAxis<T> implements Axis<T> {

    private final Map<Integer, T> enumerationMap;
    private final AxisType type;
    private final String units;

    /**
     * A shortcut for {@link #DefaultEnumeratedAxis<>(List)}.
     *
     * @param values the enumerated values for positions along the axis.
     */
    public DefaultEnumeratedAxis(final AxisType type, final String units, T... values) {
        this(type, units, Arrays.asList(values));
    }

    /**
     * Creates a {@link DefaultEnumeratedAxis} where the calibration value at index {@code i} is given by
     * {@code values.get(i)}.
     *
     * @param list the enumerated values for positions along the axis.
     */
    public DefaultEnumeratedAxis(final AxisType type, final String units, List<T> list) {
        this(type, units, mapFromList(list));
    }

    /**
     * Creates a {@link DefaultEnumeratedAxis} where the calibration value at index {@code i} is given by
     * {@code values.get(i)}.
     *
     * @param map the enumerated values for positions along the axis.
     */
    public DefaultEnumeratedAxis(final AxisType type, final String units, final Map<Integer, T> map) {
        this.type = type;
        this.units = units;
        this.enumerationMap = map;
    }

    @Override
    public T calibrated(int raw) {
        return enumerationMap.get(raw);
    }

    @Override
    public String unit() {
        return units;
    }

    @Override
    public AxisType type() {
        return type;
    }

    @Override
    public Axis<T> view(long[] steps, int... attachedAxes) {
        Map<Integer, T> newMap = new java.util.HashMap<>();
        for (int entry : enumerationMap.keySet()) {
            if (entry % steps[attachedAxes[0]] == 0) {
                newMap.put(entry / (int) steps[attachedAxes[0]], enumerationMap.get(entry));
            }
        }
        return new DefaultEnumeratedAxis<>(type, units, newMap);
    }

    @Override
    public Axis<T> view(Mixed transform, int... srcAxes) {
        Map<Integer, T> newMap = new java.util.HashMap<>();
        for (int entry : enumerationMap.keySet()) {
            entry *= transform.getComponentInversion(srcAxes[0]) ? -1 : 1;
            entry += (int) transform.getTranslation(srcAxes[0]);
            newMap.put(entry, enumerationMap.get(entry));
        }
        return new DefaultEnumeratedAxis<>(type, units, newMap);
    }

    private static <T> Map<Integer, T> mapFromList(List<T> list) {
        Map<Integer, T> map = new java.util.HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            map.put(i, list.get(i));
        }
        return map;
    }
}
