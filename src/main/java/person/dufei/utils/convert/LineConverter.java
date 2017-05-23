package person.dufei.utils.convert;

import java.util.List;

/**
 * Created by dufei on 17/3/6.
 */
public interface LineConverter<T> {

    /**
     * Convert a list of lines to object we desire.
     * @param lines line list
     * @param accessToken access token
     * @param startIndex start index
     * @return converted
     */
    T convert(List<String> lines, String accessToken, int startIndex);

}
