package io.terminus.spot.plugin.log.error;

import cloud.erda.agent.core.config.loader.ConfigAccessor;
import cloud.erda.agent.core.utils.Constants;
import cloud.erda.agent.core.utils.DateTimeUtils;
import cloud.erda.agent.core.config.AgentConfig;
import cloud.erda.agent.core.config.ServiceConfig;
import cloud.erda.agent.core.tracing.TracerContext;
import cloud.erda.agent.core.tracing.TracerManager;
import cloud.erda.agent.core.utils.UUIDGenerator;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

public class ErrorEventBuilder {

    private final static String POST = "POST";

    private final static String PUT = "PUT";

    private List<StackElement> stacks = new ArrayList<StackElement>();

    private Map<String, String> tags = new HashMap<String, String>();

    private Map<String, String> metaDatas = new HashMap<String, String>();

    private Map<String, String> requestContext = new HashMap<String, String>();

    private Map<String, String> requestHeaders = new HashMap<String, String>();

    private ErrorEventBuilder() {
    }

    public static ErrorEventBuilder newBuilder() {
        return new ErrorEventBuilder();
    }

    private static String ReadRequestBody(HttpServletRequest request) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        try {
            br = request.getReader();
            String str;
            while ((str = br.readLine()) != null) {
                sb.append(str);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != br) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    public ErrorEvent build() {
        ErrorEvent event = new ErrorEvent();
        event.setEventId(UUIDGenerator.New());
        event.setTimestamp(DateTimeUtils.currentTimeNano());

        // add trace info
        TracerContext tracerContext = TracerManager.tracer().context();
        event.setRequestId(tracerContext.requestId());
        Boolean sampled = tracerContext.sampled();
        addTag("request_sampled", String.valueOf(sampled == null ? false : sampled));

        event.setTags(tags);
        event.setRequestHeaders(requestHeaders);
        event.setRequestContext(requestContext);
        event.setMetaDatas(metaDatas);
        event.setStacks(stacks);
        return event;
    }

    public ErrorEventBuilder addTagsFromConfig() {
        AgentConfig config = ConfigAccessor.Default.getConfig(AgentConfig.class);
        ServiceConfig serviceConfig = ConfigAccessor.Default.getConfig(ServiceConfig.class);
        tags.put("application_id", serviceConfig.getApplicationId());
        tags.put("application_name", serviceConfig.getApplicationName());
        tags.put("instance_id", serviceConfig.getServiceInstanceId());
        tags.put("service_instance_id", serviceConfig.getServiceInstanceId());
        tags.put("project_id", serviceConfig.getProjectId());
        tags.put("project_name", serviceConfig.getProjectName());
        tags.put("runtime_id", serviceConfig.getRuntimeId());
        tags.put("runtime_name", serviceConfig.getRuntimeName());
        tags.put("service_name", serviceConfig.getServiceName());
        tags.put("service_id", serviceConfig.getServiceId());
        tags.put("language", "Java");
        tags.put("workspace", serviceConfig.getWorkspace());
        tags.put("terminus_key", config.terminusKey());
        return this;
    }

    public ErrorEventBuilder addTagsFromMDC(Map<String, String> propertyMap) {
        for (Map.Entry<String, String> entry : propertyMap.entrySet()) {
            tags.put(entry.getKey(), entry.getValue());
        }
        return this;
    }

    public ErrorEventBuilder addTag(String key, String value) {
        tags.put(key, value);
        return this;
    }

    public ErrorEventBuilder addStack(StackTraceElement stackTraceElement, int index) {
        StackElement stackElement = new StackElement();
        stackElement.setClassName(stackTraceElement.getClassName());
        stackElement.setMethodName(stackTraceElement.getMethodName());
        stackElement.setFileName(stackTraceElement.getFileName());
        stackElement.setLine(stackTraceElement.getLineNumber());
        stackElement.setIndex(index);
        stacks.add(stackElement);
        return this;
    }

    public ErrorEventBuilder addMetaData(String key, String value) {
        metaDatas.put(key, value);
        return this;
    }

    public ErrorEventBuilder addRequestData() {
        TracerContext context = TracerManager.tracer().context();

        Object requestObj = context.getAttachment(Constants.Keys.REQUEST_KEY_IN_RUNTIME_CONTEXT);
        if (requestObj instanceof HttpServletRequest) {
            HttpServletRequest request = (HttpServletRequest) requestObj;
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String header = headerNames.nextElement();
                // todo 应该去掉所有的字符串硬编码，在v3.3 优化
                if (header.startsWith("terminus-spot-") || header.startsWith("terminus-request-")) {
                    continue;
                }
                requestHeaders.put(header, request.getHeader(header));
            }

            requestContext.put("method", request.getMethod());
            requestContext.put("path", request.getRequestURI());
            requestContext.put("query_string", request.getQueryString());
            requestContext.put("host", request.getHeader("Host"));
//            if (POST.equals(request.getMethod().toUpperCase()) || PUT.equals(request.getMethod().toUpperCase())) {
//                requestContext.put("body", ReadRequestBody(request));
//            }
            Cookie[] cookies = request.getCookies();
            if (cookies != null && cookies.length > 0) {
                StringBuilder sb = new StringBuilder();
                for (Cookie cookie : cookies) {
                    sb.append(cookie.getName());
                    sb.append("=");
                    sb.append(cookie.getValue());
                    sb.append("; ");
                }
                requestContext.put("cookies", sb.toString());
            }
        }
        return this;
    }
}
