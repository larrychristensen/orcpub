FROM clojure/openjdk8-lein-2.8.3 as builder
MAINTAINER daemonsthere@gmail.com

ARG REPO="Orcpub"
ARG BRANCH="develop"

# Get orcpub source
WORKDIR /opt
RUN printenv &&\
	git clone --branch ${BRANCH} https://github.com/${REPO}/orcpub.git &&\
	cd orcpub &&\
	lein uberjar

FROM openjdk/8-jre-alpine as runner
MAINTAINER daemonsthere@gmail.com

COPY --from=builder /opt/orcpub/target/orcpub.jar /orcpub.jar

ENTRYPOINT ["java", "-jar"]
CMD ["/orcpub.jar"]
