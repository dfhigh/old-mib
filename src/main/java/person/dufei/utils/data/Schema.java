package person.dufei.utils.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Schema {
    private int tag;
    private String name;
    private String type;
}
