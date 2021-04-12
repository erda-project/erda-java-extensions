package cloud.erda.agent.core.tracing.propagator;

/**
 * @author: liuhaoyang
 * @create: 2019-01-07 16:48
 **/
public class HeaderFactory {

    public static Header createHeader() {
        Header header = new NoopHeader();
        header = new BaggageHeader(header);
        header = new SpanIdHeader(header);
        header = new SampledHeader(header);
        header = new RequestIdHeader(header);
        return header;
    }
}
