package cloud.erda.agent.core.adapter.prometheus.converters;

/**
 * @author liuhaoyang 2020/3/20 10:19
 */
public class MeterConverter extends StatisticsConverter  {

    public static final MetricConverter instance = new MeterConverter();

    public MeterConverter() {
        super("rate");
    }
}
