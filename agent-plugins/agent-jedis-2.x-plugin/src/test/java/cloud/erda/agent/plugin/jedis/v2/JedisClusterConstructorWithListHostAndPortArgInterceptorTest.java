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

package cloud.erda.agent.plugin.jedis.v2;

import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.DynamicFieldEnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import redis.clients.jedis.HostAndPort;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class JedisClusterConstructorWithListHostAndPortArgInterceptorTest {

    private JedisClusterConstructorWithListHostAndPortArgInterceptor interceptor;

    private Set<HostAndPort> hostAndPortSet;

    @Mock
    private DynamicFieldEnhancedInstance enhancedInstance;

    @Before
    public void setUp() throws Exception {
        hostAndPortSet = new LinkedHashSet<HostAndPort>();
        interceptor = new JedisClusterConstructorWithListHostAndPortArgInterceptor();
        hostAndPortSet.add(new HostAndPort("127.0.0.1", 6379));
        hostAndPortSet.add(new HostAndPort("127.0.0.1", 16379));
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void onConstruct() throws Exception {
        interceptor.onConstruct(enhancedInstance, new Object[] {hostAndPortSet});

        verify(enhancedInstance, times(1)).setDynamicField("127.0.0.1:6379;127.0.0.1:16379;");
    }

}
