FROM amazoncorretto:11

LABEL maintainer="kaabab@gmail.com"

ENV VERTICLE_FILE CasinoWarsServer-1.0.0-SNAPSHOT-fat.jar
ENV VERTICLE_HOME /usr/verticles
EXPOSE 8080

COPY build/libs/$VERTICLE_FILE $VERTICLE_HOME/

WORKDIR $VERTICLE_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["exec java -Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.SLF4JLogDelegateFactory \
 -jar $VERTICLE_FILE"]
