package person.dufei.utils.convert;

import java.util.List;

/**
 * Created by dufei on 17/3/6.
 */
public interface LineConverter<T> {

    /**
     * Convert a list of lines to object we desire.
     * @param columnNames column names
     * @param lines line list
     * @param delimiter column delimiter
     * @param accessToken access token
     * @param startIndex start index
     * @return converted
     */
    T convert(String[] columnNames, List<String> lines, String delimiter, String accessToken, int startIndex);

}
