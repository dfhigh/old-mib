package person.dufei.utils.data;

import lombok.Data;

import java.util.List;

@Data
public class CannonSchema {
    private List<Schema> value;
    private List<Schema> key;
}
