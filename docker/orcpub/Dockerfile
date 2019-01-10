FROM lein:2.8.3 as builder
MAINTAINER daemonsthere@gmail.com

ARG REPO="Orcpub"
ARG BRANCH="develop"

# Get orcpub source
WORKDIR /opt
RUN git clone --branch ${BRANCH} https://github.com/${REPO}/orcpub.git &&\
	cd orcpub &&\
	lein uberjar

FROM demonsthere/orcpub:bootstrap-lein as runner
MAINTAINER daemonsthere@gmail.com

COPY --from=builder /opt/orcpub/target/orcpub.jar /orcpub.jar

ENTRYPOINT ["java", "-jar"]
CMD ["/orcpub.jar"]
