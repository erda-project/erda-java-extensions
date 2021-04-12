package cloud.erda.plugin.app.insight;

import cloud.erda.agent.core.utils.DateTimeUtils;

/**
 * @author: liuhaoyang
 * @create: 2019-01-21 17:42
 **/
class AppMetricWatch {
    private Long start;

    private Long end;

    AppMetricWatch(Long start) {
        this.start = start == null ? DateTimeUtils.currentTimeNano() : start;
    }

    void stop() {
        if (end == null) {
            end = DateTimeUtils.currentTimeNano();
        }
    }

    long elapsed() {
        if (end == null) {
            return DateTimeUtils.currentTimeNano() - start;
        }
        return Math.abs(end - start);
    }
}
