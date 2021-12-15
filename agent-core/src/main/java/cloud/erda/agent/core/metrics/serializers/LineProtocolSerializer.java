/*
 * Copyright (c) 2021 Terminus, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.erda.agent.core.metrics.serializers;

import cloud.erda.agent.core.metrics.Metric;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

/**
 * @author liuhaoyang
 * @date 2021/11/29 14:48
 */
public class LineProtocolSerializer implements MetricSerializer {

    private static final int DEFAULT_STRING_BUILDER_SIZE = 1024;
    private static final ThreadLocal<StringBuilder> CACHED_STRINGBUILDERS =
            ThreadLocal.withInitial(() -> new StringBuilder(DEFAULT_STRING_BUILDER_SIZE));

    private static final int MAX_FRACTION_DIGITS = 340;
    private static final ThreadLocal<NumberFormat> NUMBER_FORMATTER =
            ThreadLocal.withInitial(() -> {
                NumberFormat numberFormat = NumberFormat.getInstance(Locale.ENGLISH);
                numberFormat.setMaximumFractionDigits(MAX_FRACTION_DIGITS);
                numberFormat.setGroupingUsed(false);
                numberFormat.setMinimumFractionDigits(1);
                return numberFormat;
            });

    private static final byte[] EMPTY_BYTES = new byte[0];

    @Override
    public byte[] serializeBytes(Metric[] metrics) {
        return serialize(metrics).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String serialize(Metric[] metrics) {
        if (metrics.length == 0) {
            return "";
        }
        StringBuilder sb = CACHED_STRINGBUILDERS.get();
        sb.setLength(0);

        for (Metric metric : metrics) {
            concatenated(sb, metric);
            sb.append("\n");
        }

        return sb.toString();
    }

    private void concatenated(StringBuilder sb, Metric metric) {
        escapeKey(sb, metric.getName());
        concatenatedTags(sb, metric.getTags());
        concatenatedFields(sb, metric.getFields());
        sb.append(" ").append(metric.getTimestamp());
    }

    private static void escapeKey(final StringBuilder sb, final String key) {
        for (int i = 0; i < key.length(); i++) {
            switch (key.charAt(i)) {
                case ' ':
                case ',':
                case '=':
                    sb.append('\\');
                default:
                    sb.append(key.charAt(i));
            }
        }
    }

    static void escapeField(final StringBuilder sb, final String field) {
        for (int i = 0; i < field.length(); i++) {
            switch (field.charAt(i)) {
                case '\\':
                case '\"':
                    sb.append('\\');
                default:
                    sb.append(field.charAt(i));
            }
        }
    }

    private static void concatenatedTags(final StringBuilder sb, Map<String, String> tags) {
        for (Map.Entry<String, String> tag : tags.entrySet()) {
            sb.append(',');
            escapeKey(sb, tag.getKey());
            sb.append('=');
            escapeKey(sb, tag.getValue());
        }
        sb.append(' ');
    }

    private static void concatenatedFields(final StringBuilder sb, Map<String, Object> fields) {
        for (Map.Entry<String, Object> field : fields.entrySet()) {
            Object value = field.getValue();
            if (value == null || isNotFinite(value)) {
                continue;
            }
            escapeKey(sb, field.getKey());
            sb.append('=');
            if (value instanceof Number) {
                if (value instanceof Double || value instanceof Float || value instanceof BigDecimal) {
                    sb.append(NUMBER_FORMATTER.get().format(value));
                } else {
                    sb.append(value).append('i');
                }
            } else if (value instanceof String) {
                String stringValue = (String) value;
                sb.append('"');
                escapeField(sb, stringValue);
                sb.append('"');
            } else {
                sb.append(value);
            }

            sb.append(',');

        }

        // efficiently chop off the trailing comma
        int lengthMinusOne = sb.length() - 1;
        if (sb.charAt(lengthMinusOne) == ',') {
            sb.setLength(lengthMinusOne);
        }
    }

    private static boolean isNotFinite(final Object value) {
        return value instanceof Double && !Double.isFinite((Double) value)
                || value instanceof Float && !Float.isFinite((Float) value);
    }
}
