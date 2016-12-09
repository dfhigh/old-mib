package person.dufei.utils.data;

import lombok.Data;

import java.util.List;

@Data
public class Config {
    private List<String> includedFields;
    private List<String> targetFields;
}
