package person.dufei.utils.data;

import lombok.Data;

import java.util.List;

@Data
public class Td2cInfo {
    private List<Schema> schema;
    private List<Transform> transform;
    private List<String> selectByType;
    private List<String> selectByTag;
}
