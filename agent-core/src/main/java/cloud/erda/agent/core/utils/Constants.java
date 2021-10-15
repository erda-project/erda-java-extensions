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

package cloud.erda.agent.core.utils;

public class Constants {

    public static final String SPOT_SERVICE_SPLIT_SEPARATOR = "\\|";

    public static class Carriers {

        public static final String RESPONSE_TERMINUS_KEY = "terminus-response-terminus-key";
    }

    public static class Keys {

        public static final String REQUEST_KEY_IN_RUNTIME_CONTEXT = "SPOT_REQUEST";

        public static final String RESPONSE_KEY_IN_RUNTIME_CONTEXT = "SPOT_RESPONSE";

        public static final String METRIC_BUILDER = "app_metric_builder";

        public static final String TRACE_SCOPE = "trace_scope";
    }

    public static class Tags {

        public static final String SPAN_KIND = "span_kind";

        public static final String SPAN_KIND_CLIENT = "client";

        public static final String SPAN_KIND_SERVER = "server";

        public static final String SPAN_KIND_PRODUCER = "producer";

        public static final String SPAN_KIND_CONSUMER = "consumer";

        public static final String COMPONENT = "component";

        public static final String COMPONENT_HTTP = "Http";

        public static final String COMPONENT_DUBBO = "Dubbo";

        public static final String COMPONENT_OKHTTP = "OkHttp";

        public static final String COMPONENT_FEIGN = "Feign";

        public static final String COMPONENT_REST_TEMPLATE = "SpringRestTemplate";

        public static final String COMPONENT_HTTPCLIENT = "HttpClient";

        public static final String COMPONENT_HTTPASYNCCLIENT = "HttpAsyncClient";

        public static final String COMPONENT_ROCKETMQ = "RocketMQ";

        public static final String COMPONENT_SHARDING_SPHERE = "ShardingSphere";

        public static final String ERROR = "error";

        public static final String ERROR_TRUE = String.valueOf(true);

        public static final String ERROR_MESSAGE = "error_message";

        public static final String PEER_ADDRESS = "peer_address";

        public static final String PEER_HOSTNAME = "peer_hostname";

        public static final String PEER_PORT = "peer_port";

        public static final String PEER_SERVICE = "peer_service";

        public static final String PEER_SERVICE_SCOPE = "peer_service_scope";

        public static final String PEER_SERVICE_EXTERNAL = "external";

        public static final String PEER_SERVICE_INTERNAL = "internal";

        public static final String SPAN_LAYER = "span_layer";

        public static final String SPAN_LAYER_RPC = "rpc";

        public static final String SPAN_LAYER_HTTP = "http";

        public static final String SPAN_LAYER_MQ = "mq";

        public static final String HOST = "host";

        public static final String DUBBO_SERVICE = "dubbo_service";

        public static final String DUBBO_METHOD = "dubbo_method";

        public static final String DB_TYPE = "db_type";

        public static final String DB_TYPE_REDIS = "Redis";

        public static final String COMPONENT_JEDIS = "Jedis";

        public static final String COMPONENT_REDISSON = "Redisson";

        public static final String COMPONENT_LETTUCE = "Lettuce";

        public static final String SPAN_LAYER_CACHE = "cache";

        public static final String DB_STATEMENT = "db_statement";

        public static final String DB_STATEMENT_ID = "db_statement_id";

        public static final String DB_INSTANCE = "db_instance";

        public static final String HTTP_URL = "http_url";

        public static final String HTTP_METHOD = "http_method";

        public static final String HTTP_PATH = "http_path";

        public static final String HTTP_STATUS = "http_status_code";

        public static final String COMPONENT_ORACLE = "Oracle";

        public static final String COMPONENT_MYSQL = "Mysql";

        public static final String COMPONENT_POSTGRESQL = "PostgreSql";

        public static final String COMPONENT_H2 = "H2";

        public static final String COMPONENT_INVOKE = "Invoke";

        public static final String SPAN_LAYER_DB = "db";

        public static final String SPAN_LAYER_LOCAL = "local";

        public static final String COMPONENT_SPRING_BOOT = "SpringBoot";

        public static final String NAME_SERVER_ADDRESS = "name_server_address";

        public static final String MESSAGE_BUS_DESTINATION = "message_bus_destination";

        public static final String MESSAGE_BUS_STATUS = "message_bus_status";

        public static final String REQUEST_ID = "request_id";

        public static final String TRACE_SAMPLED = "trace_sampled";

        public static final String HEALTH_CHECK = "health_check";

        public static final String CLASS = "class";

        public static final String METHOD = "method";

        public static final String INVOKE = "invoke";
    }

    public static class Metrics {

        public static final String SOURCE_ADDON_GROUP_METRIC = "source_addon_group";

        public static final String SOURCE_ADDON_TYPE_METRIC = "source_addon_type";
        public static final String SOURCE_ADDON_TYPE_ATTACH = "source-addon-type";

        public static final String SOURCE_ADDON_ID_METRIC = "source_addon_id";
        public static final String SOURCE_ADDON_ID_ATTACH = "source-addon-id";

        public static final String SOURCE_ORG_ID = "source_org_id";

        public static final String SOURCE_PROJECT_ID = "source_project_id";

        public static final String SOURCE_PROJECT_NAME = "source_project_name";

        public static final String SOURCE_APPLICATION_ID = "source_application_id";

        public static final String SOURCE_APPLICATION_NAME = "source_application_name";

        public static final String SOURCE_WORKSPACE = "source_workspace";

        public static final String SOURCE_RUNTIME_ID = "source_runtime_id";

        public static final String SOURCE_RUNTIME_NAME = "source_runtime_name";

        public static final String SOURCE_SERVICE_NAME = "source_service_name";

        public static final String SOURCE_SERVICE_ID = "source_service_id";

        public static final String SOURCE_INSTANCE_ID = "source_service_instance_id";

        public static final String SOURCE_TERMINUS_KEY = "source_terminus_key";

        public static final String TARGET_ADDON_GROUP = "target_addon_group";

        public static final String TARGET_ADDON_TYPE = "target_addon_type";

        public static final String TARGET_ADDON_ID = "target_addon_id";

        public static final String TARGET_ORG_ID = "target_org_id";

        public static final String TARGET_PROJECT_ID = "target_project_id";

        public static final String TARGET_PROJECT_NAME = "target_project_name";

        public static final String TARGET_APPLICATION_ID = "target_application_id";

        public static final String TARGET_APPLICATION_NAME = "target_application_name";

        public static final String TARGET_WORKSPACE = "target_workspace";

        public static final String TARGET_RUNTIME_ID = "target_runtime_id";

        public static final String TARGET_RUNTIME_NAME = "target_runtime_name";

        public static final String TARGET_SERVICE_NAME = "target_service_name";

        public static final String TARGET_INSTANCE_ID = "target_service_instance_id";

        public static final String TARGET_TERMINUS_KEY = "target_terminus_key";

        public static final String TARGET_SERVICE_ID = "target_service_id";

        public static final String ELAPSED = "elapsed";

        public static final String APPLICATION_HTTP = "application_http";

        public static final String APPLICATION_RPC = "application_rpc";

        public static final String APPLICATION_DB = "application_db";

        public static final String APPLICATION_CACHE = "application_cache";

        public static final String APPLICATION_MQ = "application_mq";

        public static final String APPLICATION_INVOKE = "application_invoke";

        public static final String APPLICATION_MICRO_SERVICE = "application_micro_service";

        public static final String APPLICATION_SERVICE_NODE = "application_service_node";

        public static final String INSTRUMENTATION_LIBRARY = "instrumentation_library";

        public static final String INSTRUMENTATION_LIBRARY_VERSION = "instrumentation_library_version";

        public static final String FIELD_KEY = "__field_key__";
    }
}
