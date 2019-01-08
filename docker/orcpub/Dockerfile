FROM bootstrap-lein:latest as builder
MAINTAINER daemonsthere@gmail.com

ENV REPO="Demonsthere"
ENV BRANCH="master"

# Get orcpub source
WORKDIR /opt
RUN git clone --branch ${BRANCH} https://github.com/${REPO}/orcpub.git &&\
	cd orcpub &&\
	lein uberjar

FROM datomic:latest as runner
MAINTAINER daemonsthere@gmail.com

ADD orcpub.conf /etc/supervisor/conf.d/orcpub.conf
COPY --from=builder /opt/orcpub/target/orcpub.jar /orcpub.jar
