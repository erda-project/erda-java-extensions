package cloud.erda.agent.plugin.method;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author liuhaoyang
 * @date 2021/5/10 16:48
 */
public class InterceptPoint {

    private String className;

    private Set<String> methodNames;

    public InterceptPoint(String className, String... methodNames) {
        this.className = className;
        this.methodNames = new HashSet<>(Arrays.asList(methodNames));
    }

    public void addPoint(String methodName) {
        if (methodName != null) {
            methodNames.add(methodName);
        }
    }

    public String getClassName() {
        return className;
    }

    public String[] getMethodNames() {
        return methodNames.toArray(new String[0]);
    }
}
