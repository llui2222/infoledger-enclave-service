FROM maven:3.6.3

ENV ENCLAVE_CID_ARG CID_ENCLAVE
ENV ENCLAVE_PORT_ARG PORT_ENCLAVE

ADD run.sh /

RUN chmod +x run.sh

COPY target/nar/lib/amd64-Linux-gpp/jni/* /usr/lib/
COPY target/*.jar enclave-service-host.jar

ENTRYPOINT ["/run.sh"]