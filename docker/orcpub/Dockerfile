FROM bootstrap-lein:latest
MAINTAINER daemonsthere@gmail.com

ENV REPO="Demonsthere"
ENV BRANCH="master"

# Get orcpub source
WORKDIR /opt
RUN git clone --branch ${BRANCH} https://github.com/${REPO}/orcpub.git &&\
	cd orcpub &&\
	lein uberjar
