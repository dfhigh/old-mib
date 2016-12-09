package person.dufei.utils.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ModelDescriptor {
    private int id;
    private long createdTime;
    private long updatedTime;
    private String format;
    private int dagId;
    private long size;
    private int resourceId;
    private int onlineResourceId;
    private String url;
    private String algorithm;
    private String meta;
    private Recipe recipe;
    private int parentId;
    private String location;
    private boolean isOnline;
    private Long onlineSize;
    private String onlineUrl;
    private CannonSchema onlineSchema;
    private String name;
    private List<ModelDescriptor> supplements;
}
