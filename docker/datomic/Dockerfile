FROM bootstrap-lein:latest
MAINTAINER daemonsthere@gmail.com

ENV DATOMIC_VERSION 0.9.5561

RUN apt-get update &&\
	apt-get -y install \
		supervisor \
		unzip

ADD https://my.datomic.com/downloads/free/${DATOMIC_VERSION} /tmp/datomic.zip

RUN unzip /tmp/datomic.zip && rm /tmp/datomic.zip &&\
	mv datomic-free-${DATOMIC_VERSION} datomic

WORKDIR datomic

RUN cp config/samples/free-transactor-template.properties transactor.properties &&\
	sed "s/host=localhost/host=0.0.0.0/" -i transactor.properties &&\
	mkdir /data &&\
	sed "s/# data-dir=data/data-dir=\/data/" -i transactor.properties &&\
	mkdir /log &&\
	sed "s/# log-dir=log/log-dir=\/log/" -i transactor.properties &&\
	mkdir -p /var/log/supervisord

VOLUME /data
VOLUME /log

ADD datomic.conf /etc/supervisor/conf.d/datomic.conf

ENTRYPOINT ["supervisord", "-c"]
CMD ["/etc/supervisor/supervisord.conf"]
