package cloud.erda.agent.core.adapter.prometheus.converters;

/**
 * @author liuhaoyang 2020/3/19 17:28
 */
public class HistogramConverter extends StatisticsConverter {

    public static final MetricConverter instance = new HistogramConverter();

    public HistogramConverter() {
        super("le");
    }
}
