# erda-java-agent
Erda APM Java Agent

## Support plugins
- Dubbo
- Fegin
- HttpClient / HttpAsyncClient
- JDBC
- Jedis
- Lettuce
- Logback
- Log4j / Log4j2
- OkHttp 3.x / OkHttp 4.x
- RocketMQ
- Jetty
- Tomcat
- Spring RestTemplate

## Getting Started
### Build agent from source

You can run the following command to build the Erde JavaAgent
```
mvn clean package -DskipTests
```


## Document
> ToDo

## Dependency
Eeda JavaAgent depends on the plugin kernel of [Apache Skywalking](https://github.com/apache/skywalking) JavaAgent.

## Getting Help

If you find a bug or an issue, please [report an issue](https://github.com/erda-project/erda-java-agent/issues/new) on the java agent repository

## License
Erda Infra is under the Apache 2.0 license. See the [LICENSE](/LICENSE) file for details.