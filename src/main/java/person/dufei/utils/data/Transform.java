package person.dufei.utils.data;

import lombok.Data;

import java.util.List;

@Data
public class Transform {
    private List<String> dependency;
    private int tag;
    private String method;
    private String name;
}
