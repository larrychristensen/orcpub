FROM clojure:openjdk-8-lein
MAINTAINER daemonsthere@gmail.com

ENV DATOMIC_VERSION 0.9.5561

ADD https://my.datomic.com/downloads/free/${DATOMIC_VERSION} /tmp/datomic.zip

RUN unzip /tmp/datomic.zip && rm /tmp/datomic.zip &&\
	mv datomic-free-${DATOMIC_VERSION} /datomic &&\
	mv /datomic/config/samples/free-transactor-template.properties /datomic/transactor.properties

VOLUME /data
VOLUME /log

ENTRYPOINT ["/datomic/bin/transactor"]
CMD ["/datomic/transactor.properties"]
