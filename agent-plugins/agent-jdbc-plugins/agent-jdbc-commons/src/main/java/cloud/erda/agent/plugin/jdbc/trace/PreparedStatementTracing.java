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


package cloud.erda.agent.plugin.jdbc.trace;

import java.sql.SQLException;

/**
 * {@link PreparedStatementTracing} create an exit span when the client call the method in the class that extend {@link
 * java.sql.PreparedStatement}.
 *
 * @author zhangxin
 */
public class PreparedStatementTracing {

    public static <R> R execute(java.sql.PreparedStatement realStatement,
                                ConnectionInfo connectInfo, String method, String sql, Executable<R> exec)
            throws SQLException {

        // todo 现在并没有用到。请使用mysql插件
        return exec.exe(realStatement, sql);
    }

    public interface Executable<R> {
        R exe(java.sql.PreparedStatement realConnection, String sql)
                throws SQLException;
    }
}
