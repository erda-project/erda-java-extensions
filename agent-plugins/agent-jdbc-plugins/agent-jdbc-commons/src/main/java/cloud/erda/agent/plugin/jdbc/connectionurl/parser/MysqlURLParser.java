/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */


package cloud.erda.agent.plugin.jdbc.connectionurl.parser;

import cloud.erda.agent.core.utils.Constants;
import cloud.erda.agent.plugin.jdbc.trace.ConnectionInfo;

/**
 * {@link MysqlURLParser} parse connection url of mysql.
 *
 * @author zhangxin
 */
public class MysqlURLParser extends AbstractURLParser {

    private static final int DEFAULT_PORT = 3306;
    private static final String DB_TYPE = "Mysql";

    public MysqlURLParser(String url) {
        super(url);
    }

    public MysqlURLParser(String url, String currentUrl) {
        super(url, currentUrl);
    }

    @Override
    protected URLLocation fetchDatabaseHostsIndexRange() {
        int hostLabelStartIndex = url.indexOf("//");
        int hostLabelEndIndex = url.indexOf("/", hostLabelStartIndex + 2);
        return new URLLocation(hostLabelStartIndex + 2, hostLabelEndIndex);
    }

    @Override
    protected URLLocation fetchDatabaseNameIndexRange() {
        int databaseStartTag = url.lastIndexOf("/");
        int databaseEndTag = url.indexOf("?", databaseStartTag);
        if (databaseEndTag == -1) {
            databaseEndTag = url.length();
        }
        return new URLLocation(databaseStartTag + 1, databaseEndTag);
    }

    @Override
    public ConnectionInfo parse() {
        URLLocation location = fetchDatabaseHostsIndexRange();
        String hosts = url.substring(location.startIndex(), location.endIndex());
        String[] hostSegment = hosts.split(",");
        if (currentUrl != null) {
            return new ConnectionInfo(Constants.Tags.COMPONENT_MYSQL, DB_TYPE, currentUrl, fetchDatabaseNameFromURL());
        }
        if (hostSegment.length > 1) {
            StringBuilder sb = new StringBuilder();
            for (String host : hostSegment) {
                if (host.split(":").length == 1) {
                    sb.append(host + ":" + DEFAULT_PORT + ",");
                } else {
                    sb.append(host + ",");
                }
            }
            return new ConnectionInfo(Constants.Tags.COMPONENT_MYSQL, DB_TYPE, sb.toString(), fetchDatabaseNameFromURL());
        } else {
            String[] hostAndPort = hostSegment[0].split(":");
            if (hostAndPort.length != 1) {
                return new ConnectionInfo(Constants.Tags.COMPONENT_MYSQL, DB_TYPE, hostAndPort[0], Integer.valueOf(hostAndPort[1]), fetchDatabaseNameFromURL());
            } else {
                return new ConnectionInfo(Constants.Tags.COMPONENT_MYSQL, DB_TYPE, hostAndPort[0], DEFAULT_PORT, fetchDatabaseNameFromURL());
            }
        }
    }

}
