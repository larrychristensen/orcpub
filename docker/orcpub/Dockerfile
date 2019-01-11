FROM clojure:openjdk-8-lein as builder
MAINTAINER daemonsthere@gmail.com

ARG REPO="Orcpub"
ARG BRANCH="develop"

# Get orcpub source
ADD ./ /orcpub
WORKDIR /orcpub
RUN printenv &&\
	lein uberjar

FROM openjdk:8-jre-alpine as runner
MAINTAINER daemonsthere@gmail.com

COPY --from=builder /opt/orcpub/target/orcpub.jar /orcpub.jar

ENTRYPOINT ["java", "-jar"]
CMD ["/orcpub.jar"]
