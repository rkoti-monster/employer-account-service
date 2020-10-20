FROM monsternextsyd-docker.jfrog.io/cicd-baselayer-java11

WORKDIR /opt

COPY --chown=1001:1001 target/universal/stage/ ./

EXPOSE 8083

ENTRYPOINT ["./bin/employer-account-service", "-J-javaagent:/usr/apps/dd-java-agent.jar", "-Dlog4j.configurationFile=log4j2-docker.yml"]
