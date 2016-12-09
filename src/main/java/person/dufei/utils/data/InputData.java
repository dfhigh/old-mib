package person.dufei.utils.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InputData {
    private List<List<String>> preview;
    private List<Schema> schema;
}
