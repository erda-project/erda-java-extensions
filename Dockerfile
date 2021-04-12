FROM registry.cn-hangzhou.aliyuncs.com/terminus/terminus-openjdk8:1.0.1

WORKDIR /
RUN ls

COPY ./dist/erda-java-agent.tar.gz /opt/erda-java-agent.tgz