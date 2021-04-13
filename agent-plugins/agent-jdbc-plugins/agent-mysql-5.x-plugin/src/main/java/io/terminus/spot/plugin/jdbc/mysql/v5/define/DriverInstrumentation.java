

package io.terminus.spot.plugin.jdbc.mysql.v5.define;

import org.apache.skywalking.apm.agent.core.plugin.match.ClassMatch;
import io.terminus.spot.plugin.jdbc.define.AbstractDriverInstrumentation;

import static org.apache.skywalking.apm.agent.core.plugin.match.MultiClassNameMatch.byMultiClassMatch;

public class DriverInstrumentation extends AbstractDriverInstrumentation {
    @Override
    protected ClassMatch enhanceClass() {
        return byMultiClassMatch("com.mysql.jdbc.Driver", "com.mysql.cj.jdbc.Driver", "com.mysql.jdbc.NonRegisteringDriver");
    }
}
