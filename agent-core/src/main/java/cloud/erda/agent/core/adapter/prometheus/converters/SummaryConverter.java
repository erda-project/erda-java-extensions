package cloud.erda.agent.core.adapter.prometheus.converters;

/**
 * @author liuhaoyang 2020/3/20 00:07
 */
public class SummaryConverter extends StatisticsConverter {

    public static final MetricConverter instance = new SummaryConverter();

    public SummaryConverter() {
        super("quantile");
    }
}
