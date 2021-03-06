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

package cloud.erda.agent.plugin.jdbc.mysql.v8.define;

import org.apache.skywalking.apm.agent.core.plugin.interceptor.ConstructorInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.InstanceMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.match.ClassMatch;
import cloud.erda.agent.plugin.jdbc.define.Constants;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static org.apache.skywalking.apm.agent.core.plugin.match.MultiClassNameMatch.byMultiClassMatch;
import static net.bytebuddy.matcher.ElementMatchers.named;

/**
 * {@link PreparedStatementInstrumentation} define that the mysql-2.x plugin intercepts the following methods in the
 * com.mysql.jdbc.JDBC42PreparedStatement, com.mysql.jdbc.PreparedStatement and
 * com.mysql.cj.jdbc.PreparedStatement class:
 * 1. execute
 * 2. executeQuery
 * 3. executeUpdate
 * 4. executeLargeUpdate
 * 5. addBatch
 *
 * @author zhangxin
 */
public class PreparedStatementInstrumentation extends AbstractMysqlInstrumentation {

    private static final String PREPARED_STATEMENT_CLASS_NAME = "com.mysql.cj.jdbc.ClientPreparedStatement";
    public static final String PREPARED_STATEMENT_SERVER_SIDE_CLASS_NAME = "com.mysql.cj.jdbc.ServerPreparedStatement";

    @Override
    protected final ConstructorInterceptPoint[] getConstructorsInterceptPoints() {
        return new ConstructorInterceptPoint[0];
    }

    @Override
    protected final InstanceMethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        return new InstanceMethodsInterceptPoint[]{
                new InstanceMethodsInterceptPoint() {
                    @Override
                    public ElementMatcher<MethodDescription> getMethodsMatcher() {
                        return named("execute")
                                .or(named("executeQuery"))
                                .or(named("executeUpdate"))
                                .or(named("executeLargeUpdate"));
                    }

                    @Override
                    public String getMethodsInterceptor() {
                        return Constants.EXECUTE_METHODS_INTERCEPT_CLASS;
                    }

                    @Override
                    public boolean isOverrideArgs() {
                        return false;
                    }
                }
        };
    }

    @Override
    protected ClassMatch enhanceClass() {
        return byMultiClassMatch(PREPARED_STATEMENT_CLASS_NAME, PREPARED_STATEMENT_SERVER_SIDE_CLASS_NAME);
    }
}
