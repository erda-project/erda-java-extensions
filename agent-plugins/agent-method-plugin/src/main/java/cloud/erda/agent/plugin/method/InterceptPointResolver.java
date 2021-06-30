package cloud.erda.agent.plugin.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author liuhaoyang
 * @date 2021/5/10 15:52
 */
public class InterceptPointResolver {

    private String[] interceptPoints;

    public InterceptPointResolver(String[] interceptPoints) {
        this.interceptPoints = interceptPoints;
    }

    public List<InterceptPoint> resolve() {
        Map<String, InterceptPoint> interceptPointMap = new HashMap<>();
        for (String point : interceptPoints) {
            String[] names = point.split("#");
            if (names.length != 2) {
                continue;
            }
            InterceptPoint interceptPoint = interceptPointMap.computeIfAbsent(names[0], InterceptPoint::new);
            interceptPoint.addPoint(names[1]);
        }
        return new ArrayList<>(interceptPointMap.values());
    }
}
