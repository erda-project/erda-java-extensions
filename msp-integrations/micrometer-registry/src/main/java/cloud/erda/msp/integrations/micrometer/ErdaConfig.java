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

package cloud.erda.msp.integrations.micrometer;

import io.micrometer.core.instrument.config.validate.PropertyValidator;
import io.micrometer.core.instrument.step.StepRegistryConfig;

import java.time.Duration;
import java.util.Map;

/**
 * @author liuhaoyang
 * @date 2021/10/10 13:18
 */
public interface ErdaConfig extends StepRegistryConfig {

    @Override
    default String prefix() {
        return "erda";
    }

    @Override
    default Duration step() {
        return Duration.ofSeconds(30);
    }

    default String proxyHost() {
        return PropertyValidator.getString(this, "proxyHost").orElse(getHostFromEnv());
    }

    default Integer proxyPort() {
        return (Integer) PropertyValidator.getInteger(this, "proxyPort").orElse(7082);
    }

    default String mspEnvId() {
        return PropertyValidator.getString(this, "mspEnvId").orElse(getMspEnvIdFromEnv());
    }

    default String mspEnvToken() {
        return PropertyValidator.getString(this, "mspEnvToken").orElse(null);
    }

    default String orgName() {
        return PropertyValidator.getString(this, "orgName").orElse(System.getenv("DICE_ORG_NAME"));
    }

    @Override
    default String get(String s) {
        return null;
    }

    static String getHostFromEnv() {
        String mspProxyHost = System.getenv("MSP_PROXY_HOST");
        if (mspProxyHost != null) {
            return mspProxyHost;
        }
        return System.getenv("HOST_IP");
    }

    static String getMspEnvIdFromEnv() {
        String envId = System.getenv("MSP_ENV_ID");
        if (envId != null) {
            return envId;
        }
        return System.getenv("TERMINUS_KEY");
    }
}
