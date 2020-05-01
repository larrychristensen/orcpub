FROM openjdk:8u242-jre
MAINTAINER daemonsthere@gmail.com

ENV DATOMIC_VERSION 0.9.5703

RUN wget https://my.datomic.com/downloads/free/${DATOMIC_VERSION} -qO /tmp/datomic.zip \
  && unzip /tmp/datomic.zip \
  && rm /tmp/datomic.zip \
  && mv /datomic-free-${DATOMIC_VERSION} /datomic

WORKDIR /datomic

RUN cp config/samples/free-transactor-template.properties transactor.properties

RUN sed "s/host=localhost/host=datomic/" -i transactor.properties

RUN sed "s/# storage-access=local/storage-access=remote/" -i transactor.properties

RUN sed "s/# storage-admin-password=/storage-admin-password=admin/" -i transactor.properties

RUN sed "s/# storage-datomic-password=/storage-datomic-password=datomic/" -i transactor.properties
RUN sed "s/# encrypt-channel=true/encrypt-channel=${ENCRYPT_CHANNEL:-true}/" -i transactor.properties

RUN mkdir /data
RUN sed "s/# data-dir=data/data-dir=\/data/" -i transactor.properties

RUN mkdir /log
RUN sed "s/# log-dir=log/log-dir=\/log/" -i transactor.properties


ENTRYPOINT ["/datomic/bin/transactor"]
CMD ["/datomic/transactor.properties"]