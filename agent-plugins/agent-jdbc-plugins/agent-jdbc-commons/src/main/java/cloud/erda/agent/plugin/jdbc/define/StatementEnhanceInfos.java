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


package cloud.erda.agent.plugin.jdbc.define;

import cloud.erda.agent.plugin.jdbc.trace.ConnectionInfo;

/**
 * {@link StatementEnhanceInfos} contain the {@link ConnectionInfo} and
 * <code>sql</code> for trace mysql.
 *
 * @author zhangxin
 */
public class StatementEnhanceInfos {
    private ConnectionInfo connectionInfo;
    private String statementName;
    private String sql;

    public StatementEnhanceInfos(ConnectionInfo connectionInfo, String sql, String statementName) {
        this.connectionInfo = connectionInfo;
        this.sql = sql;
        this.statementName = statementName;
    }

    public ConnectionInfo getConnectionInfo() {
        return connectionInfo;
    }

    public String getSql() {
        return sql;
    }

    public String getStatementName() {
        return statementName;
    }
}
