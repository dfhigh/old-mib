package person.dufei.utils.convert;

import person.dufei.utils.profiler.config.ProfileConfig;

import java.util.List;

/**
 * Created by dufei on 17/3/6.
 */
public interface LineConverter<T> {

    /**
     * Convert a list of lines to object we desire.
     * @param lines line list
     * @param pc profile config
     * @return converted
     */
    T convert(List<String> lines, ProfileConfig pc);

}
