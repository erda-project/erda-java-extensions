package io.terminus.spot.plugin.lettuce.v5;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceConstructorInterceptor;

public class RedisClientConstructorInterceptor implements InstanceConstructorInterceptor {

    @Override
    public void onConstruct(EnhancedInstance objInst, Object[] allArguments) {
        RedisURI redisURI = (RedisURI) allArguments[1];
        RedisClient redisClient = (RedisClient) objInst;
        EnhancedInstance optionsInst = (EnhancedInstance) redisClient.getOptions();
        optionsInst.setDynamicField(redisURI.getHost() + ":" + redisURI.getPort());
    }
}
