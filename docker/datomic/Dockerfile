FROM demonsthere/orcpub:bootstrap-lein
MAINTAINER daemonsthere@gmail.com

ENV DATOMIC_VERSION 0.9.5561

RUN apt-get update &&\
	apt-get -y install \
		dumb-init \
		unzip

ADD https://my.datomic.com/downloads/free/${DATOMIC_VERSION} /tmp/datomic.zip

RUN unzip /tmp/datomic.zip && rm /tmp/datomic.zip &&\
	mv datomic-free-${DATOMIC_VERSION} datomic
COPY transactor.properties /datomic/transactor.properties

VOLUME /data
VOLUME /log
