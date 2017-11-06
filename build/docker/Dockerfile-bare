# This dockerfile builds the zap stable release
FROM alpine as builder

WORKDIR /zap

RUN apk add --no-cache curl wget xmlstarlet unzip

# Download and expand the latest stable release 
RUN curl -s https://raw.githubusercontent.com/zaproxy/zap-admin/master/ZapVersions.xml | xmlstarlet sel -t -v //url |grep -i Linux | wget --content-disposition -i - -O - | tar zxv && \
    mv ZAP*/* . &&  \
    rm -R ZAP* 

FROM openjdk:8-jdk-alpine
LABEL maintainer="psiinon@gmail.com"

WORKDIR /zap
COPY --from=builder /zap .
COPY policies /home/zap/.ZAP/policies/

RUN echo "http://dl-3.alpinelinux.org/alpine/edge/main" >> /etc/apk/repositories &&\
    apk add --update --no-cache bash netcat-openbsd && \
    adduser -h /home/zap -s /bin/bash zap -D zap && \
    rm -rf /var/cache/apk/* && \
    chown zap /zap && \
    chgrp zap /zap && \
    chown -R zap:zap /zap && \
    chown -R zap:zap /home/zap/.ZAP/

#Change to the zap user so things get done as the right person (apart from copy)
USER zap

ENV PATH $JAVA_HOME/bin:/zap/:$PATH
ENV ZAP_PATH /zap/zap.sh
ENV HOME /home/zap/
ENV ZAP_PORT 8080

HEALTHCHECK --retries=15 --interval=5s CMD nc -vz 127.0.0.1 $ZAP_PORT