package person.dufei.utils.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.concurrent.TimeUnit;

@Getter
@AllArgsConstructor
public class Latency {
    private final String name;
    private final long duration;
    private final TimeUnit tu;
}